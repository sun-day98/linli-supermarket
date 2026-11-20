import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { addToCart, getCart, removeCartItem, updateCartItem } from '@/api/cart'
import { useUserStore } from './user'
import type { CartDTO, CartVO } from '@/types'

/** 购物车：所有变更先写后端，再以服务端结果刷新本地，保证三端一致。 */
export const useCartStore = defineStore('cart', () => {
  const items = ref<CartVO[]>([])

  const selectedItems = computed(() => items.value.filter((item) => item.selected === 1))
  const totalCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.subtotal, 0))
  const allSelected = computed(() => items.value.length > 0 && selectedItems.value.length === items.value.length)

  async function sync(): Promise<void> {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      items.value = []
      return
    }
    items.value = await getCart()
  }

  async function add(productId: number, quantity = 1): Promise<void> {
    await addToCart({ productId, quantity } as CartDTO)
    await sync()
  }

  async function updateQty(id: number, productId: number, quantity: number): Promise<void> {
    if (quantity < 1) return
    await updateCartItem(id, { productId, quantity, selected: 1 })
    await sync()
  }

  async function toggleSelect(id: number, productId: number, selected: boolean): Promise<void> {
    await updateCartItem(id, { productId, quantity: 1, selected: selected ? 1 : 0 })
    await sync()
  }

  async function remove(id: number): Promise<void> {
    await removeCartItem(id)
    await sync()
  }

  function clearLocal(): void {
    items.value = []
  }

  return {
    items,
    selectedItems,
    totalCount,
    totalAmount,
    allSelected,
    sync,
    add,
    updateQty,
    toggleSelect,
    remove,
    clearLocal,
  }
})
