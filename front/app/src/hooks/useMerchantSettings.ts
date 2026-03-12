import { useState, useEffect, useCallback } from 'react';
import {
  getCurrentMerchantInfo,
  updateMerchantInfo,
  getBusinessHours,
  setBusinessHours as setBusinessHoursAPI,
  type MerchantSettings,
  type BusinessHoursSlot,
  type UpdateMerchantSettingsRequest,
} from '@/api/merchant';
import { uploadFile, type UploadResponse } from '@/api/file';
import { toast } from '@/lib/toast';

// 星期几的映射
const DAY_OF_WEEK_MAP: Record<number, string> = {
  1: '周一',
  2: '周二',
  3: '周三',
  4: '周四',
  5: '周五',
  6: '周六',
  7: '周日',
};

// 将后端营业时间格式转换为前端时间段格式
function convertBusinessHoursToTimeSlots(hours: BusinessHoursSlot[] | undefined): TimeSlot[] {
  if (!hours || hours.length === 0) {
    // 默认时间段
    return [
      { id: '1', startTime: '06:30', endTime: '09:00' },
      { id: '2', startTime: '10:30', endTime: '13:30' },
      { id: '3', startTime: '16:30', endTime: '19:30' },
    ];
  }
  
  // 只取营业的时间段
  const openHours = hours.filter(h => h.isOpen);
  
  if (openHours.length === 0) {
    return [{ id: Date.now().toString(), startTime: '08:00', endTime: '20:00' }];
  }
  
  // 按开始时间排序
  openHours.sort((a, b) => a.startTime.localeCompare(b.startTime));
  
  return openHours.map((h, index) => ({
    id: (index + 1).toString(),
    startTime: h.startTime,
    endTime: h.endTime,
  }));
}

// 将前端时间段格式转换为后端营业时间格式
function convertTimeSlotsToBusinessHours(slots: TimeSlot[]): BusinessHoursSlot[] {
  // 创建一个包含所有星期的基础配置
  const baseHours: BusinessHoursSlot[] = Array.from({ length: 7 }, (_, i) => ({
    dayOfWeek: i + 1,
    startTime: '00:00',
    endTime: '00:00',
    isOpen: false,
  }));
  
  // 如果有时间段，应用到所有天（简化处理）
  if (slots.length > 0) {
    // 取第一个时间段作为主要营业时间
    const mainSlot = slots[0];
    baseHours.forEach(h => {
      h.startTime = mainSlot.startTime;
      h.endTime = mainSlot.endTime;
      h.isOpen = true;
    });
  }
  
  return baseHours;
}

interface TimeSlot {
  id: string;
  startTime: string;
  endTime: string;
}

interface MerchantFormData {
  name: string;
  avatar: string;
  phone: string;
  address: string;
  description: string;
  notice: string;
  minOrder: number;
  packagingFee: number;
  isOpen: boolean;
}

export function useMerchantSettings() {
  const [settings, setSettings] = useState<MerchantSettings | null>(null);
  const [formData, setFormData] = useState<MerchantFormData>({
    name: '',
    avatar: '',
    phone: '',
    address: '',
    description: '',
    notice: '',
    minOrder: 0,
    packagingFee: 0,
    isOpen: true,
  });
  const [businessHours, setBusinessHours] = useState<TimeSlot[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  // 获取商家信息
  const fetchMerchantInfo = useCallback(async () => {
    setIsLoading(true);
    try {
      const [merchantInfo, hours] = await Promise.all([
        getCurrentMerchantInfo(),
        getBusinessHours(),
      ]);
      
      setSettings(merchantInfo);
      setFormData({
        name: merchantInfo.name || '',
        avatar: merchantInfo.logo || '',
        phone: merchantInfo.phone || '',
        address: merchantInfo.address || '',
        description: merchantInfo.description || '',
        notice: merchantInfo.notice || '',
        minOrder: merchantInfo.minPrice || 0,
        packagingFee: merchantInfo.packagingFee || 0,
        isOpen: merchantInfo.status === 1,
      });
      setBusinessHours(convertBusinessHoursToTimeSlots(hours));
    } catch (error) {
      console.error('获取商家信息失败:', error);
      toast.error('获取商家信息失败');
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 上传头像
  const uploadAvatar = useCallback(async (file: File): Promise<string | null> => {
    setIsUploading(true);
    try {
      const response: UploadResponse = await uploadFile(file, 1); // 1 = 图片类型
      toast.success('头像上传成功');
      return response.fileUrl;
    } catch (error) {
      console.error('上传头像失败:', error);
      toast.error('上传头像失败');
      return null;
    } finally {
      setIsUploading(false);
    }
  }, []);

  // 保存商家信息
  const saveSettings = useCallback(async (data: MerchantFormData, timeSlots: TimeSlot[]): Promise<boolean> => {
    setIsSaving(true);
    try {
      // 更新商家基本信息
      const updateData: UpdateMerchantSettingsRequest = {
        name: data.name,
        logo: data.avatar,
        phone: data.phone,
        address: data.address,
        description: data.description,
        notice: data.notice,
        minPrice: data.minOrder,
        packagingFee: data.packagingFee,
        status: data.isOpen ? 1 : 0,
      };
      
      await updateMerchantInfo(updateData);
      
      // 更新营业时间
      const businessHoursData = convertTimeSlotsToBusinessHours(timeSlots);
      await setBusinessHoursAPI(businessHoursData);
      
      toast.success('保存成功');
      return true;
    } catch (error) {
      console.error('保存商家信息失败:', error);
      toast.error('保存失败，请重试');
      return false;
    } finally {
      setIsSaving(false);
    }
  }, []);

  // 更新表单数据
  const updateFormData = useCallback((field: keyof MerchantFormData, value: string | number | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  }, []);

  // 添加营业时间段
  const addTimeSlot = useCallback(() => {
    setBusinessHours(prev => [
      ...prev,
      {
        id: Date.now().toString(),
        startTime: '08:00',
        endTime: '20:00',
      },
    ]);
  }, []);

  // 删除营业时间段
  const removeTimeSlot = useCallback((id: string) => {
    setBusinessHours(prev => prev.filter(slot => slot.id !== id));
  }, []);

  // 更新营业时间段
  const updateTimeSlot = useCallback((id: string, field: 'startTime' | 'endTime', value: string) => {
    setBusinessHours(prev =>
      prev.map(slot => (slot.id === id ? { ...slot, [field]: value } : slot))
    );
  }, []);

  return {
    settings,
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
  };
}
