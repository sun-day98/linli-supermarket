/** 与后端 VO / DTO 字段一一对应的 TypeScript 镜像类型。 */

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
  createdAt: string
  reviews?: Review[]
}

export interface Review {
  id: number
  userId: number
  productId: number
  orderId: number
  rating: number
  content: string
  images: string
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

export interface AddressDTO {
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

export interface CartDTO {
  productId: number
  quantity: number
  selected?: number
}

export interface CheckoutItem {
  productId: number
  quantity: number
}

export interface CheckoutDTO {
  items: CheckoutItem[]
  addressId: number
  couponId?: number | null
  userCouponId?: number | null
  remark?: string
  expectedPayAmount?: number
}

export interface CheckoutPreviewVO {
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  freeDelivery: boolean
  userCouponId: number | null
  couponName: string | null
  deliverable: boolean
  district: string | null
  items: Array<{
    productId: number
    productName: string
    productImage: string
    price: number
    quantity: number
    totalAmount: number
  }>
}

export interface OrderItemVO {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  totalAmount: number
  createdAt: string
}

/** status: 0待付款 1待配送 2配送中 3已完成 4已取消 */
export interface OrderVO {
  id: number
  orderNo: string
  userId: number
  addressId: number
  receiver: string
  phone: string
  addressDetail: string
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  couponId: number | null
  userCouponId: number | null
  payType: string | null
  status: number
  statusText: string
  deliverymanId: number | null
  payTime: string | null
  deliveryTime: string | null
  finishTime: string | null
  cancelTime: string | null
  remark: string
  createdAt: string
  items: OrderItemVO[]
}

export interface CouponVO {
  id: number
  name: string
  type: number
  typeName: string
  discount: number
  threshold: number
  validType: number
  startTime: string | null
  endTime: string | null
  validDays: number
  scope: number
  total: number
  issued: number
  newUserAuto: number
  status: number
  userCouponId: number | null
  userCouponStatus: number | null
  getTime: string | null
  useTime: string | null
  usable: boolean
  unusableReason: string | null
}

export interface UserVO {
  id: number
  phone: string
  nickname: string
  avatar: string
  gender: number
  points: number
  status: number
  token?: string
  createdAt?: string
}

export interface PointsRecord {
  id: number
  userId: number
  type: number
  points: number
  balance: number
  source: string
  orderId: number
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
  logo: string
}

export interface LoginDTO {
  phone: string
  code: string
}

export interface ProductQuery {
  categoryId?: number
  keyword?: string
  status?: number
  isHot?: number
  lowStock?: number
  current?: number
  size?: number
}

export interface OrderQuery {
  status?: number
  keyword?: string
  current?: number
  size?: number
}

export const ORDER_STATUS_TEXT: Record<number, string> = {
  0: '待付款',
  1: '待配送',
  2: '配送中',
  3: '已完成',
  4: '已取消',
}

export const COUPON_STATUS_TEXT: Record<number, string> = {
  0: '未使用',
  1: '已使用',
  2: '已过期',
  3: '锁定中',
}

export const POINTS_TYPE_TEXT: Record<number, string> = {
  1: '获取',
  2: '使用',
  3: '回滚',
}

export function orderStatusText(status: number | undefined): string {
  return status === undefined ? '未知' : ORDER_STATUS_TEXT[status] ?? '未知'
}

export function couponStatusText(status: number | null | undefined): string {
  return status === null || status === undefined ? '—' : COUPON_STATUS_TEXT[status] ?? '未知'
}
