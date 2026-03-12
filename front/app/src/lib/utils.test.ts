import { describe, it, expect } from 'vitest'
import {
  cn,
  formatPrice,
  formatPhone,
  formatDate,
  formatTime,
  formatDateTime,
  sleep,
  generateId,
  deepClone,
  truncateText,
  isValidPhone,
  isValidEmail,
  getFileExtension,
  formatFileSize,
} from '@/lib/utils'

describe('cn (className merge)', () => {
  it('should merge class names correctly', () => {
    expect(cn('foo', 'bar')).toBe('foo bar')
  })

  it('should handle conditional classes', () => {
    expect(cn('foo', false && 'bar', 'baz')).toBe('foo baz')
  })

  it('should merge tailwind classes correctly', () => {
    expect(cn('px-2 py-1', 'px-4')).toBe('py-1 px-4')
  })

  it('should handle undefined and null values', () => {
    expect(cn('foo', undefined, null, 'bar')).toBe('foo bar')
  })
})

describe('formatPrice', () => {
  it('should format price with yuan symbol', () => {
    expect(formatPrice(10)).toBe('¥10.00')
  })

  it('should handle decimal values', () => {
    expect(formatPrice(10.5)).toBe('¥10.50')
  })

  it('should handle zero', () => {
    expect(formatPrice(0)).toBe('¥0.00')
  })

  it('should round to two decimal places', () => {
    expect(formatPrice(10.999)).toBe('¥11.00')
  })
})

describe('formatPhone', () => {
  it('should mask middle digits of phone number', () => {
    expect(formatPhone('13812345678')).toBe('138****5678')
  })

  it('should handle invalid phone format', () => {
    expect(formatPhone('123')).toBe('123')
  })
})

describe('formatDate', () => {
  it('should format date string correctly', () => {
    const result = formatDate('2024-01-15')
    expect(result).toContain('2024')
    expect(result).toContain('1月')
    expect(result).toContain('15')
  })

  it('should handle Date object', () => {
    const date = new Date('2024-06-20')
    const result = formatDate(date)
    expect(result).toContain('2024')
    expect(result).toContain('6月')
  })
})

describe('formatTime', () => {
  it('should format time correctly', () => {
    const result = formatTime('2024-01-15T14:30:00')
    expect(result).toContain('14')
    expect(result).toContain('30')
  })
})

describe('formatDateTime', () => {
  it('should format date and time together', () => {
    const result = formatDateTime('2024-01-15T14:30:00')
    expect(result).toContain('2024')
    expect(result).toContain('14')
  })
})

describe('sleep', () => {
  it('should resolve after specified milliseconds', async () => {
    const start = Date.now()
    await sleep(50)
    const elapsed = Date.now() - start
    expect(elapsed).toBeGreaterThanOrEqual(40)
  })
})

describe('generateId', () => {
  it('should generate a string id', () => {
    const id = generateId()
    expect(typeof id).toBe('string')
    expect(id.length).toBeGreaterThan(0)
  })

  it('should generate unique ids', () => {
    const ids = new Set(Array.from({ length: 100 }, () => generateId()))
    expect(ids.size).toBeGreaterThan(90)
  })
})

describe('deepClone', () => {
  it('should create a deep copy of an object', () => {
    const original = { a: 1, b: { c: 2 } }
    const cloned = deepClone(original)
    expect(cloned).toEqual(original)
    expect(cloned).not.toBe(original)
    expect(cloned.b).not.toBe(original.b)
  })

  it('should handle arrays', () => {
    const original = [1, [2, 3], { a: 4 }]
    const cloned = deepClone(original)
    expect(cloned).toEqual(original)
    expect(cloned).not.toBe(original)
  })
})

describe('truncateText', () => {
  it('should not truncate short text', () => {
    expect(truncateText('hello', 10)).toBe('hello')
  })

  it('should truncate long text with ellipsis', () => {
    expect(truncateText('hello world', 5)).toBe('hello...')
  })

  it('should handle exact length', () => {
    expect(truncateText('hello', 5)).toBe('hello')
  })
})

describe('isValidPhone', () => {
  it('should return true for valid Chinese phone numbers', () => {
    expect(isValidPhone('13812345678')).toBe(true)
    expect(isValidPhone('15912345678')).toBe(true)
    expect(isValidPhone('18812345678')).toBe(true)
  })

  it('should return false for invalid phone numbers', () => {
    expect(isValidPhone('12345678901')).toBe(false)
    expect(isValidPhone('1381234567')).toBe(false)
    expect(isValidPhone('abcdefghijk')).toBe(false)
  })
})

describe('isValidEmail', () => {
  it('should return true for valid emails', () => {
    expect(isValidEmail('test@example.com')).toBe(true)
    expect(isValidEmail('user.name@domain.co')).toBe(true)
  })

  it('should return false for invalid emails', () => {
    expect(isValidEmail('invalid')).toBe(false)
    expect(isValidEmail('test@')).toBe(false)
    expect(isValidEmail('@domain.com')).toBe(false)
  })
})

describe('getFileExtension', () => {
  it('should extract file extension', () => {
    expect(getFileExtension('document.pdf')).toBe('pdf')
    expect(getFileExtension('image.png')).toBe('png')
    expect(getFileExtension('archive.tar.gz')).toBe('gz')
  })

  it('should handle files without extension', () => {
    expect(getFileExtension('README')).toBe('')
  })
})

describe('formatFileSize', () => {
  it('should format bytes correctly', () => {
    expect(formatFileSize(0)).toBe('0 Bytes')
    expect(formatFileSize(500)).toBe('500 Bytes')
    expect(formatFileSize(1024)).toBe('1 KB')
    expect(formatFileSize(1048576)).toBe('1 MB')
    expect(formatFileSize(1073741824)).toBe('1 GB')
  })

  it('should handle decimal values', () => {
    expect(formatFileSize(1536)).toBe('1.5 KB')
  })
})
