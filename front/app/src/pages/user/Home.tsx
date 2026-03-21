import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Store, TrendingUp, Zap, ChevronRight, Star, Gift, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { StoreCard } from '@/components/common/StoreCard';
import { CategoryNav } from '@/components/common/CategoryNav';
import { useMerchants, useUser } from '@/hooks';
import { StoreCardSkeleton } from '@/components/ui/skeleton';
import { RippleButton } from '@/components/ui/ripple-button';
import { toast } from '@/lib/toast';

// 动画配置
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: [0.4, 0, 0.2, 1] as const,
    },
  },
};

export function Home() {
  const navigate = useNavigate();
  const { isAuthenticated } = useUser();
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 使用真实数据Hook
  const { merchants, loading, error, total, refresh } = useMerchants({
    page: 1,
    pageSize: 20,
    keyword: searchKeyword || undefined,
    categoryId: selectedCategory && selectedCategory !== 'all' ? Number(selectedCategory) : undefined,
  });

  // 处理新用户专享按钮点击
  const handleNewUserClick = () => {
    if (!isAuthenticated) {
      toast.error('请先登录领取优惠');
      navigate('/login', { state: { from: '/' } });
      return;
    }
    // 已登录用户跳转到优惠券页面
    navigate('/coupons');
  };

  // 处理本周热销按钮点击
  const handleHotClick = () => {
    // 滚动到推荐商家区域或跳转到商家列表
    navigate('/stores');
  };

  // 错误处理 - 使用useEffect避免在渲染阶段调用toast
  useEffect(() => {
    if (error) {
      console.error('Merchant list error:', error);
      toast.error('获取商家列表失败：' + error.message);
    }
  }, [error]);



  return (
    <div className="min-h-screen bg-gradient-to-b from-orange-50/50 to-white pb-20 lg:pb-0">
      {/* Hero Section */}
      <section className="relative pt-24 pb-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: [0.4, 0, 0.2, 1] }}
            className="text-center"
          >
            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-gray-900">
              校园美食，<span className="text-orange-500">一触即达</span>
            </h1>
            <p className="mt-4 text-lg text-gray-600 max-w-2xl mx-auto">
              汇聚校园周边优质商家，让美食不再等待
            </p>
          </motion.div>

          {/* Search Bar - 优化版本 */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2, ease: [0.4, 0, 0.2, 1] }}
            className="mt-8 max-w-2xl mx-auto"
          >
            <div className="relative group">
              {/* 发光背景效果 */}
              <div className="absolute -inset-1 bg-gradient-to-r from-orange-400 via-orange-500 to-red-500 rounded-2xl blur opacity-25 group-hover:opacity-50 transition duration-500 group-focus-within:opacity-60"></div>
              
              <div className="relative flex items-center bg-white rounded-2xl shadow-lg shadow-orange-500/10 group-focus-within:shadow-orange-500/20 transition-shadow duration-300">
                <Search className="absolute left-4 w-5 h-5 text-gray-400 group-focus-within:text-orange-500 transition-colors" />
                <Input
                  type="text"
                  placeholder="搜索商家或菜品..."
                  className="w-full h-14 pl-12 pr-32 rounded-2xl border-0 text-lg bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0"
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      refresh();
                    }
                  }}
                />
                <RippleButton
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded-xl bg-gradient-to-r from-orange-500 to-red-500 hover:from-orange-600 hover:to-red-600 text-white shadow-lg shadow-orange-500/30"
                  onClick={() => refresh()}
                >
                  搜索
                </RippleButton>
              </div>
            </div>
            
            {/* 热门搜索标签 */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.4 }}
              className="flex items-center justify-center gap-2 mt-4 flex-wrap"
            >
              <span className="text-sm text-gray-400">热门搜索：</span>
              {['奶茶', '汉堡', '盖浇饭', '麻辣烫', '寿司'].map((tag) => (
                <button
                  key={tag}
                  onClick={() => {
                    setSearchKeyword(tag);
                    setTimeout(() => refresh(), 0);
                  }}
                  className="px-3 py-1 text-sm text-gray-600 bg-white rounded-full border border-gray-200 hover:border-orange-300 hover:text-orange-500 transition-colors"
                >
                  {tag}
                </button>
              ))}
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Categories */}
      <motion.section
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.3 }}
        className="px-4 sm:px-6 lg:px-8"
      >
        <div className="max-w-7xl mx-auto">
          <CategoryNav
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
          />
        </div>
      </motion.section>

      {/* Featured Stores */}
      <section className="px-4 sm:px-6 lg:px-8 py-12">
        <div className="max-w-7xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
            className="flex items-center justify-between mb-6"
          >
            <div>
              <h2 className="text-2xl font-bold text-gray-900">推荐商家</h2>
              <p className="text-gray-500 mt-1">共 {total} 家优质商家</p>
            </div>
            <Button variant="ghost" className="text-orange-500 hover:text-orange-600 hover:bg-orange-50">
              查看全部
              <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </motion.div>

          {loading ? (
            // 使用骨架屏
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {Array.from({ length: 8 }).map((_, i) => (
                <StoreCardSkeleton key={i} />
              ))}
            </div>
          ) : merchants.length > 0 ? (
            <motion.div
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
            >
              {merchants.map((store, index) => (
                <motion.div
                  key={store.id}
                  variants={itemVariants}
                  whileHover={{ y: -8, transition: { duration: 0.2 } }}
                >
                  <StoreCard store={store} index={index} />
                </motion.div>
              ))}
            </motion.div>
          ) : (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-center py-16"
            >
              <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Store className="w-12 h-12 text-gray-300" />
              </div>
              <p className="text-gray-500 text-lg">暂无商家数据</p>
              {searchKeyword && (
                <Button
                  variant="outline"
                  className="mt-4"
                  onClick={() => {
                    setSearchKeyword('');
                    setSelectedCategory('all');
                  }}
                >
                  清除筛选条件
                </Button>
              )}
            </motion.div>
          )}
        </div>
      </section>

      {/* Promo Section */}
      <section className="px-4 sm:px-6 lg:px-8 py-12">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.5 }}
              whileHover={{ scale: 1.02 }}
              className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-orange-500 to-red-500 p-8 text-white cursor-pointer"
            >
              <div className="relative z-10">
                <Badge className="bg-white/20 text-white border-0 mb-4 backdrop-blur-sm">
                  <Zap className="w-3 h-3 mr-1" />
                  限时优惠
                </Badge>
                <h3 className="text-2xl font-bold mb-2">新用户专享</h3>
                <p className="text-white/80 mb-4">首单立减15元，快来体验吧！</p>
                <RippleButton
                  className="bg-white text-orange-500 hover:bg-white/90 rounded-xl shadow-lg"
                  onClick={handleNewUserClick}
                >
                  立即领取
                </RippleButton>
              </div>
              <Gift className="absolute -right-8 -bottom-8 w-48 h-48 text-white/10" />
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.6 }}
              whileHover={{ scale: 1.02 }}
              className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-blue-500 to-purple-500 p-8 text-white cursor-pointer"
            >
              <div className="relative z-10">
                <Badge className="bg-white/20 text-white border-0 mb-4 backdrop-blur-sm">
                  <TrendingUp className="w-3 h-3 mr-1" />
                  热门推荐
                </Badge>
                <h3 className="text-2xl font-bold mb-2">本周热销</h3>
                <p className="text-white/80 mb-4">发现校园周边最受欢迎的美食</p>
                <RippleButton
                  className="bg-white text-blue-500 hover:bg-white/90 rounded-xl shadow-lg"
                  onClick={handleHotClick}
                >
                  去探索
                </RippleButton>
              </div>
              <Star className="absolute -right-8 -bottom-8 w-48 h-48 text-white/10" />
            </motion.div>
          </div>
        </div>
      </section>
    </div>
  );
}
