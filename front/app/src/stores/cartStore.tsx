import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Dish } from '@/types';

export interface CartItem {
  productId: number;
  merchantId: number;
  merchantName: string;
  name: string;
  price: number;
  quantity: number;
  image?: string;
  dish?: Dish;
}

interface CartState {
  cart: { items: CartItem[] };
  isLoading: boolean;
  // Actions
  addItem: (item: CartItem) => void;
  addToCart: (dish: Dish, merchantId: number, merchantName: string) => void;
  removeFromCart: (dishId: number) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  clearMerchantItems: (merchantId: number) => void;
  // Getters
  getTotalCount: () => number;
  getTotalPrice: () => number;
  getMerchantTotal: (merchantId: number) => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      cart: { items: [] },
      isLoading: false,

      addItem: (item) => {
        set((state) => {
          const existingItem = state.cart.items.find(
            (i) => i.productId === item.productId
          );

          if (existingItem) {
            return {
              cart: {
                items: state.cart.items.map((i) =>
                  i.productId === item.productId
                    ? { ...i, quantity: i.quantity + item.quantity }
                    : i
                ),
              },
            };
          }

          return { cart: { items: [...state.cart.items, item] } };
        });
      },

      addToCart: (dish, merchantId, merchantName) => {
        set((state) => {
          const item: CartItem = {
            productId: dish.id,
            merchantId,
            merchantName,
            name: dish.name,
            price: dish.price,
            quantity: 1,
            image: dish.image,
            dish,
          };

          const existingItem = state.cart.items.find(
            (i) => i.productId === item.productId
          );

          if (existingItem) {
            return {
              cart: {
                items: state.cart.items.map((i) =>
                  i.productId === item.productId
                    ? { ...i, quantity: i.quantity + 1 }
                    : i
                ),
              },
            };
          }

          return { cart: { items: [...state.cart.items, item] } };
        });
      },

      removeFromCart: (dishId) => {
        set((state) => ({
          cart: {
            items: state.cart.items.filter((i) => i.productId !== dishId),
          },
        }));
      },

      removeItem: (productId) => {
        set((state) => ({
          cart: {
            items: state.cart.items.filter((i) => i.productId !== productId),
          },
        }));
      },

      updateQuantity: (productId, quantity) => {
        if (quantity <= 0) {
          get().removeItem(productId);
          return;
        }
        set((state) => ({
          cart: {
            items: state.cart.items.map((i) =>
              i.productId === productId ? { ...i, quantity } : i
            ),
          },
        }));
      },

      clearCart: () => set({ cart: { items: [] } }),

      clearMerchantItems: (merchantId) => {
        set((state) => ({
          cart: {
            items: state.cart.items.filter((i) => i.merchantId !== merchantId),
          },
        }));
      },

      getTotalCount: () => {
        return get().cart.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
      },

      getTotalPrice: () => {
        return get().cart.items.reduce(
          (sum, item) => sum + (item.price || 0) * (item.quantity || 0),
          0
        );
      },

      getMerchantTotal: (merchantId) => {
        return get()
          .cart.items.filter((i) => i.merchantId === merchantId)
          .reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0);
      },
    }),
    {
      name: 'cart-storage',
      partialize: (state) => ({ cart: state.cart }),
    }
  )
);

// 兼容旧版导出
export default useCartStore;
