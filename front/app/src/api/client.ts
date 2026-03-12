import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { toast } from '@/lib/toast';
import type { ApiResponse } from '@/types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const API_TIMEOUT = 10000;

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  <T>(response: AxiosResponse<ApiResponse<T>>) => {
    const { data } = response;

    if (data.code !== 200) {
      const error = new Error(data.message || '请求失败') as Error & { code?: number; data?: T };
      error.code = data.code;
      error.data = data.data;

      toast.error(data.message || '请求失败');

      return Promise.reject(error);
    }

    return response;
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          toast.error('登录已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '#/login';
          break;
        case 403:
          toast.error('没有权限访问该资源');
          break;
        case 404:
          toast.error('请求的资源不存在');
          break;
        case 500:
          toast.error('服务器内部错误，请稍后重试');
          break;
        default:
          toast.error(data?.message || '请求失败');
      }
    } else if (error.request) {
      toast.error('网络请求失败，请检查网络连接');
    } else {
      toast.error('请求配置错误');
    }

    return Promise.reject(error);
  }
);

export async function get<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.get(url, config);
}

export async function post<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.post(url, data, config);
}

export async function put<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.put(url, data, config);
}

export async function del<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.delete(url, config);
}

export async function patch<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.patch(url, data, config);
}

export default apiClient;
