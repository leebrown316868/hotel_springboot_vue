import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
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
      meta: { requiresAuth: true }
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
      path: '/browse-rooms',
      name: 'browse-rooms',
      component: () => import('../views/BrowseRooms.vue')
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

  if (requiresAuth && !isAuthenticated()) {
    return { name: 'login' }
  }
  if (to.path === '/login' && isAuthenticated()) {
    return { name: 'dashboard' }
  }
  // 不返回任何值 = 允许导航通过
})

export default router
