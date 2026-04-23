<template>
  <div class="space-y-8">
    <!-- Tarjetas resumen -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="card in cards" :key="card.label"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
        <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">{{ card.label }}</p>
        <p class="text-3xl font-black text-white">{{ card.valor }}</p>
        <p class="text-xs mt-2" :class="card.color">{{ card.sub }}</p>
      </div>
    </div>

    <!-- Pedidos y Mantenimientos -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
        <h2 class="text-sm font-bold uppercase tracking-widest text-[#a0a0a0] mb-4">Pedidos recientes</h2>
        <div v-if="loadingPedidos" class="space-y-3">
          <div v-for="i in 3" :key="i" class="h-10 bg-[#2a2a2a] rounded-lg animate-pulse" />
        </div>
        <div v-else-if="pedidos.length === 0" class="text-[#a0a0a0] text-sm py-4 text-center">
          No hay pedidos aún
        </div>
        <div v-else class="space-y-3">
          <div v-for="p in pedidos" :key="p.id" class="flex items-center justify-between text-sm">
            <span class="text-white">#{{ p.id }} — {{ p.nombreCliente }}</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoClass(p.estado)">
              {{ p.estado }}
            </span>
          </div>
        </div>
        <RouterLink to="/admin/pedidos" class="block text-[#E31837] text-xs mt-4 hover:underline">
          Ver todos →
        </RouterLink>
      </div>

      <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
        <h2 class="text-sm font-bold uppercase tracking-widest text-[#a0a0a0] mb-4">Mantenimientos hoy</h2>
        <div v-if="loadingMant" class="space-y-3">
          <div v-for="i in 3" :key="i" class="h-10 bg-[#2a2a2a] rounded-lg animate-pulse" />
        </div>
        <div v-else-if="mantenimientos.length === 0" class="text-[#a0a0a0] text-sm py-4 text-center">
          No hay citas para hoy
        </div>
        <div v-else class="space-y-3">
          <div v-for="m in mantenimientos" :key="m.id" class="flex items-center justify-between text-sm">
            <span class="text-white">{{ m.horaInicio }} — {{ m.nombreCliente }}</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoClass(m.estado)">
              {{ m.estado }}
            </span>
          </div>
        </div>
        <RouterLink to="/admin/mantenimientos" class="block text-[#E31837] text-xs mt-4 hover:underline">
          Ver calendario →
        </RouterLink>
      </div>
    </div>

    <!-- Accesos rápidos -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <RouterLink v-for="link in quickLinks" :key="link.to" :to="link.to"
        class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] rounded-xl p-5 flex flex-col items-center gap-3 transition-all group">
        <span class="text-3xl">{{ link.icon }}</span>
        <span class="text-xs font-semibold text-[#a0a0a0] group-hover:text-white transition-colors uppercase tracking-wider">
          {{ link.label }}
        </span>
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'

const pedidos = ref([])
const mantenimientos = ref([])
const loadingPedidos = ref(true)
const loadingMant = ref(true)

const cards = ref([
  { label: 'Productos', valor: '—', sub: 'en catálogo', color: 'text-[#a0a0a0]' },
  { label: 'Pedidos pendientes', valor: '—', sub: 'por atender', color: 'text-yellow-400' },
  { label: 'Citas hoy', valor: '—', sub: 'agendadas', color: 'text-blue-400' },
  { label: 'Ingresos hoy', valor: '$0', sub: 'en ventas', color: 'text-green-400' },
])

const quickLinks = [
  { to: '/admin/productos', icon: '🚲', label: 'Productos' },
  { to: '/admin/pedidos', icon: '📦', label: 'Pedidos' },
  { to: '/admin/mantenimientos', icon: '🔧', label: 'Mantenimientos' },
  { to: '/admin/inventario', icon: '📋', label: 'Inventario' },
]

function estadoClass(estado) {
  const map = {
    PENDIENTE: 'bg-yellow-500/20 text-yellow-400',
    CONFIRMADO: 'bg-blue-500/20 text-blue-400',
    EN_CURSO: 'bg-blue-500/20 text-blue-400',
    DESPACHADO: 'bg-purple-500/20 text-purple-400',
    ENTREGADO: 'bg-green-500/20 text-green-400',
    FINALIZADO: 'bg-green-500/20 text-green-400',
    CANCELADO: 'bg-red-500/20 text-red-400',
  }
  return map[estado] || 'bg-[#2a2a2a] text-[#a0a0a0]'
}

onMounted(async () => {
  const hoy = new Date().toISOString().split('T')[0]
  try {
    const [pedRes, mantRes, prodRes, repRes] = await Promise.all([
      api.get('/admin/pedidos?page=0&size=5').catch(() => ({ data: { data: { content: [] } } })),
      api.get(`/admin/mantenimientos/dia?fecha=${hoy}`).catch(() => ({ data: { data: [] } })),
      api.get('/admin/productos?page=0&size=1').catch(() => ({ data: { data: { page: { totalElements: 0 } } } })),
      api.get('/admin/reportes/diario').catch(() => ({ data: { data: null } })),
    ])
    pedidos.value = pedRes.data.data?.content || []
    mantenimientos.value = mantRes.data.data || []
    cards.value[0].valor = prodRes.data.data?.page?.totalElements ?? prodRes.data.data?.totalElements ?? '—'
    cards.value[1].valor = pedidos.value.filter(p => p.estado === 'PENDIENTE').length
    cards.value[2].valor = mantenimientos.value.length

    const reporte = repRes.data.data
    if (reporte) {
      const ingresos = Number(reporte.totalGeneral ?? 0)
      cards.value[3].valor = new Intl.NumberFormat('es-CO', {
        style: 'currency', currency: 'COP', minimumFractionDigits: 0
      }).format(ingresos)
      const nPedidos = (reporte.cantidadVentasWeb ?? 0) + (reporte.cantidadVentasPresencial ?? 0)
      const nCitas = reporte.cantidadMantenimientos ?? 0
      cards.value[3].sub = `${nPedidos} pedidos · ${nCitas} citas`
    }
  } catch (e) {
    console.error(e)
  } finally {
    loadingPedidos.value = false
    loadingMant.value = false
  }
})
</script>
