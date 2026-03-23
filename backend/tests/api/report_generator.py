"""
Report generator module for API testing framework.
Generates comprehensive test reports in HTML and JSON formats.
"""
import json
import os
import base64
from typing import Dict, List, Any, Optional
from datetime import datetime
from pathlib import Path

from test_result_collector import TestResultCollector, TestStatus, ValidationType
from utils.logger import get_logger

logger = get_logger()


class ReportGenerator:
    """
    Test report generator supporting multiple output formats.
    """
    
    def __init__(self, collector: TestResultCollector = None):
        self.collector = collector or TestResultCollector()
        self.report_dir = Path(__file__).parent / "reports"
        self.report_dir.mkdir(exist_ok=True)
    
    def generate_json_report(self, output_path: str = None) -> str:
        """Generate JSON format report"""
        if not output_path:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            output_path = str(self.report_dir / f"test_report_{timestamp}.json")
        
        report_data = {
            "report_info": {
                "title": "API Test Report",
                "generated_at": datetime.now().isoformat(),
                "generator": "CampusOrder API Test Framework",
                "version": "1.0.0"
            },
            "summary": self.collector.get_summary(),
            "suites": {name: suite.to_dict() for name, suite in self.collector.suites.items()},
            "failed_tests": [tc.to_dict() for tc in self.collector.get_failed_tests()],
            "slow_tests": [tc.to_dict() for tc in self.collector.get_slow_tests(threshold=1.0)],
            "metadata": self.collector.metadata
        }
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(report_data, f, ensure_ascii=False, indent=2, default=str)
        
        logger.info(f"JSON report generated: {output_path}")
        return output_path
    
    def generate_html_report(self, output_path: str = None) -> str:
        """Generate HTML format report"""
        if not output_path:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            output_path = str(self.report_dir / f"test_report_{timestamp}.html")
        
        summary = self.collector.get_summary()
        
        html_content = self._build_html_report(summary)
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        logger.info(f"HTML report generated: {output_path}")
        return output_path
    
    def generate_both_reports(self, base_name: str = None) -> Dict[str, str]:
        """Generate both JSON and HTML reports"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        base_name = base_name or f"test_report_{timestamp}"
        
        json_path = str(self.report_dir / f"{base_name}.json")
        html_path = str(self.report_dir / f"{base_name}.html")
        
        return {
            "json": self.generate_json_report(json_path),
            "html": self.generate_html_report(html_path)
        }
    
    def _build_html_report(self, summary: Dict[str, Any]) -> str:
        """Build complete HTML report content"""
        return f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>API Test Report - CampusOrder</title>
    <style>
        {self._get_css_styles()}
    </style>
</head>
<body>
    <div class="container">
        {self._build_header()}
        {self._build_summary_section(summary)}
        {self._build_performance_section(summary)}
        {self._build_suites_section()}
        {self._build_failed_tests_section()}
        {self._build_footer()}
    </div>
    <script>
        {self._get_javascript()}
    </script>
</body>
</html>"""
    
    def _get_css_styles(self) -> str:
        """Get CSS styles for the report"""
        return """
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        /* Header */
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }
        
        .header .subtitle {
            opacity: 0.9;
            font-size: 1.1em;
        }
        
        .header .timestamp {
            margin-top: 15px;
            font-size: 0.9em;
            opacity: 0.8;
        }
        
        /* Summary Cards */
        .summary-section {
            padding: 30px;
            background: #f8f9fa;
        }
        
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .summary-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            text-align: center;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .summary-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 15px rgba(0,0,0,0.1);
        }
        
        .summary-card .icon {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .summary-card .value {
            font-size: 2.5em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .summary-card .label {
            color: #6c757d;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .card-total .icon, .card-total .value { color: #3498db; }
        .card-passed .icon, .card-passed .value { color: #27ae60; }
        .card-failed .icon, .card-failed .value { color: #e74c3c; }
        .card-error .icon, .card-error .value { color: #f39c12; }
        .card-skipped .icon, .card-skipped .value { color: #95a5a6; }
        .card-rate .icon, .card-rate .value { color: #9b59b6; }
        
        /* Success Rate Bar */
        .success-rate-container {
            background: white;
            border-radius: 12px;
            padding: 25px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
        }
        
        .success-rate-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .success-rate-bar {
            height: 30px;
            background: #ecf0f1;
            border-radius: 15px;
            overflow: hidden;
            position: relative;
        }
        
        .success-rate-fill {
            height: 100%;
            background: linear-gradient(90deg, #27ae60 0%, #2ecc71 100%);
            border-radius: 15px;
            transition: width 1s ease-out;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            padding-right: 15px;
            color: white;
            font-weight: bold;
        }
        
        .success-rate-fill.low {
            background: linear-gradient(90deg, #e74c3c 0%, #c0392b 100%);
        }
        
        .success-rate-fill.medium {
            background: linear-gradient(90deg, #f39c12 0%, #e67e22 100%);
        }
        
        /* Performance Section */
        .performance-section {
            padding: 30px;
            background: white;
        }
        
        .section-title {
            font-size: 1.5em;
            margin-bottom: 20px;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .performance-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
        }
        
        .performance-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 12px;
            padding: 20px;
        }
        
        .performance-card .metric-name {
            font-size: 0.9em;
            opacity: 0.9;
            margin-bottom: 10px;
        }
        
        .performance-card .metric-value {
            font-size: 2em;
            font-weight: bold;
        }
        
        .performance-card .metric-unit {
            font-size: 0.8em;
            opacity: 0.8;
        }
        
        /* Test Suites */
        .suites-section {
            padding: 30px;
            background: #f8f9fa;
        }
        
        .suite-card {
            background: white;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
            overflow: hidden;
        }
        
        .suite-header {
            padding: 20px 25px;
            background: #34495e;
            color: white;
            cursor: pointer;
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: background 0.2s;
        }
        
        .suite-header:hover {
            background: #2c3e50;
        }
        
        .suite-header h3 {
            margin: 0;
            font-size: 1.2em;
        }
        
        .suite-stats {
            display: flex;
            gap: 20px;
            font-size: 0.9em;
        }
        
        .suite-stat {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        
        .suite-content {
            max-height: 0;
            overflow: hidden;
            transition: max-height 0.3s ease-out;
        }
        
        .suite-content.expanded {
            max-height: 50000px;
        }
        
        .test-case {
            border-bottom: 1px solid #ecf0f1;
            padding: 15px 25px;
        }
        
        .test-case:last-child {
            border-bottom: none;
        }
        
        .test-case-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            cursor: pointer;
        }
        
        .test-case-title {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.75em;
            font-weight: bold;
            text-transform: uppercase;
        }
        
        .status-passed { background: #d4edda; color: #155724; }
        .status-failed { background: #f8d7da; color: #721c24; }
        .status-error { background: #fff3cd; color: #856404; }
        .status-skipped { background: #e2e3e5; color: #383d41; }
        .status-pending { background: #d1ecf1; color: #0c5460; }
        
        .test-case-details {
            margin-top: 15px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
            display: none;
        }
        
        .test-case-details.expanded {
            display: block;
        }
        
        .detail-row {
            display: flex;
            margin-bottom: 10px;
        }
        
        .detail-label {
            width: 120px;
            color: #6c757d;
            font-weight: 500;
        }
        
        .detail-value {
            flex: 1;
            font-family: 'Courier New', monospace;
            font-size: 0.9em;
        }
        
        .validation-list {
            margin-top: 15px;
        }
        
        .validation-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px;
            margin-bottom: 8px;
            border-radius: 6px;
            font-size: 0.9em;
        }
        
        .validation-passed {
            background: #d4edda;
        }
        
        .validation-failed {
            background: #f8d7da;
        }
        
        .response-preview {
            background: #2c3e50;
            color: #ecf0f1;
            padding: 15px;
            border-radius: 8px;
            margin-top: 15px;
            overflow-x: auto;
        }
        
        .response-preview pre {
            margin: 0;
            font-size: 0.85em;
            line-height: 1.5;
        }
        
        /* Failed Tests Section */
        .failed-section {
            padding: 30px;
            background: white;
        }
        
        .failed-alert {
            background: #fff5f5;
            border-left: 4px solid #e74c3c;
            padding: 20px;
            border-radius: 0 8px 8px 0;
            margin-bottom: 20px;
        }
        
        .failed-alert h3 {
            color: #e74c3c;
            margin-bottom: 10px;
        }
        
        /* Footer */
        .footer {
            background: #2c3e50;
            color: white;
            padding: 20px;
            text-align: center;
            font-size: 0.9em;
        }
        
        /* Toggle Icon */
        .toggle-icon {
            transition: transform 0.3s;
        }
        
        .toggle-icon.rotated {
            transform: rotate(180deg);
        }
        
        /* Charts */
        .chart-container {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
        }
        
        .chart-title {
            font-size: 1.1em;
            margin-bottom: 15px;
            color: #2c3e50;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .summary-grid {
                grid-template-columns: repeat(2, 1fr);
            }
            
            .suite-stats {
                flex-direction: column;
                gap: 5px;
            }
            
            .detail-row {
                flex-direction: column;
            }
            
            .detail-label {
                width: auto;
                margin-bottom: 5px;
            }
        }
        """
    
    def _get_javascript(self) -> str:
        """Get JavaScript for interactive features"""
        return """
        document.addEventListener('DOMContentLoaded', function() {
            // Toggle suite content
            document.querySelectorAll('.suite-header').forEach(header => {
                header.addEventListener('click', function() {
                    const content = this.nextElementSibling;
                    const icon = this.querySelector('.toggle-icon');
                    content.classList.toggle('expanded');
                    icon.classList.toggle('rotated');
                });
            });
            
            // Toggle test case details
            document.querySelectorAll('.test-case-header').forEach(header => {
                header.addEventListener('click', function() {
                    const details = this.nextElementSibling;
                    details.classList.toggle('expanded');
                });
            });
            
            // Animate success rate bar
            setTimeout(() => {
                const fill = document.querySelector('.success-rate-fill');
                if (fill) {
                    const width = fill.style.width;
                    fill.style.width = '0%';
                    setTimeout(() => {
                        fill.style.width = width;
                    }, 100);
                }
            }, 500);
        });
        """
    
    def _build_header(self) -> str:
        """Build report header"""
        return f"""
        <header class="header">
            <h1>🧪 API Test Report</h1>
            <p class="subtitle">CampusOrder Backend API Testing Framework</p>
            <p class="timestamp">Generated: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}</p>
        </header>
        """
    
    def _build_summary_section(self, summary: Dict[str, Any]) -> str:
        """Build summary section with cards"""
        total = summary.get('total_tests', 0)
        passed = summary.get('passed_tests', 0)
        failed = summary.get('failed_tests', 0)
        errors = summary.get('error_tests', 0)
        skipped = summary.get('skipped_tests', 0)
        success_rate = summary.get('success_rate', 0)
        
        rate_class = 'low' if success_rate < 50 else ('medium' if success_rate < 80 else '')
        
        return f"""
        <section class="summary-section">
            <div class="summary-grid">
                <div class="summary-card card-total">
                    <div class="icon">📊</div>
                    <div class="value">{total}</div>
                    <div class="label">Total Tests</div>
                </div>
                <div class="summary-card card-passed">
                    <div class="icon">✅</div>
                    <div class="value">{passed}</div>
                    <div class="label">Passed</div>
                </div>
                <div class="summary-card card-failed">
                    <div class="icon">❌</div>
                    <div class="value">{failed}</div>
                    <div class="label">Failed</div>
                </div>
                <div class="summary-card card-error">
                    <div class="icon">⚠️</div>
                    <div class="value">{errors}</div>
                    <div class="label">Errors</div>
                </div>
                <div class="summary-card card-skipped">
                    <div class="icon">⏭️</div>
                    <div class="value">{skipped}</div>
                    <div class="label">Skipped</div>
                </div>
                <div class="summary-card card-rate">
                    <div class="icon">🎯</div>
                    <div class="value">{success_rate:.1f}%</div>
                    <div class="label">Success Rate</div>
                </div>
            </div>
            
            <div class="success-rate-container">
                <div class="success-rate-header">
                    <span>Overall Success Rate</span>
                    <span class="value" style="color: {'#e74c3c' if success_rate < 50 else ('#f39c12' if success_rate < 80 else '#27ae60')}; font-weight: bold;">{success_rate:.1f}%</span>
                </div>
                <div class="success-rate-bar">
                    <div class="success-rate-fill {rate_class}" style="width: {success_rate}%;">
                        {success_rate:.1f}%
                    </div>
                </div>
            </div>
        </section>
        """
    
    def _build_performance_section(self, summary: Dict[str, Any]) -> str:
        """Build performance metrics section"""
        perf = summary.get('performance_stats', {})
        
        if not perf:
            return """
            <section class="performance-section">
                <h2 class="section-title">⚡ Performance Metrics</h2>
                <p style="color: #6c757d;">No performance data available</p>
            </section>
            """
        
        return f"""
        <section class="performance-section">
            <h2 class="section-title">⚡ Performance Metrics</h2>
            <div class="performance-grid">
                <div class="performance-card">
                    <div class="metric-name">Total Requests</div>
                    <div class="metric-value">{perf.get('total_requests', 0)}</div>
                </div>
                <div class="performance-card">
                    <div class="metric-name">Average Response Time</div>
                    <div class="metric-value">{perf.get('avg_response_time', 0):.3f}<span class="metric-unit">s</span></div>
                </div>
                <div class="performance-card">
                    <div class="metric-name">Min Response Time</div>
                    <div class="metric-value">{perf.get('min_response_time', 0):.3f}<span class="metric-unit">s</span></div>
                </div>
                <div class="performance-card">
                    <div class="metric-name">Max Response Time</div>
                    <div class="metric-value">{perf.get('max_response_time', 0):.3f}<span class="metric-unit">s</span></div>
                </div>
                <div class="performance-card">
                    <div class="metric-name">P90 Response Time</div>
                    <div class="metric-value">{perf.get('p90_response_time', 0):.3f}<span class="metric-unit">s</span></div>
                </div>
                <div class="performance-card">
                    <div class="metric-name">P95 Response Time</div>
                    <div class="metric-value">{perf.get('p95_response_time', 0):.3f}<span class="metric-unit">s</span></div>
                </div>
            </div>
        </section>
        """
    
    def _build_suites_section(self) -> str:
        """Build test suites section"""
        if not self.collector.suites:
            return """
            <section class="suites-section">
                <h2 class="section-title">📁 Test Suites</h2>
                <p style="color: #6c757d;">No test suites executed</p>
            </section>
            """
        
        suites_html = []
        for suite_name, suite in self.collector.suites.items():
            suites_html.append(self._build_suite_card(suite))
        
        return f"""
        <section class="suites-section">
            <h2 class="section-title">📁 Test Suites</h2>
            {''.join(suites_html)}
        </section>
        """
    
    def _build_suite_card(self, suite) -> str:
        """Build individual suite card"""
        test_cases_html = []
        for tc in suite.test_cases:
            test_cases_html.append(self._build_test_case_html(tc))
        
        return f"""
        <div class="suite-card">
            <div class="suite-header">
                <h3>📂 {suite.suite_name}</h3>
                <div class="suite-stats">
                    <span class="suite-stat">✅ {suite.passed_tests}</span>
                    <span class="suite-stat">❌ {suite.failed_tests}</span>
                    <span class="suite-stat">⚠️ {suite.error_tests}</span>
                    <span class="suite-stat">⏱️ {suite.duration:.2f}s</span>
                </div>
                <span class="toggle-icon">▼</span>
            </div>
            <div class="suite-content">
                {''.join(test_cases_html)}
            </div>
        </div>
        """
    
    def _build_test_case_html(self, tc) -> str:
        """Build individual test case HTML"""
        status_class = f"status-{tc.status.value}"
        status_icon = {
            TestStatus.PASSED: '✅',
            TestStatus.FAILED: '❌',
            TestStatus.ERROR: '⚠️',
            TestStatus.SKIPPED: '⏭️',
            TestStatus.PENDING: '⏳'
        }.get(tc.status, '❓')
        
        validations_html = []
        for v in tc.validations:
            v_class = 'validation-passed' if v.passed else 'validation-failed'
            v_icon = '✓' if v.passed else '✗'
            validations_html.append(f"""
                <div class="validation-item {v_class}">
                    <span>{v_icon}</span>
                    <span><strong>{v.validation_type.value}</strong>: {v.name}</span>
                    {f'<span style="color: #6c757d; margin-left: auto;">Expected: {v.expected}, Got: {v.actual}</span>' if not v.passed and v.expected is not None else ''}
                </div>
            """)
        
        response_html = ""
        if tc.response:
            response_body = json.dumps(tc.response.body, ensure_ascii=False, indent=2) if tc.response.body else "No body"
            response_html = f"""
            <div class="response-preview">
                <div style="margin-bottom: 10px; color: #bdc3c7;">Response Body:</div>
                <pre>{response_body[:2000]}{'...' if len(response_body) > 2000 else ''}</pre>
            </div>
            """
        
        error_html = ""
        if tc.error_message:
            error_html = f"""
            <div class="detail-row">
                <div class="detail-label">Error:</div>
                <div class="detail-value" style="color: #e74c3c;">{tc.error_message}</div>
            </div>
            """
        
        return f"""
        <div class="test-case">
            <div class="test-case-header">
                <div class="test-case-title">
                    <span class="status-badge {status_class}">{status_icon} {tc.status.value}</span>
                    <span style="font-weight: 500;">{tc.test_name}</span>
                </div>
                <span style="color: #6c757d; font-size: 0.9em;">{f"{tc.response.response_time:.3f}s" if tc.response else "N/A"}</span>
            </div>
            <div class="test-case-details">
                <div class="detail-row">
                    <div class="detail-label">Test ID:</div>
                    <div class="detail-value">{tc.test_id}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Description:</div>
                    <div class="detail-value">{tc.description or 'N/A'}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Method:</div>
                    <div class="detail-value">{tc.method}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Endpoint:</div>
                    <div class="detail-value">{tc.endpoint}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Status Code:</div>
                    <div class="detail-value">{tc.response.status_code if tc.response else 'N/A'}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Duration:</div>
                    <div class="detail-value">{tc.duration:.3f}s</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Priority:</div>
                    <div class="detail-value">{tc.priority}</div>
                </div>
                {error_html}
                
                <div class="validation-list">
                    <div style="font-weight: 500; margin-bottom: 10px;">Validations ({len(tc.validations)}):</div>
                    {''.join(validations_html) if validations_html else '<p style="color: #6c757d;">No validations recorded</p>'}
                </div>
                
                {response_html}
            </div>
        </div>
        """
    
    def _build_failed_tests_section(self) -> str:
        """Build failed tests section"""
        failed_tests = self.collector.get_failed_tests()
        
        if not failed_tests:
            return """
            <section class="failed-section">
                <h2 class="section-title">🔍 Failed Tests Analysis</h2>
                <div style="background: #d4edda; padding: 20px; border-radius: 8px; color: #155724;">
                    🎉 All tests passed! No failures to analyze.
                </div>
            </section>
            """
        
        failed_html = []
        for tc in failed_tests[:20]:  # Limit to first 20 failed tests
            failed_validations = tc.failed_validations
            fv_summary = ', '.join([f"{v.name}: {v.message}" for v in failed_validations[:3]])
            
            failed_html.append(f"""
            <div style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                <div style="font-weight: 500; color: #e74c3c; margin-bottom: 5px;">
                    {tc.test_suite} / {tc.test_name}
                </div>
                <div style="font-size: 0.9em; color: #6c757d;">
                    {tc.method} {tc.endpoint}
                </div>
                <div style="font-size: 0.85em; color: #e74c3c; margin-top: 5px;">
                    {fv_summary}
                </div>
            </div>
            """)
        
        return f"""
        <section class="failed-section">
            <h2 class="section-title">🔍 Failed Tests Analysis</h2>
            <div class="failed-alert">
                <h3>⚠️ {len(failed_tests)} Test(s) Failed</h3>
                <p>Review the failed tests below and check the detailed error messages.</p>
            </div>
            <div style="background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.05);">
                {''.join(failed_html)}
            </div>
        </section>
        """
    
    def _build_footer(self) -> str:
        """Build report footer"""
        return f"""
        <footer class="footer">
            <p>CampusOrder API Test Framework v1.0.0 | Generated with ❤️</p>
            <p style="margin-top: 5px; opacity: 0.8;">{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}</p>
        </footer>
        """


