"""
Test result collector module for API testing framework.
Collects and aggregates test results, performance metrics, and validation results.
"""
import time
import json
from typing import Dict, List, Any, Optional, Callable
from dataclasses import dataclass, field, asdict
from datetime import datetime
from enum import Enum
from collections import defaultdict

from utils.http_client import APIResponse
from utils.logger import get_logger

logger = get_logger()


class TestStatus(Enum):
    """Test execution status"""
    PENDING = "pending"
    RUNNING = "running"
    PASSED = "passed"
    FAILED = "failed"
    ERROR = "error"
    SKIPPED = "skipped"


class ValidationType(Enum):
    """Types of validation"""
    STATUS_CODE = "status_code"
    RESPONSE_TIME = "response_time"
    SCHEMA = "schema"
    BUSINESS_LOGIC = "business_logic"
    DATA_INTEGRITY = "data_integrity"
    SECURITY = "security"


@dataclass
class ValidationResult:
    """Single validation result"""
    validation_type: ValidationType
    name: str
    passed: bool
    message: str
    expected: Any = None
    actual: Any = None
    timestamp: datetime = field(default_factory=datetime.now)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "validation_type": self.validation_type.value,
            "name": self.name,
            "passed": self.passed,
            "message": self.message,
            "expected": self.expected,
            "actual": self.actual,
            "timestamp": self.timestamp.isoformat()
        }


@dataclass
class PerformanceMetrics:
    """Performance metrics for a single request"""
    response_time: float
    request_size: int = 0
    response_size: int = 0
    dns_lookup_time: float = 0.0
    connection_time: float = 0.0
    ssl_handshake_time: float = 0.0
    ttfb: float = 0.0  # Time to first byte
    
    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)


@dataclass
class TestCaseResult:
    """Result of a single test case"""
    test_id: str
    test_name: str
    test_suite: str
    description: str = ""
    status: TestStatus = TestStatus.PENDING
    
    # Request/Response info
    method: str = ""
    endpoint: str = ""
    request_headers: Dict[str, str] = field(default_factory=dict)
    request_body: Any = None
    response: Optional[APIResponse] = None
    
    # Validation results
    validations: List[ValidationResult] = field(default_factory=list)
    
    # Performance metrics
    performance: Optional[PerformanceMetrics] = None
    
    # Timing
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    duration: float = 0.0
    
    # Error info
    error_message: str = ""
    error_traceback: str = ""
    
    # Metadata
    tags: List[str] = field(default_factory=list)
    priority: str = "medium"  # low, medium, high, critical
    
    def __post_init__(self):
        if not self.test_id:
            self.test_id = f"{self.test_suite}_{self.test_name}_{int(time.time() * 1000)}"
    
    @property
    def is_success(self) -> bool:
        """Check if test passed all validations"""
        return self.status == TestStatus.PASSED and all(v.passed for v in self.validations)
    
    @property
    def failed_validations(self) -> List[ValidationResult]:
        """Get list of failed validations"""
        return [v for v in self.validations if not v.passed]
    
    @property
    def status_code_valid(self) -> bool:
        """Check if status code validation passed"""
        for v in self.validations:
            if v.validation_type == ValidationType.STATUS_CODE:
                return v.passed
        return True
    
    @property
    def schema_valid(self) -> bool:
        """Check if schema validation passed"""
        for v in self.validations:
            if v.validation_type == ValidationType.SCHEMA:
                return v.passed
        return True
    
    def add_validation(self, validation: ValidationResult):
        """Add a validation result"""
        self.validations.append(validation)
    
    def start(self):
        """Mark test as started"""
        self.status = TestStatus.RUNNING
        self.start_time = datetime.now()
    
    def finish(self, status: TestStatus, error_msg: str = "", traceback: str = ""):
        """Mark test as finished"""
        self.status = status
        self.end_time = datetime.now()
        self.error_message = error_msg
        self.error_traceback = traceback
        
        if self.start_time:
            self.duration = (self.end_time - self.start_time).total_seconds()
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            "test_id": self.test_id,
            "test_name": self.test_name,
            "test_suite": self.test_suite,
            "description": self.description,
            "status": self.status.value,
            "method": self.method,
            "endpoint": self.endpoint,
            "request_headers": self.request_headers,
            "request_body": self.request_body,
            "response": {
                "status_code": self.response.status_code if self.response else None,
                "headers": dict(self.response.headers) if self.response else {},
                "body": self.response.body if self.response else None,
                "response_time": self.response.response_time if self.response else 0,
                "url": self.response.url if self.response else "",
                "method": self.response.method if self.response else ""
            } if self.response else None,
            "validations": [v.to_dict() for v in self.validations],
            "performance": self.performance.to_dict() if self.performance else None,
            "start_time": self.start_time.isoformat() if self.start_time else None,
            "end_time": self.end_time.isoformat() if self.end_time else None,
            "duration": self.duration,
            "error_message": self.error_message,
            "error_traceback": self.error_traceback,
            "tags": self.tags,
            "priority": self.priority,
            "is_success": self.is_success,
            "failed_validations_count": len(self.failed_validations)
        }


