// 性能监控工具

interface PerformanceMetrics {
  pageLoadTime?: number;
  fcp?: number;
  lcp?: number;
  fid?: number;
  cls?: number;
}

interface LayoutShiftEntry extends PerformanceEntry {
  hadRecentInput: boolean;
  value: number;
}

interface FirstInputEntry extends PerformanceEntry {
  processingStart: number;
}

// 初始化性能监控
export function initPerformanceMonitoring(): void {
  if (typeof window === 'undefined') return;

  // 等待页面加载完成
  window.addEventListener('load', () => {
    // 使用 requestIdleCallback 在浏览器空闲时收集性能数据
    if ('requestIdleCallback' in window) {
      requestIdleCallback(() => {
        collectPerformanceMetrics();
      });
    } else {
      setTimeout(collectPerformanceMetrics, 1000);
    }
  });

  // 监听 LCP
  observeLCP();
  
  // 监听 CLS
  observeCLS();
  
  // 监听 FID
  observeFID();
}

// 收集性能指标
function collectPerformanceMetrics(): PerformanceMetrics {
  const metrics: PerformanceMetrics = {};
  
  try {
    const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
    
    if (navigation) {
      metrics.pageLoadTime = navigation.loadEventEnd - navigation.startTime;
    }

    // 获取 FCP
    const paintEntries = performance.getEntriesByType('paint');
    const fcpEntry = paintEntries.find(entry => entry.name === 'first-contentful-paint');
    if (fcpEntry) {
      metrics.fcp = fcpEntry.startTime;
    }

    // 输出性能数据到控制台（开发环境）
    if (import.meta.env.DEV) {
      console.log('[Performance]', metrics);
    }

    // 可以发送到分析服务
    // sendToAnalytics(metrics);
    
  } catch (error) {
    console.error('Failed to collect performance metrics:', error);
  }
  
  return metrics;
}

// 监听 LCP (Largest Contentful Paint)
function observeLCP(): void {
  if (!('PerformanceObserver' in window)) return;
  
  try {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      
      if (import.meta.env.DEV) {
        console.log('[LCP]', lastEntry.startTime);
      }
    });
    
    observer.observe({ entryTypes: ['largest-contentful-paint'] });
  } catch {
    // 浏览器不支持
  }
}

// 监听 CLS (Cumulative Layout Shift)
function observeCLS(): void {
  if (!('PerformanceObserver' in window)) return;
  
  try {
    let clsValue = 0;
    
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        const lsEntry = entry as LayoutShiftEntry;
        if (!lsEntry.hadRecentInput) {
          clsValue += lsEntry.value;
        }
      }
      
      if (import.meta.env.DEV) {
        console.log('[CLS]', clsValue);
      }
    });
    
    observer.observe({ entryTypes: ['layout-shift'] });
  } catch {
    // 浏览器不支持
  }
}

// 监听 FID (First Input Delay)
function observeFID(): void {
  if (!('PerformanceObserver' in window)) return;
  
  try {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        const fiEntry = entry as FirstInputEntry;
        const delay = fiEntry.processingStart - entry.startTime;
        
        if (import.meta.env.DEV) {
          console.log('[FID]', delay);
        }
      }
    });
    
    observer.observe({ entryTypes: ['first-input'] });
  } catch {
    // 浏览器不支持
  }
}

// 图片预加载
export function preloadImage(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve();
    img.onerror = reject;
    img.src = src;
  });
}

// 批量预加载图片
export function preloadImages(srcs: string[]): Promise<void[]> {
  return Promise.all(srcs.map(src => preloadImage(src).catch(() => undefined)));
}

// 防抖函数
export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null;
  
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}

// 节流函数
export function throttle<T extends (...args: unknown[]) => unknown>(
  fn: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle = false;
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      fn(...args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}
