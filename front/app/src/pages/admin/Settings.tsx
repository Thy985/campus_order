import { useState } from 'react';
import { Save, Bell, Shield, Database, Server } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Card } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { toast } from '@/lib/toast';

export function AdminSettings() {
  const [isSaving, setIsSaving] = useState(false);
  const [settings, setSettings] = useState({
    siteName: '校园订餐平台',
    siteDescription: '为校园师生提供便捷的在线订餐服务',
    contactEmail: 'admin@campus-order.com',
    contactPhone: '13800138000',
    maintenanceMode: false,
    allowRegistration: true,
    emailNotification: true,
    smsNotification: false,
    orderNotification: true,
    maxOrdersPerDay: 100,
    orderTimeoutMinutes: 15,
  });

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      toast.success('保存成功');
    } catch {
      toast.error('保存失败');
    } finally {
      setIsSaving(false);
    }
  };

  const handleToggle = (key: string, value: boolean) => {
    setSettings({ ...settings, [key]: value });
  };

  const handleInputChange = (key: string, value: string | number) => {
    setSettings({ ...settings, [key]: value });
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">系统设置</h1>
        <p className="text-gray-500 mt-1">配置平台各项系统参数</p>
      </div>

      <div className="flex justify-end">
        <Button onClick={handleSave} disabled={isSaving} className="flex items-center gap-2">
          <Save className="w-4 h-4" />
          {isSaving ? '保存中...' : '保存设置'}
        </Button>
      </div>

      {/* 站点信息 */}
      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Database className="w-5 h-5 text-orange-500" />
          <h3 className="text-base font-semibold">站点信息</h3>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="siteName">网站名称</Label>
            <Input
              id="siteName"
              value={settings.siteName}
              onChange={(e) => handleInputChange('siteName', e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="contactPhone">联系电话</Label>
            <Input
              id="contactPhone"
              value={settings.contactPhone}
              onChange={(e) => handleInputChange('contactPhone', e.target.value)}
            />
          </div>
          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="siteDescription">网站描述</Label>
            <Input
              id="siteDescription"
              value={settings.siteDescription}
              onChange={(e) => handleInputChange('siteDescription', e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="contactEmail">联系邮箱</Label>
            <Input
              id="contactEmail"
              type="email"
              value={settings.contactEmail}
              onChange={(e) => handleInputChange('contactEmail', e.target.value)}
            />
          </div>
        </div>
      </Card>

      {/* 系统配置 */}
      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Server className="w-5 h-5 text-orange-500" />
          <h3 className="text-base font-semibold">系统配置</h3>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="maxOrdersPerDay">每日最大订单数</Label>
            <Input
              id="maxOrdersPerDay"
              type="number"
              min="1"
              value={settings.maxOrdersPerDay}
              onChange={(e) => handleInputChange('maxOrdersPerDay', parseInt(e.target.value) || 0)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="orderTimeoutMinutes">订单超时时间(分钟)</Label>
            <Input
              id="orderTimeoutMinutes"
              type="number"
              min="1"
              value={settings.orderTimeoutMinutes}
              onChange={(e) => handleInputChange('orderTimeoutMinutes', parseInt(e.target.value) || 0)}
            />
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>维护模式</Label>
              <p className="text-sm text-gray-500">开启后用户将无法访问前台</p>
            </div>
            <Switch
              checked={settings.maintenanceMode}
              onCheckedChange={(checked) => handleToggle('maintenanceMode', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>允许用户注册</Label>
              <p className="text-sm text-gray-500">关闭后新用户无法注册账号</p>
            </div>
            <Switch
              checked={settings.allowRegistration}
              onCheckedChange={(checked) => handleToggle('allowRegistration', checked)}
            />
          </div>
        </div>
      </Card>

      {/* 通知设置 */}
      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Bell className="w-5 h-5 text-orange-500" />
          <h3 className="text-base font-semibold">通知设置</h3>
        </div>
        
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>邮件通知</Label>
              <p className="text-sm text-gray-500">发送重要通知到管理员邮箱</p>
            </div>
            <Switch
              checked={settings.emailNotification}
              onCheckedChange={(checked) => handleToggle('emailNotification', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>短信通知</Label>
              <p className="text-sm text-gray-500">发送短信通知(需要配置短信服务)</p>
            </div>
            <Switch
              checked={settings.smsNotification}
              onCheckedChange={(checked) => handleToggle('smsNotification', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label>新订单通知</Label>
              <p className="text-sm text-gray-500">商家收到新订单时发送通知</p>
            </div>
            <Switch
              checked={settings.orderNotification}
              onCheckedChange={(checked) => handleToggle('orderNotification', checked)}
            />
          </div>
        </div>
      </Card>

      {/* 安全设置 */}
      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Shield className="w-5 h-5 text-orange-500" />
          <h3 className="text-base font-semibold">安全设置</h3>
        </div>
        
        <div className="space-y-4">
          <div className="flex items-center justify-between py-3 border-b">
            <div className="space-y-1">
              <Label className="text-base">修改管理员密码</Label>
              <p className="text-sm text-gray-500">定期修改密码可以提高账户安全性</p>
            </div>
            <Button variant="outline">修改密码</Button>
          </div>

          <div className="flex items-center justify-between py-3 border-b">
            <div className="space-y-1">
              <Label className="text-base">两因素认证</Label>
              <p className="text-sm text-gray-500">为账户添加额外的安全保护</p>
            </div>
            <Switch />
          </div>

          <div className="flex items-center justify-between py-3">
            <div className="space-y-1">
              <Label className="text-base">登录日志</Label>
              <p className="text-sm text-gray-500">查看账户登录历史记录</p>
            </div>
            <Button variant="outline">查看日志</Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
