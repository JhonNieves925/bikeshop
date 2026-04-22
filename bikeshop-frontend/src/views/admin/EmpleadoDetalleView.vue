<template>
  <div class="space-y-6">

    <!-- Loading inicial -->
    <div v-if="loading" class="space-y-4">
      <div class="h-28 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
      <div class="h-64 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
    </div>

    <template v-else-if="empleado">

      <!-- Cabecera empleado -->
      <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6 flex flex-col sm:flex-row sm:items-center gap-4">
        <div class="w-14 h-14 rounded-full bg-[#E31837]/20 flex items-center justify-center text-2xl font-black text-[#E31837] shrink-0">
          {{ (empleado.nombre || '?')[0].toUpperCase() }}
        </div>
        <div class="flex-1">
          <h2 class="text-white text-xl font-black">{{ empleado.nombre }} {{ empleado.apellido }}</h2>
          <p class="text-[#a0a0a0] text-sm">{{ empleado.email }} · {{ empleado.documento }}</p>
          <div class="flex gap-2 mt-2 flex-wrap">
            <span class="px-2 py-0.5 rounded-full text-xs font-medium"
              :class="empleado.rol === 'ADMIN' ? 'bg-[#E31837]/20 text-[#E31837]' : 'bg-blue-500/20 text-blue-400'">
              {{ empleado.rol }}
            </span>
            <span class="px-2 py-0.5 rounded-full text-xs font-medium bg-[#2a2a2a] text-[#a0a0a0]">
              Pago: {{ empleado.tipoPago?.replace('_', ' ') }}
            </span>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex gap-1 bg-[#141414] border border-[#2a2a2a] rounded-xl p-1 w-fit">
        <button v-for="tab in tabs" :key="tab.id" @click="tabActiva = tab.id"
          class="px-5 py-2 rounded-lg text-sm font-medium transition-all"
          :class="tabActiva === tab.id ? 'bg-[#E31837] text-white' : 'text-[#a0a0a0] hover:text-white'">
          {{ tab.label }}
        </button>
      </div>

      <!-- ── TAB JORNADAS ─────────────────────────────────────────── -->
      <div v-if="tabActiva === 'jornadas'" class="space-y-4">

        <!-- Acciones del día -->
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 flex flex-col sm:flex-row sm:items-center gap-4">
          <div class="flex-1">
            <p class="text-white font-semibold text-sm">Jornada de hoy</p>
            <p class="text-[#a0a0a0] text-xs mt-0.5">{{ hoy }}</p>
          </div>
          <div class="flex gap-3">
            <button @click="registrarEntrada"
              :disabled="accionando"
              class="bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
              ↩ Registrar entrada
            </button>
            <button @click="registrarSalida"
              :disabled="accionando"
              class="bg-yellow-600 hover:bg-yellow-700 disabled:opacity-50 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
              ↪ Registrar salida
            </button>
          </div>
        </div>

        <!-- Filtro de fechas -->
        <div class="flex gap-3 items-end flex-wrap">
          <div>
            <label class="block text-xs text-[#a0a0a0] mb-1">Desde</label>
            <input v-model="filtroDesde" type="date"
              class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837]" />
          </div>
          <div>
            <label class="block text-xs text-[#a0a0a0] mb-1">Hasta</label>
            <input v-model="filtroHasta" type="date"
              class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837]" />
          </div>
          <button @click="cargarJornadas"
            class="bg-[#2a2a2a] hover:bg-[#3a3a3a] text-white px-4 py-2 rounded-xl text-sm transition-colors">
            Buscar
          </button>
        </div>

        <!-- Mensaje de error/éxito -->
        <p v-if="jornadaMsg" class="text-sm" :class="jornadaError ? 'text-red-400' : 'text-green-400'">
          {{ jornadaMsg }}
        </p>

        <!-- Tabla jornadas -->
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
          <div v-if="loadingJornadas" class="p-8 text-center text-[#a0a0a0] text-sm">Cargando...</div>
          <div v-else-if="jornadas.length === 0" class="p-8 text-center text-[#a0a0a0] text-sm">
            No hay jornadas en este período.
          </div>
          <table v-else class="w-full">
            <thead class="border-b border-[#2a2a2a]">
              <tr class="text-left">
                <th class="px-5 py-3 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
                <th class="px-5 py-3 text-xs uppercase tracking-widest text-[#a0a0a0]">Entrada</th>
                <th class="px-5 py-3 text-xs uppercase tracking-widest text-[#a0a0a0]">Salida</th>
                <th class="px-5 py-3 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Horas</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-[#2a2a2a]">
              <tr v-for="j in jornadas" :key="j.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
                <td class="px-5 py-3 text-white text-sm">{{ formatFecha(j.fecha) }}</td>
                <td class="px-5 py-3 text-green-400 text-sm font-mono">{{ j.horaEntrada || '—' }}</td>
                <td class="px-5 py-3 text-yellow-400 text-sm font-mono">{{ j.horaSalida || '—' }}</td>
                <td class="px-5 py-3 text-right text-white text-sm font-semibold">
                  {{ j.horasTrabajadas != null ? j.horasTrabajadas + 'h' : '—' }}
                </td>
              </tr>
            </tbody>
            <tfoot class="border-t border-[#2a2a2a]">
              <tr>
                <td colspan="3" class="px-5 py-3 text-[#a0a0a0] text-sm">Total período</td>
                <td class="px-5 py-3 text-right text-[#E31837] font-black text-sm">{{ totalHoras }}h</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <!-- ── TAB PAGOS ────────────────────────────────────────────── -->
      <div v-if="tabActiva === 'pagos'" class="space-y-5">

        <!-- Formulario generar pago -->
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6 space-y-4">
          <h3 class="text-white font-bold text-base">Generar liquidación de pago</h3>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Desde *</label>
              <input v-model="pagoForm.fechaDesde" type="date"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837]" />
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Hasta *</label>
              <input v-model="pagoForm.fechaHasta" type="date"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837]" />
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Bonificaciones ($)</label>
              <input v-model.number="pagoForm.bonificaciones" type="number" min="0" step="1000"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837]"
                placeholder="0" />
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Deducciones ($)</label>
              <input v-model.number="pagoForm.deducciones" type="number" min="0" step="1000"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837]"
                placeholder="0" />
            </div>
            <div class="sm:col-span-2">
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Observaciones</label>
              <input v-model="pagoForm.observaciones" type="text"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837]"
                placeholder="Ej: quincena del 1 al 15" />
            </div>
          </div>

          <p v-if="pagoError" class="text-red-400 text-sm">{{ pagoError }}</p>

          <button @click="generarPago" :disabled="generando"
            class="bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white px-6 py-3 rounded-xl text-sm font-bold transition-colors">
            {{ generando ? 'Calculando...' : 'Generar liquidación' }}
          </button>
        </div>

        <!-- Historial de pagos -->
        <div class="space-y-3">
          <h3 class="text-white font-bold text-base">Historial de pagos</h3>

          <div v-if="loadingPagos" class="space-y-2">
            <div v-for="i in 3" :key="i" class="h-24 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
          </div>

          <div v-else-if="pagos.length === 0" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-8 text-center text-[#a0a0a0] text-sm">
            No hay pagos registrados para este empleado.
          </div>

          <div v-else v-for="p in pagos" :key="p.id"
            class="bg-[#1a1a1a] border rounded-xl p-5 space-y-3 transition-colors"
            :class="p.estado === 'PAGADO' ? 'border-green-800/40' : 'border-[#E31837]/30'">

            <div class="flex items-start justify-between gap-3 flex-wrap">
              <div>
                <p class="text-white font-semibold text-sm">
                  {{ formatFecha(p.fechaDesde) }} → {{ formatFecha(p.fechaHasta) }}
                </p>
                <p v-if="p.observaciones" class="text-[#a0a0a0] text-xs mt-0.5">{{ p.observaciones }}</p>
              </div>
              <span class="px-2 py-0.5 rounded-full text-xs font-bold shrink-0"
                :class="p.estado === 'PAGADO' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'">
                {{ p.estado === 'PAGADO' ? '✓ Pagado' : '⏳ Pendiente' }}
              </span>
            </div>

            <!-- Desglose -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div class="bg-[#0a0a0a] rounded-lg p-3">
                <p class="text-[#a0a0a0] text-xs">Servicios</p>
                <p class="text-white font-semibold text-sm mt-0.5">{{ p.totalServicios }}</p>
                <p class="text-[#a0a0a0] text-xs">$ {{ fmt(p.montoServicios) }}</p>
              </div>
              <div class="bg-[#0a0a0a] rounded-lg p-3">
                <p class="text-[#a0a0a0] text-xs">Horas</p>
                <p class="text-white font-semibold text-sm mt-0.5">{{ p.totalHoras }}h</p>
                <p class="text-[#a0a0a0] text-xs">$ {{ fmt(p.montoHoras) }}</p>
              </div>
              <div class="bg-[#0a0a0a] rounded-lg p-3">
                <p class="text-[#a0a0a0] text-xs">Bonif. / Deduc.</p>
                <p class="text-green-400 text-xs mt-0.5">+$ {{ fmt(p.bonificaciones) }}</p>
                <p class="text-red-400 text-xs">-$ {{ fmt(p.deducciones) }}</p>
              </div>
              <div class="bg-[#E31837]/10 border border-[#E31837]/20 rounded-lg p-3">
                <p class="text-[#a0a0a0] text-xs">TOTAL</p>
                <p class="text-[#E31837] font-black text-base mt-0.5">$ {{ fmt(p.totalPago) }}</p>
              </div>
            </div>

            <!-- Acción pagar -->
            <div v-if="p.estado !== 'PAGADO'" class="flex justify-end">
              <button @click="marcarPagado(p)" :disabled="marcando === p.id"
                class="bg-green-700 hover:bg-green-600 disabled:opacity-50 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
                {{ marcando === p.id ? 'Procesando...' : '✓ Marcar como pagado' }}
              </button>
            </div>
            <div v-else class="text-right">
              <p class="text-[#a0a0a0] text-xs">Pagado el {{ formatFechaHora(p.pagadoEn) }}</p>
            </div>

          </div>
        </div>
      </div>

    </template>

    <!-- Error carga -->
    <div v-else class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-12 text-center">
      <p class="text-[#a0a0a0]">No se pudo cargar el empleado.</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const empId = route.params.id

// ─── Estado ─────────────────────────────────────────────────────────────────
const loading = ref(true)
const empleado = ref(null)
const tabActiva = ref('jornadas')
const tabs = [
  { id: 'jornadas', label: '🕐 Jornadas' },
  { id: 'pagos',    label: '💰 Pagos' },
]

// Jornadas
const jornadas = ref([])
const loadingJornadas = ref(false)
const accionando = ref(false)
const jornadaMsg = ref('')
const jornadaError = ref(false)

const hoy = new Date().toLocaleDateString('es-CO', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

// Filtro fechas jornadas — últimos 30 días por defecto
const ahora = new Date()
const hace30 = new Date(ahora); hace30.setDate(hace30.getDate() - 30)
const filtroDesde = ref(hace30.toISOString().slice(0, 10))
const filtroHasta = ref(ahora.toISOString().slice(0, 10))

const totalHoras = computed(() =>
  jornadas.value.reduce((sum, j) => sum + (parseFloat(j.horasTrabajadas) || 0), 0).toFixed(2)
)

// Pagos
const pagos = ref([])
const loadingPagos = ref(false)
const generando = ref(false)
const marcando = ref(null)
const pagoError = ref('')

// Primer día del mes actual y hoy
const primerDiaMes = new Date(ahora.getFullYear(), ahora.getMonth(), 1).toISOString().slice(0, 10)
const pagoForm = ref({
  fechaDesde: primerDiaMes,
  fechaHasta: ahora.toISOString().slice(0, 10),
  bonificaciones: 0,
  deducciones: 0,
  observaciones: '',
})

// ─── Carga inicial ───────────────────────────────────────────────────────────
onMounted(async () => {
  try {
    const res = await api.get(`/admin/empleados/${empId}`)
    empleado.value = res.data.data
  } catch {
    empleado.value = null
  } finally {
    loading.value = false
  }
  await Promise.all([cargarJornadas(), cargarPagos()])
})

// ─── Jornadas ────────────────────────────────────────────────────────────────
async function cargarJornadas() {
  loadingJornadas.value = true
  try {
    const res = await api.get(`/admin/empleados/${empId}/jornadas`, {
      params: { desde: filtroDesde.value, hasta: filtroHasta.value }
    })
    jornadas.value = res.data.data || []
  } catch {
    jornadas.value = []
  } finally {
    loadingJornadas.value = false
  }
}

async function registrarEntrada() {
  accionando.value = true
  jornadaMsg.value = ''
  jornadaError.value = false
  try {
    await api.post(`/admin/empleados/${empId}/jornada/entrada`)
    jornadaMsg.value = 'Entrada registrada correctamente.'
    await cargarJornadas()
  } catch (e) {
    jornadaMsg.value = e.response?.data?.message || 'Error al registrar entrada.'
    jornadaError.value = true
  } finally {
    accionando.value = false
  }
}

async function registrarSalida() {
  accionando.value = true
  jornadaMsg.value = ''
  jornadaError.value = false
  try {
    await api.post(`/admin/empleados/${empId}/jornada/salida`)
    jornadaMsg.value = 'Salida registrada correctamente.'
    await cargarJornadas()
  } catch (e) {
    jornadaMsg.value = e.response?.data?.message || 'Error al registrar salida.'
    jornadaError.value = true
  } finally {
    accionando.value = false
  }
}

// ─── Pagos ───────────────────────────────────────────────────────────────────
async function cargarPagos() {
  loadingPagos.value = true
  try {
    const res = await api.get(`/admin/empleados/${empId}/pagos`)
    pagos.value = res.data.data || []
  } catch {
    pagos.value = []
  } finally {
    loadingPagos.value = false
  }
}

async function generarPago() {
  if (!pagoForm.value.fechaDesde || !pagoForm.value.fechaHasta) {
    pagoError.value = 'Las fechas son obligatorias.'
    return
  }
  pagoError.value = ''
  generando.value = true
  try {
    await api.post('/admin/empleados/pagos', {
      usuarioId: Number(empId),
      fechaDesde: pagoForm.value.fechaDesde,
      fechaHasta: pagoForm.value.fechaHasta,
      bonificaciones: pagoForm.value.bonificaciones || 0,
      deducciones: pagoForm.value.deducciones || 0,
      observaciones: pagoForm.value.observaciones || null,
    })
    await cargarPagos()
    tabActiva.value = 'pagos'
  } catch (e) {
    pagoError.value = e.response?.data?.message || 'Error al generar el pago.'
  } finally {
    generando.value = false
  }
}

async function marcarPagado(pago) {
  marcando.value = pago.id
  try {
    await api.patch(`/admin/empleados/pagos/${pago.id}/pagar`)
    await cargarPagos()
  } catch (e) {
    alert(e.response?.data?.message || 'Error al marcar como pagado.')
  } finally {
    marcando.value = null
  }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
function formatFecha(fecha) {
  if (!fecha) return '—'
  return new Date(fecha + 'T00:00:00').toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatFechaHora(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('es-CO', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function fmt(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString('es-CO')
}
</script>
