import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Store,
  Camera,
  MapPin,
  Phone,
  Clock,
  Save,
  Plus,
  X,
  Info,
  RotateCcw
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { useMerchantSettings } from '@/hooks';
import { toast } from '@/lib/toast';

// 营业时间段接口
interface TimeSlot {
  id: string;
  startTime: string;
  endTime: string;
}

export function MerchantSettings() {
  const navigate = useNavigate();
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  const {
    formData,
    businessHours,
    isLoading,
    isSaving,
    isUploading,
    fetchMerchantInfo,
    uploadAvatar,
    saveSettings,
    updateFormData,
    addTimeSlot,
    removeTimeSlot,
    updateTimeSlot,
  } = useMerchantSettings();

  useEffect(() => {
    fetchMerchantInfo();
  }, [fetchMerchantInfo]);

  // 上传头像
  const handleUploadAvatar = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      toast.error('请选择图片文件');
      return;
    }

    // 验证文件大小(最大5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('图片大小不能超过5MB');
      return;
    }

    const url = await uploadAvatar(file);
    if (url) {
      updateFormData('avatar', url);
    }
  };

  // 表单验证
  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = '请输入店铺名称';
    }

    if (!formData.phone.trim()) {
      newErrors.phone = '请输入联系电话';
    } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
      newErrors.phone = '手机号格式不正确';
    }

    if (!formData.address.trim()) {
      newErrors.address = '请输入店铺地址';
    }

    if (formData.minOrder < 0) {
      newErrors.minOrder = '起送金额不能为负数';
    }

    if (formData.packagingFee < 0) {
      newErrors.packagingFee = '打包费不能为负数';
    }

    // 验证营业时间
    for (const slot of businessHours) {
      if (slot.startTime >= slot.endTime) {
        newErrors.businessHours = '结束时间必须晚于开始时间';
        break;
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 保存设置
  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }

    const success = await saveSettings(formData, businessHours);
    if (success) {
      // 保存成功后可以做一些额外操作
    }
  };

  // 重置设置
  const handleReset = async () => {
    if (window.confirm('确定要重置所有修改吗？这将恢复到上次保存的状态。')) {
      await fetchMerchantInfo();
      setErrors({});
      toast.success('已重置到上次保存的状态');
    }
  };

  const handleInputChange = (field: string, value: string | number | boolean) => {
    updateFormData(field as keyof typeof formData, value);
    
    // 清除该字段的错误
    if (errors[field]) {
      const newErrors = { ...errors };
      delete newErrors[field];
      setErrors(newErrors);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-500">加载中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-24">
      {/* Header */}
      <header className="sticky top-0 z-10 bg-white border-b">
        <div className="flex items-center justify-between p-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-lg font-semibold">店铺设置</h1>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              onClick={handleReset}
              disabled={isSaving || isUploading || isLoading}
              className="flex items-center gap-2"
            >
              <RotateCcw className="w-4 h-4" />
              重置
            </Button>
            <Button
              onClick={handleSave}
              disabled={isSaving || isUploading}
              className="flex items-center gap-2"
            >
              <Save className="w-4 h-4" />
              {isSaving ? '保存中...' : '保存'}
            </Button>
          </div>
        </div>
      </header>

      <div className="max-w-2xl mx-auto p-4 space-y-4">
        {/* 店铺头像 */}
        <Card className="p-6">
          <Label className="text-base font-semibold mb-4 block">店铺头像</Label>
          <div className="flex items-center gap-6">
            <div className="relative">
              <div className="w-24 h-24 rounded-lg overflow-hidden bg-gray-100">
                {formData.avatar ? (
                  <img
                    src={formData.avatar}
                    alt="店铺头像"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center">
                    <Store className="w-12 h-12 text-gray-400" />
                  </div>
                )}
              </div>
              <label className={`absolute -bottom-2 -right-2 w-10 h-10 bg-orange-500 rounded-full flex items-center justify-center cursor-pointer hover:bg-orange-600 transition-colors shadow-lg ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}>
                <Camera className="w-5 h-5 text-white" />
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleUploadAvatar}
                  disabled={isUploading}
                  className="hidden"
                />
              </label>
            </div>
            <div className="flex-1">
              <p className="text-sm text-gray-500">
                支持JPG、PNG格式，建议尺寸400x400像素，文件大小不超过5MB
              </p>
              {isUploading && <p className="text-sm text-orange-500 mt-2">上传中...</p>}
            </div>
          </div>
        </Card>

        {/* 基本信息 */}
        <Card className="p-6 space-y-4">
          <h3 className="text-base font-semibold">基本信息</h3>

          {/* 店铺名称 */}
          <div className="space-y-2">
            <Label htmlFor="name">店铺名称 *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              placeholder="请输入店铺名称"
              maxLength={50}
              className={errors.name ? 'border-red-500' : ''}
            />
            {errors.name && (
              <p className="text-sm text-red-500">{errors.name}</p>
            )}
          </div>

          {/* 联系电话 */}
          <div className="space-y-2">
            <Label htmlFor="phone">联系电话 *</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="phone"
                type="tel"
                value={formData.phone}
                onChange={(e) => handleInputChange('phone', e.target.value)}
                placeholder="请输入联系电话"
                maxLength={11}
                className={`pl-10 ${errors.phone ? 'border-red-500' : ''}`}
              />
            </div>
            {errors.phone && (
              <p className="text-sm text-red-500">{errors.phone}</p>
            )}
          </div>

          {/* 店铺地址 */}
          <div className="space-y-2">
            <Label htmlFor="address">店铺地址 *</Label>
            <div className="relative">
              <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <Input
                id="address"
                value={formData.address}
                onChange={(e) => handleInputChange('address', e.target.value)}
                placeholder="请输入店铺地址"
                maxLength={100}
                className={`pl-10 ${errors.address ? 'border-red-500' : ''}`}
              />
            </div>
            {errors.address && (
              <p className="text-sm text-red-500">{errors.address}</p>
            )}
          </div>

          {/* 店铺简介 */}
          <div className="space-y-2">
            <Label htmlFor="description">店铺简介</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => handleInputChange('description', e.target.value)}
              placeholder="介绍一下您的店铺特色..."
              maxLength={200}
              rows={3}
            />
            <p className="text-xs text-gray-400 text-right">
              {formData.description.length}/200
            </p>
          </div>

          {/* 店铺公告 */}
          <div className="space-y-2">
            <Label htmlFor="notice">店铺公告</Label>
            <Textarea
              id="notice"
              value={formData.notice}
              onChange={(e) => handleInputChange('notice', e.target.value)}
              placeholder="发布店铺公告，如营业时间调整、特殊说明等"
              maxLength={200}
              rows={3}
            />
            <p className="text-xs text-gray-400 text-right">
              {formData.notice.length}/200
            </p>
          </div>
        </Card>

        {/* 营业设置 */}
        <Card className="p-6 space-y-4">
          <h3 className="text-base font-semibold">营业设置</h3>

          {/* 营业状态 */}
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>营业状态</Label>
              <p className="text-sm text-gray-500">
                关闭后用户将无法下单
              </p>
            </div>
            <Switch
              checked={formData.isOpen}
              onCheckedChange={(checked) => handleInputChange('isOpen', checked)}
            />
          </div>

          <Separator />

          {/* 营业时间 */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <Label>营业时间</Label>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={addTimeSlot}
                className="flex items-center gap-1"
              >
                <Plus className="w-4 h-4" />
                添加时间段
              </Button>
            </div>
            
            {errors.businessHours && (
              <p className="text-sm text-red-500">{errors.businessHours}</p>
            )}

            <div className="space-y-2">
              {businessHours.map((slot) => (
                <div key={slot.id} className="flex items-center gap-2">
                  <Clock className="w-5 h-5 text-gray-400" />
                  <Input
                    type="time"
                    value={slot.startTime}
                    onChange={(e) => updateTimeSlot(slot.id, 'startTime', e.target.value)}
                    className="flex-1"
                  />
                  <span className="text-gray-500">-</span>
                  <Input
                    type="time"
                    value={slot.endTime}
                    onChange={(e) => updateTimeSlot(slot.id, 'endTime', e.target.value)}
                    className="flex-1"
                  />
                  {businessHours.length > 1 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeTimeSlot(slot.id)}
                      className="text-red-500 hover:text-red-600 hover:bg-red-50"
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          </div>

          <Separator />

          {/* 起送金额 */}
          <div className="space-y-2">
            <Label htmlFor="minOrder">起送金额(元)</Label>
            <Input
              id="minOrder"
              type="number"
              min="0"
              step="0.01"
              value={formData.minOrder}
              onChange={(e) => handleInputChange('minOrder', parseFloat(e.target.value) || 0)}
              className={errors.minOrder ? 'border-red-500' : ''}
            />
            {errors.minOrder && (
              <p className="text-sm text-red-500">{errors.minOrder}</p>
            )}
          </div>

          {/* 打包费 */}
          <div className="space-y-2">
            <Label htmlFor="packagingFee">打包费(元)</Label>
            <Input
              id="packagingFee"
              type="number"
              min="0"
              step="0.01"
              value={formData.packagingFee}
              onChange={(e) => handleInputChange('packagingFee', parseFloat(e.target.value) || 0)}
              className={errors.packagingFee ? 'border-red-500' : ''}
            />
            {errors.packagingFee && (
              <p className="text-sm text-red-500">{errors.packagingFee}</p>
            )}
          </div>
        </Card>

        {/* 温馨提示 */}
        <Card className="p-4 bg-blue-50 border-blue-200">
          <div className="flex gap-3">
            <Info className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
            <div className="space-y-2 text-sm text-blue-800">
              <p className="font-medium">温馨提示</p>
              <ul className="space-y-1 list-disc list-inside">
                <li>修改店铺信息后，请点击右上角"保存"按钮</li>
                <li>营业时间调整会影响用户下单，请谨慎操作</li>
                <li>关闭营业状态后，用户将无法下单</li>
                <li>建议及时更新店铺公告，告知用户最新信息</li>
              </ul>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
