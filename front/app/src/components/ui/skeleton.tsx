import { cn } from "@/lib/utils"

/**
 * Skeleton 组件
 * 用于展示加载状态的骨架屏
 */

import { CSSProperties } from 'react';

interface SkeletonProps {
  className?: string
  style?: CSSProperties
}

function Skeleton({ className, style }: SkeletonProps) {
  return (
    <div
      className={cn(
        "animate-pulse rounded-md bg-gray-200",
        className
      )}
      style={style}
    />
  )
}

/**
 * 商家卡片骨架屏
 */
function StoreCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("bg-white rounded-2xl p-4 space-y-3", className)}>
      {/* 图片区域 */}
      <Skeleton className="w-full h-40 rounded-xl" />
      
      {/* 标题 */}
      <Skeleton className="w-3/4 h-5" />
      
      {/* 描述 */}
      <Skeleton className="w-1/2 h-4" />
      
      {/* 标签 */}
      <div className="flex gap-2">
        <Skeleton className="w-16 h-6 rounded-full" />
        <Skeleton className="w-16 h-6 rounded-full" />
      </div>
      
      {/* 评分和价格 */}
      <div className="flex items-center justify-between pt-2">
        <Skeleton className="w-20 h-4" />
        <Skeleton className="w-16 h-4" />
      </div>
    </div>
  )
}

/**
 * 菜品卡片骨架屏
 */
function DishCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("bg-white rounded-2xl overflow-hidden", className)}>
      {/* 图片区域 */}
      <Skeleton className="w-full h-32" />
      
      {/* 内容区域 */}
      <div className="p-3 space-y-2">
        <Skeleton className="w-3/4 h-4" />
        <Skeleton className="w-full h-3" />
        <div className="flex items-center justify-between pt-1">
          <Skeleton className="w-16 h-5" />
          <Skeleton className="w-8 h-8 rounded-full" />
        </div>
      </div>
    </div>
  )
}

/**
 * 订单卡片骨架屏
 */
function OrderCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("bg-white rounded-2xl p-4 space-y-4", className)}>
      {/* 头部 */}
      <div className="flex items-center gap-3">
        <Skeleton className="w-12 h-12 rounded-xl" />
        <div className="flex-1 space-y-2">
          <Skeleton className="w-32 h-4" />
          <Skeleton className="w-24 h-3" />
        </div>
        <Skeleton className="w-16 h-6 rounded-full" />
      </div>
      
      {/* 商品列表 */}
      <div className="flex gap-3 overflow-hidden">
        <Skeleton className="w-16 h-16 rounded-lg flex-shrink-0" />
        <Skeleton className="w-16 h-16 rounded-lg flex-shrink-0" />
        <Skeleton className="w-16 h-16 rounded-lg flex-shrink-0" />
      </div>
      
      {/* 底部 */}
      <div className="flex items-center justify-between pt-2">
        <Skeleton className="w-20 h-4" />
        <Skeleton className="w-24 h-8 rounded-xl" />
      </div>
    </div>
  )
}

/**
 * 列表骨架屏
 */
function ListSkeleton({ 
  count = 5, 
  className 
}: { 
  count?: number
  className?: string 
}) {
  return (
    <div className={cn("space-y-3", className)}>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className="w-full h-16 rounded-xl" />
      ))}
    </div>
  )
}

/**
 * 文本骨架屏
 */
function TextSkeleton({ 
  lines = 3, 
  className 
}: { 
  lines?: number
  className?: string 
}) {
  return (
    <div className={cn("space-y-2", className)}>
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton 
          key={i} 
          className="h-4"
          style={{ width: i === lines - 1 ? '60%' : '100%' }}
        />
      ))}
    </div>
  )
}

/**
 * 统计卡片骨架屏
 */
function StatCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("bg-white rounded-2xl p-6 space-y-3", className)}>
      <div className="flex items-center justify-between">
        <Skeleton className="w-10 h-10 rounded-xl" />
        <Skeleton className="w-16 h-6 rounded-full" />
      </div>
      <Skeleton className="w-24 h-8" />
      <Skeleton className="w-32 h-4" />
    </div>
  )
}

/**
 * 页面骨架屏
 */
function PageSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("space-y-6 p-4", className)}>
      {/* 标题区域 */}
      <Skeleton className="w-48 h-8" />
      
      {/* 统计卡片 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCardSkeleton />
        <StatCardSkeleton />
        <StatCardSkeleton />
        <StatCardSkeleton />
      </div>
      
      {/* 内容区域 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <StoreCardSkeleton />
        <StoreCardSkeleton />
        <StoreCardSkeleton />
        <StoreCardSkeleton />
        <StoreCardSkeleton />
        <StoreCardSkeleton />
      </div>
    </div>
  )
}

export {
  Skeleton,
  StoreCardSkeleton,
  DishCardSkeleton,
  OrderCardSkeleton,
  ListSkeleton,
  TextSkeleton,
  StatCardSkeleton,
  PageSkeleton,
}
