/** 管理后台使用的后端 VO / DTO TypeScript 镜像。 */

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

export interface AdminLoginDTO {
  username: string
  password: string
}

export interface AdminProfile {
  token?: string
  username: string
  realName: string
  role: string
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
}

export interface ProductDTO {
  categoryId: number
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

export interface OrderQuery {
  status?: number
  keyword?: string
  current?: number
  size?: number
  startTime?: string
  endTime?: string
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
}

export interface CouponDTO {
  name: string
  type: number
  discount: number
  threshold: number
  validType: number
  startTime: string | null
  endTime: string | null
  validDays: number
  scope: number
  total: number
  newUserAuto: number
  status: number
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

export interface DeliveryArea {
  id: number
  parentId: number
  name: string
  type: number
  enabled: number
  hot: number
  sort: number
  children?: DeliveryArea[]
}

export interface DeliveryAreaDTO {
  parentId: number
  name: string
  type: number
  enabled: number
  hot: number
  sort: number
}

export interface Deliveryman {
  id?: number
  name: string
  phone: string
  status: number
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

export interface StatsOverviewVO {
  todaySales: number
  todayOrders: number
  avgOrderAmount: number
  newMembers: number
  pendingPayOrders: number
  deliveringOrders: number
  lowStockProducts: number
  totalMembers: number
  totalOrders: number
  totalSales: number
}

export interface StatsTrendVO {
  dates: string[]
  sales: number[]
  orders: number[]
}

/** 由 /admin/orders 聚合出的会员概览（后端演示环境未单独提供会员列表接口）。 */
export interface MemberSummary {
  userId: number
  receiver: string
  phone: string
  orderCount: number
  totalAmount: number
  lastOrderAt: string
}

export const ORDER_STATUS_TEXT: Record<number, string> = {
  0: '待付款',
  1: '待配送',
  2: '配送中',
  3: '已完成',
  4: '已取消',
}

export const ORDER_STATUS_TAG: Record<number, string> = {
  0: 'warning',
  1: 'primary',
  2: 'success',
  3: 'info',
  4: 'danger',
}

export const REVIEW_STATUS_TEXT: Record<number, string> = {
  0: '待审核',
  1: '通过',
  2: '拒绝',
}
