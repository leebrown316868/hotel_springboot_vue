import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/bookings/new'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/dashboard/bookings',
      name: 'dashboard-bookings',
      component: () => import('../views/DashboardBookings.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('../views/Rooms.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },
    {
      path: '/guests',
      name: 'guests',
      component: () => import('../views/Guests.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: () => import('../views/Analytics.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/Settings.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/bookings/new',
      name: 'booking-wizard',
      component: () => import('../views/BookingWizard.vue'),
      meta: { public: true }
    },
    {
      path: '/staff-bookings',
      name: 'staff-bookings',
      component: () => import('../views/StaffBookings.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },
    {
      path: '/rooms-resource',
      name: 'rooms-resource',
      component: () => import('../views/RoomsResource.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/room-types',
      name: 'room-types',
      component: () => import('../views/RoomTypes.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },

    {
      path: '/room-detail/:id',
      name: 'room-detail',
      component: () => import('../views/RoomDetail.vue'),
      meta: { public: true }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/About.vue'),
      meta: { public: true }
    },
    {
      path: '/my-bookings',
      name: 'my-bookings',
      component: () => import('../views/MyBookings.vue'),
      meta: { requiresAuth: true, roles: ['CUSTOMER'] }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true, roles: ['CUSTOMER', 'ADMIN', 'STAFF'] }
    },
    {
      path: '/history-feedback',
      name: 'history-feedback',
      component: () => import('../views/HistoryFeedback.vue'),
      meta: { requiresAuth: true, roles: ['CUSTOMER'] }
    },
    {
      path: '/payment/result',
      name: 'payment-result',
      component: () => import('../views/PaymentResult.vue'),
      meta: { public: true }
    }
  ]
})

// Navigation guard for authentication
router.beforeEach((to, from) => {
  const requiresAuth = to.meta.requiresAuth
  const isPublic = to.meta.public

  // 公开页面不需要认证检查
  if (isPublic) {
    return
  }

  // 需要认证的页面，如果未登录则重定向到登录页，并保存目标页面
  if (requiresAuth && !isAuthenticated()) {
    // 保存目标路由用于登录后重定向
    sessionStorage.setItem('redirect', to.fullPath)
    return { name: 'login' }
  }

  // 已登录用户访问登录页，检查是否有保存的重定向目标
  if (to.path === '/login' && isAuthenticated()) {
    const redirectPath = sessionStorage.getItem('redirect')
    if (redirectPath) {
      sessionStorage.removeItem('redirect')
      return redirectPath
    }

    // 没有保存的重定向目标，根据角色跳转
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role === 'ADMIN') {
      return { name: 'dashboard' }
    } else if (user.role === 'STAFF') {
      return { name: 'staff-bookings' }
    } else {
      return { name: 'booking-wizard' }
    }
  }

  // 角色权限检查
  if (to.meta.roles && isAuthenticated()) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const requiredRoles = to.meta.roles as string[]
    if (!requiredRoles.includes(user.role)) {
      // 权限不足，根据角色返回合适的页面
      if (user.role === 'CUSTOMER') {
        return { name: 'booking-wizard' }
      } else if (user.role === 'STAFF') {
        // STAFF 用户尝试访问无权限页面，返回 staff-bookings
        return { name: 'staff-bookings' }
      } else {
        // ADMIN 用户不应该出现这种情况，返回 dashboard
        return { name: 'dashboard' }
      }
    }
  }

  // 不返回任何值 = 允许导航通过
})

export default router
