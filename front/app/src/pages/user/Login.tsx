import { useState } from 'react';
import { Phone, Lock, Eye, EyeOff, Store, ArrowLeft } from 'lucide-react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useUserStore } from '@/stores/userStore';
import { login as loginApi } from '@/api/auth';
import { toast } from '@/lib/toast';
import { UserType } from '@/types';

export function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useUserStore();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // 获取登录前要跳转的页面
  const from = (location.state as { from?: string })?.from || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 表单验证
    if (!phone.trim()) {
      toast.error('请输入手机号');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      toast.error('手机号格式不正确');
      return;
    }
    if (!password.trim()) {
      toast.error('请输入密码');
      return;
    }

    setIsLoading(true);

    try {
      // 调用后端登录 API
      const response = await loginApi({ phone, password });

      // 保存登录状态
      login(response.user as any, response.token);

      toast.success('登录成功');

      // 根据用户类型跳转到不同页面
      const userType = response.user.userType;
      if (userType === UserType.MERCHANT) {
        navigate('/merchant');
      } else if (userType === UserType.ADMIN) {
        navigate('/admin');
      } else {
        // NORMAL (0) 或其他类型跳转到之前页面或首页
        navigate(from);
      }
    } catch (error) {
      // 错误已在 api client 中统一处理
      console.error('登录失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-orange-50 to-white flex flex-col">
      {/* Header */}
      <header className="p-4">
        <Link to="/" className="inline-flex items-center gap-2 text-gray-700 hover:text-gray-900">
          <ArrowLeft className="w-5 h-5" />
          <span>返回</span>
        </Link>
      </header>

      {/* Content */}
      <div className="flex-1 flex flex-col items-center justify-center px-6 py-12">
        {/* Logo */}
        <div className="w-20 h-20 rounded-2xl gradient-primary flex items-center justify-center shadow-primary mb-8">
          <Store className="w-10 h-10 text-white" />
        </div>

        {/* Title */}
        <h1 className="text-2xl font-bold text-gray-900 mb-2">欢迎回来</h1>
        <p className="text-gray-500 mb-8">登录您的校园点餐账号</p>

        {/* Form */}
        <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-5">
          {/* Phone Input */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">手机号</label>
            <div className="relative">
              <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                type="tel"
                placeholder="请输入手机号"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="h-14 pl-12 rounded-xl border-gray-200 focus:border-orange-500 focus:ring-orange-500"
                maxLength={11}
              />
            </div>
          </div>

          {/* Password Input */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">密码</label>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                type={showPassword ? 'text' : 'password'}
                placeholder="请输入密码"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="h-14 pl-12 pr-12 rounded-xl border-gray-200 focus:border-orange-500 focus:ring-orange-500"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
          </div>

          {/* Forgot Password */}
          <div className="flex justify-end">
            <Link to="/forgot-password" className="text-sm text-orange-500 hover:text-orange-600">
              忘记密码？
            </Link>
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            disabled={isLoading}
            className="w-full h-14 rounded-xl gradient-primary text-white font-semibold shadow-primary hover:shadow-primary-lg transition-shadow disabled:opacity-70"
          >
            {isLoading ? '登录中...' : '登录'}
          </Button>
        </form>

        {/* Register Link */}
        <p className="mt-8 text-gray-500">
          还没有账号？
          <Link to="/register" className="text-orange-500 hover:text-orange-600 font-medium">
            立即注册
          </Link>
        </p>
      </div>
    </div>
  );
}
