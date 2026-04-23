import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/staff/login', name: 'staff-login', component: () => import('@/views/auth/StaffLoginView.vue') },

  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'STAFF' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard',      name: 'dashboard',            component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'productos',      name: 'admin-productos',      component: () => import('@/views/admin/ProductosView.vue') },
      { path: 'categorias',     name: 'admin-categorias',     component: () => import('@/views/admin/CategoriasView.vue') },
      { path: 'pedidos',        name: 'admin-pedidos',        component: () => import('@/views/admin/PedidosView.vue') },
      { path: 'mantenimientos', name: 'admin-mantenimientos', component: () => import('@/views/admin/MantenimientosView.vue') },
      { path: 'inventario',     name: 'admin-inventario',     component: () => import('@/views/admin/InventarioView.vue') },
      { path: 'facturacion',    name: 'admin-facturacion',    component: () => import('@/views/admin/FacturacionView.vue') },
      { path: 'ventas',         name: 'admin-ventas',         component: () => import('@/views/admin/VentasView.vue') },
      { path: 'novedades',      name: 'admin-novedades',      component: () => import('@/views/admin/NovedadesView.vue') },
      { path: 'cupones',        name: 'admin-cupones',        component: () => import('@/views/admin/CuponesView.vue') },
      { path: 'agenda',         name: 'admin-agenda',         component: () => import('@/views/admin/AgendaView.vue') },
      {
        path: 'empleados', name: 'admin-empleados',
        component: () => import('@/views/admin/EmpleadosView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'empleados/:id', name: 'admin-empleado-detalle',
        component: () => import('@/views/admin/EmpleadoDetalleView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'reportes', name: 'admin-reportes',
        component: () => import('@/views/admin/ReportesView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
    ]
  },

  { path: '/', redirect: '/staff/login' },
  { path: '/:pathMatch(.*)*', redirect: '/staff/login' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (!to.meta.requiresAuth) return next()
  if (!auth.isLoggedIn) return next({ name: 'staff-login' })
  const role = to.meta.role
  if (role === 'ADMIN' && !auth.isAdmin) return next({ name: 'dashboard' })
  if (role === 'STAFF' && !auth.isStaff) return next({ name: 'staff-login' })
  next()
})

export default router
