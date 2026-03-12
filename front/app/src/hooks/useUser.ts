/**
 * 用户数据Hook
 * 封装用户信息和登录状态的管理
 */

import { useState, useEffect, useCallback } from 'react';
import { authApi, userApi } from '@/api';
import { adaptUser } from '@/utils/dataAdapter';
import type { User, LoginResponse } from '@/types';

/**
 * 获取当前用户信息
 */
export function useUser() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const fetchUser = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 检查是否有token
      const token = localStorage.getItem('token');
      if (!token) {
        setIsAuthenticated(false);
        setUser(null);
        return;
      }
      
      // 调用后端接口获取用户信息
      const response = await authApi.getCurrentUser();
      setUser(adaptUser(response));
      setIsAuthenticated(true);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取用户信息失败'));
      setIsAuthenticated(false);
      setUser(null);
      // 清除失效的token
      localStorage.removeItem('token');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error('退出登录失败:', err);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
      setIsAuthenticated(false);
    }
  }, []);

  return {
    user,
    loading,
    error,
    isAuthenticated,
    refresh: fetchUser,
    logout,
  };
}

/**
 * 用户登录
 */
export function useLogin() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const login = async (phone: string, password: string): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      const response: LoginResponse = await authApi.login({ phone, password });
      
      // 保存token
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('登录失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { login, loading, error };
}

/**
 * 用户注册
 */
export function useRegister() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const register = async (data: {
    phone: string;
    password: string;
    verifyCode: string;
    nickname?: string;
  }): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      const response: LoginResponse = await authApi.register(data);
      
      // 保存token
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('注册失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { register, loading, error };
}

