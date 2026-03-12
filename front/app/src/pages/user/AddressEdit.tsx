import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MapPin, User, Phone, ArrowLeft, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { toast } from '@/lib/toast';
import { createAddress, updateAddress, getAddressDetail, type AddressRequest } from '@/api/address';

export function AddressEdit() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [formData, setFormData] = useState<AddressRequest>({
    contactName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0,
  });

  // 加载地址详情（编辑模式）
  useEffect(() => {
    if (isEdit && id) {
      loadAddressDetail(parseInt(id));
    }
  }, [id]);

  const loadAddressDetail = async (addressId: number) => {
    try {
      setLoading(true);
      const data = await getAddressDetail(addressId);
      setFormData({
        contactName: data.contactName,
        contactPhone: data.contactPhone,
        province: data.province || '',
        city: data.city || '',
        district: data.district || '',
        detail: data.detail,
        isDefault: data.isDefault,
      });
    } catch (error) {
      const err = error as Error & { message?: string };
      toast.error(err.message || '加载地址失败');
      navigate('/address');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 表单验证
    if (!formData.contactName.trim()) {
      toast.error('请输入联系人姓名');
      return;
    }
    if (!formData.contactPhone.trim()) {
      toast.error('请输入联系人电话');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(formData.contactPhone)) {
      toast.error('手机号格式不正确');
      return;
    }
    if (!formData.detail.trim()) {
      toast.error('请输入详细地址');
      return;
    }

    try {
      setSaving(true);
      if (isEdit && id) {
        await updateAddress(parseInt(id), formData);
        toast.success('更新成功');
      } else {
        await createAddress(formData);
        toast.success('创建成功');
      }
      navigate('/address');
    } catch (error) {
      const err = error as Error & { message?: string };
      toast.error(err.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-orange-500" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 头部 */}
      <div className="bg-white sticky top-0 z-10 border-b">
        <div className="flex items-center p-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="text-lg font-semibold flex-1 text-center">
            {isEdit ? '编辑地址' : '添加地址'}
          </h1>
          <div className="w-10" />
        </div>
      </div>

      {/* 表单 */}
      <form onSubmit={handleSubmit} className="p-4">
        <Card className="p-6 space-y-6">
          {/* 联系人姓名 */}
          <div className="space-y-2">
            <Label htmlFor="contactName" className="flex items-center gap-2">
              <User className="w-4 h-4 text-gray-400" />
              联系人姓名
            </Label>
            <Input
              id="contactName"
              placeholder="请输入联系人姓名"
              value={formData.contactName}
              onChange={(e) => setFormData({ ...formData, contactName: e.target.value })}
              maxLength={50}
            />
          </div>

          {/* 联系人电话 */}
          <div className="space-y-2">
            <Label htmlFor="contactPhone" className="flex items-center gap-2">
              <Phone className="w-4 h-4 text-gray-400" />
              联系人电话
            </Label>
            <Input
              id="contactPhone"
              placeholder="请输入手机号"
              value={formData.contactPhone}
              onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
              maxLength={11}
            />
          </div>

          {/* 省市区（简化版，实际应该使用级联选择器） */}
          <div className="space-y-2">
            <Label className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-gray-400" />
              省市区
            </Label>
            <div className="grid grid-cols-3 gap-2">
              <Input
                placeholder="省"
                value={formData.province}
                onChange={(e) => setFormData({ ...formData, province: e.target.value })}
              />
              <Input
                placeholder="市"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              />
              <Input
                placeholder="区"
                value={formData.district}
                onChange={(e) => setFormData({ ...formData, district: e.target.value })}
              />
            </div>
          </div>

          {/* 详细地址 */}
          <div className="space-y-2">
            <Label htmlFor="detail">详细地址</Label>
            <textarea
              id="detail"
              placeholder="请输入详细地址，如街道、门牌号等"
              value={formData.detail}
              onChange={(e) => setFormData({ ...formData, detail: e.target.value })}
              maxLength={200}
              rows={3}
              className="w-full px-3 py-2 border rounded-md resize-none focus:outline-none focus:ring-2 focus:ring-orange-500"
            />
          </div>

          {/* 设为默认 */}
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isDefault"
              checked={formData.isDefault === 1}
              onCheckedChange={(checked) =>
                setFormData({ ...formData, isDefault: checked ? 1 : 0 })
              }
            />
            <Label htmlFor="isDefault" className="text-sm font-normal cursor-pointer">
              设为默认收货地址
            </Label>
          </div>
        </Card>

        {/* 提交按钮 */}
        <div className="mt-6">
          <Button
            type="submit"
            className="w-full bg-orange-500 hover:bg-orange-600 h-12"
            disabled={saving}
          >
            {saving ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                保存中...
              </>
            ) : (
              '保存'
            )}
          </Button>
        </div>
      </form>
    </div>
  );
}
