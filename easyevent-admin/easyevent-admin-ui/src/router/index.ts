import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import EventList from '@/views/EventList.vue'
import Dashboard from '@/views/Dashboard.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/events',
    name: 'EventList',
    component: EventList
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
