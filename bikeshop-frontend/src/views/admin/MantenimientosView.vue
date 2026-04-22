<template>
  <div class="space-y-6">
    <!-- Tabs -->
    <div class="flex gap-1 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-1 w-fit">
      <button @click="tab = 'lista'"
        class="px-5 py-2 rounded-lg text-sm font-semibold transition-colors"
        :class="tab === 'lista' ? 'bg-[#E31837] text-white' : 'text-[#a0a0a0] hover:text-white'">
        Lista
      </button>
      <button @click="tab = 'dia'"
        class="px-5 py-2 rounded-lg text-sm font-semibold transition-colors"
        :class="tab === 'dia' ? 'bg-[#E31837] text-white' : 'text-[#a0a0a0] hover:text-white'">
        Vista del día
      </button>
    </div>

    <!-- TAB: LISTA -->
    <template v-if="tab === 'lista'">
      <div class="flex flex-wrap gap-2">
        <button v-for="f in filtrosEstado" :key="f.value" @click="cambiarFiltro(f.value)"
          class="border px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors"
          :class="filtroActivo === f.value
            ? 'bg-[#E31837] text-white border-transparent'
            : 'bg-[#1a1a1a] text-[#a0a0a0] border-[#2a2a2a] hover:border-white hover:text-white'">
          {{ f.label }}
        </button>
      </div>

      <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
        <div v-if="loadingLista" class="p-8 space-y-3">
          <div v-for="i in 5" :key="i" class="h-14 bg-[#2a2a2a] rounded-lg animate-pulse" />
        </div>
        <div v-else-if="mantenimientos.length === 0" class="p-12 text-center">
          <p class="text-4xl mb-3">🔧</p>
          <p class="text-[#a0a0a0]">No hay citas para este filtro.</p>
        </div>
        <template v-else>
          <!-- Desktop tabla -->
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full">
              <thead class="border-b border-[#2a2a2a]">
                <tr class="text-left">
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">#</th>
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Cliente</th>
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Bicicleta</th>
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Estado</th>
                  <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#2a2a2a]">
                <tr v-for="m in mantenimientos" :key="m.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
                  <td class="px-6 py-4 text-white font-mono text-sm font-semibold">#{{ m.id }}</td>
                  <td class="px-6 py-4">
                    <p class="text-white text-sm">{{ m.nombreCliente }}</p>
                    <p class="text-[#a0a0a0] text-xs">{{ m.emailCliente }}</p>
                  </td>
                  <td class="px-6 py-4 text-[#a0a0a0] text-sm">{{ m.tipoBicicleta }}</td>
                  <td class="px-6 py-4 text-[#a0a0a0] text-xs">{{ m.fecha }} {{ m.horaInicio || '' }}</td>
                  <td class="px-6 py-4">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoBadge(m.estado)">
                      {{ estadoLabel(m.estado) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-right">
                    <button @click="abrirGestionar(m)" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                      Gestionar
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <!-- Móvil cards -->
          <div class="md:hidden divide-y divide-[#2a2a2a]">
            <div v-for="m in mantenimientos" :key="m.id" @click="abrirGestionar(m)"
              class="px-5 py-4 cursor-pointer hover:bg-[#2a2a2a]/40 transition-colors space-y-1">
              <div class="flex items-center justify-between">
                <span class="text-white font-semibold text-sm">{{ m.nombreCliente }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoBadge(m.estado)">
                  {{ estadoLabel(m.estado) }}
                </span>
              </div>
              <p class="text-[#a0a0a0] text-xs">{{ m.tipoBicicleta }} · {{ m.fecha }}</p>
              <p class="text-[#a0a0a0] text-xs truncate">{{ m.problemaReportado }}</p>
            </div>
          </div>
        </template>

        <div v-if="totalPagesLista > 1" class="flex items-center justify-between px-6 py-4 border-t border-[#2a2a2a]">
          <button @click="cargarLista(pageLista - 1)" :disabled="pageLista === 0"
            class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
          <span class="text-sm text-[#a0a0a0]">Página {{ pageLista + 1 }} de {{ totalPagesLista }}</span>
          <button @click="cargarLista(pageLista + 1)" :disabled="pageLista >= totalPagesLista - 1"
            class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
        </div>
      </div>
    </template>

    <!-- TAB: DÍA -->
    <template v-if="tab === 'dia'">
      <div class="flex items-center gap-3">
        <input v-model="fechaDia" type="date"
          class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-4 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
        <button @click="cargarDia"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white px-5 py-2.5 rounded-xl text-sm font-semibold transition-colors">
          Ver día
        </button>
      </div>

      <div v-if="loadingDia" class="space-y-3">
        <div v-for="i in 3" :key="i" class="h-20 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
      </div>
      <div v-else-if="citasDia.length === 0"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-12 text-center">
        <p class="text-4xl mb-3">📅</p>
        <p class="text-[#a0a0a0]">No hay citas para este día.</p>
      </div>
      <div v-else class="space-y-3">
        <div v-for="m in citasDia" :key="m.id"
          class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-5 py-4 flex items-start gap-4 cursor-pointer hover:border-[#E31837]/40 transition-colors"
          @click="abrirGestionar(m)">
          <div class="shrink-0 text-center">
            <p class="text-[#E31837] font-black text-lg leading-none">{{ m.horaInicio || '—' }}</p>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between gap-2">
              <p class="text-white font-semibold text-sm">{{ m.nombreCliente }}</p>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium shrink-0" :class="estadoBadge(m.estado)">
                {{ estadoLabel(m.estado) }}
              </span>
            </div>
            <p class="text-[#a0a0a0] text-xs mt-0.5">{{ m.tipoBicicleta }}</p>
            <p class="text-[#a0a0a0] text-xs mt-1 truncate">{{ m.problemaReportado }}</p>
          </div>
        </div>
      </div>
    </template>

    <!-- Modal gestionar -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-start justify-center p-4 overflow-y-auto"
        @click.self="modal = false">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6 sm:p-8 w-full max-w-lg my-8">
          <div class="flex items-start justify-between mb-6">
            <div>
              <h2 class="text-xl font-black text-white">Cita #{{ citaActiva?.id }}</h2>
              <p class="text-[#a0a0a0] text-xs mt-1">{{ citaActiva?.fecha }} {{ citaActiva?.horaInicio }}</p>
            </div>
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="estadoBadge(citaActiva?.estado)">
              {{ estadoLabel(citaActiva?.estado) }}
            </span>
          </div>

          <div class="space-y-2 mb-5">
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Cliente</span><span class="text-white font-medium">{{ citaActiva?.nombreCliente }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Email</span><span class="text-white">{{ citaActiva?.emailCliente }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Teléfono</span><span class="text-white">{{ citaActiva?.celularCliente || '—' }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Tipo bici</span><span class="text-white">{{ citaActiva?.tipoBicicleta }}</span></div>
          </div>

          <div class="mb-5">
            <p class="text-xs uppercase tracking-widest text-[#a0a0a0] mb-2">Descripción del cliente</p>
            <p class="text-white text-sm bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 leading-relaxed">
              {{ citaActiva?.problemaReportado || 'Sin descripción' }}
            </p>
          </div>

          <form @submit.prevent="guardarGestion" class="space-y-4">
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Estado</label>
              <select v-model="gestion.estado"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                <option v-for="e in todosEstados" :key="e.value" :value="e.value">{{ e.label }}</option>
              </select>
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Diagnóstico</label>
              <textarea v-model="gestion.diagnostico" rows="3"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
                placeholder="Diagnóstico técnico..." />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Mano de obra</label>
                <input v-model="gestion.costoManoObra" type="number" min="0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="0" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Repuestos</label>
                <input v-model="gestion.costoRepuestos" type="number" min="0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="0" />
              </div>
            </div>
            <p v-if="errorGestion" class="text-red-400 text-sm">{{ errorGestion }}</p>
            <div class="flex gap-3 pt-2">
              <button type="button" @click="modal = false"
                class="flex-1 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button type="submit" :disabled="savingGestion"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                {{ savingGestion ? 'Guardando...' : 'Guardar' }}
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
import api from '@/services/api'

const tab = ref('lista')

// Lista
const mantenimientos = ref([])
const loadingLista = ref(true)
const filtroActivo = ref('PENDIENTE')
const pageLista = ref(0)
const totalPagesLista = ref(0)

// Día
const fechaDia = ref(new Date().toISOString().split('T')[0])
const citasDia = ref([])
const loadingDia = ref(false)

// Modal
const modal = ref(false)
const citaActiva = ref(null)
const savingGestion = ref(false)
const errorGestion = ref('')
const gestion = ref({ estado: '', diagnostico: '', costoManoObra: '', costoRepuestos: '' })

const filtrosEstado = [
  { label: 'Pendiente', value: 'PENDIENTE' },
  { label: 'En curso', value: 'EN_CURSO' },
  { label: 'Finalizado', value: 'FINALIZADO' },
  { label: 'Cancelado', value: 'CANCELADO' },
]
const todosEstados = [
  { label: 'Pendiente', value: 'PENDIENTE' },
  { label: 'En curso', value: 'EN_CURSO' },
  { label: 'Finalizado', value: 'FINALIZADO' },
  { label: 'Cancelado', value: 'CANCELADO' },
]

const ESTADO_LABELS = { PENDIENTE: 'Pendiente', EN_CURSO: 'En curso', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado' }
const ESTADO_BADGES = { PENDIENTE: 'bg-yellow-500/20 text-yellow-400', EN_CURSO: 'bg-orange-500/20 text-orange-400', FINALIZADO: 'bg-green-500/20 text-green-400', CANCELADO: 'bg-red-500/20 text-red-400' }

function estadoLabel(e) { return ESTADO_LABELS[e] || e }
function estadoBadge(e) { return ESTADO_BADGES[e] || 'bg-[#2a2a2a] text-[#a0a0a0]' }

async function cargarLista(p = 0) {
  pageLista.value = p
  loadingLista.value = true
  try {
    const { data } = await api.get(`/admin/mantenimientos?estado=${filtroActivo.value}&page=${p}&size=20`)
    mantenimientos.value = data.data?.content || []
    totalPagesLista.value = data.data?.totalPages || 0
  } catch (e) { console.error(e) }
  finally { loadingLista.value = false }
}

async function cargarDia() {
  loadingDia.value = true
  try {
    const { data } = await api.get(`/admin/mantenimientos/dia?fecha=${fechaDia.value}`)
    citasDia.value = data.data || []
  } catch (e) { console.error(e) }
  finally { loadingDia.value = false }
}

function cambiarFiltro(val) { filtroActivo.value = val; cargarLista(0) }

function abrirGestionar(m) {
  citaActiva.value = m
  gestion.value = {
    estado: m.estado,
    diagnostico: m.diagnostico || '',
    costoManoObra: m.costoManoObra || '',
    costoRepuestos: m.costoRepuestos || ''
  }
  errorGestion.value = ''
  modal.value = true
}

async function guardarGestion() {
  savingGestion.value = true
  errorGestion.value = ''
  try {
    const payload = {
      estado: gestion.value.estado,
      diagnostico: gestion.value.diagnostico || null,
      costoManoObra: gestion.value.costoManoObra !== '' ? Number(gestion.value.costoManoObra) : null,
      costoRepuestos: gestion.value.costoRepuestos !== '' ? Number(gestion.value.costoRepuestos) : null,
    }
    await api.patch(`/admin/mantenimientos/${citaActiva.value.id}`, payload)
    modal.value = false
    await cargarLista(pageLista.value)
    if (tab.value === 'dia') cargarDia()
  } catch (e) {
    errorGestion.value = e.response?.data?.message || 'Error al guardar'
  } finally { savingGestion.value = false }
}

onMounted(() => {
  cargarLista(0)
  cargarDia()
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