@dataclass
class TestSuiteResult:
    """Result of a test suite"""
    suite_name: str
    description: str = ""
    test_cases: List[TestCaseResult] = field(default_factory=list)
    
    # Timing
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    
    # Metadata
    environment: str = ""
    tags: List[str] = field(default_factory=list)
    
    def __post_init__(self):
        if not self.start_time:
            self.start_time = datetime.now()
    
    @property
    def duration(self) -> float:
        """Get total duration"""
        if self.start_time and self.end_time:
            return (self.end_time - self.start_time).total_seconds()
        return 0.0
    
    @property
    def total_tests(self) -> int:
        return len(self.test_cases)
    
    @property
    def passed_tests(self) -> int:
        return sum(1 for tc in self.test_cases if tc.status == TestStatus.PASSED)
    
    @property
    def failed_tests(self) -> int:
        return sum(1 for tc in self.test_cases if tc.status == TestStatus.FAILED)
    
    @property
    def error_tests(self) -> int:
        return sum(1 for tc in self.test_cases if tc.status == TestStatus.ERROR)
    
    @property
    def skipped_tests(self) -> int:
        return sum(1 for tc in self.test_cases if tc.status == TestStatus.SKIPPED)
    
    @property
    def success_rate(self) -> float:
        """Calculate success rate"""
        if self.total_tests == 0:
            return 0.0
        return (self.passed_tests / self.total_tests) * 100
    
    def add_test_case(self, test_case: TestCaseResult):
        """Add a test case result"""
        self.test_cases.append(test_case)
    
    def finish(self):
        """Mark suite as finished"""
        self.end_time = datetime.now()
    
    def get_performance_summary(self) -> Dict[str, Any]:
        """Get performance summary"""
        response_times = []
        for tc in self.test_cases:
            if tc.response:
                response_times.append(tc.response.response_time)
        
        if not response_times:
            return {}
        
        response_times.sort()
        total = len(response_times)
        
        return {
            "total_requests": total,
            "avg_response_time": sum(response_times) / total,
            "min_response_time": min(response_times),
            "max_response_time": max(response_times),
            "p50_response_time": response_times[int(total * 0.5)],
            "p90_response_time": response_times[int(total * 0.9)] if total > 1 else response_times[0],
            "p95_response_time": response_times[int(total * 0.95)] if total > 1 else response_times[0],
            "p99_response_time": response_times[int(total * 0.99)] if total > 1 else response_times[0]
        }
    
    def get_validation_summary(self) -> Dict[str, Any]:
        """Get validation summary by type"""
        summary = defaultdict(lambda: {"total": 0, "passed": 0, "failed": 0})
        
        for tc in self.test_cases:
            for v in tc.validations:
                vtype = v.validation_type.value
                summary[vtype]["total"] += 1
                if v.passed:
                    summary[vtype]["passed"] += 1
                else:
                    summary[vtype]["failed"] += 1
        
        return dict(summary)
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            "suite_name": self.suite_name,
            "description": self.description,
            "total_tests": self.total_tests,
            "passed_tests": self.passed_tests,
            "failed_tests": self.failed_tests,
            "error_tests": self.error_tests,
            "skipped_tests": self.skipped_tests,
            "success_rate": self.success_rate,
            "duration": self.duration,
            "start_time": self.start_time.isoformat() if self.start_time else None,
            "end_time": self.end_time.isoformat() if self.end_time else None,
            "environment": self.environment,
            "tags": self.tags,
            "performance_summary": self.get_performance_summary(),
            "validation_summary": self.get_validation_summary(),
            "test_cases": [tc.to_dict() for tc in self.test_cases]
        }


