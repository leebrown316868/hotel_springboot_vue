import { createRouter, createWebHistory } from 'vue-router'

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
      component: () => import('../views/Dashboard.vue')
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('../views/Rooms.vue')
    },
    {
      path: '/guests',
      name: 'guests',
      component: () => import('../views/Guests.vue')
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: () => import('../views/Analytics.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/Settings.vue')
    },
    {
      path: '/bookings/new',
      name: 'booking-wizard',
      component: () => import('../views/BookingWizard.vue')
    },
    {
      path: '/staff-bookings',
      name: 'staff-bookings',
      component: () => import('../views/StaffBookings.vue')
    },
    {
      path: '/rooms-resource',
      name: 'rooms-resource',
      component: () => import('../views/RoomsResource.vue')
    },
    {
      path: '/browse-rooms',
      name: 'browse-rooms',
      component: () => import('../views/BrowseRooms.vue')
    },
    {
      path: '/my-bookings',
      name: 'my-bookings',
      component: () => import('../views/MyBookings.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue')
    },
    {
      path: '/history-feedback',
      name: 'history-feedback',
      component: () => import('../views/HistoryFeedback.vue')
    }
  ]
})

export default router
