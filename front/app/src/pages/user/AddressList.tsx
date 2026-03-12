import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Phone, User, Plus, Edit2, Trash2, Check, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { getUserAddresses, deleteAddress, setDefaultAddress, type Address } from '@/api/address';

export function AddressList() {
  const navigate = useNavigate();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);

  const loadAddresses = async () => {
    try {
      setLoading(true);
      const data = await getUserAddresses();
      setAddresses(data || []);
    } catch (error) {
      const err = error as Error & { message?: string };
      toast.error(err.message || '加载地址失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAddresses();
  }, []);

  const handleDelete = async (addressId: number) => {
    if (!confirm('确定要删除这个地址吗？')) return;

    try {
      await deleteAddress(addressId);
      toast.success('删除成功');
      loadAddresses();
    } catch (error) {
      const err = error as Error & { message?: string };
      toast.error(err.message || '删除失败');
    }
  };

  const handleSetDefault = async (addressId: number) => {
    try {
      await setDefaultAddress(addressId);
      toast.success('设置成功');
      loadAddresses();
    } catch (error) {
      const err = error as Error & { message?: string };
      toast.error(err.message || '设置失败');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* 头部 */}
      <div className="bg-white sticky top-0 z-10 border-b">
        <div className="flex items-center p-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="text-lg font-semibold flex-1 text-center">收货地址</h1>
          <div className="w-10" />
        </div>
      </div>

      {/* 地址列表 */}
      <div className="p-4 space-y-4">
        {addresses.length === 0 ? (
          <div className="text-center py-12">
            <MapPin className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500 mb-4">还没有收货地址</p>
            <Button
              className="bg-orange-500 hover:bg-orange-600"
              onClick={() => navigate('/address/edit')}
            >
              <Plus className="w-4 h-4 mr-2" />
              添加地址
            </Button>
          </div>
        ) : (
          addresses.map((address) => (
            <Card key={address.id} className={`p-4 ${address.isDefault ? 'border-orange-500 border-2' : ''}`}>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  {/* 联系人信息 */}
                  <div className="flex items-center gap-4 mb-2">
                    <div className="flex items-center gap-1">
                      <User className="w-4 h-4 text-gray-400" />
                      <span className="font-medium">{address.contactName}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Phone className="w-4 h-4 text-gray-400" />
                      <span className="text-gray-600">{address.contactPhone}</span>
                    </div>
                  </div>

                  {/* 地址信息 */}
                  <div className="flex items-start gap-1 text-gray-700">
                    <MapPin className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                    <div>
                      {address.province && address.city && address.district && (
                        <span>{address.province} {address.city} {address.district} </span>
                      )}
                      <span>{address.detail}</span>
                    </div>
                  </div>

                  {/* 默认标签 */}
                  {address.isDefault === 1 && (
                    <div className="mt-2">
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-orange-100 text-orange-600 text-xs rounded">
                        <Check className="w-3 h-3" />
                        默认地址
                      </span>
                    </div>
                  )}
                </div>

                {/* 操作按钮 */}
                <div className="flex items-center gap-2 ml-4">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8"
                    onClick={() => navigate(`/address/edit/${address.id}`)}
                  >
                    <Edit2 className="w-4 h-4 text-gray-500" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8"
                    onClick={() => handleDelete(address.id)}
                  >
                    <Trash2 className="w-4 h-4 text-red-500" />
                  </Button>
                </div>
              </div>

              {/* 底部操作 */}
              {address.isDefault !== 1 && (
                <div className="mt-4 pt-4 border-t flex justify-end">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleSetDefault(address.id)}
                  >
                    设为默认
                  </Button>
                </div>
              )}
            </Card>
          ))
        )}
      </div>

      {/* 底部添加按钮 */}
      {addresses.length > 0 && (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-4">
          <Button
            className="w-full bg-orange-500 hover:bg-orange-600"
            onClick={() => navigate('/address/edit')}
          >
            <Plus className="w-4 h-4 mr-2" />
            添加新地址
          </Button>
        </div>
      )}
    </div>
  );
}
