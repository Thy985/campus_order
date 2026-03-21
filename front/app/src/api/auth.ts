import { get, post } from '@/lib/request';
import type { User } from '@/types';

interface LoginParams {
  phone: string;
  password: string;
}

interface LoginResponse {
  token: string;
  user: User;
  expireTime?: number;
}

interface RegisterParams {
  email: string;
  password: string;
  verifyCode: string;
  nickname?: string;
}

/**
 * 用户登录
 */
export async function login(params: LoginParams): Promise<LoginResponse> {
  return post<LoginResponse>('/api/auth/login', params);
}

/**
 * 用户注册
 */
export async function register(params: RegisterParams): Promise<LoginResponse> {
  return post<LoginResponse>('/api/auth/register', params);
}

/**
 * 发送验证码
 * 修改为与后端对齐：POST /api/auth/verify-code
 */
export async function sendVerifyCode(email: string): Promise<void> {
  return post<void>('/api/auth/verify-code', { email });
}

/**
 * 用户退出登录
 * 注意：后端需要实现此接口
 */
export async function logout(): Promise<void> {
  return post<void>('/api/auth/logout');
}

/**
 * 获取当前登录用户信息
 * 注意：后端需要实现此接口
 */
export async function getCurrentUser(): Promise<User> {
  return get<User>('/api/auth/me');
}

/**
 * 忘记密码
 */
interface ForgotPasswordParams {
  email: string;
  verifyCode: string;
  newPassword: string;
}

export async function forgotPassword(params: ForgotPasswordParams): Promise<void> {
  return post<void>('/api/auth/forgot-password', params);
}
