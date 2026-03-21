import { useState } from 'react';
import { Plus, Minus, Flame } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { LazyImage } from '@/components/ui/LazyImage';
import { useA11y } from '@/hooks/useA11y';
import { useCartStore } from '@/stores/cartStore';
import type { Dish } from '@/types';

interface DishCardProps {
  dish: Dish;
  index?: number;
  merchantId?: number;
  merchantName?: string;
}

export function DishCard({ dish, index = 0, merchantId = 0, merchantName = '' }: DishCardProps) {
  const { addToCart, removeFromCart, cart } = useCartStore();
  const [isAnimating, setIsAnimating] = useState(false);
  const { announce } = useA11y();

  const cartItem = cart?.items.find((item) => item.productId === dish.id);
  const quantity = cartItem?.quantity || 0;

  const handleAdd = () => {
    setIsAnimating(true);
    addToCart(dish, merchantId, merchantName);
    announce(`${dish.name} 已添加到购物车，当前数量 ${quantity + 1}`, 'polite');
    setTimeout(() => setIsAnimating(false), 300);
  };

  const handleRemove = () => {
    if (quantity > 0) {
      removeFromCart(dish.id);
      if (quantity - 1 > 0) {
        announce(`${dish.name} 数量已减少，当前数量 ${quantity - 1}`, 'polite');
      } else {
        announce(`${dish.name} 已从购物车移除`, 'polite');
      }
    }
  };

  return (
    <article
      className="group bg-white rounded-2xl shadow-card overflow-hidden card-hover animate-fade-in-up h-full flex flex-col"
      style={{ animationDelay: `${index * 60}ms` }}
      data-testid="product-item"
      aria-label={`${dish.name}，${dish.price} 元${quantity > 0 ? `，购物车中已有 ${quantity} 份` : ''}`}
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-gradient-to-br from-gray-100 to-gray-200">
        <LazyImage
          src={dish.image}
          alt={`${dish.name} 菜品图片`}
          className="w-full h-full object-cover transition-all duration-500 group-hover:scale-110 group-hover:brightness-105"
          aspectRatio="4/3"
          priority={index < 6}
          placeholderColor="#f3f4f6"
        />
        
        <div className="absolute top-3 left-3 flex flex-col gap-1.5 z-10">
          {dish.isRecommended && (
            <Badge className="bg-gradient-to-r from-orange-500 to-orange-600 text-white text-xs font-medium shadow-lg shadow-orange-500/30">
              店长推荐
            </Badge>
          )}
          {dish.originalPrice && dish.originalPrice > dish.price && (
            <Badge className="bg-gradient-to-r from-red-500 to-red-600 text-white text-xs font-medium shadow-lg shadow-red-500/30">
              限时特惠
            </Badge>
          )}
        </div>

        {dish.isSpicy && (
          <div className="absolute top-3 right-3 w-8 h-8 rounded-xl bg-white/95 backdrop-blur-md flex items-center justify-center shadow-md z-10">
            <Flame className="w-4 h-4 text-red-500" />
          </div>
        )}

        <div className="absolute inset-0 bg-gradient-to-t from-black/10 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
      </div>

      <div className="p-5 flex flex-col flex-1">
        <h3 className="text-lg font-bold text-gray-900 line-clamp-1 group-hover:text-orange-600 transition-colors">
          {dish.name}
        </h3>
        
        {dish.description && (
          <p className="text-sm text-gray-500 mt-2 line-clamp-2 leading-relaxed flex-1">
            {dish.description}
          </p>
        )}

        {dish.tags && dish.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mt-3" role="list" aria-label="菜品标签">
            {dish.tags.slice(0, 3).map((tag, i) => (
              <span
                key={i}
                className="text-xs text-gray-500 bg-gray-50 px-2.5 py-1 rounded-lg border border-gray-100"
                role="listitem"
              >
                {tag}
              </span>
            ))}
          </div>
        )}

        <p className="text-xs text-gray-400 mt-3 flex items-center gap-1">
          <span className="inline-block w-2 h-2 rounded-full bg-orange-100" />
          月售 {dish.sales || 0} 份
        </p>

        <div className="flex items-end justify-between mt-4 pt-4 border-t border-gray-100">
          <div className="flex items-baseline gap-2" aria-label={`价格 ${dish.price} 元`}>
            <span className="text-2xl font-bold text-orange-500">
              ¥{dish.price}
            </span>
            {dish.originalPrice && dish.originalPrice > dish.price && (
              <span className="text-sm text-gray-400 line-through" aria-label={`原价 ${dish.originalPrice} 元`}>
                ¥{dish.originalPrice}
              </span>
            )}
          </div>

          <div className="flex items-center">
            {quantity === 0 ? (
              <Button
                size="sm"
                onClick={handleAdd}
                aria-label={`添加 ${dish.name} 到购物车`}
                className={`h-10 w-10 p-0 rounded-2xl bg-gradient-to-br from-orange-500 to-orange-600 text-white shadow-lg shadow-orange-500/30 hover:shadow-xl hover:shadow-orange-500/40 hover:from-orange-600 hover:to-orange-700 transition-all duration-250 ${
                  isAnimating ? 'animate-cart-bounce' : ''
                }`}
              >
                <Plus className="w-5 h-5" aria-hidden="true" />
              </Button>
            ) : (
              <div className="flex items-center gap-2 bg-gradient-to-r from-gray-50 to-gray-100 rounded-2xl p-1.5 border border-gray-200">
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={handleRemove}
                  className="h-8 w-8 p-0 rounded-xl hover:bg-white hover:shadow-sm transition-all border-0"
                >
                  <Minus className="w-4 h-4 text-gray-600" />
                </Button>
                <span className="text-base font-bold w-8 text-center text-gray-900">
                  {quantity}
                </span>
                <Button
                  size="sm"
                  onClick={handleAdd}
                  className={`h-8 w-8 p-0 rounded-xl bg-gradient-to-br from-orange-500 to-orange-600 text-white hover:from-orange-600 hover:to-orange-700 transition-all ${
                    isAnimating ? 'animate-cart-bounce' : ''
                  }`}
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </article>
  );
}
