import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react';

// 用户类型定义
export interface User {
  id: number;
  phone: string;
  nickname?: string;
  avatar?: string;
  role: 'user' | 'merchant' | 'admin';
  createdAt?: string;
  updatedAt?: string;
}

interface UserContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  currentRole: 'user' | 'merchant' | 'admin' | null;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  login: (user: User, token: string) => void;
  logout: () => void;
  setRole: (role: 'user' | 'merchant' | 'admin') => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

// 安全地解析 JSON
function safeJSONParse<T>(value: string | null, defaultValue: T): T {
  if (!value) return defaultValue;
  try {
    return JSON.parse(value) as T;
  } catch {
    return defaultValue;
  }
}

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentRole, setCurrentRole] = useState<'user' | 'merchant' | 'admin' | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // 从 localStorage 初始化状态
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

  const setUser = useCallback((newUser: User | null) => {
    setUserState(newUser);
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser));
    } else {
      localStorage.removeItem('user');
    }
  }, []);

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

  const logout = useCallback(() => {
    setUserState(null);
    setToken(null);
    setIsAuthenticated(false);
    setCurrentRole(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }, []);

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

// Hook
export function useUserStore(): UserContextType {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error('useUserStore must be used within a UserProvider');
  }
  return context;
}

// 兼容旧版默认导出
export default useUserStore;
