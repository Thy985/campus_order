/**
 * WebSocket服务
 * 用于实时接收订单状态推送
 */

import { useUserStore } from '@/stores/userStore';

export interface WebSocketMessage {
  type: 'order_status_change' | 'new_order' | 'merchant_notice';
  orderId?: number;
  merchantId?: number;
  status?: number;
  message?: string;
  timestamp: number;
}

type MessageHandler = (message: WebSocketMessage) => void;

class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectTimeout = 3000;
  private messageHandlers: MessageHandler[] = [];
  private wsUrl: string;

  constructor() {
    // 根据当前环境设置WebSocket地址
    const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090';
    this.wsUrl = apiUrl.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws/order';
  }

  /**
   * 连接WebSocket
   */
  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接');
      return;
    }

    try {
      this.ws = new WebSocket(this.wsUrl);

      this.ws.onopen = () => {
        console.log('WebSocket连接成功');
        this.reconnectAttempts = 0;
        
        // 发送认证信息
        const userStore = useUserStore();
        if (userStore.token) {
          this.send({
            type: 'auth',
            token: userStore.token,
          });
        }
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('WebSocket收到消息:', message);
          this.handleMessage(message);
        } catch (error) {
          console.error('WebSocket消息解析失败:', error);
        }
      };

      this.ws.onclose = () => {
        console.log('WebSocket连接关闭');
        this.attemptReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
      };
    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.attemptReconnect();
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   */
  send(data: any): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  }

  /**
   * 订阅消息
   */
  onMessage(handler: MessageHandler): void {
    this.messageHandlers.push(handler);
  }

  /**
   * 取消订阅
   */
  offMessage(handler: MessageHandler): void {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  /**
   * 处理收到的消息
   */
  private handleMessage(message: WebSocketMessage): void {
    this.messageHandlers.forEach((handler) => {
      try {
        handler(message);
      } catch (error) {
        console.error('消息处理错误:', error);
      }
    });
  }

  /**
   * 尝试重连
   */
  private attemptReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`WebSocket ${this.reconnectTimeout / 1000}秒后尝试重连...`);
      setTimeout(() => {
        this.connect();
      }, this.reconnectTimeout);
    } else {
      console.error('WebSocket重连次数已达上限');
    }
  }

  /**
   * 订阅订单状态变更
   */
  subscribeOrderStatus(orderId: number): void {
    this.send({
      type: 'subscribe_order',
      orderId,
    });
  }

  /**
   * 订阅商家新订单
   */
  subscribeMerchantOrders(merchantId: number): void {
    this.send({
      type: 'subscribe_merchant',
      merchantId,
    });
  }
}

// 导出单例
export const wsService = new WebSocketService();

// 导出Hook
export function useWebSocket() {
  return {
    connect: () => wsService.connect(),
    disconnect: () => wsService.disconnect(),
    onMessage: (handler: MessageHandler) => wsService.onMessage(handler),
    offMessage: (handler: MessageHandler) => wsService.offMessage(handler),
    subscribeOrderStatus: (orderId: number) => wsService.subscribeOrderStatus(orderId),
    subscribeMerchantOrders: (merchantId: number) => wsService.subscribeMerchantOrders(merchantId),
  };
}

