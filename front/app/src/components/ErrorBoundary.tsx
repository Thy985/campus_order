import { Component, ErrorInfo, ReactNode } from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
    // 可接入 Sentry 等错误监控服务
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback || (
          <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <div className="max-w-md w-full text-center">
              <div className="w-20 h-20 mx-auto mb-6 rounded-full bg-red-100 flex items-center justify-center">
                <AlertCircle className="w-10 h-10 text-red-500" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                页面出错了
              </h2>
              <p className="text-gray-500 mb-6">
                抱歉，页面加载时出现了问题。请尝试刷新页面或返回首页。
              </p>
              <div className="flex gap-3 justify-center">
                <Button
                  onClick={this.handleRetry}
                  className="h-12 px-6 rounded-xl gradient-primary text-white shadow-primary"
                >
                  <RefreshCw className="w-4 h-4 mr-2" />
                  重试
                </Button>
                <Button
                  variant="outline"
                  onClick={() => (window.location.href = '#/')}
                  className="h-12 px-6 rounded-xl border-gray-200"
                >
                  返回首页
                </Button>
              </div>
              {import.meta.env.DEV && this.state.error && (
                <div className="mt-6 p-4 bg-gray-100 rounded-lg text-left">
                  <p className="text-sm font-mono text-red-600 break-all">
                    {this.state.error.message}
                  </p>
                </div>
              )}
            </div>
          </div>
        )
      );
    }

    return this.props.children;
  }
}
