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

  // ── AUTH ──────────────────────────────────────────────────────────────────
  { path: '/login',          name: 'login',          component: () => import('@/views/auth/LoginView.vue') },
  { path: '/staff/login',    name: 'staff-login',    component: () => import('@/views/auth/StaffLoginView.vue') },
  { path: '/registro',       name: 'registro',       component: () => import('@/views/auth/RegistroView.vue') },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/auth/ResetPasswordView.vue') },

  // ── CLIENTE ──────────────────────────────────────────────────────────────
  {
    path: '/mi-cuenta',
    component: () => import('@/layouts/ClienteLayout.vue'),
    meta: { requiresAuth: true, role: 'CLIENTE' },
    children: [
      { path: '', redirect: '/mi-cuenta/pedidos' },
      { path: 'pedidos', name: 'mis-pedidos', component: () => import('@/views/cliente/MisPedidosView.vue') },
      { path: 'mantenimientos', name: 'mis-mantenimientos', component: () => import('@/views/cliente/MisMantenimientosView.vue') },
      { path: 'mis-bicicletas', name: 'mis-bicicletas', component: () => import('@/views/cliente/MisBicicletasView.vue') },
    ]
  },

  // ── ADMIN / EMPLEADO ─────────────────────────────────────────────────────
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'STAFF' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'productos', name: 'admin-productos', component: () => import('@/views/admin/ProductosView.vue') },
      { path: 'categorias', name: 'admin-categorias', component: () => import('@/views/admin/CategoriasView.vue') },
      { path: 'pedidos', name: 'admin-pedidos', component: () => import('@/views/admin/PedidosView.vue') },
      { path: 'mantenimientos', name: 'admin-mantenimientos', component: () => import('@/views/admin/MantenimientosView.vue') },
      { path: 'inventario', name: 'admin-inventario', component: () => import('@/views/admin/InventarioView.vue') },
      { path: 'facturacion', name: 'admin-facturacion', component: () => import('@/views/admin/FacturacionView.vue') },
      { path: 'ventas',     name: 'admin-ventas',      component: () => import('@/views/admin/VentasView.vue') },
      { path: 'novedades', name: 'admin-novedades', component: () => import('@/views/admin/NovedadesView.vue') },
      { path: 'cupones', name: 'admin-cupones', component: () => import('@/views/admin/CuponesView.vue') },
      {
        path: 'empleados',
        name: 'admin-empleados',
        component: () => import('@/views/admin/EmpleadosView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'empleados/:id',
        name: 'admin-empleado-detalle',
        component: () => import('@/views/admin/EmpleadoDetalleView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      { path: 'agenda', name: 'admin-agenda', component: () => import('@/views/admin/AgendaView.vue') },
      {
        path: 'reportes',
        name: 'admin-reportes',
        component: () => import('@/views/admin/ReportesView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
    ]
  },

  // ── 404 ───────────────────────────────────────────────────────────────────
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()

  // Si abre la app en "/" decidimos a dónde mandar según estado
  if (to.name === 'home') {
    if (auth.isStaff) {
      // Tiene sesión de staff activa → dashboard
      return next({ name: 'dashboard' })
    }
    if (!auth.isLoggedIn && localStorage.getItem('lastRole') === 'STAFF') {
      // Sin sesión pero la última vez usó acceso staff → login staff
      return next({ name: 'staff-login' })
    }
    // Cliente o primera vez → tienda normal
    return next()
  }

  if (!to.meta.requiresAuth) return next()

  // Rutas de staff redirigen al login de personal, las demás al login de clientes
  const isStaffRoute = to.path.startsWith('/admin')
  if (!auth.isLoggedIn) {
    return isStaffRoute
      ? next({ name: 'staff-login' })
      : next({ name: 'login', query: { redirect: to.fullPath } })
  }

  const role = to.meta.role
  if (role === 'ADMIN' && !auth.isAdmin) return next({ name: 'dashboard' })
  if (role === 'CLIENTE' && !auth.isCliente) return next({ name: 'home' })
  if (role === 'STAFF' && !auth.isStaff) return next({ name: 'staff-login' })

  next()
})

export default router
