// Hooks 统一导出
export { useScrollAnimation, useScrollTo, useScrollPosition } from './useScrollAnimation';
export { useA11y } from './useA11y';
export { SkipLink, VisuallyHidden } from './A11yComponents';
export { useIsMobile, useIsTablet, useIsDesktop, useBreakpoint, useIsTouchDevice } from './use-mobile';
export { useWebSocket } from './useWebSocket';

// 数据获取Hooks
export { useMerchants, useMerchantDetail } from './useMerchants';
export { useProducts, useProductDetail, useProductCategories } from './useProducts';
export { useOrders, useOrderDetail, useCancelOrder, useCreateOrder } from './useOrders';
export { useUser, useLogin, useRegister } from './useUser';

// 商家端Hooks
export { 
  useMerchantStats, 
  useMerchantOrders, 
  useAcceptOrder, 
  useRejectOrder, 
  useCompleteOrder 
} from './useMerchantStats';

export {
  useMerchantMenu,
  useCreateDish,
  useUpdateDish,
  useDeleteDish,
  useToggleDishAvailability,
} from './useMerchantMenu';

export {
  useMerchantSettings,
} from './useMerchantSettings';

// 管理端Hooks
export { 
  useAdminStats, 
  useAdminUsers, 
  useAdminMerchants, 
  useAdminMerchantDetail,
  useAdminOrders,
  useToggleUserStatus,
  useAuditMerchant,
  useCreateMerchant,
  useUpdateMerchant,
  useDeleteMerchant,
  useMerchantStatistics,
} from './useAdmin';

