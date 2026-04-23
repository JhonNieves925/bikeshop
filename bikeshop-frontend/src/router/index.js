import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ── PÚBLICO ──────────────────────────────────────────────────────────────
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/public/HomeView.vue') },
      { path: 'catalogo', name: 'catalogo', component: () => import('@/views/public/CatalogoView.vue') },
      { path: 'producto/:id', name: 'producto', component: () => import('@/views/public/ProductoDetalleView.vue') },
      { path: 'checkout', name: 'checkout', component: () => import('@/views/public/CheckoutView.vue') },
      { path: 'mantenimiento', name: 'mantenimiento', component: () => import('@/views/public/MantenimientoView.vue') },
      { path: 'calculadora', name: 'calculadora', component: () => import('@/views/public/CalculadoraView.vue') },
    ]
  },

  // ── AUTH CLIENTE ──────────────────────────────────────────────────────────
  { path: '/login',          name: 'login',          component: () => import('@/views/auth/LoginView.vue') },
  { path: '/registro',       name: 'registro',       component: () => import('@/views/auth/RegistroView.vue') },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/auth/ResetPasswordView.vue') },

  // ── CLIENTE ──────────────────────────────────────────────────────────────
  {
    path: '/mi-cuenta',
    component: () => import('@/layouts/ClienteLayout.vue'),
    meta: { requiresAuth: true, role: 'CLIENTE' },
    children: [
      { path: '', redirect: '/mi-cuenta/pedidos' },
      { path: 'pedidos',        name: 'mis-pedidos',        component: () => import('@/views/cliente/MisPedidosView.vue') },
      { path: 'mantenimientos', name: 'mis-mantenimientos', component: () => import('@/views/cliente/MisMantenimientosView.vue') },
      { path: 'mis-bicicletas', name: 'mis-bicicletas',     component: () => import('@/views/cliente/MisBicicletasView.vue') },
    ]
  },

  // ── 404 ───────────────────────────────────────────────────────────────────
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()

  if (!to.meta.requiresAuth) return next()

  if (!auth.isLoggedIn) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  const role = to.meta.role
  if (role === 'CLIENTE' && !auth.isCliente) return next({ name: 'home' })

  next()
})

export default router
