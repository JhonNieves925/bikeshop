<template>
  <div class="space-y-6">

    <!-- ── Selector de período ──────────────────────────────────────────── -->
    <div class="flex flex-col sm:flex-row sm:items-center gap-4">

      <!-- Tabs -->
      <div class="flex gap-1 bg-[#141414] border border-[#2a2a2a] rounded-xl p-1 flex-shrink-0">
        <button v-for="p in periodos" :key="p.id" @click="seleccionarPeriodo(p.id)"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-all whitespace-nowrap"
          :class="periodo === p.id ? 'bg-[#E31837] text-white' : 'text-[#a0a0a0] hover:text-white'">
          {{ p.label }}
        </button>
      </div>

      <!-- Controles adicionales según período -->
      <div class="flex items-center gap-2">

        <!-- Semana: navegar semanas -->
        <template v-if="periodo === 'semanal'">
          <button @click="navSemana(-1)" class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors">←</button>
          <span class="text-[#a0a0a0] text-sm whitespace-nowrap">{{ labelSemana }}</span>
          <button @click="navSemana(1)" :disabled="semanaOffset >= 0"
            class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors disabled:opacity-40">→</button>
        </template>

        <!-- Mes: mes/año -->
        <template v-if="periodo === 'mensual'">
          <button @click="navMes(-1)" class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors">←</button>
          <span class="text-[#a0a0a0] text-sm whitespace-nowrap capitalize">{{ labelMes }}</span>
          <button @click="navMes(1)" :disabled="mesOffset >= 0"
            class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors disabled:opacity-40">→</button>
        </template>

        <!-- Año -->
        <template v-if="periodo === 'anual'">
          <button @click="navAnio(-1)" class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors">←</button>
          <span class="text-[#a0a0a0] text-sm">{{ anioActual }}</span>
          <button @click="navAnio(1)" :disabled="anioOffset >= 0"
            class="bg-[#1a1a1a] border border-[#2a2a2a] hover:border-[#E31837] text-white px-3 py-2 rounded-lg text-sm transition-colors disabled:opacity-40">→</button>
        </template>

      </div>

      <!-- Botones exportar -->
      <div class="flex items-center gap-2 sm:ml-auto">
        <button @click="exportar('excel')" :disabled="descargando === 'excel'"
          class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all border
                 bg-[#1a1a1a] border-[#2a2a2a] hover:border-green-500 hover:text-green-400 text-[#a0a0a0]
                 disabled:opacity-50 disabled:cursor-not-allowed">
          <svg v-if="descargando === 'excel'" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
          </svg>
          <span v-else>📊</span>
          Excel
        </button>
        <button @click="exportar('pdf')" :disabled="descargando === 'pdf'"
          class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all border
                 bg-[#1a1a1a] border-[#2a2a2a] hover:border-red-500 hover:text-red-400 text-[#a0a0a0]
                 disabled:opacity-50 disabled:cursor-not-allowed">
          <svg v-if="descargando === 'pdf'" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
          </svg>
          <span v-else>📄</span>
          PDF
        </button>
      </div>
    </div>

    <!-- ── Loading ──────────────────────────────────────────────────────── -->
    <template v-if="loading">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div v-for="i in 4" :key="i" class="h-24 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
      </div>
      <div class="h-64 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
    </template>

    <template v-else-if="reporte">

      <!-- ── Métricas clave ─────────────────────────────────────────────── -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Ingresos totales</p>
          <p class="text-2xl font-black text-[#E31837]">{{ fmt(reporte.totalGeneral) }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Ventas online</p>
          <p class="text-2xl font-black text-white">{{ reporte.cantidadVentasWeb }}</p>
          <p class="text-[#a0a0a0] text-xs mt-1">{{ fmt(reporte.totalWeb) }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Ventas presencial</p>
          <p class="text-2xl font-black text-white">{{ reporte.cantidadVentasPresencial }}</p>
          <p class="text-[#a0a0a0] text-xs mt-1">{{ fmt(reporte.totalPresencial) }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Mantenimientos</p>
          <p class="text-2xl font-black text-white">{{ reporte.cantidadMantenimientos }}</p>
          <p class="text-[#a0a0a0] text-xs mt-1">{{ fmt(reporte.totalMantenimiento) }}</p>
        </div>
      </div>

      <!-- ── Gráfica de barras ──────────────────────────────────────────── -->
      <div v-if="serieTotal.length > 0" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">

        <!-- Leyenda -->
        <div class="flex items-center justify-between mb-6 flex-wrap gap-3">
          <h3 class="text-white font-bold text-sm">
            Ingresos por {{ periodo === 'semanal' ? 'día' : periodo === 'mensual' ? 'semana' : 'mes' }}
          </h3>
          <div class="flex gap-4 text-xs text-[#a0a0a0]">
            <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-[#E31837] inline-block"></span>Online</span>
            <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block"></span>Presencial</span>
            <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-yellow-500 inline-block"></span>Mantenimiento</span>
          </div>
        </div>

        <!-- Barras -->
        <div class="flex items-end gap-2 h-48" style="align-items: flex-end;">
          <div v-for="(punto, i) in serieTotal" :key="i"
            class="flex-1 flex flex-col items-center gap-1 group min-w-0">

            <!-- Tooltip valor -->
            <div class="opacity-0 group-hover:opacity-100 transition-opacity text-xs text-white bg-[#2a2a2a] px-2 py-1 rounded-lg whitespace-nowrap z-10 pointer-events-none text-center">
              {{ fmtCorto(punto.total) }}
            </div>

            <!-- Barra apilada -->
            <div class="w-full flex flex-col justify-end rounded-t-lg overflow-hidden" :style="{ height: barAltura(punto.total) + 'px' }">
              <div :style="{ height: barPct(punto.mant, punto.total) + '%' }" class="bg-yellow-500 w-full shrink-0" />
              <div :style="{ height: barPct(punto.presencial, punto.total) + '%' }" class="bg-blue-500 w-full shrink-0" />
              <div :style="{ height: barPct(punto.web, punto.total) + '%' }" class="bg-[#E31837] w-full shrink-0" />
            </div>

            <!-- Etiqueta -->
            <p class="text-[#a0a0a0] text-xs truncate w-full text-center">{{ punto.etiqueta }}</p>
          </div>
        </div>

        <!-- Escala Y (valores referencia) -->
        <div class="flex justify-between mt-2 border-t border-[#2a2a2a] pt-2">
          <span class="text-[#a0a0a0] text-xs">0</span>
          <span class="text-[#a0a0a0] text-xs">{{ fmtCorto(maxValor / 2) }}</span>
          <span class="text-[#a0a0a0] text-xs">{{ fmtCorto(maxValor) }}</span>
        </div>
      </div>

      <!-- Sin datos de gráfica -->
      <div v-else class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-10 text-center">
        <p class="text-4xl mb-3">📊</p>
        <p class="text-[#a0a0a0] text-sm">No hay datos de ventas para este período.</p>
      </div>

      <!-- ── Desglose canal ─────────────────────────────────────────────── -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <!-- Online vs Presencial barra proporcional -->
        <div class="sm:col-span-2 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-4">Distribución de ventas</p>
          <div v-if="reporte.totalGeneral > 0" class="space-y-3">
            <!-- Online -->
            <div>
              <div class="flex justify-between text-xs mb-1">
                <span class="text-[#a0a0a0]">Online</span>
                <span class="text-white font-semibold">{{ pctVenta(reporte.totalWeb) }}%</span>
              </div>
              <div class="h-2.5 bg-[#0a0a0a] rounded-full overflow-hidden">
                <div class="h-full bg-[#E31837] rounded-full transition-all duration-700"
                  :style="{ width: pctVenta(reporte.totalWeb) + '%' }" />
              </div>
            </div>
            <!-- Presencial -->
            <div>
              <div class="flex justify-between text-xs mb-1">
                <span class="text-[#a0a0a0]">Presencial</span>
                <span class="text-white font-semibold">{{ pctVenta(reporte.totalPresencial) }}%</span>
              </div>
              <div class="h-2.5 bg-[#0a0a0a] rounded-full overflow-hidden">
                <div class="h-full bg-blue-500 rounded-full transition-all duration-700"
                  :style="{ width: pctVenta(reporte.totalPresencial) + '%' }" />
              </div>
            </div>
            <!-- Mantenimiento -->
            <div>
              <div class="flex justify-between text-xs mb-1">
                <span class="text-[#a0a0a0]">Mantenimiento</span>
                <span class="text-white font-semibold">{{ pctVenta(reporte.totalMantenimiento) }}%</span>
              </div>
              <div class="h-2.5 bg-[#0a0a0a] rounded-full overflow-hidden">
                <div class="h-full bg-yellow-500 rounded-full transition-all duration-700"
                  :style="{ width: pctVenta(reporte.totalMantenimiento) + '%' }" />
              </div>
            </div>
          </div>
          <p v-else class="text-[#a0a0a0] text-sm text-center py-4">Sin ventas en este período</p>
        </div>

        <!-- Ticket promedio -->
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 flex flex-col justify-between">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest">Ticket promedio</p>
          <div class="text-center py-4">
            <p class="text-3xl font-black text-green-400">{{ fmt(ticketPromedio) }}</p>
            <p class="text-[#a0a0a0] text-xs mt-1">por pedido</p>
          </div>
          <p class="text-[#a0a0a0] text-xs">
            {{ totalPedidos }} pedido{{ totalPedidos !== 1 ? 's' : '' }} en el período
          </p>
        </div>
      </div>

    </template>

    <!-- Error de carga -->
    <div v-else class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-10 text-center">
      <p class="text-[#a0a0a0] text-sm">No se pudieron cargar los datos del período.</p>
    </div>

    <!-- ── Stock bajo ─────────────────────────────────────────────────────── -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <div>
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest">Alertas de inventario</p>
          <h3 class="text-white font-bold text-sm mt-0.5">Productos con stock bajo</h3>
        </div>
        <button @click="cargarStock" class="text-[#a0a0a0] hover:text-white text-xs transition-colors flex items-center gap-1">
          ↻ Actualizar
        </button>
      </div>

      <div v-if="loadingStock" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        <div v-for="i in 3" :key="i" class="h-16 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
      </div>

      <div v-else-if="stockBajo.length === 0"
        class="bg-[#1a1a1a] border border-green-800/30 rounded-xl p-5 flex items-center gap-3">
        <span class="text-2xl">✅</span>
        <p class="text-green-400 text-sm font-semibold">Todos los productos tienen stock suficiente.</p>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        <div v-for="p in stockBajo" :key="p.id"
          class="bg-[#1a1a1a] border border-yellow-500/30 rounded-xl px-4 py-3 flex items-center justify-between gap-3">
          <div class="min-w-0">
            <p class="text-white text-sm font-semibold truncate">{{ p.nombre }}</p>
            <p class="text-[#a0a0a0] text-xs">{{ p.categoriaNombre || 'Sin categoría' }}</p>
          </div>
          <div class="text-right shrink-0">
            <p class="text-yellow-400 text-xl font-black">{{ p.stock }}</p>
            <p class="text-[#a0a0a0] text-xs">uds</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

// ── Exportar ──────────────────────────────────────────────────────────────────
const descargando = ref(null) // 'excel' | 'pdf' | null

async function exportar(formato) {
  descargando.value = formato
  try {
    // Construir los mismos parámetros que usa la vista actual
    const params = new URLSearchParams({ tipo: periodo.value })

    if (periodo.value === 'semanal') {
      params.set('fecha', fechaSemana())
    } else if (periodo.value === 'mensual') {
      const { anio, mes } = mesSeleccionado.value
      params.set('anio', anio)
      params.set('mes', mes)
    } else {
      params.set('anio', anioActual.value)
    }

    const url = `/admin/reportes/exportar/${formato}?${params.toString()}`
    const res = await api.get(url, { responseType: 'arraybuffer' })

    // Construir nombre de archivo
    const ext = formato === 'excel' ? 'xlsx' : 'pdf'
    const fecha = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const nombre = `bikeshop-reporte-${periodo.value}-${fecha}.${ext}`

    // Disparar descarga en el navegador
    const mimeType = formato === 'excel'
      ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      : 'application/pdf'
    const blob = new Blob([res.data], { type: mimeType })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = nombre
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (e) {
    alert('Error al generar el reporte. Verifica que el servidor esté activo.')
    console.error(e)
  } finally {
    descargando.value = null
  }
}

// ── Períodos disponibles ──────────────────────────────────────────────────────
const periodos = [
  { id: 'semanal', label: 'Semana' },
  { id: 'mensual', label: 'Mes' },
  { id: 'anual',   label: 'Año' },
]
const periodo = ref('mensual')

// ── Navegación de fechas ──────────────────────────────────────────────────────
const semanaOffset = ref(0)   // 0 = semana actual, -1 = semana anterior…
const mesOffset    = ref(0)   // 0 = mes actual
const anioOffset   = ref(0)   // 0 = año actual

const hoyDate = new Date()

function navSemana(d)  { semanaOffset.value += d; cargar() }
function navMes(d)     { mesOffset.value += d;    cargar() }
function navAnio(d)    { anioOffset.value += d;   cargar() }

function seleccionarPeriodo(p) {
  periodo.value = p
  cargar()
}

// Fecha base de la semana seleccionada
function fechaSemana() {
  const d = new Date(hoyDate)
  d.setDate(d.getDate() + semanaOffset.value * 7)
  return d.toISOString().split('T')[0]
}

// Mes y año seleccionados
const mesSeleccionado = computed(() => {
  const d = new Date(hoyDate.getFullYear(), hoyDate.getMonth() + mesOffset.value, 1)
  return { anio: d.getFullYear(), mes: d.getMonth() + 1 }
})
const anioActual = computed(() => hoyDate.getFullYear() + anioOffset.value)

// Labels de navegación
const labelSemana = computed(() => {
  const d = new Date(hoyDate)
  d.setDate(d.getDate() + semanaOffset.value * 7)
  const lunes = new Date(d)
  lunes.setDate(d.getDate() - ((d.getDay() + 6) % 7))
  const dom = new Date(lunes); dom.setDate(lunes.getDate() + 6)
  const fmt = (x) => x.toLocaleDateString('es-CO', { day: '2-digit', month: 'short' })
  return `${fmt(lunes)} – ${fmt(dom)}`
})
const labelMes = computed(() => {
  const d = new Date(hoyDate.getFullYear(), hoyDate.getMonth() + mesOffset.value, 1)
  return d.toLocaleDateString('es-CO', { month: 'long', year: 'numeric' })
})

// ── Carga de datos ────────────────────────────────────────────────────────────
const reporte = ref(null)
const loading = ref(false)

async function cargar() {
  loading.value = true
  reporte.value = null
  try {
    let url = ''
    if (periodo.value === 'semanal') {
      url = `/admin/reportes/semanal?fecha=${fechaSemana()}`
    } else if (periodo.value === 'mensual') {
      const { anio, mes } = mesSeleccionado.value
      url = `/admin/reportes/mensual?anio=${anio}&mes=${mes}`
    } else {
      url = `/admin/reportes/anual?anio=${anioActual.value}`
    }
    const { data } = await api.get(url)
    reporte.value = data.data || data
  } catch {
    reporte.value = null
  } finally {
    loading.value = false
  }
}

// ── Stock bajo ────────────────────────────────────────────────────────────────
const stockBajo = ref([])
const loadingStock = ref(false)

async function cargarStock() {
  loadingStock.value = true
  try {
    const { data } = await api.get('/admin/productos/stock-bajo')
    stockBajo.value = data.data || []
  } catch {
    stockBajo.value = []
  } finally {
    loadingStock.value = false
  }
}

// ── Datos de gráfica ──────────────────────────────────────────────────────────
const serieTotal = computed(() => {
  if (!reporte.value) return []
  const web   = reporte.value.serieWeb   || []
  const pres  = reporte.value.seriePresencial || []
  const mant  = reporte.value.serieMantenimiento || []
  const n = Math.max(web.length, pres.length, mant.length)
  return Array.from({ length: n }, (_, i) => ({
    etiqueta: (web[i] || pres[i] || mant[i])?.etiqueta || '',
    web:      Number(web[i]?.valor  || 0),
    presencial: Number(pres[i]?.valor || 0),
    mant:     Number(mant[i]?.valor || 0),
    total:    Number(web[i]?.valor || 0) + Number(pres[i]?.valor || 0) + Number(mant[i]?.valor || 0),
  }))
})

const maxValor = computed(() => Math.max(...serieTotal.value.map(p => p.total), 1))
const MAX_BAR_H = 160  // px

function barAltura(val) {
  return Math.max(4, (val / maxValor.value) * MAX_BAR_H)
}
function barPct(val, total) {
  return total > 0 ? (val / total) * 100 : 0
}

// ── Métricas calculadas ───────────────────────────────────────────────────────
const totalPedidos = computed(() =>
  (reporte.value?.cantidadVentasWeb ?? 0) + (reporte.value?.cantidadVentasPresencial ?? 0)
)
const ticketPromedio = computed(() => {
  if (!reporte.value || totalPedidos.value === 0) return 0
  const totalVentas = Number(reporte.value.totalWeb || 0) + Number(reporte.value.totalPresencial || 0)
  return totalVentas / totalPedidos.value
})

function pctVenta(val) {
  if (!reporte.value?.totalGeneral || reporte.value.totalGeneral === 0) return 0
  return Math.round((Number(val || 0) / Number(reporte.value.totalGeneral)) * 100)
}

// ── Formatos ──────────────────────────────────────────────────────────────────
function fmt(v) {
  if (!v && v !== 0) return '—'
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(v)
}
function fmtCorto(v) {
  if (!v) return '$0'
  if (v >= 1_000_000) return `$${(v / 1_000_000).toFixed(1)}M`
  if (v >= 1_000) return `$${Math.round(v / 1_000)}K`
  return `$${Math.round(v)}`
}

onMounted(() => {
  cargar()
  cargarStock()
})
</script>
