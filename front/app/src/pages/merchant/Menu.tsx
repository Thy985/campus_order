import { useState } from 'react';
import { Plus, Search, Edit2, Trash2, Eye, EyeOff, Flame, Star, Upload, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { useMerchantMenu, useToggleDishAvailability, useDeleteDish, useCreateDish, useUpdateDish } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { toast } from '@/lib/toast';
import type { Dish } from '@/types';

const CURRENT_MERCHANT_ID = 1;

interface DishFormData {
  name: string;
  description: string;
  price: string;
  originalPrice: string;
  stock: string;
  image: string;
  category: string;
  isRecommended: boolean;
  isSpicy: boolean;
}

const initialFormData: DishFormData = {
  name: '',
  description: '',
  price: '',
  originalPrice: '',
  stock: '99',
  image: '',
  category: '',
  isRecommended: false,
  isSpicy: false,
};

export function MerchantMenu() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingDish, setEditingDish] = useState<Dish | null>(null);
  const [formData, setFormData] = useState<DishFormData>(initialFormData);
  
  const { 
    dishes, 
    loading, 
    error, 
    total, 
    refresh 
  } = useMerchantMenu({
    merchantId: CURRENT_MERCHANT_ID,
    keyword: searchQuery || undefined,
  });
  
  const { toggleAvailability } = useToggleDishAvailability();
  const { deleteDish } = useDeleteDish();
  const { createDish, loading: createLoading } = useCreateDish();
  const { updateDish, loading: updateLoading } = useUpdateDish();

  const categories = ['all', ...Array.from(new Set(dishes.map(d => d.category).filter(Boolean)))];

  const filteredDishes = dishes.filter(dish => {
    const matchesSearch = dish.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || dish.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const handleToggleAvailability = async (dish: Dish) => {
    const success = await toggleAvailability(dish.id, dish.isAvailable || false);
    if (success) {
      toast.success(dish.isAvailable ? '菜品已下架' : '菜品已上架');
      refresh();
    }
  };

  const handleDelete = async (dishId: number) => {
    if (!confirm('确定要删除这个菜品吗？')) return;
    
    const success = await deleteDish(dishId);
    if (success) {
      toast.success('菜品已删除');
      refresh();
    }
  };

  const handleOpenAddDialog = () => {
    setFormData(initialFormData);
    setEditingDish(null);
    setShowAddDialog(true);
  };

  const handleOpenEditDialog = (dish: Dish) => {
    setEditingDish(dish);
    setFormData({
      name: dish.name,
      description: dish.description || '',
      price: String(dish.price),
      originalPrice: String(dish.originalPrice || dish.price),
      stock: '99',
      image: dish.image || '',
      category: dish.category || '',
      isRecommended: dish.isRecommended || false,
      isSpicy: dish.isSpicy || false,
    });
    setShowAddDialog(true);
  };

  const handleFormChange = (field: keyof DishFormData, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      toast.error('请输入菜品名称');
      return;
    }
    if (!formData.price || parseFloat(formData.price) <= 0) {
      toast.error('请输入有效的价格');
      return;
    }

    const submitData = {
      name: formData.name.trim(),
      description: formData.description.trim(),
      price: parseFloat(formData.price),
      originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : undefined,
      stock: parseInt(formData.stock) || 99,
      image: formData.image.trim() || undefined,
      category: formData.category.trim() || undefined,
    };

    if (editingDish) {
      const success = await updateDish(editingDish.id, submitData);
      if (success) {
        toast.success('菜品已更新');
        setShowAddDialog(false);
        refresh();
      }
    } else {
      const success = await createDish(submitData);
      if (success) {
        toast.success('菜品已添加');
        setShowAddDialog(false);
        refresh();
      }
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-red-500">加载失败：{error.message}</p>
          <Button className="mt-4" onClick={refresh}>重试</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">菜品管理</h1>
          <p className="text-gray-500 mt-1">共 {total} 个菜品</p>
        </div>
        <Button 
          className="h-12 px-6 rounded-xl gradient-primary text-white shadow-primary"
          onClick={handleOpenAddDialog}
        >
          <Plus className="w-5 h-5 mr-2" />
          添加菜品
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <Input
            placeholder="搜索菜品名称..."
            className="pl-10 h-12 rounded-xl"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="flex gap-2 overflow-x-auto pb-2">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-colors ${selectedCategory === cat ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              {cat === 'all' ? '全部分类' : cat}
            </button>
          ))}
        </div>
      </div>

      {filteredDishes.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">暂无菜品</p>
          <p className="text-sm text-gray-400 mt-2">点击上方按钮添加菜品</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredDishes.map((dish) => (
            <DishCard
              key={dish.id}
              dish={dish}
              onToggleAvailability={() => handleToggleAvailability(dish)}
              onDelete={() => handleDelete(dish.id)}
              onEdit={() => handleOpenEditDialog(dish)}
            />
          ))}
        </div>
      )}

      <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editingDish ? '编辑菜品' : '添加新菜品'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">菜品名称 *</Label>
              <Input
                id="name"
                placeholder="请输入菜品名称"
                value={formData.name}
                onChange={(e) => handleFormChange('name', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">菜品描述</Label>
              <Textarea
                id="description"
                placeholder="请输入菜品描述"
                value={formData.description}
                onChange={(e) => handleFormChange('description', e.target.value)}
                rows={3}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="price">售价 *</Label>
                <Input
                  id="price"
                  type="number"
                  placeholder="0.00"
                  value={formData.price}
                  onChange={(e) => handleFormChange('price', e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="originalPrice">原价</Label>
                <Input
                  id="originalPrice"
                  type="number"
                  placeholder="0.00"
                  value={formData.originalPrice}
                  onChange={(e) => handleFormChange('originalPrice', e.target.value)}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="stock">库存</Label>
                <Input
                  id="stock"
                  type="number"
                  placeholder="99"
                  value={formData.stock}
                  onChange={(e) => handleFormChange('stock', e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="category">分类</Label>
                <Input
                  id="category"
                  placeholder="如：主食、饮品"
                  value={formData.category}
                  onChange={(e) => handleFormChange('category', e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="image">图片链接</Label>
              <Input
                id="image"
                placeholder="请输入图片URL"
                value={formData.image}
                onChange={(e) => handleFormChange('image', e.target.value)}
              />
            </div>

            <div className="flex items-center gap-6">
              <div className="flex items-center gap-2">
                <Switch
                  id="isRecommended"
                  checked={formData.isRecommended}
                  onCheckedChange={(checked) => handleFormChange('isRecommended', checked)}
                />
                <Label htmlFor="isRecommended" className="cursor-pointer">推荐</Label>
              </div>
              <div className="flex items-center gap-2">
                <Switch
                  id="isSpicy"
                  checked={formData.isSpicy}
                  onCheckedChange={(checked) => handleFormChange('isSpicy', checked)}
                />
                <Label htmlFor="isSpicy" className="cursor-pointer">辣味</Label>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAddDialog(false)}>
              取消
            </Button>
            <Button 
              onClick={handleSubmit}
              disabled={createLoading || updateLoading}
            >
              {createLoading || updateLoading ? '保存中...' : (editingDish ? '保存修改' : '添加菜品')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

interface DishCardProps {
  dish: Dish;
  onToggleAvailability: () => void;
  onDelete: () => void;
  onEdit: () => void;
}

function DishCard({ dish, onToggleAvailability, onDelete, onEdit }: DishCardProps) {
  return (
    <Card className={`overflow-hidden transition-all ${!dish.isAvailable ? 'opacity-60' : ''}`}>
      <div className="relative aspect-video">
        <img
          src={dish.image || 'https://via.placeholder.com/200?text=No+Image'}
          alt={dish.name}
          className="w-full h-full object-cover"
        />
        {!dish.isAvailable && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <Badge className="bg-gray-800 text-white border-0">已下架</Badge>
          </div>
        )}
        <div className="absolute top-2 right-2 flex gap-1">
          {dish.isRecommended && (
            <Badge className="bg-orange-500 text-white border-0">
              <Star className="w-3 h-3 mr-1" />
              推荐
            </Badge>
          )}
          {dish.isSpicy && (
            <Badge className="bg-red-500 text-white border-0">
              <Flame className="w-3 h-3 mr-1" />
              辣
            </Badge>
          )}
        </div>
      </div>
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <h3 className="font-bold text-gray-900 truncate">{dish.name}</h3>
            <p className="text-sm text-gray-500 mt-1 line-clamp-2">{dish.description}</p>
            <div className="flex items-center gap-2 mt-2">
              <span className="text-lg font-bold text-orange-500">¥{dish.price}</span>
              {dish.originalPrice > dish.price && (
                <span className="text-sm text-gray-400 line-through">¥{dish.originalPrice}</span>
              )}
            </div>
            <p className="text-xs text-gray-400 mt-1">月售 {dish.sales || 0}</p>
          </div>
        </div>
        
        <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
          <div className="flex items-center gap-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={onEdit}
            >
              <Edit2 className="w-4 h-4 mr-1" />
              编辑
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="text-red-500 hover:text-red-600"
              onClick={onDelete}
            >
              <Trash2 className="w-4 h-4 mr-1" />
              删除
            </Button>
          </div>
          <Button
            variant={dish.isAvailable ? 'outline' : 'default'}
            size="sm"
            onClick={onToggleAvailability}
          >
            {dish.isAvailable ? (
              <>
                <EyeOff className="w-4 h-4 mr-1" />
                下架
              </>
            ) : (
              <>
                <Eye className="w-4 h-4 mr-1" />
                上架
              </>
            )}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
