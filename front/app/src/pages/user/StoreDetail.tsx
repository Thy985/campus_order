import { useState, useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Plus, Minus, ShoppingCart, Flame, Heart, Share2, ChevronLeft, Store, Clock, MapPin, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { DishCard } from '@/components/common/DishCard';
import { useMerchantDetail, useProducts, useProductCategories } from '@/hooks';
import { useCartStore } from '@/stores/cartStore';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { toast } from '@/lib/toast';
import type { Dish } from '@/types';

export function StoreDetail() {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [isLiked, setIsLiked] = useState(false);
  
  const { id } = useParams<{ id: string }>();
  const storeId = Number(id) || 0;
  
  const { merchant: store, loading: storeLoading, error: storeError } = useMerchantDetail(storeId);
  
  const { products: dishes, loading: dishesLoading } = useProducts({
    merchantId: storeId,
  });
  
  const { categories } = useProductCategories(storeId);
  
  const { cart, addToCart, removeFromCart, getTotalCount, getTotalPrice } = useCartStore();

  if (storeError) {
    toast.error('获取商家信息失败：' + storeError.message);
  }

  const allCategories = useMemo(() => {
    const cats = new Set(dishes.map((d) => d.category).filter(Boolean));
    return ['all', ...Array.from(cats)];
  }, [dishes]);

  const filteredDishes = useMemo(() => {
    if (selectedCategory === 'all') return dishes;
    return dishes.filter((d) => d.category === selectedCategory);
  }, [dishes, selectedCategory]);

  if (storeLoading || dishesLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <PageSkeleton />
      </div>
    );
  }

  if (!store) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Store className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900">商家不存在</h2>
          <p className="text-gray-500 mt-2">该商家可能已下架或ID错误</p>
          <Link to="/">
            <Button className="mt-4 rounded-xl">返回首页</Button>
          </Link>
        </div>
      </div>
    );
  }

  const cartCount = getTotalCount();
  const cartTotal = getTotalPrice();

  return (
    <div className="min-h-screen bg-gray-50 pb-32">
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-lg border-b border-gray-100/50">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-gray-700 hover:text-gray-900 transition-colors">
            <ChevronLeft className="w-5 h-5" />
            <span className="font-medium">返回</span>
          </Link>
          <h1 className="text-lg font-semibold truncate max-w-[200px]">{store.name}</h1>
          <div className="flex items-center gap-2">
            <button 
              className={`p-2 rounded-full transition-all duration-200 ${isLiked ? 'text-red-500 bg-red-50 shadow-sm' : 'text-gray-400 hover:text-gray-600 hover:bg-gray-50'}`}
              onClick={() => setIsLiked(!isLiked)}
            >
              <Heart className={`w-5 h-5 ${isLiked ? 'fill-current' : ''}`} />
            </button>
            <button className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-50 rounded-full transition-all duration-200">
              <Share2 className="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      <div className="relative">
        <div className="h-56 w-full overflow-hidden bg-gradient-to-br from-orange-100 to-orange-50">
          {store.banner ? (
            <img
              src={store.banner}
              alt={store.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <Store className="w-24 h-24 text-orange-200" />
            </div>
          )}
        </div>
        <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent" />
      </div>

      <div className="relative -mt-16 z-10">
        <div className="max-w-6xl mx-auto px-4">
          <div className="bg-white rounded-3xl shadow-xl p-6 border border-gray-100">
            <div className="flex gap-6">
              <div className="relative flex-shrink-0">
                <div className="w-28 h-28 rounded-2xl overflow-hidden bg-gray-100 shadow-lg border-4 border-white">
                  <img
                    src={store.logo}
                    alt={store.name}
                    className="w-full h-full object-cover"
                  />
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900">{store.name}</h2>
                    <div className="flex items-center gap-3 mt-2">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 text-yellow-400 fill-yellow-400" />
                        <span className="font-semibold text-gray-900">{store.rating}</span>
                      </div>
                      <span className="text-gray-300">|</span>
                      <span className="text-sm text-gray-600">月售{store.monthlySales}+</span>
                      <span className="text-gray-300">|</span>
                      <div className="flex items-center gap-1 text-gray-600">
                        <Clock className="w-4 h-4" />
                        <span className="text-sm">{store.deliveryTime || 30}分钟</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="flex flex-wrap gap-2 mt-4">
                  {store.tags?.slice(0, 4).map((tag, i) => (
                    <Badge 
                      key={i} 
                      variant="secondary" 
                      className="bg-orange-50 text-orange-600 border border-orange-100"
                    >
                      {tag}
                    </Badge>
                  ))}
                </div>

                {store.address && (
                  <div className="flex items-center gap-2 mt-4 text-sm text-gray-500">
                    <MapPin className="w-4 h-4" />
                    <span className="line-clamp-1">{store.address}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 mt-8">
        <div className="sticky top-14 z-30 bg-gray-50/95 backdrop-blur-sm py-4 -mx-4 px-4">
          <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
            {allCategories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-5 py-2.5 rounded-full text-sm font-medium whitespace-nowrap transition-all duration-200 ${
                  selectedCategory === cat 
                    ? 'bg-gradient-to-r from-orange-500 to-orange-600 text-white shadow-lg shadow-orange-500/30' 
                    : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-100'
                }`}
              >
                {cat === 'all' ? '全部' : cat}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-4">
          {filteredDishes.length > 0 ? (
            filteredDishes.map((dish, index) => (
              <div key={dish.id} className="transform hover:-translate-y-1 transition-transform duration-200">
                <DishCard
                  dish={dish}
                  index={index}
                  merchantId={storeId}
                  merchantName={store?.name}
                />
              </div>
            ))
          ) : (
            <div className="col-span-full text-center py-20">
              <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-4">
                <ShoppingCart className="w-12 h-12 text-gray-300" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900">暂无商品</h3>
              <p className="text-gray-500 mt-2">该商家暂未上架商品</p>
            </div>
          )}
        </div>
      </div>

      {cartCount > 0 && (
        <div className="fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur-xl border-t border-gray-100/50 p-4 shadow-2xl z-50">
          <div className="max-w-6xl mx-auto flex items-center justify-between">
            <div className="flex items-center gap-5">
              <div className="relative">
                <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-orange-500 to-orange-600 flex items-center justify-center shadow-lg shadow-orange-500/30">
                  <ShoppingCart className="w-7 h-7 text-white" />
                </div>
                <Badge className="absolute -top-2 -right-2 bg-red-500 text-white border-2 border-white shadow-lg text-xs font-bold h-6 min-w-6 flex items-center justify-center">
                  {cartCount}
                </Badge>
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">¥{cartTotal.toFixed(2)}</div>
                <div className="text-sm text-gray-500">另需配送费 ¥0</div>
              </div>
            </div>
            <Button className="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 rounded-2xl px-8 h-12 text-lg font-semibold shadow-lg shadow-orange-500/30 transition-all duration-200 hover:shadow-xl hover:shadow-orange-500/40">
              去结算
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
