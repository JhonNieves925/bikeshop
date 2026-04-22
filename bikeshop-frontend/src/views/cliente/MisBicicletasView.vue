<template>
  <div class="space-y-6">

    <!-- Header -->
    <div class="flex items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-black text-white">Mis Bicicletas</h1>
        <p class="text-[#a0a0a0] text-sm mt-1">
          {{ bicis.length }} bicicleta{{ bicis.length !== 1 ? 's' : '' }} registrada{{ bicis.length !== 1 ? 's' : '' }}
        </p>
      </div>
      <button @click="abrirModalNueva"
        class="shrink-0 bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Agregar bici
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div v-for="i in 3" :key="i" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 animate-pulse">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-12 h-12 bg-[#2a2a2a] rounded-xl" />
          <div class="space-y-2 flex-1">
            <div class="h-4 w-32 bg-[#2a2a2a] rounded" />
            <div class="h-3 w-20 bg-[#2a2a2a] rounded" />
          </div>
        </div>
      </div>
    </div>

    <!-- Vacío -->
    <div v-else-if="bicis.length === 0 && !viendoHistorial"
      class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-16 text-center">
      <p class="text-5xl mb-4">🚲</p>
      <p class="text-white font-semibold text-lg mb-2">Aún no tienes bicicletas registradas</p>
      <p class="text-[#a0a0a0] text-sm mb-6">
        Registra tus bicis para ver el historial de servicio de cada una.
      </p>
      <button @click="abrirModalNueva"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl text-sm font-semibold transition-colors">
        Agregar mi primera bicicleta
      </button>
    </div>

    <!-- Panel de historial -->
    <template v-else-if="viendoHistorial">
      <div class="flex items-center gap-3 mb-2">
        <button @click="viendoHistorial = null"
          class="flex items-center gap-1.5 text-[#a0a0a0] hover:text-white text-sm transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
          Volver a mis bicicletas
        </button>
      </div>

      <!-- Info bici seleccionada -->
      <div class="bg-[#141414] border border-[#2a2a2a] rounded-xl p-4 flex items-center gap-4">
        <div class="w-14 h-14 bg-[#E31837]/10 rounded-xl flex items-center justify-center text-3xl shrink-0">
          {{ tipoEmoji(viendoHistorial.tipo) }}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-white font-bold">{{ viendoHistorial.marca }} {{ viendoHistorial.modelo }}</p>
          <p class="text-[#a0a0a0] text-xs mt-0.5">
            {{ viendoHistorial.tipo || 'Bicicleta' }}
            <span v-if="viendoHistorial.anio"> · {{ viendoHistorial.anio }}</span>
            <span v-if="viendoHistorial.color"> · {{ viendoHistorial.color }}</span>
          </p>
        </div>
        <RouterLink to="/mantenimiento" @click="guardarBiciParaAgendar(viendoHistorial)"
          class="shrink-0 bg-[#E31837] hover:bg-[#b5112a] text-white px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors">
          + Agendar
        </RouterLink>
      </div>

      <!-- Historial de servicios -->
      <div>
        <h2 class="text-base font-bold text-white mb-3">Historial de servicios</h2>

        <div v-if="loadingHistorial" class="space-y-3">
          <div v-for="i in 3" :key="i" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 animate-pulse">
            <div class="h-4 w-40 bg-[#2a2a2a] rounded mb-2" />
            <div class="h-3 w-64 bg-[#2a2a2a] rounded" />
          </div>
        </div>

        <div v-else-if="historial.length === 0"
          class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-10 text-center">
          <p class="text-3xl mb-3">🔍</p>
          <p class="text-white font-semibold mb-1">Sin historial de servicio</p>
          <p class="text-[#a0a0a0] text-sm">Esta bicicleta aún no tiene mantenimientos registrados.</p>
        </div>

        <div v-else class="space-y-3">
          <div v-for="srv in historial" :key="srv.id"
            class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5">
            <!-- Encabezado -->
            <div class="flex items-start justify-between gap-3 mb-3">
              <div>
                <p class="text-white font-semibold text-sm">{{ formatDate(srv.fecha) }}</p>
                <p class="text-[#a0a0a0] text-xs mt-0.5">
                  {{ srv.horaInicio ? `a las ${srv.horaInicio}` : '' }}
                  <span v-if="srv.atendidoPorNombre"> · Atendido por {{ srv.atendidoPorNombre }}</span>
                </p>
              </div>
              <span class="shrink-0 px-2.5 py-1 rounded-full text-xs font-bold"
                :class="estadoClass(srv.estado)">
                {{ estadoLabel(srv.estado) }}
              </span>
            </div>

            <!-- Problema / trabajos -->
            <p class="text-[#a0a0a0] text-xs mb-1">Problema reportado</p>
            <p class="text-white text-sm mb-3">{{ srv.problemaReportado }}</p>

            <template v-if="srv.trabajosRealizados">
              <p class="text-[#a0a0a0] text-xs mb-1">Trabajos realizados</p>
              <p class="text-white text-sm mb-3">{{ srv.trabajosRealizados }}</p>
            </template>

            <!-- Repuestos -->
            <div v-if="srv.repuestos && srv.repuestos.length > 0" class="mb-3">
              <p class="text-[#a0a0a0] text-xs mb-2">Repuestos utilizados</p>
              <div class="space-y-1">
                <div v-for="rep in srv.repuestos" :key="rep.productoId"
                  class="flex items-center justify-between text-xs bg-[#141414] rounded-lg px-3 py-2">
                  <span class="text-white">{{ rep.productoNombre }} <span class="text-[#a0a0a0]">×{{ rep.cantidad }}</span></span>
                  <span class="text-[#a0a0a0]">${{ rep.subtotal?.toLocaleString('es-CO') }}</span>
                </div>
              </div>
            </div>

            <!-- Costos totales -->
            <div v-if="srv.costoTotal > 0"
              class="flex items-center justify-between pt-3 border-t border-[#2a2a2a]">
              <span class="text-[#a0a0a0] text-xs">Costo total</span>
              <span class="text-white font-bold text-sm">${{ srv.costoTotal?.toLocaleString('es-CO') }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Grid de bicicletas -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div v-for="bici in bicis" :key="bici.id"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 hover:border-[#444] transition-colors">

        <!-- Cabecera bici -->
        <div class="flex items-start gap-3 mb-4">
          <div class="w-12 h-12 bg-[#E31837]/10 rounded-xl flex items-center justify-center text-2xl shrink-0">
            {{ tipoEmoji(bici.tipo) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-white font-bold truncate">{{ bici.marca }} {{ bici.modelo }}</p>
            <p class="text-[#a0a0a0] text-xs mt-0.5">
              {{ bici.tipo || 'Bicicleta' }}
              <span v-if="bici.anio"> · {{ bici.anio }}</span>
            </p>
          </div>
          <!-- Menú acciones -->
          <div class="flex items-center gap-1.5 shrink-0">
            <button @click="editarBici(bici)"
              class="p-1.5 text-[#a0a0a0] hover:text-white transition-colors rounded-lg hover:bg-[#2a2a2a]"
              title="Editar">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
              </svg>
            </button>
            <button @click="confirmarEliminar(bici)"
              class="p-1.5 text-[#a0a0a0] hover:text-red-400 transition-colors rounded-lg hover:bg-[#2a2a2a]"
              title="Eliminar">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Detalles -->
        <div class="space-y-1.5 mb-4">
          <div v-if="bici.color" class="flex items-center gap-2 text-xs text-[#a0a0a0]">
            <span>🎨</span> {{ bici.color }}
          </div>
          <div v-if="bici.numeroSerie" class="flex items-center gap-2 text-xs text-[#a0a0a0]">
            <span>🔑</span> S/N: {{ bici.numeroSerie }}
          </div>
          <div v-if="bici.notas" class="flex items-start gap-2 text-xs text-[#a0a0a0]">
            <span class="shrink-0">📝</span> {{ bici.notas }}
          </div>
        </div>

        <!-- Acciones -->
        <button @click="verHistorial(bici)"
          class="w-full flex items-center justify-center gap-2 py-2.5 bg-[#141414] hover:bg-[#2a2a2a] border border-[#2a2a2a] hover:border-[#444] rounded-xl text-sm text-[#a0a0a0] hover:text-white transition-all font-medium">
          <span>📋</span> Ver historial de servicios
        </button>
      </div>
    </div>

    <!-- ── MODAL crear / editar ─────────────────────────────────────────── -->
    <Transition name="fade">
      <div v-if="modalAbierto" class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4"
        @click.self="cerrarModal">
        <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl w-full max-w-md p-6">
          <h2 class="text-lg font-black mb-5">
            {{ editando ? 'Editar bicicleta' : 'Agregar bicicleta' }}
          </h2>

          <form @submit.prevent="guardar" class="space-y-4">
            <!-- Tipo -->
            <div>
              <label class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-2 block">Tipo</label>
              <div class="flex gap-2">
                <button v-for="t in tipos" :key="t.id" type="button" @click="form.tipo = t.id"
                  class="flex-1 flex flex-col items-center gap-1 p-2.5 rounded-xl border-2 transition-all text-xs font-semibold"
                  :class="form.tipo === t.id
                    ? 'border-[#E31837] bg-[#E31837]/10 text-white'
                    : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-[#555]'">
                  <span class="text-xl">{{ t.emoji }}</span>{{ t.label }}
                </button>
              </div>
            </div>

            <!-- Marca + Modelo -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs text-[#a0a0a0] mb-1.5 block">Marca *</label>
                <input v-model="form.marca" required placeholder="Trek, Giant, Specialized..."
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837]" />
              </div>
              <div>
                <label class="text-xs text-[#a0a0a0] mb-1.5 block">Modelo *</label>
                <input v-model="form.modelo" required placeholder="Marlin 5, Talon..."
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837]" />
              </div>
            </div>

            <!-- Año + Color -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs text-[#a0a0a0] mb-1.5 block">Año</label>
                <input v-model="form.anio" type="number" min="1990" :max="new Date().getFullYear()" placeholder="2022"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837]" />
              </div>
              <div>
                <label class="text-xs text-[#a0a0a0] mb-1.5 block">Color</label>
                <input v-model="form.color" placeholder="Negro, Rojo..."
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837]" />
              </div>
            </div>

            <!-- Número de serie -->
            <div>
              <label class="text-xs text-[#a0a0a0] mb-1.5 block">Número de serie</label>
              <input v-model="form.numeroSerie" placeholder="Opcional — está bajo el pedalier"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837]" />
            </div>

            <!-- Notas -->
            <div>
              <label class="text-xs text-[#a0a0a0] mb-1.5 block">Notas</label>
              <textarea v-model="form.notas" rows="2" placeholder="Modificaciones, detalles especiales..."
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] resize-none" />
            </div>

            <!-- Error -->
            <p v-if="errorMsg" class="text-red-400 text-sm">{{ errorMsg }}</p>

            <!-- Botones -->
            <div class="flex gap-3 pt-1">
              <button type="button" @click="cerrarModal"
                class="flex-1 py-2.5 bg-[#2a2a2a] hover:bg-[#333] text-white rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button type="submit" :disabled="guardando"
                class="flex-1 py-2.5 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-60 text-white rounded-xl text-sm font-semibold transition-colors">
                {{ guardando ? 'Guardando...' : (editando ? 'Guardar cambios' : 'Agregar bici') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'

// ─── estado ─────────────────────────────────────────────────────────────────
const bicis          = ref([])
const loading        = ref(true)
const viendoHistorial = ref(null)   // bici seleccionada
const historial      = ref([])
const loadingHistorial = ref(false)

const modalAbierto   = ref(false)
const editando       = ref(null)    // bici que se está editando (o null si es nueva)
const guardando      = ref(false)
const errorMsg       = ref('')

const formVacio = () => ({ marca: '', modelo: '', anio: '', color: '', tipo: 'MTB', numeroSerie: '', notas: '' })
const form = ref(formVacio())

const tipos = [
  { id: 'MTB',   label: 'Montaña',  emoji: '🏔️' },
  { id: 'RUTA',  label: 'Ruta',     emoji: '🛣️' },
  { id: 'URBAN', label: 'Urbana',   emoji: '🏙️' },
  { id: 'OTRA',  label: 'Otra',     emoji: '🚲' },
]

// ─── carga inicial ───────────────────────────────────────────────────────────
onMounted(cargar)

async function cargar() {
  loading.value = true
  try {
    const res = await api.get('/cliente/bicicletas')
    bicis.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// ─── historial ───────────────────────────────────────────────────────────────
async function verHistorial(bici) {
  viendoHistorial.value = bici
  historial.value = []
  loadingHistorial.value = true
  try {
    const res = await api.get(`/cliente/bicicletas/${bici.id}/historial`)
    historial.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loadingHistorial.value = false
  }
}

function guardarBiciParaAgendar(bici) {
  // Guarda en sessionStorage para que MantenimientoView pueda preseleccionarla
  sessionStorage.setItem('biciPreseleccionada', JSON.stringify(bici))
}

// ─── modal ───────────────────────────────────────────────────────────────────
function abrirModalNueva() {
  editando.value = null
  form.value = formVacio()
  errorMsg.value = ''
  modalAbierto.value = true
}

function editarBici(bici) {
  editando.value = bici
  form.value = {
    marca: bici.marca || '',
    modelo: bici.modelo || '',
    anio: bici.anio || '',
    color: bici.color || '',
    tipo: bici.tipo || 'MTB',
    numeroSerie: bici.numeroSerie || '',
    notas: bici.notas || '',
  }
  errorMsg.value = ''
  modalAbierto.value = true
}

function cerrarModal() {
  modalAbierto.value = false
}

async function guardar() {
  if (!form.value.marca.trim() || !form.value.modelo.trim()) {
    errorMsg.value = 'La marca y el modelo son obligatorios.'
    return
  }
  guardando.value = true
  errorMsg.value = ''
  try {
    if (editando.value) {
      const res = await api.put(`/cliente/bicicletas/${editando.value.id}`, form.value)
      const idx = bicis.value.findIndex(b => b.id === editando.value.id)
      if (idx !== -1) bicis.value[idx] = res.data
    } else {
      const res = await api.post('/cliente/bicicletas', form.value)
      bicis.value.unshift(res.data)
    }
    cerrarModal()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Error al guardar. Intenta de nuevo.'
  } finally {
    guardando.value = false
  }
}

async function confirmarEliminar(bici) {
  if (!confirm(`¿Eliminar "${bici.marca} ${bici.modelo}"? Esta acción no se puede deshacer.`)) return
  try {
    await api.delete(`/cliente/bicicletas/${bici.id}`)
    bicis.value = bicis.value.filter(b => b.id !== bici.id)
  } catch (e) {
    alert('No se pudo eliminar. Intenta de nuevo.')
  }
}

// ─── helpers ─────────────────────────────────────────────────────────────────
function tipoEmoji(tipo) {
  const mapa = { MTB: '🏔️', RUTA: '🛣️', URBAN: '🏙️', OTRA: '🚲' }
  return mapa[tipo] || '🚲'
}

function formatDate(fecha) {
  if (!fecha) return ''
  const [y, m, d] = fecha.split('-')
  const meses = ['', 'ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']
  return `${d} ${meses[parseInt(m)]} ${y}`
}

function estadoLabel(estado) {
  const labels = { PENDIENTE: 'Pendiente', EN_CURSO: 'En curso', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado' }
  return labels[estado] || estado
}

function estadoClass(estado) {
  const clases = {
    PENDIENTE:  'bg-yellow-500/20 text-yellow-300',
    EN_CURSO:   'bg-blue-500/20 text-blue-300',
    FINALIZADO: 'bg-green-500/20 text-green-300',
    CANCELADO:  'bg-red-500/20 text-red-400',
  }
  return clases[estado] || 'bg-[#2a2a2a] text-[#a0a0a0]'
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
