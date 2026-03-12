import { toast as sonnerToast } from 'sonner'

// Toast 封装
export const toast = {
  success: (message: string) => {
    sonnerToast.success(message, {
      duration: 2000,
    })
  },
  error: (message: string) => {
    sonnerToast.error(message, {
      duration: 3000,
    })
  },
  warning: (message: string) => {
    sonnerToast.warning(message, {
      duration: 2500,
    })
  },
  info: (message: string) => {
    sonnerToast.info(message, {
      duration: 2000,
    })
  },
  loading: (message: string) => {
    return sonnerToast.loading(message)
  },
  dismiss: (toastId?: string | number) => {
    sonnerToast.dismiss(toastId)
  },
}
