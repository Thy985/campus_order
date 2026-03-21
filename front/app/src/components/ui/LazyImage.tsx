import { useState, useRef, useEffect } from 'react';
import { Skeleton } from './skeleton';

interface LazyImageProps {
  src: string;
  alt: string;
  className?: string;
  aspectRatio?: string;
  priority?: boolean;
  objectFit?: 'cover' | 'contain' | 'fill' | 'none' | 'scale-down';
  placeholderColor?: string;
}

export function LazyImage({
  src,
  alt,
  className = '',
  aspectRatio = '4/3',
  priority = false,
  objectFit = 'cover',
  placeholderColor = '#f3f4f6',
}: LazyImageProps) {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isInView, setIsInView] = useState(priority);
  const [hasError, setHasError] = useState(false);
  const imgRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (priority) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsInView(true);
          observer.disconnect();
        }
      },
      { 
        rootMargin: '200px', // 提前 200px 开始加载
        threshold: 0.01 
      }
    );

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => observer.disconnect();
  }, [priority]);

  // 预加载关键图片
  useEffect(() => {
    if (priority && src) {
      const img = new Image();
      img.src = src;
    }
  }, [priority, src]);

  return (
    <div
      ref={imgRef}
      className={`relative overflow-hidden ${className}`}
      style={{ 
        aspectRatio,
        backgroundColor: placeholderColor 
      }}
      role="img"
      aria-label={alt}
    >
      {/* 加载骨架屏 */}
      {!isLoaded && !hasError && (
        <Skeleton className="absolute inset-0" />
      )}
      
      {/* 错误状态 - 默认占位图 */}
      {hasError && (
        <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
          <div className="text-center">
            <svg
              className="w-12 h-12 mx-auto text-gray-300 mb-2"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span className="text-gray-400 text-sm">图片加载失败</span>
          </div>
        </div>
      )}
      
      {/* 实际图片 */}
      {isInView && (
        <img
          src={src}
          alt={alt}
          loading={priority ? 'eager' : 'lazy'}
          decoding="async"
          style={{ objectFit }}
          className={`w-full h-full transition-all duration-500 ${
            isLoaded ? 'opacity-100 scale-100' : 'opacity-0 scale-105'
          }`}
          onLoad={() => setIsLoaded(true)}
          onError={() => {
            setHasError(true);
            setIsLoaded(true);
          }}
        />
      )}
    </div>
  );
}

// 预加载图片工具函数
export function preloadImage(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve();
    img.onerror = reject;
    img.src = src;
  });
}

// 批量预加载
export function preloadImages(srcs: string[]): Promise<void[]> {
  return Promise.all(srcs.map(src => preloadImage(src).catch(() => undefined)));
}
