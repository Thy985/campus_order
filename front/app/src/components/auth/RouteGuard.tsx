import { Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useUserStore } from '@/stores';

// 用户角色类型
export type UserRole = 'user' | 'merchant' | 'admin';

// 路由守卫配置
interface RouteGuardProps {
  children: React.ReactNode;
  allowedRoles?: UserRole[];
  requireAuth?: boolean;
  fallback?: React.ReactNode;
}

// 检查用户是否有权限访问
export function RouteGuard({
  children,
  allowedRoles = ['user'],
  requireAuth = true,
  fallback,
}: RouteGuardProps) {
  const location = useLocation();
  const { user, isAuthenticated, isLoading } = useUserStore();
  const [isChecking, setIsChecking] = useState(true);
  const [hasPermission, setHasPermission] = useState(false);

  useEffect(() => {
    // 模拟权限检查
    const checkPermission = () => {
      // 如果不需要认证，直接通过
      if (!requireAuth) {
        setHasPermission(true);
        setIsChecking(false);
        return;
      }

      // 检查是否登录
      if (!isAuthenticated) {
        setHasPermission(false);
        setIsChecking(false);
        return;
      }

      // 检查角色权限
      const userRole = user?.role || 'user';
      const hasRole = allowedRoles.includes(userRole as UserRole);
      
      setHasPermission(hasRole);
      setIsChecking(false);
    };

    checkPermission();
  }, [user, isAuthenticated, requireAuth, allowedRoles]);

  // 加载中状态
  if (isLoading || isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-500">加载中...</p>
        </div>
      </div>
    );
  }

  // 未登录且需要认证，跳转到登录页
  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 已登录但没有权限
  if (requireAuth && isAuthenticated && !hasPermission) {
    // 如果有自定义 fallback，显示 fallback
    if (fallback) {
      return <>{fallback}</>;
    }
    
    // 根据用户角色跳转到对应的首页
    const userRole = user?.role;
    if (userRole === 'admin') {
      return <Navigate to="/admin" replace />;
    } else if (userRole === 'merchant') {
      return <Navigate to="/merchant" replace />;
    } else {
      return <Navigate to="/" replace />;
    }
  }

  // 有权限，显示内容
  return <>{children}</>;
}

// 用户端路由守卫
export function UserGuard({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard allowedRoles={['user', 'merchant', 'admin']} requireAuth={false}>
      {children}
    </RouteGuard>
  );
}

// 需要登录的路由守卫
export function AuthGuard({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard allowedRoles={['user', 'merchant', 'admin']} requireAuth={true}>
      {children}
    </RouteGuard>
  );
}

// 商家端路由守卫
export function MerchantGuard({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard 
      allowedRoles={['merchant']} 
      requireAuth={true}
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center p-8 bg-white rounded-2xl shadow-lg max-w-md">
            <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl">🏪</span>
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">商家权限 required</h2>
            <p className="text-gray-500 mb-6">您需要商家账号才能访问此页面</p>
            <a 
              href="/#/" 
              className="inline-flex items-center justify-center px-6 py-3 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition-colors"
            >
              返回首页
            </a>
          </div>
        </div>
      }
    >
      {children}
    </RouteGuard>
  );
}

// 管理员路由守卫
export function AdminGuard({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard 
      allowedRoles={['admin']} 
      requireAuth={true}
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center p-8 bg-white rounded-2xl shadow-lg max-w-md">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl">🔒</span>
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">管理员权限 required</h2>
            <p className="text-gray-500 mb-6">您需要管理员账号才能访问此页面</p>
            <a 
              href="/#/" 
              className="inline-flex items-center justify-center px-6 py-3 bg-slate-800 text-white rounded-xl hover:bg-slate-700 transition-colors"
            >
              返回首页
            </a>
          </div>
        </div>
      }
    >
      {children}
    </RouteGuard>
  );
}
