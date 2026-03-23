"""
Comprehensive API Test Framework - Main Entry Point

This module provides a comprehensive API testing framework with:
- Interface authenticity validation (HTTP status code checking)
- Interface correctness validation (response data format and business logic checking)
- Performance metrics collection (response time, etc.)
- Detailed test report generation (HTML and JSON formats)

Usage:
    python comprehensive_api_test.py --suite auth --env dev
    python comprehensive_api_test.py --all --env test --report both
    python comprehensive_api_test.py --list-suites
"""
import sys
import time
import json
import argparse
import traceback
from typing import Dict, List, Any, Optional, Callable
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from utils.http_client import HTTPClient, APIResponse, get_http_client, reset_http_client
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.logger import get_logger
from test_result_collector import (
    TestResultCollector, TestCaseResult, TestSuiteResult,
    TestStatus, ValidationType, get_collector, reset_collector
)
from report_generator import ReportGenerator, generate_report
from test_suites_config import TEST_SUITES, TEST_ACCOUNTS, get_all_test_suites

logger = get_logger()


class APITestValidator:
    """
    API Test Validator - Performs comprehensive validation of API responses.
    
    Supports:
    - HTTP Status Code Validation
    - Response Schema Validation
    - Business Logic Validation
    - Response Time Validation
    - Data Integrity Validation
    - Security Validation
    """
    
    def __init__(self, collector: TestResultCollector = None):
        self.collector = collector or get_collector()
        self.assertions = APIAssertions()
        self.validator = ResponseValidator()
    
    def validate_status_code(self, response: APIResponse, expected_code: int = None, 
                            allowed_codes: List[int] = None) -> bool:
        """
        Validate HTTP status code.
        
        Args:
            response: API response object
            expected_code: Expected status code (exact match)
            allowed_codes: List of allowed status codes
            
        Returns:
            bool: True if validation passed
        """
        passed = True
        message = "Status code validation passed"
        actual = response.status_code
        expected = expected_code
        
        try:
            if expected_code is not None:
                self.assertions.assert_status_code(response, expected_code)
                message = f"Status code is {expected_code}"
            elif allowed_codes is not None:
                if response.status_code not in allowed_codes:
                    raise AssertionError(f"Status code {response.status_code} not in allowed codes: {allowed_codes}")
                expected = allowed_codes
                message = f"Status code {response.status_code} is in allowed codes"
            else:
                # Default: check for 2xx success
                self.assertions.assert_success(response)
                expected = "2xx"
                message = f"Status code {response.status_code} indicates success"
                
        except AssertionError as e:
            passed = False
            message = str(e)
        
        self.collector.add_validation(
            ValidationType.STATUS_CODE,
            "HTTP Status Code",
            passed,
            message,
            expected,
            actual
        )
        
        return passed
    
    def validate_response_time(self, response: APIResponse, max_time: float = 2.0) -> bool:
        """
        Validate response time is within acceptable limit.
        
        Args:
            response: API response object
            max_time: Maximum acceptable response time in seconds
            
        Returns:
            bool: True if validation passed
        """
        passed = True
        message = f"Response time {response.response_time:.3f}s is within limit"
        
        try:
            self.assertions.assert_response_time(response, max_time)
        except AssertionError as e:
            passed = False
            message = str(e)
        
        self.collector.add_validation(
            ValidationType.RESPONSE_TIME,
            "Response Time",
            passed,
            message,
            f"<= {max_time}s",
            f"{response.response_time:.3f}s"
        )
        
        return passed
    
    def validate_schema(self, response: APIResponse, schema_type: str = None, 
                       custom_schema: Dict = None) -> bool:
        """
        Validate response against JSON schema.
        
        Args:
            response: API response object
            schema_type: Predefined schema type ('user', 'merchant', 'product', 'order', 'pagination')
            custom_schema: Custom JSON schema for validation
            
        Returns:
            bool: True if validation passed
        """
        passed = True
        message = "Schema validation passed"
        
        try:
            if custom_schema:
                result = self.validator.validate_response_schema(response, custom_schema)
                if not result:
                    raise AssertionError("Response does not match custom schema")
            elif schema_type == 'user':
                result = self.validator.validate_user_response(response)
                if not result:
                    raise AssertionError("Response does not match user schema")
            elif schema_type == 'merchant':
                result = self.validator.validate_merchant_response(response)
                if not result:
                    raise AssertionError("Response does not match merchant schema")
            elif schema_type == 'product':
                result = self.validator.validate_product_response(response)
                if not result:
                    raise AssertionError("Response does not match product schema")
            elif schema_type == 'order':
                result = self.validator.validate_order_response(response)
                if not result:
                    raise AssertionError("Response does not match order schema")
            elif schema_type == 'pagination':
                result = self.validator.validate_pagination_response(response)
                if not result:
                    raise AssertionError("Response does not match pagination schema")
            else:
                # Basic validation - check if response is valid JSON object
                if not isinstance(response.body, (dict, list)):
                    raise AssertionError(f"Response is not a valid JSON object: {type(response.body)}")
                    
        except Exception as e:
            passed = False
            message = str(e)
        
        self.collector.add_validation(
            ValidationType.SCHEMA,
            f"Schema ({schema_type or 'basic'})",
            passed,
            message
        )
        
        return passed
    
    def validate_business_logic(self, response: APIResponse, 
                                checks: Dict[str, Callable[[Any], bool]]) -> bool:
        """
        Validate business logic constraints.
        
        Args:
            response: API response object
            checks: Dictionary of field names to validation functions
            
        Returns:
            bool: True if all validations passed
        """
        all_passed = True
        
        for field_name, check_func in checks.items():
            passed = True
            message = f"Business logic check '{field_name}' passed"
            actual_value = None
            
            try:
                if isinstance(response.body, dict):
                    actual_value = response.body.get(field_name)
                    if actual_value is None:
                        passed = False
                        message = f"Field '{field_name}' not found in response"
                    elif not check_func(actual_value):
                        passed = False
                        message = f"Business logic check failed for '{field_name}'"
                else:
                    passed = False
                    message = f"Cannot check field '{field_name}' - response is not an object"
                    
            except Exception as e:
                passed = False
                message = f"Error checking '{field_name}': {str(e)}"
            
            if not passed:
                all_passed = False
            
            self.collector.add_validation(
                ValidationType.BUSINESS_LOGIC,
                f"Business Logic: {field_name}",
                passed,
                message,
                "custom check",
                actual_value
            )
        
        return all_passed
    
    def validate_data_integrity(self, response: APIResponse, 
                                required_fields: List[str] = None,
                                field_types: Dict[str, type] = None) -> bool:
        """
        Validate data integrity - required fields and types.
        
        Args:
            response: API response object
            required_fields: List of required field names
            field_types: Dictionary of field names to expected types
            
        Returns:
            bool: True if validation passed
        """
        all_passed = True
        
        # Check required fields
        if required_fields:
            for field in required_fields:
                passed = True
                message = f"Required field '{field}' exists"
                
                try:
                    self.assertions.assert_has_field(response, field)
                except AssertionError as e:
                    passed = False
                    message = str(e)
                    all_passed = False
                
                self.collector.add_validation(
                    ValidationType.DATA_INTEGRITY,
                    f"Required Field: {field}",
                    passed,
                    message
                )
        
        # Check field types
        if field_types:
            for field, expected_type in field_types.items():
                passed = True
                message = f"Field '{field}' has correct type"
                actual_type = None
                
                try:
                    if isinstance(response.body, dict) and field in response.body:
                        actual_value = response.body[field]
                        actual_type = type(actual_value).__name__
                        if not isinstance(actual_value, expected_type):
                            passed = False
                            message = f"Field '{field}' expected type {expected_type.__name__}, got {actual_type}"
                            all_passed = False
                    else:
                        passed = False
                        message = f"Field '{field}' not found for type check"
                        all_passed = False
                        
                except Exception as e:
                    passed = False
                    message = str(e)
                    all_passed = False
                
                self.collector.add_validation(
                    ValidationType.DATA_INTEGRITY,
                    f"Type Check: {field}",
                    passed,
                    message,
                    expected_type.__name__,
                    actual_type
                )
        
        return all_passed
    
    def validate_security(self, response: APIResponse, 
                         sensitive_fields: List[str] = None) -> bool:
        """
        Validate security - check for sensitive data exposure.
        
        Args:
            response: API response object
            sensitive_fields: List of sensitive field names to check
            
        Returns:
            bool: True if validation passed (no sensitive data exposed)
        """
        passed = True
        message = "No sensitive data exposed"
        
        try:
            self.assertions.assert_no_sensitive_data(response, sensitive_fields)
        except AssertionError as e:
            passed = False
            message = str(e)
        
        self.collector.add_validation(
            ValidationType.SECURITY,
            "Sensitive Data Check",
            passed,
            message
        )
        
        return passed


