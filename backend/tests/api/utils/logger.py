"""
Logging utility for API testing framework.
Uses loguru for enhanced logging capabilities.
"""
import sys
from pathlib import Path
from loguru import logger as _logger

from config.settings import get_settings


def setup_logger():
    """Setup logger with configuration"""
    settings = get_settings()

    # Remove default handler
    _logger.remove()

    # Add console handler
    _logger.add(
        sys.stdout,
        level=settings.logging.level,
        format=settings.logging.format,
        colorize=True
    )

    # Add file handler
    log_dir = Path(__file__).parent.parent / "reports"
    log_dir.mkdir(exist_ok=True)
    _logger.add(
        log_dir / "test.log",
        level="DEBUG",
        format=settings.logging.format,
        rotation="10 MB",
        retention="7 days"
    )

    return _logger


# Global logger instance
logger = setup_logger()


def get_logger():
    """Get logger instance"""
    return logger
