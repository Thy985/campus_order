"""
Configuration management module for API testing framework.
Supports multiple environments: dev, test, prod
"""
import os
import yaml
from pathlib import Path
from typing import Dict, Any, Optional
from dataclasses import dataclass


@dataclass
class APIConfig:
    """API configuration"""
    base_url: str
    timeout: int
    retry_max_attempts: int
    retry_backoff_factor: float


@dataclass
class DatabaseConfig:
    """Database configuration"""
    host: str
    port: int
    name: str
    user: str
    password: str


@dataclass
class TestDataConfig:
    """Test data configuration"""
    user_prefix: str
    user_password: str
    merchant_prefix: str
    product_prefix: str


@dataclass
class LoggingConfig:
    """Logging configuration"""
    level: str
    format: str


@dataclass
class RateLimitConfig:
    """Rate limiting configuration"""
    enabled: bool
    requests_per_second: int = 10


class Settings:
    """
    Settings manager for API testing framework.
    Loads configuration from YAML files based on environment.
    """

    _instance = None
    _config: Dict[str, Any] = {}

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load_config()
        return cls._instance

    def _load_config(self):
        """Load configuration from YAML file"""
        env = os.getenv('ENV', 'test')
        config_dir = Path(__file__).parent
        config_file = config_dir / f"{env}.yaml"

        if not config_file.exists():
            raise FileNotFoundError(f"Configuration file not found: {config_file}")

        with open(config_file, 'r', encoding='utf-8') as f:
            self._config = yaml.safe_load(f)

        # Replace environment variables in config
        self._config = self._replace_env_vars(self._config)

    def _replace_env_vars(self, obj: Any) -> Any:
        """Recursively replace environment variables in config"""
        if isinstance(obj, dict):
            return {k: self._replace_env_vars(v) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [self._replace_env_vars(item) for item in obj]
        elif isinstance(obj, str) and obj.startswith('${') and obj.endswith('}'):
            env_var = obj[2:-1]
            return os.getenv(env_var, obj)
        return obj

    @property
    def env(self) -> str:
        """Get current environment"""
        return self._config.get('env', 'test')

    @property
    def api(self) -> APIConfig:
        """Get API configuration"""
        api_config = self._config.get('api', {})
        return APIConfig(
            base_url=api_config.get('base_url', 'http://localhost:8080'),
            timeout=api_config.get('timeout', 30),
            retry_max_attempts=api_config.get('retry', {}).get('max_attempts', 3),
            retry_backoff_factor=api_config.get('retry', {}).get('backoff_factor', 1)
        )

    @property
    def database(self) -> Optional[DatabaseConfig]:
        """Get database configuration"""
        db_config = self._config.get('database')
        if not db_config:
            return None
        return DatabaseConfig(
            host=db_config.get('host', 'localhost'),
            port=db_config.get('port', 3306),
            name=db_config.get('name', 'campus_order'),
            user=db_config.get('user', 'root'),
            password=db_config.get('password', '')
        )

    @property
    def test_data(self) -> TestDataConfig:
        """Get test data configuration"""
        td_config = self._config.get('test_data', {})
        return TestDataConfig(
            user_prefix=td_config.get('user', {}).get('prefix', 'test_user_'),
            user_password=td_config.get('user', {}).get('password', 'Test@123456'),
            merchant_prefix=td_config.get('merchant', {}).get('prefix', 'test_merchant_'),
            product_prefix=td_config.get('product', {}).get('prefix', 'test_product_')
        )

    @property
    def logging(self) -> LoggingConfig:
        """Get logging configuration"""
        log_config = self._config.get('logging', {})
        return LoggingConfig(
            level=log_config.get('level', 'INFO'),
            format=log_config.get('format', '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        )

    @property
    def rate_limit(self) -> RateLimitConfig:
        """Get rate limiting configuration"""
        rl_config = self._config.get('rate_limit', {})
        return RateLimitConfig(
            enabled=rl_config.get('enabled', False),
            requests_per_second=rl_config.get('requests_per_second', 10)
        )

    def get(self, key: str, default: Any = None) -> Any:
        """Get configuration value by key"""
        keys = key.split('.')
        value = self._config
        for k in keys:
            if isinstance(value, dict):
                value = value.get(k)
                if value is None:
                    return default
            else:
                return default
        return value

    def reload(self):
        """Reload configuration"""
        self._load_config()


# Global settings instance
settings = Settings()


def get_settings() -> Settings:
    """Get settings instance"""
    return settings