class ComprehensiveAPITest:
    """
    Main comprehensive API test class.
    Provides high-level API for running tests with full validation.
    """
    
    def __init__(self, base_url: str = None, environment: str = "dev"):
        self.environment = environment
        self.http_client = get_http_client()
        self.collector = get_collector()
        self.validator = APITestValidator(self.collector)
        self.report_generator = ReportGenerator(self.collector)
        
        # Initialize collector with metadata
        self.collector.start_collection({
            "environment": environment,
            "base_url": base_url or self.http_client.base_url,
            "framework_version": "1.0.0"
        })
    
    def test_endpoint(self, name: str, method: str, endpoint: str,
                     description: str = "", priority: str = "medium",
                     expected_status: int = None, allowed_statuses: List[int] = None,
                     max_response_time: float = 2.0,
                     schema_type: str = None, custom_schema: Dict = None,
                     required_fields: List[str] = None,
                     field_types: Dict[str, type] = None,
                     business_checks: Dict[str, Callable] = None,
                     request_body: Dict = None, request_params: Dict = None,
                     headers: Dict[str, str] = None,
                     tags: List[str] = None) -> TestCaseResult:
        """
        Test a single API endpoint with comprehensive validation.
        
        Args:
            name: Test case name
            method: HTTP method (GET, POST, PUT, DELETE, PATCH)
            endpoint: API endpoint path
            description: Test description
            priority: Test priority (low, medium, high, critical)
            expected_status: Expected HTTP status code
            allowed_statuses: List of allowed status codes
            max_response_time: Maximum acceptable response time
            schema_type: Predefined schema type for validation
            custom_schema: Custom JSON schema
            required_fields: List of required fields
            field_types: Expected field types
            business_checks: Business logic validation functions
            request_body: Request body for POST/PUT/PATCH
            request_params: Query parameters
            headers: Additional headers
            tags: Test tags
            
        Returns:
            TestCaseResult: Test case result object
        """
        # Start test case
        test_case = self.collector.start_test(
            test_name=name,
            description=description,
            priority=priority,
            tags=tags or []
        )
        
        try:
            # Make HTTP request
            method = method.upper()
            if method == "GET":
                response = self.http_client.get(endpoint, params=request_params, headers=headers)
            elif method == "POST":
                response = self.http_client.post(endpoint, json_data=request_body, headers=headers)
            elif method == "PUT":
                response = self.http_client.put(endpoint, json_data=request_body, headers=headers)
            elif method == "PATCH":
                response = self.http_client.patch(endpoint, json_data=request_body, headers=headers)
            elif method == "DELETE":
                response = self.http_client.delete(endpoint, params=request_params, headers=headers)
            else:
                raise ValueError(f"Unsupported HTTP method: {method}")
            
            # Record response
            self.collector.record_response(
                response=response,
                method=method,
                endpoint=endpoint,
                request_headers=headers or {},
                request_body=request_body
            )
            
            # Record performance metrics
            self.collector.record_performance(response_time=response.response_time)
            
            # Perform validations
            validations_passed = []
            
            # 1. Status code validation (authenticity)
            validations_passed.append(
                self.validator.validate_status_code(response, expected_status, allowed_statuses)
            )
            
            # 2. Response time validation (performance)
            validations_passed.append(
                self.validator.validate_response_time(response, max_response_time)
            )
            
            # 3. Schema validation (correctness)
            if schema_type or custom_schema:
                validations_passed.append(
                    self.validator.validate_schema(response, schema_type, custom_schema)
                )
            
            # 4. Data integrity validation (correctness)
            if required_fields or field_types:
                validations_passed.append(
                    self.validator.validate_data_integrity(response, required_fields, field_types)
                )
            
            # 5. Business logic validation (correctness)
            if business_checks:
                validations_passed.append(
                    self.validator.validate_business_logic(response, business_checks)
                )
            
            # Determine final status
            if all(validations_passed):
                status = TestStatus.PASSED
            else:
                status = TestStatus.FAILED
            
            self.collector.finish_test(status)
            
        except Exception as e:
            logger.error(f"Test failed with exception: {e}")
            self.collector.finish_test(
                TestStatus.ERROR,
                error_msg=str(e),
                traceback=traceback.format_exc()
            )
        
        return test_case
    
    def run_test_suite(self, suite_name: str, tests: List[Dict[str, Any]], 
                      description: str = "") -> TestSuiteResult:
        """
        Run a suite of tests.
        
        Args:
            suite_name: Name of the test suite
            tests: List of test configurations
            description: Suite description
            
        Returns:
            TestSuiteResult: Test suite result object
        """
        suite = self.collector.start_suite(
            suite_name=suite_name,
            description=description,
            environment=self.environment
        )
        
        logger.info(f"Running test suite: {suite_name} ({len(tests)} tests)")
        
        for test_config in tests:
            self.test_endpoint(**test_config)
        
        self.collector.finish_suite(suite_name)
        
        return suite
    
    def generate_reports(self, output_dir: str = None, format: str = "both") -> Dict[str, str]:
        """
        Generate test reports.
        
        Args:
            output_dir: Output directory for reports
            format: Report format ('json', 'html', or 'both')
            
        Returns:
            Dict with report file paths
        """
        self.collector.finish_collection()
        
        if output_dir:
            self.report_generator.report_dir = Path(output_dir)
            self.report_generator.report_dir.mkdir(exist_ok=True)
        
        if format == "json":
            return {"json": self.report_generator.generate_json_report()}
        elif format == "html":
            return {"html": self.report_generator.generate_html_report()}
        else:
            return self.report_generator.generate_both_reports()
    
    def get_summary(self) -> Dict[str, Any]:
        """Get test execution summary"""
        return self.collector.get_summary()
    
    def close(self):
        """Clean up resources"""
        self.http_client.close()
        reset_http_client()
        reset_collector()
    
    def __enter__(self):
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()


