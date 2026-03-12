import { useState } from 'react';
import { Phone, Lock, Eye, EyeOff, Store, User, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from '@/lib/toast';
import { sendVerifyCode, register } from '@/api/auth';

// 常量定义
const VERIFICATION_CODE_LENGTH = 6;
const PHONE_REGEX = /^1[3-9]\d{9}$/;
const COUNTDOWN_SECONDS = 60;

export function Register() {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    phone: '',
    password: '',
    confirmPassword: '',
    verifyCode: '',
    nickname: '',
    username: ''
  });
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // 验证单个字段
  const validateField = (name: string, value: string): string => {
    switch (name) {
      case 'phone':
        if (!value) return '请输入手机号';
        if (!PHONE_REGEX.test(value)) return '手机号格式不正确';
        return '';
      case 'password':
        if (!value) return '请输入密码';
        if (value.length < 6) return '密码至少6位';
        return '';
      case 'confirmPassword':
        if (!value) return '请确认密码';
        if (value !== formData.password) return '两次密码不一致';
        return '';
      case 'verifyCode':
        if (!value) return '请输入验证码';
        if (value.length !== VERIFICATION_CODE_LENGTH) return `验证码为${VERIFICATION_CODE_LENGTH}位`;
        return '';
      case 'nickname':
        if (!value) return '请输入昵称';
        if (value.length < 2) return '昵称至少2个字符';
        return '';
      default:
        return '';
    }
  };

  // 处理输入变化
  const handleChange = (name: string, value: string) => {
    setFormData(prev => ({ ...prev, [name]: value }));
    // 清除对应错误
    if (errors[name]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  // 发送验证码
  const handleSendCode = async () => {
    const phoneError = validateField('phone', formData.phone);
    if (phoneError) {
      setErrors(prev => ({ ...prev, phone: phoneError }));
      return;
    }

    setIsSendingCode(true);
    
    try {
      // 调用发送验证码API
      await sendVerifyCode(formData.phone);

      setCountdown(COUNTDOWN_SECONDS);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      
      toast.success('验证码已发送');
    } catch {
      toast.error('发送失败，请重试');
    } finally {
      setIsSendingCode(false);
    }
  };

  // 表单提交
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // 验证所有字段
    const newErrors: Record<string, string> = {};
    Object.keys(formData).forEach((key) => {
      const error = validateField(key, formData[key as keyof typeof formData]);
      if (error) newErrors[key] = error;
    });
    
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setIsLoading(true);

    try {
      // 调用注册API
      await register({
        phone: formData.phone,
        password: formData.password,
        verifyCode: formData.verifyCode,
        nickname: formData.nickname,
      });

      toast.success('注册成功');
      navigate('/login');
    } catch (error) {
      toast.error('注册失败，请重试');
      console.error('注册失败:', error);
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
      <div className="flex-1 flex flex-col items-center justify-center px-6 py-8">
        {/* Logo */}
        <div className="w-16 h-16 rounded-2xl gradient-primary flex items-center justify-center shadow-primary mb-6">
          <Store className="w-8 h-8 text-white" />
        </div>

        {/* Title */}
        <h1 className="text-2xl font-bold text-gray-900 mb-2">创建账号</h1>
        <p className="text-gray-500 mb-6">加入校园点餐，开启美食之旅</p>

        {/* Form */}
        <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4">
          {/* Phone Input */}
          <div className="space-y-1.5">
            <Label htmlFor="phone">手机号</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="phone"
                type="tel"
                placeholder="请输入手机号"
                value={formData.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                className="pl-10"
                maxLength={11}
                aria-invalid={!!errors.phone}
                aria-describedby={errors.phone ? 'phone-error' : undefined}
              />
            </div>
            {errors.phone && (
              <p id="phone-error" className="text-sm text-red-500">{errors.phone}</p>
            )}
          </div>

          {/* Verification Code */}
          <div className="space-y-1.5">
            <Label htmlFor="verifyCode">验证码</Label>
            <div className="flex gap-2">
              <Input
                id="verifyCode"
                placeholder={`请输入${VERIFICATION_CODE_LENGTH}位验证码`}
                value={formData.verifyCode}
                onChange={(e) => handleChange('verifyCode', e.target.value)}
                maxLength={VERIFICATION_CODE_LENGTH}
                className="flex-1"
                aria-invalid={!!errors.verifyCode}
              />
              <Button
                type="button"
                variant="outline"
                onClick={handleSendCode}
                disabled={isSendingCode || countdown > 0}
                className="w-28 shrink-0"
              >
                {countdown > 0 ? `${countdown}s` : isSendingCode ? '发送中...' : '获取验证码'}
              </Button>
            </div>
            {errors.verifyCode && (
              <p className="text-sm text-red-500">{errors.verifyCode}</p>
            )}
          </div>

          {/* Nickname */}
          <div className="space-y-1.5">
            <Label htmlFor="nickname">昵称</Label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="nickname"
                placeholder="请输入昵称"
                value={formData.nickname}
                onChange={(e) => handleChange('nickname', e.target.value)}
                className="pl-10"
                aria-invalid={!!errors.nickname}
              />
            </div>
            {errors.nickname && (
              <p className="text-sm text-red-500">{errors.nickname}</p>
            )}
          </div>

          {/* Password */}
          <div className="space-y-1.5">
            <Label htmlFor="password">密码</Label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="请设置密码（至少6位）"
                value={formData.password}
                onChange={(e) => handleChange('password', e.target.value)}
                className="pl-10 pr-10"
                aria-invalid={!!errors.password}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.password && (
              <p className="text-sm text-red-500">{errors.password}</p>
            )}
          </div>

          {/* Confirm Password */}
          <div className="space-y-1.5">
            <Label htmlFor="confirmPassword">确认密码</Label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="请再次输入密码"
                value={formData.confirmPassword}
                onChange={(e) => handleChange('confirmPassword', e.target.value)}
                className="pl-10 pr-10"
                aria-invalid={!!errors.confirmPassword}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-sm text-red-500">{errors.confirmPassword}</p>
            )}
          </div>

          {/* Submit */}
          <Button
            type="submit"
            disabled={isLoading}
            className="w-full h-12 mt-2"
          >
            {isLoading ? '注册中...' : '注册'}
          </Button>
        </form>

        {/* Login Link */}
        <p className="mt-6 text-gray-500">
          已有账号？
          <Link to="/login" className="text-orange-500 hover:text-orange-600 font-medium">
            立即登录
          </Link>
        </p>
      </div>
    </div>
  );
}
