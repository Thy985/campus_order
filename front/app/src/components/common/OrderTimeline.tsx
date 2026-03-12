import { motion } from 'framer-motion';
import {
  Clock,
  CheckCircle,
  ChefHat,
  Package,
  Store,
  XCircle,
  CreditCard,
} from 'lucide-react';
import { OrderStatus } from '@/types';

interface OrderTimelineProps {
  status: OrderStatus;
  createTime?: string;
  payTime?: string;
  acceptTime?: string;
  finishTime?: string;
  cancelTime?: string;
}

interface TimelineStep {
  status: OrderStatus;
  title: string;
  icon: React.ElementType;
  description: string;
}

const timelineSteps: TimelineStep[] = [
  {
    status: OrderStatus.PENDING_PAYMENT,
    title: '提交订单',
    icon: Clock,
    description: '订单已提交，等待支付',
  },
  {
    status: OrderStatus.PENDING_ACCEPTANCE,
    title: '商家接单',
    icon: Store,
    description: '商家已接单，准备制作',
  },
  {
    status: OrderStatus.PREPARING,
    title: '制作中',
    icon: ChefHat,
    description: '商家正在制作您的订单',
  },
  {
    status: OrderStatus.READY_FOR_PICKUP,
    title: '待取餐',
    icon: Package,
    description: '订单已制作完成，请前往取餐',
  },
  {
    status: OrderStatus.COMPLETED,
    title: '已完成',
    icon: CheckCircle,
    description: '订单已完成，感谢您的光临',
  },
];

export function OrderTimeline({
  status,
  createTime,
  payTime,
  acceptTime,
  finishTime,
  cancelTime,
}: OrderTimelineProps) {
  // 获取当前步骤索引
  const currentStepIndex = timelineSteps.findIndex((step) => step.status === status);

  // 如果订单已取消，显示取消状态
  if (status === OrderStatus.CANCELLED) {
    return (
      <div className="bg-red-50 rounded-2xl p-6 border border-red-100">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
            <XCircle className="w-6 h-6 text-red-500" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-red-700">订单已取消</h3>
            {cancelTime && (
              <p className="text-sm text-red-500">{cancelTime}</p>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="relative">
      {timelineSteps.map((step, index) => {
        const isCompleted = index < currentStepIndex;
        const isCurrent = index === currentStepIndex;
        const isPending = index > currentStepIndex;

        const Icon = step.icon;

        return (
          <motion.div
            key={step.status}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.1 }}
            className="relative flex gap-4 pb-8 last:pb-0"
          >
            {/* 连接线 */}
            {index < timelineSteps.length - 1 && (
              <div
                className={`absolute left-5 top-10 w-0.5 h-full ${
                  isCompleted ? 'bg-orange-500' : 'bg-gray-200'
                }`}
              />
            )}

            {/* 节点 */}
            <div className="relative z-10">
              <motion.div
                animate={
                  isCurrent
                    ? {
                        scale: [1, 1.1, 1],
                        boxShadow: [
                          '0 0 0 0 rgba(249, 115, 22, 0.4)',
                          '0 0 0 10px rgba(249, 115, 22, 0)',
                        ],
                      }
                    : {}
                }
                transition={{
                  duration: 2,
                  repeat: Infinity,
                  ease: 'easeInOut',
                }}
                className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors duration-300 ${
                  isCompleted
                    ? 'bg-orange-500 text-white'
                    : isCurrent
                    ? 'bg-orange-500 text-white ring-4 ring-orange-200'
                    : 'bg-gray-100 text-gray-400'
                }`}
              >
                <Icon className="w-5 h-5" />
              </motion.div>
            </div>

            {/* 内容 */}
            <div className="flex-1 pt-1">
              <h4
                className={`font-semibold transition-colors duration-300 ${
                  isCompleted || isCurrent
                    ? 'text-gray-900'
                    : 'text-gray-400'
                }`}
              >
                {step.title}
              </h4>
              <p
                className={`text-sm mt-1 transition-colors duration-300 ${
                  isCompleted || isCurrent
                    ? 'text-gray-600'
                    : 'text-gray-400'
                }`}
              >
                {step.description}
              </p>
              {/* 时间显示 */}
              {isCompleted && index === 0 && createTime && (
                <p className="text-xs text-gray-400 mt-1">{createTime}</p>
              )}
              {isCompleted && index === 1 && payTime && (
                <p className="text-xs text-gray-400 mt-1">{payTime}</p>
              )}
              {isCompleted && index === 2 && acceptTime && (
                <p className="text-xs text-gray-400 mt-1">{acceptTime}</p>
              )}
              {(isCompleted || isCurrent) &&
                index === 4 &&
                finishTime && (
                  <p className="text-xs text-gray-400 mt-1">{finishTime}</p>
                )}
            </div>
          </motion.div>
        );
      })}
    </div>
  );
}