class TestResultCollector:
    """
    Main test result collector class.
    Collects and manages all test results across multiple test suites.
    """
    
    def __init__(self):
        self.suites: Dict[str, TestSuiteResult] = {}
        self.current_suite: Optional[TestSuiteResult] = None
        self.current_test: Optional[TestCaseResult] = None
        self.global_start_time: Optional[datetime] = None
        self.global_end_time: Optional[datetime] = None
        self.metadata: Dict[str, Any] = {}
    
    def start_collection(self, metadata: Dict[str, Any] = None):
        """Start collecting test results"""
        self.global_start_time = datetime.now()
        if metadata:
            self.metadata.update(metadata)
        logger.info(f"Started test result collection at {self.global_start_time}")
    
    def finish_collection(self):
        """Finish collecting test results"""
        self.global_end_time = datetime.now()
        for suite in self.suites.values():
            if not suite.end_time:
                suite.finish()
        logger.info(f"Finished test result collection at {self.global_end_time}")
    
    def start_suite(self, suite_name: str, description: str = "", environment: str = "", tags: List[str] = None):
        """Start a new test suite"""
        suite = TestSuiteResult(
            suite_name=suite_name,
            description=description,
            environment=environment,
            tags=tags or []
        )
        self.suites[suite_name] = suite
        self.current_suite = suite
        logger.info(f"Started test suite: {suite_name}")
        return suite
    
    def finish_suite(self, suite_name: str = None):
        """Finish a test suite"""
        suite = self.suites.get(suite_name) if suite_name else self.current_suite
        if suite:
            suite.finish()
            logger.info(f"Finished test suite: {suite.suite_name} - {suite.passed_tests}/{suite.total_tests} passed")
    
    def start_test(self, test_name: str, description: str = "", suite_name: str = None, 
                   priority: str = "medium", tags: List[str] = None) -> TestCaseResult:
        """Start a new test case"""
        suite = self.suites.get(suite_name) if suite_name else self.current_suite
        if not suite:
            suite = self.start_suite("default", "Default test suite")
        
        test_case = TestCaseResult(
            test_id=f"{suite.suite_name}_{test_name}_{int(time.time() * 1000)}",
            test_name=test_name,
            test_suite=suite.suite_name,
            description=description,
            priority=priority,
            tags=tags or []
        )
        test_case.start()
        suite.add_test_case(test_case)
        self.current_test = test_case
        return test_case
    
    def finish_test(self, status: TestStatus, error_msg: str = "", traceback: str = ""):
        """Finish current test case"""
        if self.current_test:
            self.current_test.finish(status, error_msg, traceback)
            logger.debug(f"Finished test: {self.current_test.test_name} - {status.value}")
    
    def add_validation(self, validation_type: ValidationType, name: str, passed: bool, 
                       message: str = "", expected: Any = None, actual: Any = None):
        """Add validation result to current test"""
        if self.current_test:
            validation = ValidationResult(
                validation_type=validation_type,
                name=name,
                passed=passed,
                message=message,
                expected=expected,
                actual=actual
            )
            self.current_test.add_validation(validation)
            
            if not passed:
                logger.warning(f"Validation failed: {name} - {message}")
    
    def record_response(self, response: APIResponse, method: str = "", endpoint: str = "",
                        request_headers: Dict[str, str] = None, request_body: Any = None):
        """Record API response for current test"""
        if self.current_test:
            self.current_test.response = response
            self.current_test.method = method or response.method
            self.current_test.endpoint = endpoint
            self.current_test.request_headers = request_headers or {}
            self.current_test.request_body = request_body
    
    def record_performance(self, response_time: float, request_size: int = 0, 
                          response_size: int = 0, **kwargs):
        """Record performance metrics for current test"""
        if self.current_test:
            self.current_test.performance = PerformanceMetrics(
                response_time=response_time,
                request_size=request_size,
                response_size=response_size,
                **kwargs
            )
    
    @property
    def global_duration(self) -> float:
        """Get total collection duration"""
        if self.global_start_time and self.global_end_time:
            return (self.global_end_time - self.global_start_time).total_seconds()
        return 0.0
    
    def get_summary(self) -> Dict[str, Any]:
        """Get overall test summary"""
        total_tests = sum(s.total_tests for s in self.suites.values())
        passed_tests = sum(s.passed_tests for s in self.suites.values())
        failed_tests = sum(s.failed_tests for s in self.suites.values())
        error_tests = sum(s.error_tests for s in self.suites.values())
        skipped_tests = sum(s.skipped_tests for s in self.suites.values())
        
        success_rate = (passed_tests / total_tests * 100) if total_tests > 0 else 0.0
        
        # Aggregate performance metrics
        all_response_times = []
        for suite in self.suites.values():
            for tc in suite.test_cases:
                if tc.response:
                    all_response_times.append(tc.response.response_time)
        
        performance_stats = {}
        if all_response_times:
            all_response_times.sort()
            total = len(all_response_times)
            performance_stats = {
                "total_requests": total,
                "avg_response_time": round(sum(all_response_times) / total, 3),
                "min_response_time": round(min(all_response_times), 3),
                "max_response_time": round(max(all_response_times), 3),
                "p50_response_time": round(all_response_times[int(total * 0.5)], 3),
                "p90_response_time": round(all_response_times[int(total * 0.9)], 3) if total > 1 else round(all_response_times[0], 3),
                "p95_response_time": round(all_response_times[int(total * 0.95)], 3) if total > 1 else round(all_response_times[0], 3),
                "p99_response_time": round(all_response_times[int(total * 0.99)], 3) if total > 1 else round(all_response_times[0], 3)
            }
        
        return {
            "total_suites": len(self.suites),
            "total_tests": total_tests,
            "passed_tests": passed_tests,
            "failed_tests": failed_tests,
            "error_tests": error_tests,
            "skipped_tests": skipped_tests,
            "success_rate": round(success_rate, 2),
            "duration": self.global_duration,
            "start_time": self.global_start_time.isoformat() if self.global_start_time else None,
            "end_time": self.global_end_time.isoformat() if self.global_end_time else None,
            "performance_stats": performance_stats,
            "metadata": self.metadata
        }
    
    def get_failed_tests(self) -> List[TestCaseResult]:
        """Get all failed tests"""
        failed = []
        for suite in self.suites.values():
            for tc in suite.test_cases:
                if tc.status in [TestStatus.FAILED, TestStatus.ERROR]:
                    failed.append(tc)
        return failed
    
    def get_slow_tests(self, threshold: float = 1.0) -> List[TestCaseResult]:
        """Get tests with response time above threshold"""
        slow = []
        for suite in self.suites.values():
            for tc in suite.test_cases:
                if tc.response and tc.response.response_time > threshold:
                    slow.append(tc)
        return sorted(slow, key=lambda x: x.response.response_time, reverse=True)
    
    def get_tests_by_tag(self, tag: str) -> List[TestCaseResult]:
        """Get tests by tag"""
        tests = []
        for suite in self.suites.values():
            for tc in suite.test_cases:
                if tag in tc.tags:
                    tests.append(tc)
        return tests
    
    def get_tests_by_status(self, status: TestStatus) -> List[TestCaseResult]:
        """Get tests by status"""
        tests = []
        for suite in self.suites.values():
            for tc in suite.test_cases:
                if tc.status == status:
                    tests.append(tc)
        return tests
    
    def export_to_dict(self) -> Dict[str, Any]:
        """Export all results to dictionary"""
        return {
            "summary": self.get_summary(),
            "suites": {name: suite.to_dict() for name, suite in self.suites.items()},
            "failed_tests": [tc.to_dict() for tc in self.get_failed_tests()],
            "metadata": self.metadata
        }
    
    def export_to_json(self, filepath: str = None) -> str:
        """Export all results to JSON"""
        data = self.export_to_dict()
        json_str = json.dumps(data, ensure_ascii=False, indent=2, default=str)
        
        if filepath:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(json_str)
            logger.info(f"Exported test results to {filepath}")
        
        return json_str


# Global collector instance
_collector: Optional[TestResultCollector] = None


def get_collector() -> TestResultCollector:
    """Get global test result collector instance"""
    global _collector
    if _collector is None:
        _collector = TestResultCollector()
    return _collector


def reset_collector():
    """Reset global collector instance"""
    global _collector
    _collector = TestResultCollector()