# Test suites are now imported from test_suites_config.py


def list_available_suites():
    """Print available test suites"""
    print("\n📋 Available Test Suites:")
    print("=" * 60)
    for suite_id, suite_config in TEST_SUITES.items():
        print(f"\n  {suite_id:12} - {suite_config['name']}")
        print(f"  {' ' * 12}   {suite_config['description']}")
        print(f"  {' ' * 12}   Tests: {len(suite_config['tests'])}")
    print("\n" + "=" * 60)


def run_single_suite(suite_id: str, environment: str = "dev", report_format: str = "both"):
    """Run a single test suite"""
    if suite_id not in TEST_SUITES:
        print(f"❌ Unknown test suite: {suite_id}")
        print(f"Available suites: {', '.join(TEST_SUITES.keys())}")
        return
    
    suite_config = TEST_SUITES[suite_id]
    
    print(f"\n🚀 Running Test Suite: {suite_config['name']}")
    print(f"   Description: {suite_config['description']}")
    print(f"   Environment: {environment}")
    print(f"   Tests: {len(suite_config['tests'])}")
    print("=" * 60)
    
    with ComprehensiveAPITest(environment=environment) as tester:
        # Run the test suite
        tester.run_test_suite(
            suite_name=suite_config['name'],
            tests=suite_config['tests'],
            description=suite_config['description']
        )
        
        # Generate reports
        reports = tester.generate_reports(format=report_format)
        
        # Print summary
        summary = tester.get_summary()
        print("\n" + "=" * 60)
        print("📊 Test Execution Summary:")
        print(f"   Total Tests:    {summary['total_tests']}")
        print(f"   Passed:         {summary['passed_tests']} ✅")
        print(f"   Failed:         {summary['failed_tests']} ❌")
        print(f"   Errors:         {summary['error_tests']} ⚠️")
        print(f"   Skipped:        {summary['skipped_tests']} ⏭️")
        print(f"   Success Rate:   {summary['success_rate']:.1f}%")
        print(f"   Duration:       {summary['duration']:.2f}s")
        
        if summary.get('performance_stats'):
            perf = summary['performance_stats']
            print(f"\n⚡ Performance Metrics:")
            print(f"   Avg Response:   {perf.get('avg_response_time', 0):.3f}s")
            print(f"   Min Response:   {perf.get('min_response_time', 0):.3f}s")
            print(f"   Max Response:   {perf.get('max_response_time', 0):.3f}s")
        
        print("\n📄 Generated Reports:")
        for format_type, path in reports.items():
            print(f"   {format_type.upper()}: {path}")
        print("=" * 60 + "\n")


