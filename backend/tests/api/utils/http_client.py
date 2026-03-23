"""
HTTP client wrapper for API testing framework.
Provides retry mechanism, timeout control, and authentication management.
"""
import time
import json
from typing import Dict, Any, Optional, Callable
from dataclasses import dataclass

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
from tenacity import retry, stop_after_attempt, wait_exponential

from config.settings import get_settings
from utils.logger import get_logger

logger = get_logger()


@dataclass
class APIResponse:
    """API response wrapper"""
    status_code: int
    headers: Dict[str, str]
    body: Any
    text: str
    response_time: float
    url: str
    method: str

    @property
    def is_success(self) -> bool:
        """Check if response is successful (2xx)"""
        return 200 <= self.status_code < 300

    @property
    def is_client_error(self) -> bool:
        """Check if response is client error (4xx)"""
        return 400 <= self.status_code < 500

    @property
    def is_server_error(self) -> bool:
        """Check if response is server error (5xx)"""
        return 500 <= self.status_code < 600

    def json(self) -> Any:
        """Get JSON body"""
        return self.body


class HTTPClient:
    """
    HTTP client for API testing.
    Features:
    - Session management
    - Automatic retry with exponential backoff
    - Request/response logging
    - Authentication token management
    - Timeout control
    """

    def __init__(self):
        self.settings = get_settings()
        self.session = requests.Session()
        self.base_url = self.settings.api.base_url
        self.timeout = self.settings.api.timeout

        # Setup retry strategy
        retry_strategy = Retry(
            total=self.settings.api.retry_max_attempts,
            backoff_factor=self.settings.api.retry_backoff_factor,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["HEAD", "GET", "OPTIONS", "POST", "PUT", "DELETE", "PATCH"]
        )

        adapter = HTTPAdapter(max_retries=retry_strategy)
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)

        # Token storage
        self._token: Optional[str] = None
        self._token_type: str = "Bearer"

    def set_token(self, token: str, token_type: str = "Bearer"):
        """Set authentication token"""
        self._token = token
        self._token_type = token_type

    def clear_token(self):
        """Clear authentication token"""
        self._token = None

    def _get_headers(self, extra_headers: Optional[Dict[str, str]] = None) -> Dict[str, str]:
        """Build request headers"""
        headers = {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "CampusOrder-API-Test/1.0"
        }

        if self._token:
            headers["Authorization"] = f"{self._token_type} {self._token}"

        if extra_headers:
            headers.update(extra_headers)

        return headers

    def _log_request(self, method: str, url: str, **kwargs):
        """Log request details"""
        logger.debug(f"Request: {method} {url}")
        if 'json' in kwargs:
            logger.debug(f"Request Body: {json.dumps(kwargs['json'], ensure_ascii=False)}")
        if 'params' in kwargs:
            logger.debug(f"Request Params: {kwargs['params']}")

    def _log_response(self, response: requests.Response, elapsed: float):
        """Log response details"""
        logger.debug(f"Response: {response.status_code} ({elapsed:.3f}s)")
        try:
            body = response.json()
            logger.debug(f"Response Body: {json.dumps(body, ensure_ascii=False)}")
        except:
            logger.debug(f"Response Text: {response.text[:500]}")

    def _make_request(
        self,
        method: str,
        endpoint: str,
        **kwargs
    ) -> APIResponse:
        """Make HTTP request"""
        url = f"{self.base_url}{endpoint}"
        headers = self._get_headers(kwargs.pop('headers', None))

        self._log_request(method, url, **kwargs)

        start_time = time.time()
        try:
            response = self.session.request(
                method=method,
                url=url,
                headers=headers,
                timeout=self.timeout,
                **kwargs
            )
            elapsed = time.time() - start_time

            self._log_response(response, elapsed)

            # Parse response body
            try:
                body = response.json()
            except:
                body = response.text

            return APIResponse(
                status_code=response.status_code,
                headers=dict(response.headers),
                body=body,
                text=response.text,
                response_time=elapsed,
                url=url,
                method=method
            )

        except requests.exceptions.Timeout:
            logger.error(f"Request timeout: {method} {url}")
            raise
        except requests.exceptions.ConnectionError as e:
            logger.error(f"Connection error: {method} {url} - {e}")
            raise
        except Exception as e:
            logger.error(f"Request failed: {method} {url} - {e}")
            raise

    def get(
        self,
        endpoint: str,
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Send GET request"""
        return self._make_request("GET", endpoint, params=params, headers=headers)

    def post(
        self,
        endpoint: str,
        json_data: Optional[Dict[str, Any]] = None,
        data: Optional[Any] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Send POST request"""
        kwargs = {'headers': headers}
        if json_data:
            kwargs['json'] = json_data
        if data:
            kwargs['data'] = data
        return self._make_request("POST", endpoint, **kwargs)

    def put(
        self,
        endpoint: str,
        json_data: Optional[Dict[str, Any]] = None,
        data: Optional[Any] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Send PUT request"""
        kwargs = {'headers': headers}
        if json_data:
            kwargs['json'] = json_data
        if data:
            kwargs['data'] = data
        return self._make_request("PUT", endpoint, **kwargs)

    def patch(
        self,
        endpoint: str,
        json_data: Optional[Dict[str, Any]] = None,
        data: Optional[Any] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Send PATCH request"""
        kwargs = {'headers': headers}
        if json_data:
            kwargs['json'] = json_data
        if data:
            kwargs['data'] = data
        return self._make_request("PATCH", endpoint, **kwargs)

    def delete(
        self,
        endpoint: str,
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Send DELETE request"""
        return self._make_request("DELETE", endpoint, params=params, headers=headers)

    def upload(
        self,
        endpoint: str,
        files: Dict[str, Any],
        data: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> APIResponse:
        """Upload files"""
        # Remove Content-Type header for multipart requests
        headers = headers or {}
        headers.pop('Content-Type', None)

        kwargs = {'headers': headers, 'files': files}
        if data:
            kwargs['data'] = data

        return self._make_request("POST", endpoint, **kwargs)

    def close(self):
        """Close session"""
        self.session.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()


# Global HTTP client instance
_http_client: Optional[HTTPClient] = None


def get_http_client() -> HTTPClient:
    """Get HTTP client instance"""
    global _http_client
    if _http_client is None:
        _http_client = HTTPClient()
    return _http_client


def reset_http_client():
    """Reset HTTP client instance"""
    global _http_client
    if _http_client:
        _http_client.close()
    _http_client = None
