import { Star, Clock, MapPin, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Badge } from '@/components/ui/badge';
import { LazyImage } from '@/components/ui/LazyImage';
import type { Store } from '@/types';

interface StoreCardProps {
  store: Store;
  index?: number;
}

export function StoreCard({ store, index = 0 }: StoreCardProps) {
  return (
    <Link
      to={`/store/${store.id}`}
      className="group block"
      data-testid="store-card"
      aria-label={`查看 ${store.name} 店铺详情，评分 ${store.rating} 分，月售 ${store.monthlySales}+`}
    >
      <motion.article
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{
          duration: 0.5,
          delay: index * 0.08,
          ease: [0.4, 0, 0.2, 1],
        }}
        whileHover={{
          y: -8,
          transition: { duration: 0.2 },
        }}
        className="bg-white rounded-2xl shadow-sm overflow-hidden transition-shadow duration-300 hover:shadow-xl"
      >
        <div className="flex p-4 gap-4">
          {/* Logo - 使用 LazyImage 优化 */}
          <div className="relative w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 bg-gray-100 group-hover:ring-2 group-hover:ring-orange-200 transition-all duration-300">
            <LazyImage
              src={store.logo}
              alt={`${store.name} 店铺Logo`}
              className="w-full h-full transition-transform duration-500 group-hover:scale-110"
              aspectRatio="1/1"
              priority={index < 3}  // 前3个优先加载
            />
            {/* 推荐标签 */}
            {store.rating >= 4.5 && (
              <div className="absolute top-1 left-1 bg-gradient-to-r from-amber-400 to-orange-500 text-white text-[10px] px-1.5 py-0.5 rounded-full flex items-center gap-0.5">
                <Sparkles className="w-3 h-3" />
                推荐
              </div>
            )}
          </div>

          {/* Info */}
          <header className="flex-1 min-w-0">
            <h3 className="text-lg font-semibold text-gray-900 truncate group-hover:text-orange-500 transition-colors duration-300">
              {store.name}
            </h3>

            {/* Rating */}
            <div className="flex items-center gap-2 mt-1.5" aria-label={`评分 ${store.rating} 分`}>
              <div className="flex items-center gap-1">
                <Star className="w-4 h-4 fill-amber-400 text-amber-400" aria-hidden="true" />
                <span className="text-sm font-semibold text-gray-900">{store.rating}</span>
              </div>
              <span className="text-sm text-gray-500">月售 {store.monthlySales}+</span>
            </div>

            {/* Delivery Info */}
            <div className="flex items-center gap-3 mt-2 text-sm text-gray-500">
              <span className="flex items-center gap-1">
                <Clock className="w-3.5 h-3.5" aria-hidden="true" />
                {store.deliveryTime}分钟
              </span>
              <span className="flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5" aria-hidden="true" />
                {store.distance}km
              </span>
            </div>

            {/* Tags */}
            {store.tags && store.tags.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mt-2.5">
                {store.tags.slice(0, 3).map((tag) => (
                  <Badge
                    key={tag}
                    variant="secondary"
                    className="text-xs bg-orange-50 text-orange-600 border-0 group-hover:bg-orange-100 transition-colors"
                  >
                    {tag}
                  </Badge>
                ))}
              </div>
            )}

            {/* Min Price */}
            <p className="mt-2 text-sm text-gray-500">
              起送 <span className="text-orange-500 font-medium">¥{store.minPrice}</span>
            </p>
          </header>
        </div>

        {/* 底部装饰条 */}
        <div className="h-1 bg-gradient-to-r from-orange-400 via-orange-500 to-red-500 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left" />
      </motion.article>
    </Link>
  );
}
