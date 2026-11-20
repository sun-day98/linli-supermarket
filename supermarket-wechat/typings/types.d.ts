/// <reference types="miniprogram-api-typings" />

declare global {
  interface IAppOption {
    globalData: {
      token: string
      userInfo: Record<string, unknown> | null
    }
  }
}

/** 后端统一响应包装：code=0 成功。 */
export interface Result<T> {
  code: number
  data: T
  message: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  status: number
}

export interface ProductVO {
  id: number
  categoryId: number
  categoryName?: string
  name: string
  subtitle: string
  code: string
  price: number
  originalPrice: number
  stock: number
  stockAlert: number
  sales: number
  unit: string
  spec: string
  image: string
  images: string
  description: string
  nutrition: string
  sort: number
  isHot: number
  status: number
  lowStock: boolean
}

export interface Review {
  id: number
  userId: number
  productId: number
  rating: number
  content: string
  status: number
  createdAt: string
}

export interface CartVO {
  id: number
  productId: number
  productName: string
  productImage: string
  spec: string
  price: number
  quantity: number
  selected: number
  stock: number
  subtotal: number
}

export interface Address {
  id: number
  userId: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

export interface CouponVO {
  id: number
  name: string
  type: number
  typeName: string
  discount: number
  threshold: number
  usable: boolean
  userCouponId: number | null
  userCouponStatus: number | null
}

export interface CheckoutPreviewVO {
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  freeDelivery: boolean
  deliverable: boolean
  userCouponId: number | null
  couponName: string | null
  items: Array<{
    productId: number
    productName: string
    price: number
    quantity: number
    totalAmount: number
  }>
}

export interface OrderItemVO {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  totalAmount: number
}

export interface OrderVO {
  id: number
  orderNo: string
  userId: number
  receiver: string
  phone: string
  addressDetail: string
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  status: number
  statusText: string
  payType: string | null
  createdAt: string
  payTime: string | null
  finishTime: string | null
  remark: string
  items: OrderItemVO[]
}

export interface UserVO {
  id: number
  phone: string
  nickname: string
  avatar: string
  points: number
  status: number
  token?: string
}

export interface PointsRecord {
  id: number
  type: number
  points: number
  balance: number
  source: string
  createdAt: string
}

export interface StoreSetting {
  id: number
  storeName: string
  slogan: string
  notice: string
  deliveryFee: number
  freeDeliveryThreshold: number
  servicePhone: string
}

export interface DeliveryAreaVO {
  id: number
  parentId: number
  name: string
  type: number
  enabled: number
  hot: number
  children?: DeliveryAreaVO[]
}
