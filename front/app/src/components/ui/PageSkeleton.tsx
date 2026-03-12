import { Skeleton } from './skeleton';
import { Card } from './card';

interface PageSkeletonProps {
  type?: 'home' | 'store' | 'orders' | 'profile' | 'dashboard';
}

export function PageSkeleton({ type = 'home' }: PageSkeletonProps) {
  switch (type) {
    case 'home':
      return <HomeSkeleton />;
    case 'store':
      return <StoreSkeleton />;
    case 'orders':
      return <OrdersSkeleton />;
    case 'profile':
      return <ProfileSkeleton />;
    case 'dashboard':
      return <DashboardSkeleton />;
    default:
      return <HomeSkeleton />;
  }
}

function HomeSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Hero Skeleton */}
      <section className="relative pt-24 pb-12 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <Skeleton className="h-10 w-64 mx-auto mb-4" />
          <Skeleton className="h-5 w-48 mx-auto" />
        </div>
        <Skeleton className="h-12 max-w-2xl mx-auto mt-8 rounded-xl" />
      </section>

      {/* Category Nav Skeleton */}
      <div className="px-4 mb-8">
        <div className="flex gap-3 overflow-hidden">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-20 rounded-2xl flex-shrink-0" />
          ))}
        </div>
      </div>

      {/* Store Cards Skeleton */}
      <div className="px-4 space-y-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i} className="p-4">
            <div className="flex gap-4">
              <Skeleton className="w-24 h-24 rounded-xl flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-5 w-32" />
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-40" />
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function StoreSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Store Header Skeleton */}
      <Skeleton className="h-48 w-full" />

      {/* Category Tabs Skeleton */}
      <div className="sticky top-0 bg-white border-b px-4 py-3">
        <div className="flex gap-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-9 w-20 rounded-full" />
          ))}
        </div>
      </div>

      {/* Dish Cards Skeleton */}
      <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
        {Array.from({ length: 6 }).map((_, i) => (
          <Card key={i} className="overflow-hidden">
            <Skeleton className="h-40 w-full" />
            <div className="p-4 space-y-2">
              <Skeleton className="h-5 w-3/4" />
              <Skeleton className="h-4 w-1/2" />
              <div className="flex justify-between items-center pt-2">
                <Skeleton className="h-6 w-16" />
                <Skeleton className="h-8 w-8 rounded-full" />
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function OrdersSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Tabs Skeleton */}
      <div className="sticky top-0 bg-white border-b px-4">
        <div className="flex gap-4 h-12 items-center">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-8 w-16" />
          ))}
        </div>
      </div>

      {/* Order Cards Skeleton */}
      <div className="p-4 space-y-4">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="p-4">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <Skeleton className="w-12 h-12 rounded-xl" />
                <div className="space-y-1">
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-3 w-16" />
                </div>
              </div>
              <Skeleton className="h-6 w-16 rounded-full" />
            </div>
            <div className="space-y-2">
              {Array.from({ length: 2 }).map((_, j) => (
                <div key={j} className="flex gap-2">
                  <Skeleton className="w-12 h-12 rounded-lg" />
                  <div className="flex-1 space-y-1">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-3 w-12" />
                  </div>
                </div>
              ))}
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function ProfileSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header Background */}
      <Skeleton className="h-48 w-full" />

      {/* Profile Card */}
      <div className="max-w-3xl mx-auto px-4 -mt-20 relative z-10">
        <Card className="p-6">
          <div className="flex items-center gap-4">
            <Skeleton className="w-20 h-20 rounded-2xl" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-6 w-32" />
              <Skeleton className="h-4 w-24" />
            </div>
          </div>
        </Card>
      </div>

      {/* Menu Groups */}
      <div className="max-w-3xl mx-auto px-4 mt-6 space-y-4">
        {Array.from({ length: 2 }).map((_, i) => (
          <Card key={i} className="p-4">
            <Skeleton className="h-5 w-20 mb-4" />
            <div className="space-y-3">
              {Array.from({ length: 3 }).map((_, j) => (
                <div key={j} className="flex items-center gap-3">
                  <Skeleton className="w-10 h-10 rounded-xl" />
                  <Skeleton className="h-4 w-24 flex-1" />
                  <Skeleton className="h-4 w-4" />
                </div>
              ))}
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="space-y-2">
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-4 w-48" />
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i} className="p-6">
            <div className="flex items-start justify-between">
              <div className="space-y-2">
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-8 w-16" />
                <Skeleton className="h-3 w-12" />
              </div>
              <Skeleton className="w-12 h-12 rounded-xl" />
            </div>
          </Card>
        ))}
      </div>

      {/* Chart Area */}
      <Card className="p-6">
        <Skeleton className="h-6 w-32 mb-4" />
        <Skeleton className="h-64 w-full rounded-xl" />
      </Card>
    </div>
  );
}