def run_all_suites(environment: str = "dev", report_format: str = "both"):
    """Run all test suites"""
    print("\n🚀 Running All Test Suites")
    print(f"   Environment: {environment}")
    print(f"   Total Suites: {len(TEST_SUITES)}")
    print("=" * 60)
    
    with ComprehensiveAPITest(environment=environment) as tester:
        # Run all suites
        for suite_id, suite_config in TEST_SUITES.items():
            print(f"\n▶️  Running: {suite_config['name']}")
            tester.run_test_suite(
                suite_name=suite_config['name'],
                tests=suite_config['tests'],
                description=suite_config['description']
            )
        
        # Generate reports
        reports = tester.generate_reports(format=report_format)
        
        # Print summary
        summary = tester.get_summary()
        print("\n" + "=" * 60)
        print("📊 Overall Test Execution Summary:")
        print(f"   Total Suites:   {summary['total_suites']}")
        print(f"   Total Tests:    {summary['total_tests']}")
        print(f"   Passed:         {summary['passed_tests']} ✅")
        print(f"   Failed:         {summary['failed_tests']} ❌")
        print(f"   Errors:         {summary['error_tests']} ⚠️")
        print(f"   Skipped:        {summary['skipped_tests']} ⏭️")
        print(f"   Success Rate:   {summary['success_rate']:.1f}%")
        print(f"   Duration:       {summary['duration']:.2f}s")
        
        if summary.get('performance_stats'):
            perf = summary['performance_stats']
            print(f"\n⚡ Performance Metrics:")
            print(f"   Avg Response:   {perf.get('avg_response_time', 0):.3f}s")
            print(f"   P90 Response:   {perf.get('p90_response_time', 0):.3f}s")
            print(f"   P95 Response:   {perf.get('p95_response_time', 0):.3f}s")
        
        print("\n📄 Generated Reports:")
        for format_type, path in reports.items():
            print(f"   {format_type.upper()}: {path}")
        print("=" * 60 + "\n")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description="Comprehensive API Test Framework for CampusOrder",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # List available test suites
  python comprehensive_api_test.py --list-suites
  
  # Run a specific test suite
  python comprehensive_api_test.py --suite auth --env dev
  
  # Run all test suites
  python comprehensive_api_test.py --all --env test
  
  # Run with specific report format
  python comprehensive_api_test.py --all --report html
  
  # Run health check only
  python comprehensive_api_test.py --suite health
        """
    )
    
    parser.add_argument(
        "--suite",
        type=str,
        help="Run specific test suite (use --list-suites to see available suites)"
    )
    
    parser.add_argument(
        "--all",
        action="store_true",
        help="Run all test suites"
    )
    
    parser.add_argument(
        "--list-suites",
        action="store_true",
        help="List available test suites"
    )
    
    parser.add_argument(
        "--env",
        type=str,
        default="dev",
        choices=["dev", "test", "prod"],
        help="Environment to run tests against (default: dev)"
    )
    
    parser.add_argument(
        "--report",
        type=str,
        default="both",
        choices=["json", "html", "both"],
        help="Report format (default: both)"
    )
    
    parser.add_argument(
        "--output-dir",
        type=str,
        help="Output directory for reports"
    )
    
    args = parser.parse_args()
    
    # Setup logging - logger is already configured in utils.logger
    pass
    
    # Handle commands
    if args.list_suites:
        list_available_suites()
        return
    
    if args.all:
        run_all_suites(args.env, args.report)
    elif args.suite:
        run_single_suite(args.suite, args.env, args.report)
    else:
        parser.print_help()
        print("\n❌ Please specify --suite, --all, or --list-suites")


if __name__ == "__main__":
    main()
