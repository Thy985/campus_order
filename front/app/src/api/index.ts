// API 统一导出
export * as authApi from './auth';
export * as orderApi from './order';
export * as userApi from './user';
export * as merchantApi from './merchant';
export * as productApi from './product';
export * as paymentApi from './payment';
export * as fileApi from './file';
export * as addressApi from './address';
export * as wsApi from './websocket';
export * as adminApi from './admin';    // 管理端接口

// 默认导出 request 实例
export { default as request } from '@/lib/request';

