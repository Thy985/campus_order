import { createBrowserRouter, Navigate, Outlet, Link } from 'react-router-dom';
import { Suspense, lazy } from 'react';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { AuthGuard, MerchantGuard, AdminGuard } from '@/components/auth/RouteGuard';

// Layouts
import { UserHeader } from '@/components/layout/UserHeader';
import { BottomNav } from '@/components/layout/BottomNav';

// User Pages - 立即加载
import { Home } from '@/pages/user/Home';
import { Login } from '@/pages/user/Login';

// User Pages - 懒加载
const Stores = lazy(() => import('@/pages/user/Stores').then(m => ({ default: m.Stores })));
const StoreDetail = lazy(() => import('@/pages/user/StoreDetail').then(m => ({ default: m.StoreDetail })));
const Cart = lazy(() => import('@/pages/user/Cart').then(m => ({ default: m.Cart })));
const Checkout = lazy(() => import('@/pages/user/Checkout').then(m => ({ default: m.Checkout })));
const Orders = lazy(() => import('@/pages/user/Orders').then(m => ({ default: m.Orders })));
const OrderDetail = lazy(() => import('@/pages/user/OrderDetail').then(m => ({ default: m.OrderDetail })));
const Profile = lazy(() => import('@/pages/user/Profile').then(m => ({ default: m.Profile })));
const Register = lazy(() => import('@/pages/user/Register').then(m => ({ default: m.Register })));
const ForgotPassword = lazy(() => import('@/pages/user/ForgotPassword').then(m => ({ default: m.ForgotPassword })));
const Payment = lazy(() => import('@/pages/user/Payment').then(m => ({ default: m.PaymentPage })));
const AddressList = lazy(() => import('@/pages/user/AddressList').then(m => ({ default: m.AddressList })));
const AddressEdit = lazy(() => import('@/pages/user/AddressEdit').then(m => ({ default: m.AddressEdit })));
const Notifications = lazy(() => import('@/pages/user/Notifications').then(m => ({ default: m.Notifications })));
const Coupons = lazy(() => import('@/pages/user/Coupons').then(m => ({ default: m.Coupons })));
const ReviewOrder = lazy(() => import('@/pages/user/ReviewOrder').then(m => ({ default: m.ReviewOrder })));

// Merchant Pages - 懒加载
const MerchantDashboard = lazy(() => import('@/pages/merchant/Dashboard').then(m => ({ default: m.MerchantDashboard })));
const MerchantOrders = lazy(() => import('@/pages/merchant/Orders').then(m => ({ default: m.MerchantOrders })));
const MerchantMenu = lazy(() => import('@/pages/merchant/Menu').then(m => ({ default: m.MerchantMenu })));
const MerchantStatistics = lazy(() => import('@/pages/merchant/Statistics').then(m => ({ default: m.MerchantStatistics })));
const MerchantSettings = lazy(() => import('@/pages/merchant/Settings').then(m => ({ default: m.MerchantSettings })));

// Admin Pages - 懒加载
const AdminDashboard = lazy(() => import('@/pages/admin/Dashboard').then(m => ({ default: m.AdminDashboard })));
const AdminOrders = lazy(() => import('@/pages/admin/Orders').then(m => ({ default: m.AdminOrders })));
const AdminUserManagement = lazy(() => import('@/pages/admin/UserManagement').then(m => ({ default: m.UserManagement })));
const AdminMerchantManagement = lazy(() => import('@/pages/admin/MerchantManagement').then(m => ({ default: m.MerchantManagement })));
const AdminSettings = lazy(() => import('@/pages/admin/Settings').then(m => ({ default: m.AdminSettings })));

// 加载状态组件
function PageLoader({ type }: { type?: 'home' | 'store' | 'orders' | 'profile' | 'dashboard' }) {
  return <PageSkeleton type={type} />;
}

// 用户布局
function UserLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <UserHeader />
      <main className="pt-16">
        <ErrorBoundary>
          <Suspense fallback={<PageLoader type="home" />}>
            <Outlet />
          </Suspense>
        </ErrorBoundary>
      </main>
      <BottomNav />
    </div>
  );
}

// 无布局页面（登录、注册等）
function AuthLayout() {
  return (
    <ErrorBoundary>
      <Suspense fallback={<PageLoader />}>
        <Outlet />
      </Suspense>
    </ErrorBoundary>
  );
}

// 商家布局
function MerchantLayout() {
  return (
    <MerchantGuard>
      <div className="min-h-screen bg-gray-50 flex">
        <aside className="w-64 bg-white border-r border-gray-200 fixed h-full">
          <div className="p-4 border-b border-gray-200">
            <h1 className="text-xl font-bold text-orange-500">商家中心</h1>
          </div>
          <nav className="p-4 space-y-2">
            <Link to="/merchant" className="flex items-center gap-3 px-4 py-2 rounded-lg text-gray-700 hover:bg-orange-50 hover:text-orange-500">
              <span>📊</span> 数据概览
            </Link>
            <Link to="/merchant/orders" className="flex items-center gap-3 px-4 py-2 rounded-lg text-gray-700 hover:bg-orange-50 hover:text-orange-500">
              <span>📋</span> 订单管理
            </Link>
            <Link to="/merchant/menu" className="flex items-center gap-3 px-4 py-2 rounded-lg text-gray-700 hover:bg-orange-50 hover:text-orange-500">
              <span>🍽️</span> 菜品管理
            </Link>
            <Link to="/merchant/statistics" className="flex items-center gap-3 px-4 py-2 rounded-lg text-gray-700 hover:bg-orange-50 hover:text-orange-500">
              <span>📈</span> 数据统计
            </Link>
            <Link to="/merchant/settings" className="flex items-center gap-3 px-4 py-2 rounded-lg text-gray-700 hover:bg-orange-50 hover:text-orange-500">
              <span>⚙️</span> 店铺设置
            </Link>
          </nav>
          <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
            <Link to="/" className="flex items-center gap-2 text-sm text-gray-500 hover:text-orange-500">
              ← 返回用户端
            </Link>
          </div>
        </aside>
        <main className="flex-1 ml-64">
          <ErrorBoundary>
            <Suspense fallback={<PageLoader type="dashboard" />}>
              <Outlet />
            </Suspense>
          </ErrorBoundary>
        </main>
      </div>
    </MerchantGuard>
  );
}

