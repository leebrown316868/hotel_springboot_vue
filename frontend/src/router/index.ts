import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/browse-rooms'
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
      meta: { requiresAuth: true }
    },
    {
      path: '/dashboard/bookings',
      name: 'dashboard-bookings',
      component: () => import('../views/DashboardBookings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('../views/Rooms.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/guests',
      name: 'guests',
      component: () => import('../views/Guests.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: () => import('../views/Analytics.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/Settings.vue'),
      meta: { requiresAuth: true }
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
      meta: { requiresAuth: true }
    },
    {
      path: '/rooms-resource',
      name: 'rooms-resource',
      component: () => import('../views/RoomsResource.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/room-types',
      name: 'room-types',
      component: () => import('../views/RoomTypes.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
    },
    {
      path: '/browse-rooms',
      name: 'browse-rooms',
      component: () => import('../views/BrowseRooms.vue'),
      meta: { public: true }
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
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true, roles: ['CUSTOMER'] }
    },
    {
      path: '/history-feedback',
      name: 'history-feedback',
      component: () => import('../views/HistoryFeedback.vue'),
      meta: { requiresAuth: true, roles: ['CUSTOMER'] }
    }
  ]
})

// Navigation guard for authentication
router.beforeEach((to) => {
  const requiresAuth = to.meta.requiresAuth
  const isPublic = to.meta.public

  // 公开页面不需要认证检查
  if (isPublic) {
    return
  }

  // 需要认证的页面
  if (requiresAuth && !isAuthenticated()) {
    return { name: 'login' }
  }

  // 已登录用户访问登录页，根据角色跳转
  if (to.path === '/login' && isAuthenticated()) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role === 'ADMIN' || user.role === 'STAFF') {
      return { name: 'dashboard' }
    } else {
      return { name: 'browse-rooms' }
    }
  }

  // 不返回任何值 = 允许导航通过
})

export default router
