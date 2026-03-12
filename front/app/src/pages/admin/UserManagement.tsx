import { useState } from 'react';
import { Search, Filter, MoreHorizontal, UserCheck, UserX, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { useAdminUsers, useToggleUserStatus } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { toast } from '@/lib/toast';

export function UserManagement() {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(1);
  
  const { users, loading, error, total, refresh } = useAdminUsers({
    page,
    pageSize: 10,
    keyword: searchQuery || undefined,
  });
  
  const { toggleStatus, loading: toggleLoading } = useToggleUserStatus();

  const handleToggleStatus = async (userId: number, currentStatus: number) => {
    const newStatus = currentStatus === 1 ? 0 : 1;
    const success = await toggleStatus(userId, newStatus);
    if (success) {
      toast.success(newStatus === 1 ? '用户已启用' : '用户已禁用');
      refresh();
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">用户管理</h1>
          <p className="text-gray-500 mt-1">共 {total} 位用户</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="搜索用户手机号或昵称..."
              className="pl-10 w-64"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <Button variant="outline" size="icon">
            <Filter className="w-4 h-4" />
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>用户列表</CardTitle>
        </CardHeader>
        <CardContent>
          {users.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500">暂无用户数据</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="text-left py-3 px-4 font-medium text-gray-500">用户信息</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">手机号</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">类型</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">状态</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">注册时间</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {users.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50">
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-3">
                          <Avatar className="w-10 h-10">
                            <AvatarImage src={user.avatar} />
                            <AvatarFallback>{user.nickname?.[0] || user.phone?.[0]}</AvatarFallback>
                          </Avatar>
                          <div>
                            <p className="font-medium text-gray-900">{user.nickname || '未设置昵称'}</p>
                            <p className="text-sm text-gray-500">ID: {user.id}</p>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-gray-600">{user.phone}</td>
                      <td className="py-4 px-4">
                        <Badge variant={user.userType === 1 ? 'default' : 'secondary'}>
                          {user.userType === 0 ? '普通用户' : user.userType === 1 ? '商家' : '管理员'}
                        </Badge>
                      </td>
                      <td className="py-4 px-4">
                        <Badge className={user.status === 1 ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'}>
                          {user.status === 1 ? '正常' : '禁用'}
                        </Badge>
                      </td>
                      <td className="py-4 px-4 text-gray-500">
                        {user.registerTime ? new Date(user.registerTime).toLocaleDateString() : '-'}
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleToggleStatus(user.id, user.status)}
                            disabled={toggleLoading}
                          >
                            {user.status === 1 ? (
                              <span className="flex items-center">
                                <UserX className="w-4 h-4 mr-1" />
                                禁用
                              </span>
                            ) : (
                              <span className="flex items-center">
                                <UserCheck className="w-4 h-4 mr-1" />
                                启用
                              </span>
                            )}
                          </Button>
                          <Button variant="ghost" size="icon">
                            <MoreHorizontal className="w-4 h-4" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {total > 10 && (
        <div className="flex items-center justify-between">
          <p className="text-sm text-gray-500">
            显示第 {(page - 1) * 10 + 1} 到 {Math.min(page * 10, total)} 条，共 {total} 条
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
            >
              <ChevronLeft className="w-4 h-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => p + 1)}
              disabled={page * 10 >= total}
            >
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