// 管理员布局
function AdminLayout() {
  return (
    <AdminGuard>
      <div className="min-h-screen bg-gray-50 flex">
        <aside className="w-64 bg-slate-800 fixed h-full">
          <div className="p-4 border-b border-slate-700">
            <h1 className="text-xl font-bold text-white">管理后台</h1>
          </div>
          <nav className="p-4 space-y-2">
            <Link to="/admin" className="flex items-center gap-3 px-4 py-2 rounded-lg text-slate-300 hover:bg-slate-700 hover:text-white">
              <span>📊</span> 系统概览
            </Link>
            <Link to="/admin/users" className="flex items-center gap-3 px-4 py-2 rounded-lg text-slate-300 hover:bg-slate-700 hover:text-white">
              <span>👥</span> 用户管理
            </Link>
            <Link to="/admin/merchants" className="flex items-center gap-3 px-4 py-2 rounded-lg text-slate-300 hover:bg-slate-700 hover:text-white">
              <span>🏪</span> 商家管理
            </Link>
            <Link to="/admin/orders" className="flex items-center gap-3 px-4 py-2 rounded-lg text-slate-300 hover:bg-slate-700 hover:text-white">
              <span>📋</span> 订单管理
            </Link>
            <Link to="/admin/settings" className="flex items-center gap-3 px-4 py-2 rounded-lg text-slate-300 hover:bg-slate-700 hover:text-white">
              <span>⚙️</span> 系统设置
            </Link>
          </nav>
          <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-slate-700">
            <Link to="/" className="flex items-center gap-2 text-sm text-slate-400 hover:text-white">
              ← 返回用户端
            </Link>
          </div>
        </aside>
        <main className="flex-1 ml-64">
          <ErrorBoundary>
            <Suspense fallback={<PageLoader type="dashboard" />}>
              <Outlet />
            </Suspense>
          </ErrorBoundary>
        </main>
      </div>
    </AdminGuard>
  );
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <UserLayout />,
    children: [
      { index: true, element: <Home /> },
      { path: 'stores', element: <Stores /> },
      { path: 'store/:id', element: <StoreDetail /> },
      { path: 'cart', element: <Cart /> },
      { 
        path: 'checkout', 
        element: (
          <AuthGuard>
            <Checkout />
          </AuthGuard>
        ) 
      },
      { 
        path: 'orders', 
        element: (
          <AuthGuard>
            <Orders />
          </AuthGuard>
        ) 
      },
      { 
        path: 'order/:id', 
        element: (
          <AuthGuard>
            <OrderDetail />
          </AuthGuard>
        ) 
      },
      { 
        path: 'profile', 
        element: (
          <AuthGuard>
            <Profile />
          </AuthGuard>
        ) 
      },
      {
        path: 'payment/:orderId',
        element: (
          <AuthGuard>
            <Payment />
          </AuthGuard>
        )
      },
      { 
        path: 'addresses', 
        element: (
          <AuthGuard>
            <AddressList />
          </AuthGuard>
        ) 
      },
      { 
        path: 'address/add', 
        element: (
          <AuthGuard>
            <AddressEdit />
          </AuthGuard>
        ) 
      },
      { 
        path: 'address/edit/:id', 
        element: (
          <AuthGuard>
            <AddressEdit />
          </AuthGuard>
        ) 
      },
      { 
        path: 'notifications', 
        element: (
          <AuthGuard>
            <Notifications />
          </AuthGuard>
        ) 
      },
      { 
        path: 'review/:orderId', 
        element: (
          <AuthGuard>
            <ReviewOrder />
          </AuthGuard>
        ) 
      },
      { 
        path: 'coupons', 
        element: (
          <AuthGuard>
            <Coupons />
          </AuthGuard>
        ) 
      },
    ],
  },
  {
    path: '/',
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <Login /> },
      { path: 'register', element: <Register /> },
      { path: 'forgot-password', element: <ForgotPassword /> },
    ],
  },
  {
    path: '/merchant',
    element: <MerchantLayout />,
    children: [
      { index: true, element: <MerchantDashboard /> },
      { path: 'orders', element: <MerchantOrders /> },
      { path: 'menu', element: <MerchantMenu /> },
      { path: 'statistics', element: <MerchantStatistics /> },
      { path: 'settings', element: <MerchantSettings /> },
    ],
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <AdminDashboard /> },
      { path: 'users', element: <AdminUserManagement /> },
      { path: 'merchants', element: <AdminMerchantManagement /> },
      { path: 'orders', element: <AdminOrders /> },
      { path: 'settings', element: <AdminSettings /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]);