class ReportComparison:
    """Compare multiple test reports"""
    
    @staticmethod
    def compare_reports(report_paths: List[str]) -> Dict[str, Any]:
        """Compare multiple JSON reports"""
        reports = []
        for path in report_paths:
            with open(path, 'r', encoding='utf-8') as f:
                reports.append(json.load(f))
        
        comparison = {
            "reports": [],
            "trends": {
                "success_rate": [],
                "total_tests": [],
                "avg_response_time": []
            },
            "comparison_summary": {}
        }
        
        for i, report in enumerate(reports):
            summary = report.get('summary', {})
            comparison["reports"].append({
                "index": i + 1,
                "timestamp": report.get('report_info', {}).get('generated_at', 'Unknown'),
                "total_tests": summary.get('total_tests', 0),
                "passed_tests": summary.get('passed_tests', 0),
                "success_rate": summary.get('success_rate', 0),
                "avg_response_time": summary.get('performance_stats', {}).get('avg_response_time', 0)
            })
            
            comparison["trends"]["success_rate"].append(summary.get('success_rate', 0))
            comparison["trends"]["total_tests"].append(summary.get('total_tests', 0))
            comparison["trends"]["avg_response_time"].append(
                summary.get('performance_stats', {}).get('avg_response_time', 0)
            )
        
        # Calculate trends
        if len(comparison["reports"]) >= 2:
            first = comparison["reports"][0]
            last = comparison["reports"][-1]
            
            comparison["comparison_summary"] = {
                "success_rate_change": round(last["success_rate"] - first["success_rate"], 2),
                "total_tests_change": last["total_tests"] - first["total_tests"],
                "avg_response_time_change": round(last["avg_response_time"] - first["avg_response_time"], 3)
            }
        
        return comparison


# Convenience functions
def generate_report(collector: TestResultCollector, format: str = "both", output_dir: str = None) -> Dict[str, str]:
    """Generate report in specified format"""
    generator = ReportGenerator(collector)
    
    if output_dir:
        generator.report_dir = Path(output_dir)
        generator.report_dir.mkdir(exist_ok=True)
    
    if format == "json":
        return {"json": generator.generate_json_report()}
    elif format == "html":
        return {"html": generator.generate_html_report()}
    else:
        return generator.generate_both_reports()


def compare_reports(report_paths: List[str]) -> Dict[str, Any]:
    """Compare multiple test reports"""
    return ReportComparison.compare_reports(report_paths)
