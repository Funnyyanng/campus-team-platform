import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人信息' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理后台' }
  },
  {
    path: '/competition/:id',
    name: 'CompetitionDetail',
    component: () => import('@/views/competition/Detail.vue'),
    meta: { title: '竞赛详情' }
  },
  {
    path: '/my-teams',
    name: 'MyTeams',
    component: () => import('@/views/user/MyTeams.vue'),
    meta: { title: '我的队伍' }
  },
  {
    path: '/my-applications',
    name: 'MyApplications',
    component: () => import('@/views/user/MyApplications.vue'),
    meta: { title: '我的申请' }
  },
  {
    path: '/team/:teamId/applications',
    name: 'TeamApplications',
    component: () => import('@/views/team/Applications.vue'),
    meta: { title: '申请管理' }
  },
  {
    path: '/team/:teamId/members',
    name: 'TeamMembers',
    component: () => import('@/views/team/Members.vue'),
    meta: { title: '成员管理' }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/message/MessageCenter.vue'),
    meta: { title: '消息中心' }
  },
  {
    path: '/talent-pool',
    name: 'TalentPool',
    component: () => import('@/views/talent/TalentPool.vue'),
    meta: { title: '人才库' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（可选，根据需求决定是否需要）
router.beforeEach((to, from, next) => {
  document.title = (to.meta.title as string) || '校园组队平台'
  next()
})

export default router
