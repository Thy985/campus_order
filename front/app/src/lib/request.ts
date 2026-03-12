import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';
import { toast } from './toast';
import type { ApiResponse } from '@/types';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

request.interceptors.request.use(
  (config) => {
    console.log('Request:', config.method?.toUpperCase(), config.url, config.params);
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    // 统一处理响应，检查code
    const { data } = response;
    console.log('Response:', response.config.url, data);
    
    // 检查data是否存在
    if (!data) {
      console.error('API Error: Response data is empty');
      throw new Error('响应数据为空');
    }
    
    // 如果后端返回的code不是200，认为是错误
    if (data.code !== 200) {
      console.error('API Error - Response data:', data);
      const error = new Error(data.message || '请求失败') as Error & { code?: number; data?: any };
      error.code = data.code;
      error.data = data.data;
      throw error;
    }
    
    // 返回完整响应，不自动提取data
    return response;
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    console.error('Request Error:', error);
    
    if (error.response) {
      const { status, data } = error.response;
      const message = data?.message || '请求失败';

      switch (status) {
        case 401:
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          toast.error('登录已过期，请重新登录');
          window.location.href = '#/login';
          break;
        case 403:
          toast.error('没有权限执行此操作');
          break;
        case 404:
          toast.error('请求的资源不存在');
          break;
        case 422:
          toast.error(message || '请求参数错误');
          break;
        case 500:
          toast.error('服务器错误，请稍后重试');
          break;
        default:
          toast.error(message);
      }
    } else if (error.request) {
      console.error('Network Error - No response received:', error.request);
      toast.error('网络错误，请检查网络连接或后端服务是否启动');
    } else {
      console.error('Request Config Error:', error.message);
      toast.error('请求配置错误: ' + error.message);
    }

    return Promise.reject(error);
  }
);

export default request;

// 封装常用请求方法，自动提取data.data
export const get = <T>(url: string, config?: AxiosRequestConfig) => 
  request.get<ApiResponse<T>>(url, config).then(res => res.data.data);

export const post = <T>(url: string, data?: any, config?: AxiosRequestConfig) =>
  request.post<ApiResponse<T>>(url, data, config).then(res => res.data.data);

export const put = <T>(url: string, data?: any, config?: AxiosRequestConfig) =>
  request.put<ApiResponse<T>>(url, data, config).then(res => res.data.data);

export const del = <T>(url: string, config?: AxiosRequestConfig) =>
  request.delete<ApiResponse<T>>(url, config).then(res => res.data.data);

