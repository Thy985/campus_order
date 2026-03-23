/**
 * 用户状态管理模块
 *
 * 使用React Context API实现全局用户状态管理，提供：
 * - 用户信息存储（id、手机号、昵称、头像、角色）
 * - 登录状态管理
 * - Token持久化（localStorage）
 * - 角色切换功能
 *
 * @module stores/userStore
 */

import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react';

/**
 * 用户类型定义
 * @interface User
 */
export interface User {
  /** 用户ID */
  id: number;
  /** 手机号 */
  phone: string;
  /** 昵称 */
  nickname?: string;
  /** 头像URL */
  avatar?: string;
  /** 用户角色 */
  role: 'user' | 'merchant' | 'admin';
  /** 创建时间 */
  createdAt?: string;
  /** 更新时间 */
  updatedAt?: string;
}

/**
 * UserContext类型定义
 * @interface UserContextType
 */
interface UserContextType {
  /** 当前用户信息 */
  user: User | null;
  /** 认证Token */
  token: string | null;
  /** 是否已登录 */
  isAuthenticated: boolean;
  /** 当前角色 */
  currentRole: 'user' | 'merchant' | 'admin' | null;
  /** 是否正在加载 */
  isLoading: boolean;
  /** 设置用户信息 */
  setUser: (user: User | null) => void;
  /** 登录方法 */
  login: (user: User, token: string) => void;
  /** 登出方法 */
  logout: () => void;
  /** 切换角色 */
  setRole: (role: 'user' | 'merchant' | 'admin') => void;
}

/** 用户上下文 */
const UserContext = createContext<UserContextType | undefined>(undefined);

/**
 * 安全地解析JSON字符串
 *
 * @template T 解析后的类型
 * @param value JSON字符串
 * @param defaultValue 解析失败时的默认值
 * @returns 解析结果
 */
function safeJSONParse<T>(value: string | null, defaultValue: T): T {
  if (!value) return defaultValue;
  try {
    return JSON.parse(value) as T;
  } catch {
    return defaultValue;
  }
}

/**
 * 用户状态提供者组件
 *
 * 功能：
 * - 从localStorage恢复登录状态
 * - 提供登录/登出方法
 * - 提供角色切换功能
 *
 * @param props.children 子组件
 */
export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentRole, setCurrentRole] = useState<'user' | 'merchant' | 'admin' | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * 从localStorage初始化认证状态
   * 页面刷新后自动恢复登录状态
   */
  useEffect(() => {
    const initAuth = () => {
      try {
        const savedUser = localStorage.getItem('user');
        const savedToken = localStorage.getItem('token');

        if (savedUser && savedToken) {
          const parsedUser = safeJSONParse<User>(savedUser, null);
          if (parsedUser) {
            setUserState(parsedUser);
            setToken(savedToken);
            setIsAuthenticated(true);
            setCurrentRole(parsedUser.role || null);
          } else {
            // 数据损坏，清除
            localStorage.removeItem('user');
            localStorage.removeItem('token');
          }
        }
      } catch (e) {
        console.error('Failed to parse auth data:', e);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * 设置用户信息
   * @param newUser 用户信息，null表示清除
   */
  const setUser = useCallback((newUser: User | null) => {
    setUserState(newUser);
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser));
    } else {
      localStorage.removeItem('user');
    }
  }, []);

  /**
   * 用户登录
   *
   * 将API返回的用户数据转换为前端User类型，
   * 并持久化到localStorage
   *
   * @param apiUser API返回的用户数据
   * @param newToken 认证Token
   */
  const login = useCallback((apiUser: any, newToken: string) => {
    // 根据 userType 映射 role
    // 0: 普通用户, 1: 商家, 2: 管理员
    const userType = apiUser.userType;
    let role: 'user' | 'merchant' | 'admin' = 'user';
    if (userType === 1) {
      role = 'merchant';
    } else if (userType === 2) {
      role = 'admin';
    }

    const newUser: User = {
      id: Number(apiUser.id),
      phone: apiUser.phone,
      nickname: apiUser.nickname,
      avatar: apiUser.avatar,
      role: role,
      createdAt: apiUser.createdAt,
      updatedAt: apiUser.updatedAt,
    };
    setUserState(newUser);
    setToken(newToken);
    setIsAuthenticated(true);
    setCurrentRole(newUser.role);
    localStorage.setItem('user', JSON.stringify(newUser));
    localStorage.setItem('token', newToken);
  }, []);

  /**
   * 用户登出
   *
   * 清除所有用户状态和localStorage数据
   */
  const logout = useCallback(() => {
    setUserState(null);
    setToken(null);
    setIsAuthenticated(false);
    setCurrentRole(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }, []);

  /**
   * 切换当前角色
   *
   * 用于同一账号多角色的场景（如既是商户又是管理员）
   *
   * @param role 目标角色
   */
  const setRole = useCallback((role: 'user' | 'merchant' | 'admin') => {
    setCurrentRole(role);
    if (user) {
      const updatedUser = { ...user, role };
      setUserState(updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));
    }
  }, [user]);

  const value: UserContextType = {
    user,
    token,
    isAuthenticated,
    currentRole,
    isLoading,
    setUser,
    login,
    logout,
    setRole,
  };

  return (
    <UserContext.Provider value={value}>
      {children}
    </UserContext.Provider>
  );
}

/**
 * 使用用户状态的Hook
 *
 * 必须在UserProvider包裹的组件中使用
 *
 * @returns UserContextType 用户状态和方法
 * @throws Error 如果不在UserProvider中使用
 */
export function useUserStore(): UserContextType {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error('useUserStore must be used within a UserProvider');
  }
  return context;
}

/** 兼容旧版默认导出 */
export default useUserStore;
