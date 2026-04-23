<template>
  <div class="space-y-8">

    <!-- Registrar movimiento -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
      <h2 class="text-sm font-bold uppercase tracking-widest text-[#a0a0a0] mb-6">Registrar movimiento</h2>
      <form @submit.prevent="registrar" class="space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">

          <!-- Producto -->
          <div class="sm:col-span-2">
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Producto *</label>
            <select v-model="form.productoId" required
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
              <option value="">— Seleccionar producto —</option>
              <option v-for="p in productos" :key="p.id" :value="p.id">
                {{ p.nombre }} (stock: {{ p.stock }})
              </option>
            </select>
          </div>

          <!-- Tipo -->
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Tipo *</label>
            <select v-model="form.tipoMovimiento" required
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
              <option value="">— Seleccionar tipo —</option>
              <option value="ENTRADA">ENTRADA — nueva mercancía</option>
              <option value="AJUSTE_MANUAL">AJUSTE_MANUAL — corrección</option>
              <option value="DEVOLUCION">DEVOLUCION</option>
            </select>
          </div>

          <!-- Cantidad -->
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">
              Cantidad *
              <span v-if="form.tipoMovimiento === 'AJUSTE_MANUAL'" class="normal-case text-yellow-400 ml-1">(puede ser negativa)</span>
            </label>
            <input v-model.number="form.cantidad" type="number"
              :min="form.tipoMovimiento === 'AJUSTE_MANUAL' ? undefined : 1"
              required
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
              placeholder="0" />
          </div>

          <!-- Notas -->
          <div class="sm:col-span-2">
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Notas</label>
            <textarea v-model="form.motivo" rows="2"
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
              placeholder="Observaciones opcionales..." />
          </div>
        </div>

        <p v-if="formError" class="text-[#E31837] text-sm">{{ formError }}</p>
        <p v-if="formSuccess" class="text-green-400 text-sm">{{ formSuccess }}</p>

        <div class="flex justify-end">
          <button type="submit" :disabled="saving"
            class="bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white px-6 py-3 rounded-xl text-sm font-semibold transition-colors">
            {{ saving ? 'Registrando...' : 'Registrar movimiento' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Historial -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div class="flex items-center justify-between px-6 py-4 border-b border-[#2a2a2a]">
        <h2 class="text-sm font-bold uppercase tracking-widest text-[#a0a0a0]">Historial de movimientos</h2>
        <button @click="cargarHistorial(0)" class="text-[#a0a0a0] hover:text-white text-xs transition-colors">
          ↻ Actualizar
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loadingHistorial" class="p-8">
        <div class="space-y-3">
          <div v-for="i in 5" :key="i" class="h-12 bg-[#2a2a2a] rounded-lg animate-pulse" />
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="historialError" class="p-8 text-center">
        <p class="text-[#a0a0a0] text-sm">{{ historialError }}</p>
      </div>

      <!-- Empty -->
      <div v-else-if="movimientos.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">📋</p>
        <p class="text-[#a0a0a0]">No hay movimientos registrados.</p>
      </div>

      <!-- Desktop table -->
      <div v-else class="overflow-x-auto hidden md:block">
        <table class="w-full">
          <thead class="border-b border-[#2a2a2a]">
            <tr class="text-left">
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Producto</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Tipo</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Cantidad</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Stock</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Notas</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Usuario</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#2a2a2a]">
            <tr v-for="m in movimientos" :key="m.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
              <td class="px-6 py-4 text-[#a0a0a0] text-xs whitespace-nowrap">{{ formatFecha(m.fecha) }}</td>
              <td class="px-6 py-4 text-white text-sm font-medium">{{ m.productoNombre || '—' }}</td>
              <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="tipoBadge(m.tipoMovimiento)">
                  {{ m.tipoMovimiento }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm font-bold" :class="m.cantidad >= 0 ? 'text-green-400' : 'text-red-400'">
                {{ m.cantidad >= 0 ? '+' : '' }}{{ m.cantidad }}
              </td>
              <td class="px-6 py-4 text-sm text-[#a0a0a0] whitespace-nowrap">
                {{ m.stockAnterior }} → <span class="text-white font-semibold">{{ m.stockNuevo }}</span>
              </td>
              <td class="px-6 py-4 text-[#a0a0a0] text-xs max-w-[160px] truncate">{{ m.motivo || '—' }}</td>
              <td class="px-6 py-4 text-[#a0a0a0] text-xs">Sistema</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile cards -->
      <div v-if="!loadingHistorial && movimientos.length > 0" class="md:hidden divide-y divide-[#2a2a2a]">
        <div v-for="m in movimientos" :key="m.id" class="px-5 py-4 space-y-2">
          <div class="flex items-center justify-between">
            <span class="text-white text-sm font-semibold">{{ m.productoNombre || '—' }}</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="tipoBadge(m.tipoMovimiento)">{{ m.tipoMovimiento }}</span>
          </div>
          <div class="flex items-center gap-4 text-xs">
            <span :class="m.cantidad >= 0 ? 'text-green-400 font-bold' : 'text-red-400 font-bold'">
              {{ m.cantidad >= 0 ? '+' : '' }}{{ m.cantidad }}
            </span>
            <span class="text-[#a0a0a0]">{{ m.stockAnterior }} → <span class="text-white">{{ m.stockNuevo }}</span></span>
            <span class="text-[#a0a0a0]">{{ formatFecha(m.fecha) }}</span>
          </div>
          <p v-if="m.motivo" class="text-[#a0a0a0] text-xs">{{ m.motivo }}</p>
        </div>
      </div>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-[#2a2a2a]">
        <button @click="cargarHistorial(historialPage - 1)" :disabled="historialPage === 0"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
        <span class="text-sm text-[#a0a0a0]">Página {{ historialPage + 1 }} de {{ totalPages }}</span>
        <button @click="cargarHistorial(historialPage + 1)" :disabled="historialPage >= totalPages - 1"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

// ── Productos para el select ──────────────────────────────────────────────────
const productos = ref([])

async function cargarProductos() {
  try {
    const { data } = await api.get('/admin/productos?page=0&size=100')
    productos.value = data.data?.content || []
  } catch (e) {
    console.error('Error cargando productos', e)
  }
}

// ── Formulario ────────────────────────────────────────────────────────────────
const form = ref({ productoId: '', tipoMovimiento: '', cantidad: '', motivo: '' })
const saving = ref(false)
const formError = ref('')
const formSuccess = ref('')

async function registrar() {
  formError.value = ''
  formSuccess.value = ''
  saving.value = true
  try {
    const payload = {
      productoId: form.value.productoId,
      tipoMovimiento: form.value.tipoMovimiento,
      cantidad: Number(form.value.cantidad),
      motivo: form.value.motivo || null,
    }
    await api.post('/admin/inventario/movimientos', payload)
    formSuccess.value = 'Movimiento registrado correctamente.'
    form.value = { productoId: '', tipoMovimiento: '', cantidad: '', motivo: '' }
    await Promise.all([cargarProductos(), cargarHistorial(0)])
  } catch (e) {
    formError.value = e.response?.data?.message || 'Error al registrar el movimiento.'
  } finally {
    saving.value = false
  }
}

// ── Historial ─────────────────────────────────────────────────────────────────
const movimientos = ref([])
const loadingHistorial = ref(true)
const historialError = ref('')
const historialPage = ref(0)
const totalPages = ref(0)

async function cargarHistorial(page = 0) {
  historialPage.value = page
  loadingHistorial.value = true
  historialError.value = ''
  try {
    const { data } = await api.get(`/admin/inventario/movimientos?page=${page}&size=30`)
    movimientos.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (e) {
    historialError.value = e.response?.data?.message || 'No se pudo cargar el historial.'
  } finally {
    loadingHistorial.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function tipoBadge(tipo) {
  const map = {
    ENTRADA: 'bg-green-500/20 text-green-400',
    AJUSTE_MANUAL: 'bg-yellow-500/20 text-yellow-400',
    DEVOLUCION: 'bg-blue-500/20 text-blue-400',
    VENTA: 'bg-[#2a2a2a] text-[#a0a0a0]',
    AJUSTE_MANTENIMIENTO: 'bg-purple-500/20 text-purple-400',
  }
  return map[tipo] || 'bg-[#2a2a2a] text-[#a0a0a0]'
}

function formatFecha(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(iso))
}

onMounted(() => {
  Promise.all([cargarProductos(), cargarHistorial(0)])
})
</script>
