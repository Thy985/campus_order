import { useState, useMemo, useEffect, useCallback } from 'react';
import { Search, SlidersHorizontal, Star, Clock, ArrowUpDown } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { StoreCard } from '@/components/common/StoreCard';
import { CategoryNav } from '@/components/common/CategoryNav';
import { getMerchantList } from '@/api/merchant';
import { useScrollAnimation } from '@/hooks/useScrollAnimation';
import { toast } from '@/lib/toast';
import type { Merchant, Store } from '@/types';

type SortOption = 'default' | 'rating' | 'sales' | 'time';

export function Stores() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [sortBy, setSortBy] = useState<SortOption>('default');
  const [showFilters, setShowFilters] = useState(false);
  const [stores, setStores] = useState<Store[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { ref, isVisible } = useScrollAnimation<HTMLDivElement>();

  const loadStores = useCallback(async () => {
    setIsLoading(true);
    try {
      const categoryId = selectedCategory ? parseInt(selectedCategory) : undefined;
      const response = await getMerchantList({
        page: 1,
        pageSize: 10,
        categoryId,
        keyword: searchQuery || undefined,
        status: 1,
      });
      const merchantList = response.merchantList || [];
      const convertedStores: Store[] = merchantList.map(merchant => ({
        id: merchant.id,
        name: merchant.name,
        logo: merchant.logo,
        rating: merchant.rating,
        monthlySales: merchant.salesVolume || 0,
        deliveryTime: merchant.deliveryTime || 30,
        distance: merchant.distance || 0,
        minPrice: merchant.minPrice || 0,
        tags: [],
        categories: []
      }));
      setStores(convertedStores);
    } catch (error) {
      console.error('加载商家列表失败:', error);
      toast.error('加载商家列表失败');
    } finally {
      setIsLoading(false);
    }
  }, [selectedCategory, searchQuery]);

  useEffect(() => {
    loadStores();
  }, [loadStores]);

  const filteredStores = useMemo(() => {
    const result = [...stores];

    switch (sortBy) {
      case 'rating':
        result.sort((a, b) => b.rating - a.rating);
        break;
      case 'sales':
        result.sort((a, b) => b.monthlySales - a.monthlySales);
        break;
      default:
        break;
    }

    return result;
  }, [stores, sortBy]);

  const sortOptions: { value: SortOption; label: string; icon: React.ElementType }[] = [
    { value: 'default', label: '综合排序', icon: ArrowUpDown },
    { value: 'rating', label: '评分最高', icon: Star },
    { value: 'sales', label: '销量最高', icon: ArrowUpDown },
    { value: 'time', label: '配送最快', icon: Clock },
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20 lg:pb-8">
      {/* 页面标题 */}
      <header className="bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <h1 className="text-2xl font-bold text-gray-900">商家列表</h1>
          <p className="text-gray-500 mt-1">发现校园周边美食</p>
        </div>
      </header>

      <div className="sticky top-16 lg:top-[72px] z-30 bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              type="text"
              placeholder="搜索商家、菜品..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-12 pl-12 pr-4 rounded-xl border-gray-200 focus:border-orange-500 focus:ring-orange-500"
            />
          </div>

          <div className="flex items-center justify-between mt-4 gap-4">
            <div className="flex-1 overflow-x-auto custom-scrollbar">
              <CategoryNav
                selectedCategory={selectedCategory}
                onSelectCategory={setSelectedCategory}
              />
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowFilters(!showFilters)}
              className={`flex-shrink-0 h-10 px-4 rounded-xl border-gray-200 ${
                showFilters ? 'bg-orange-50 border-orange-200 text-orange-500' : ''
              }`}
            >
              <SlidersHorizontal className="w-4 h-4 mr-2" />
              筛选
            </Button>
          </div>

          {showFilters && (
            <div className="flex flex-wrap gap-2 mt-4 animate-fade-in">
              {sortOptions.map((option) => {
                const Icon = option.icon;
                return (
                  <Button
                    key={option.value}
                    variant={sortBy === option.value ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setSortBy(option.value)}
                    className={`h-9 rounded-lg text-sm ${
                      sortBy === option.value
                        ? 'gradient-primary text-white border-0'
                        : 'border-gray-200 text-gray-600 hover:bg-gray-50'
                    }`}
                  >
                    <Icon className="w-4 h-4 mr-1.5" />
                    {option.label}
                  </Button>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <div ref={ref} className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div
          className={`flex items-center justify-between mb-6 transition-all duration-500 ${
            isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
          }`}
        >
          <p className="text-gray-600">
            共找到 <span className="font-semibold text-gray-900">{filteredStores.length}</span> 家商家
          </p>
          {selectedCategory && (
            <Badge
              variant="secondary"
              className="bg-orange-50 text-orange-600 cursor-pointer hover:bg-orange-100"
              onClick={() => setSelectedCategory(null)}
            >
              清除筛选 ×
            </Badge>
          )}
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="bg-white rounded-xl h-48 animate-pulse" />
            ))}
          </div>
        ) : filteredStores.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredStores.map((store, index) => (
              <StoreCard key={store.id} store={store} index={index} />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center mb-4">
              <Search className="w-10 h-10 text-gray-400" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900">未找到商家</h3>
            <p className="text-gray-500 mt-2">试试其他关键词或筛选条件</p>
            <Button
              variant="outline"
              onClick={() => {
                setSearchQuery('');
                setSelectedCategory(null);
                setSortBy('default');
              }}
              className="mt-4 rounded-xl"
            >
              清除筛选
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
