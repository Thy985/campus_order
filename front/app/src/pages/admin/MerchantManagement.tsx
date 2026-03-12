import { useState } from 'react';
import { Search, Edit2, Store, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

const mockMerchants = [
  {
    id: '1',
    name: '兰州拉面',
    logo: 'https://images.unsplash.com/photo-1562967960-f55495a8899b?w=200&h=200&fit=crop',
    category: '快餐',
    phone: '13800138000',
    status: 'active',
    rating: 4.8,
    orderCount: 1256,
    revenue: 56890,
    createdAt: '2024-01-05'
  },
  {
    id: '2',
    name: '沙县小吃',
    logo: 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=200&h=200&fit=crop',
    category: '快餐',
    phone: '13900139000',
    status: 'active',
    rating: 4.5,
    orderCount: 892,
    revenue: 35670,
    createdAt: '2024-01-08'
  },
  {
    id: '3',
    name: '黄焖鸡米饭',
    logo: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=200&h=200&fit=crop',
    category: '盖浇饭',
    phone: '13700137000',
    status: 'inactive',
    rating: 4.3,
    orderCount: 456,
    revenue: 18900,
    createdAt: '2024-01-10'
  },
  {
    id: '4',
    name: '麻辣烫',
    logo: 'https://images.unsplash.com/photo-1574848198584-1d72c3820c0e?w=200&h=200&fit=crop',
    category: '麻辣',
    phone: '13600136000',
    status: 'active',
    rating: 4.7,
    orderCount: 2341,
    revenue: 98230,
    createdAt: '2024-01-03'
  },
];

export function MerchantManagement() {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');

  const filteredMerchants = mockMerchants.filter(merchant => {
    const matchesSearch = merchant.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         merchant.phone.includes(searchQuery);
    const matchesStatus = statusFilter === 'all' || merchant.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">商家管理</h1>
        <p className="text-gray-500 mt-1">管理平台入驻商家，查看商家信息</p>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-4 text-center">
            <p className="text-2xl font-bold text-gray-900">128</p>
            <p className="text-sm text-gray-500">总商家</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <p className="text-2xl font-bold text-green-600">115</p>
            <p className="text-sm text-gray-500">营业中</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <p className="text-2xl font-bold text-orange-500">¥28.5万</p>
            <p className="text-sm text-gray-500">本月营收</p>
          </CardContent>
        </Card>
      </div>

      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <Input
            type="text"
            placeholder="搜索商家名称、手机号..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="h-12 pl-12 rounded-xl"
          />
        </div>
        <div className="flex gap-2">
          {(['all', 'active', 'inactive'] as const).map((status) => (
            <Button
              key={status}
              variant={statusFilter === status ? 'default' : 'outline'}
              onClick={() => setStatusFilter(status)}
              className={`h-12 px-4 rounded-xl ${
                statusFilter === status
                  ? 'gradient-primary text-white'
                  : 'border-gray-200'
              }`}
            >
              {status === 'all' ? '全部' : status === 'active' ? '营业中' : '已歇业'}
            </Button>
          ))}
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-100 bg-gray-50">
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">商家信息</th>
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">分类</th>
                  <th className="text-center py-4 px-6 text-sm font-medium text-gray-500">评分</th>
                  <th className="text-center py-4 px-6 text-sm font-medium text-gray-500">订单数</th>
                  <th className="text-center py-4 px-6 text-sm font-medium text-gray-500">营收</th>
                  <th className="text-center py-4 px-6 text-sm font-medium text-gray-500">状态</th>
                  <th className="text-right py-4 px-6 text-sm font-medium text-gray-500">操作</th>
                </tr>
              </thead>
              <tbody>
                {filteredMerchants.map((merchant, index) => (
                  <tr 
                    key={merchant.id} 
                    className="border-b border-gray-50 hover:bg-gray-50 animate-fade-in-up"
                    style={{ animationDelay: `${index * 50}ms` }}
                  >
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-3">
                        <img
                          src={merchant.logo}
                          alt={merchant.name}
                          className="w-10 h-10 rounded-xl object-cover"
                        />
                        <div>
                          <p className="font-medium text-gray-900">{merchant.name}</p>
                          <p className="text-sm text-gray-500">{merchant.phone}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <Badge className="bg-blue-100 text-blue-600">{merchant.category}</Badge>
                    </td>
                    <td className="py-4 px-6 text-center">
                      <div className="flex items-center justify-center gap-1">
                        <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                        <span className="font-medium">{merchant.rating}</span>
                      </div>
                    </td>
                    <td className="py-4 px-6 text-center text-gray-900">{merchant.orderCount}</td>
                    <td className="py-4 px-6 text-center">
                      <span className="font-semibold text-orange-500">¥{merchant.revenue}</span>
                    </td>
                    <td className="py-4 px-6 text-center">
                      <Badge className={merchant.status === 'active' ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-600'}>
                        {merchant.status === 'active' ? '营业中' : '已歇业'}
                      </Badge>
                    </td>
                    <td className="py-4 px-6 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button size="sm" variant="ghost" className="h-8 w-8 p-0">
                          <Edit2 className="w-4 h-4" />
                        </Button>
                        <Button size="sm" variant="ghost" className="h-8 w-8 p-0 text-blue-500">
                          <Store className="w-4 h-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {filteredMerchants.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500">未找到匹配的商家</p>
        </div>
      )}
    </div>
  );
}
