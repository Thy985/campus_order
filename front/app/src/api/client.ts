/**
 * API客户端模块
 *
 * 基于Axios封装的HTTP客户端，提供：
 * - 统一的API基础配置（URL、超时、请求头）
 * - 请求拦截器（自动添加认证Token）
 * - 响应拦截器（统一错误处理、Token过期跳转）
 * - 封装常用HTTP方法（GET/POST/PUT/DELETE/PATCH）
 *
 * @module api/client
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { toast } from '@/lib/toast';
import type { ApiResponse } from '@/types';

/** API基础URL，从环境变量读取，默认本地开发地址 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/** API请求超时时间（毫秒） */
const API_TIMEOUT = 10000;

/**
 * Axios实例配置
 *
 * 配置项：
 * - baseURL: API基础地址
 * - timeout: 请求超时时间
 * - headers: 默认请求头
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 请求拦截器
 *
 * 功能：
 * - 从localStorage读取token
 * - 自动添加到请求头Authorization字段
 */
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

/**
 * 响应拦截器
 *
 * 功能：
 * - 统一处理业务错误（code !== 200）
 * - 统一处理HTTP错误状态码
 * - Token过期自动跳转登录页
 * - 网络错误提示
 */
apiClient.interceptors.response.use(
  <T>(response: AxiosResponse<ApiResponse<T>>) => {
    const { data } = response;

    // 业务逻辑错误处理
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

      // 根据HTTP状态码处理不同类型的错误
      switch (status) {
        case 401:
          // Token过期，清除登录状态并跳转登录页
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
      // 请求发送成功但没有收到响应
      toast.error('网络请求失败，请检查网络连接');
    } else {
      // 请求配置错误
      toast.error('请求配置错误');
    }

    return Promise.reject(error);
  }
);

/**
 * 发送GET请求
 * @template T 响应数据类型
 * @param url 请求地址
 * @param config 请求配置
 * @returns Promise<AxiosResponse<ApiResponse<T>>>
 */
export async function get<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.get(url, config);
}

/**
 * 发送POST请求
 * @template T 响应数据类型
 * @param url 请求地址
 * @param data 请求体数据
 * @param config 请求配置
 * @returns Promise<AxiosResponse<ApiResponse<T>>>
 */
export async function post<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.post(url, data, config);
}

/**
 * 发送PUT请求
 * @template T 响应数据类型
 * @param url 请求地址
 * @param data 请求体数据
 * @param config 请求配置
 * @returns Promise<AxiosResponse<ApiResponse<T>>>
 */
export async function put<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.put(url, data, config);
}

/**
 * 发送DELETE请求
 * @template T 响应数据类型
 * @param url 请求地址
 * @param config 请求配置
 * @returns Promise<AxiosResponse<ApiResponse<T>>>
 */
export async function del<T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.delete(url, config);
}

/**
 * 发送PATCH请求
 * @template T 响应数据类型
 * @param url 请求地址
 * @param data 请求体数据
 * @param config 请求配置
 * @returns Promise<AxiosResponse<ApiResponse<T>>>
 */
export async function patch<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<AxiosResponse<ApiResponse<T>>> {
  return apiClient.patch(url, data, config);
}

/** 默认导出Axios实例 */
export default apiClient;
