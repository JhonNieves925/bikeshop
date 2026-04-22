<template>
  <div>
  <div class="space-y-6">

    <!-- Cabecera con mes y nav -->
    <div class="flex items-center justify-between gap-4 flex-wrap">
      <div class="flex items-center gap-3">
        <button @click="cambiarMes(-1)"
          class="w-9 h-9 rounded-xl bg-[#2a2a2a] hover:bg-[#3a3a3a] flex items-center justify-center transition-colors">
          ←
        </button>
        <h2 class="text-xl font-black w-44 text-center capitalize">{{ mesLabel }}</h2>
        <button @click="cambiarMes(1)"
          class="w-9 h-9 rounded-xl bg-[#2a2a2a] hover:bg-[#3a3a3a] flex items-center justify-center transition-colors">
          →
        </button>
      </div>
      <button @click="irHoy" class="text-sm text-[#a0a0a0] hover:text-white transition-colors">
        Hoy
      </button>
      <button @click="abrirModal(null)"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nueva cita
      </button>
      <!-- Leyenda -->
      <div class="flex items-center gap-4 text-xs text-[#a0a0a0]">
        <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-green-500"></span> Disponible</span>
        <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-yellow-400"></span> Parcial</span>
        <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-[#E31837]"></span> Lleno</span>
        <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-[#3a3a3a]"></span> Bloqueado</span>
      </div>
    </div>

    <div class="flex flex-col lg:flex-row gap-6">

      <!-- Calendario -->
      <div class="flex-1 bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl overflow-hidden">

        <!-- Encabezado días -->
        <div class="grid grid-cols-7 border-b border-[#2a2a2a]">
          <div v-for="d in diasSemana" :key="d"
            class="py-3 text-center text-xs font-semibold text-[#a0a0a0] uppercase tracking-wider">
            {{ d }}
          </div>
        </div>

        <!-- Celdas del mes -->
        <div v-if="loading" class="p-8 text-center text-[#a0a0a0] animate-pulse">Cargando agenda...</div>
        <div v-else class="grid grid-cols-7 divide-x divide-y divide-[#2a2a2a]">
          <!-- Celdas vacías inicio -->
          <div v-for="_ in primerDiaSemana" :key="'e'+_" class="min-h-[80px] bg-[#0f0f0f]" />

          <!-- Días del mes -->
          <div v-for="dia in diasDelMes" :key="dia.fecha"
            @click="seleccionarDia(dia)"
            class="min-h-[80px] p-2 cursor-pointer transition-colors relative"
            :class="[
              diaClase(dia),
              diaSeleccionado === dia.fecha ? 'ring-2 ring-[#E31837] ring-inset' : ''
            ]">
            <!-- Número del día -->
            <div class="flex items-center justify-between mb-1">
              <span class="text-sm font-bold leading-none"
                :class="esHoy(dia.fecha) ? 'w-6 h-6 bg-[#E31837] rounded-full flex items-center justify-center text-white text-xs'
                                          : dia.bloqueado ? 'text-[#555]' : 'text-white'">
                {{ new Date(dia.fecha + 'T12:00:00').getDate() }}
              </span>
              <span v-if="dia.citas > 0" class="text-xs font-bold"
                :class="colorCitas(dia)">
                {{ dia.citas }}
              </span>
            </div>
            <!-- Barras de citas (mini) -->
            <div v-if="dia.citas > 0 && !dia.bloqueado" class="space-y-0.5 mt-1">
              <div v-for="c in dia.citasData.slice(0, 3)" :key="c.id"
                class="h-1.5 rounded-full truncate"
                :class="estadoColor(c.estado)" />
              <p v-if="dia.citas > 3" class="text-[9px] text-[#a0a0a0]">+{{ dia.citas - 3 }} más</p>
            </div>
            <!-- Día bloqueado -->
            <p v-if="dia.bloqueado" class="text-[9px] text-[#555] mt-1 leading-tight">{{ dia.motivoBloqueo || 'Bloqueado' }}</p>
          </div>

          <!-- Celdas vacías fin -->
          <div v-for="_ in celdasFin" :key="'f'+_" class="min-h-[80px] bg-[#0f0f0f]" />
        </div>
      </div>

      <!-- Panel lateral: detalle del día -->
      <div class="lg:w-80 shrink-0">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-5 sticky top-6">

          <div v-if="!diaSeleccionado" class="text-center py-8">
            <p class="text-4xl mb-3">📅</p>
            <p class="text-[#a0a0a0] text-sm">Selecciona un día para ver los detalles</p>
          </div>

          <div v-else>
            <div class="flex items-center justify-between mb-4">
              <div>
                <p class="text-xs text-[#a0a0a0] uppercase tracking-wider">{{ labelDiaSeleccionado }}</p>
                <p class="text-white font-black text-lg capitalize">{{ formatFechaDia(diaSeleccionado) }}</p>
              </div>
              <button v-if="!diaBloqueadoSeleccionado" @click="bloquearDia"
                class="text-xs text-[#a0a0a0] hover:text-[#E31837] transition-colors border border-[#2a2a2a] hover:border-[#E31837] rounded-lg px-2 py-1">
                Bloquear
              </button>
              <button v-else @click="desbloquearDia"
                class="text-xs text-green-400 hover:text-green-300 transition-colors border border-green-500/30 hover:border-green-400 rounded-lg px-2 py-1">
                Desbloquear
              </button>
            </div>

            <!-- Citas del día -->
            <!-- Botón agendar en el día seleccionado -->
            <button v-if="!diaBloqueadoSeleccionado" @click="abrirModal(diaSeleccionado)"
              class="w-full mb-3 bg-[#E31837]/10 hover:bg-[#E31837]/20 border border-[#E31837]/30 text-[#E31837] rounded-xl py-2 text-sm font-semibold transition-colors">
              + Agendar en este día
            </button>

            <div v-if="citasDia.length === 0 && !diaBloqueadoSeleccionado" class="text-center py-6">
              <p class="text-[#a0a0a0] text-sm">Sin citas este día</p>
            </div>

            <div v-if="diaBloqueadoSeleccionado" class="bg-[#2a2a2a] rounded-xl p-4 text-center mb-4">
              <p class="text-[#a0a0a0] text-sm">🚫 Día bloqueado</p>
            </div>

            <div class="space-y-3">
              <div v-for="c in citasDia" :key="c.id"
                class="bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl p-3 hover:border-[#3a3a3a] transition-colors">
                <div class="flex items-start justify-between gap-2 mb-1">
                  <p class="text-white text-sm font-semibold leading-tight">{{ c.horaInicio }}</p>
                  <span class="px-2 py-0.5 rounded-full text-[10px] font-bold shrink-0"
                    :class="estadoBadge(c.estado)">
                    {{ c.estado }}
                  </span>
                </div>
                <p class="text-white text-xs font-medium">{{ c.nombreCliente }}</p>
                <p class="text-[#a0a0a0] text-xs">{{ c.tipoBicicleta }}
                  <span v-if="c.marcaBicicleta"> · {{ c.marcaBicicleta }}</span>
                </p>
                <p class="text-[#555] text-xs mt-1 line-clamp-2">{{ c.problemaReportado }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div><!-- /space-y-6 -->

  <!-- ── Modal: Nueva cita ──────────────────────────────────────────────── -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70"
        @click.self="showModal = false">
        <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between px-6 pt-5 pb-4 border-b border-[#2a2a2a]">
            <h2 class="text-white font-black text-lg">Nueva cita de mantenimiento</h2>
            <button @click="showModal = false" class="text-[#a0a0a0] hover:text-white text-xl leading-none">✕</button>
          </div>

          <form @submit.prevent="crearCita" class="px-6 py-5 space-y-4">

            <!-- Fecha + hora -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Fecha *</label>
                <input v-model="formCita.fecha" type="date" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  @change="cargarSlots" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Hora *</label>
                <select v-model="formCita.horaInicio" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                  <option value="">-- Seleccionar --</option>
                  <option v-if="loadingSlots" disabled>Cargando…</option>
                  <option v-for="s in slotsDisponibles" :key="s.hora" :value="s.hora">{{ s.hora }}</option>
                  <option v-if="!loadingSlots && slotsDisponibles.length === 0 && formCita.fecha" disabled>
                    Sin horarios disponibles
                  </option>
                </select>
              </div>
            </div>

            <!-- Datos del cliente -->
            <div class="grid grid-cols-2 gap-3">
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Nombre completo *</label>
                <input v-model="formCita.nombreCliente" type="text" required placeholder="Ej: Juan Pérez"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Email *</label>
                <input v-model="formCita.emailCliente" type="email" required placeholder="correo@ejemplo.com"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Celular *</label>
                <input v-model="formCita.celularCliente" type="tel" required placeholder="3001234567"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
              </div>
            </div>

            <!-- Datos de la bicicleta -->
            <div class="grid grid-cols-3 gap-3">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Tipo *</label>
                <select v-model="formCita.tipoBicicleta" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                  <option value="">-- Tipo --</option>
                  <option>Ruta</option>
                  <option>Montaña</option>
                  <option>Urbana</option>
                  <option>BMX</option>
                  <option>Eléctrica</option>
                  <option>Otra</option>
                </select>
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Marca</label>
                <input v-model="formCita.marcaBicicleta" type="text" placeholder="Trek, Giant…"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Modelo</label>
                <input v-model="formCita.modeloBicicleta" type="text" placeholder="FX3, Marlin…"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
              </div>
            </div>

            <!-- Problema -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Descripción del problema / servicio *</label>
              <textarea v-model="formCita.problemaReportado" required rows="3"
                placeholder="Ej: Cambio de frenos, revisión general, ruido en la cadena…"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none" />
            </div>

            <p v-if="errorCita" class="text-[#E31837] text-sm">{{ errorCita }}</p>

            <div class="flex gap-3 pt-1">
              <button type="submit" :disabled="loadingCita"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl font-bold transition-colors">
                {{ loadingCita ? 'Agendando…' : 'Confirmar cita' }}
              </button>
              <button type="button" @click="showModal = false"
                class="px-5 bg-[#2a2a2a] hover:bg-[#3a3a3a] text-[#a0a0a0] rounded-xl transition-colors">
                Cancelar
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
  </div><!-- /root -->
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/services/api'

const diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// Estado del mes actual
const hoyDate = new Date()
const anio   = ref(hoyDate.getFullYear())
const mes    = ref(hoyDate.getMonth()) // 0-based
const loading = ref(false)

// Datos del mes
const citasMes    = ref([])   // lista plana de MantenimientoResponse
const bloqueados  = ref([])   // fechas bloqueadas del mes

const diaSeleccionado = ref(null)

// ── Labels ──────────────────────────────────────────────────────────────────
const mesLabel = computed(() =>
  new Date(anio.value, mes.value, 1).toLocaleDateString('es-CO', { month: 'long', year: 'numeric' })
)

function esHoy(fecha) {
  const h = hoyDate
  return fecha === `${h.getFullYear()}-${String(h.getMonth()+1).padStart(2,'0')}-${String(h.getDate()).padStart(2,'0')}`
}

// ── Calendario ──────────────────────────────────────────────────────────────
const primerDia   = computed(() => new Date(anio.value, mes.value, 1))
const diasEnMes   = computed(() => new Date(anio.value, mes.value + 1, 0).getDate())

// Lunes=0…Domingo=6
const primerDiaSemana = computed(() => {
  const d = primerDia.value.getDay() // Dom=0
  return d === 0 ? 6 : d - 1
})

const celdasFin = computed(() => {
  const total = primerDiaSemana.value + diasEnMes.value
  const rem = total % 7
  return rem === 0 ? 0 : 7 - rem
})

const diasDelMes = computed(() => {
  return Array.from({ length: diasEnMes.value }, (_, i) => {
    const d = i + 1
    const fecha = `${anio.value}-${String(mes.value + 1).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    const citasData = citasMes.value.filter(c => c.fecha === fecha)
    const bloqObj   = bloqueados.value.find(b => b.fecha === fecha)
    return {
      fecha,
      citas: citasData.length,
      citasData,
      bloqueado: !!bloqObj,
      motivoBloqueo: bloqObj?.motivo || null,
    }
  })
})

// ── Colores ──────────────────────────────────────────────────────────────────
function diaClase(dia) {
  if (dia.bloqueado) return 'bg-[#0f0f0f] text-[#555] cursor-not-allowed'
  if (dia.citas === 0) return 'hover:bg-[#2a2a2a]/50'
  if (dia.citas <= 2) return 'bg-green-500/5 hover:bg-green-500/10'
  if (dia.citas <= 4) return 'bg-yellow-400/5 hover:bg-yellow-400/10'
  return 'bg-[#E31837]/5 hover:bg-[#E31837]/10'
}

function colorCitas(dia) {
  if (dia.citas <= 2) return 'text-green-400'
  if (dia.citas <= 4) return 'text-yellow-400'
  return 'text-[#E31837]'
}

function estadoColor(estado) {
  const m = { PENDIENTE: 'bg-yellow-400', EN_CURSO: 'bg-blue-400', FINALIZADO: 'bg-green-500', CANCELADO: 'bg-[#555]' }
  return m[estado] || 'bg-[#555]'
}

function estadoBadge(estado) {
  const m = {
    PENDIENTE:  'bg-yellow-400/20 text-yellow-400',
    EN_CURSO:   'bg-blue-400/20 text-blue-400',
    FINALIZADO: 'bg-green-500/20 text-green-400',
    CANCELADO:  'bg-[#2a2a2a] text-[#555]',
  }
  return m[estado] || m.PENDIENTE
}

// ── Panel lateral ────────────────────────────────────────────────────────────
const citasDia = computed(() => {
  if (!diaSeleccionado.value) return []
  return citasMes.value
    .filter(c => c.fecha === diaSeleccionado.value)
    .sort((a, b) => (a.horaInicio > b.horaInicio ? 1 : -1))
})

const diaBloqueadoSeleccionado = computed(() =>
  bloqueados.value.some(b => b.fecha === diaSeleccionado.value)
)

const labelDiaSeleccionado = computed(() => {
  if (!diaSeleccionado.value) return ''
  const diasNombre = ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
  const d = new Date(diaSeleccionado.value + 'T12:00:00')
  return diasNombre[d.getDay()]
})

function formatFechaDia(fecha) {
  return new Date(fecha + 'T12:00:00').toLocaleDateString('es-CO', { day: 'numeric', month: 'long' })
}

function seleccionarDia(dia) {
  diaSeleccionado.value = dia.fecha
}

// ── Navegación ────────────────────────────────────────────────────────────────
function cambiarMes(delta) {
  let m = mes.value + delta
  let a = anio.value
  if (m > 11) { m = 0; a++ }
  if (m < 0)  { m = 11; a-- }
  mes.value = m
  anio.value = a
  diaSeleccionado.value = null
}

function irHoy() {
  anio.value = hoyDate.getFullYear()
  mes.value  = hoyDate.getMonth()
  diaSeleccionado.value = null
}

// ── Bloquear / desbloquear ────────────────────────────────────────────────────
async function bloquearDia() {
  if (!diaSeleccionado.value) return
  const motivo = prompt('Motivo del bloqueo (opcional):') ?? ''
  try {
    await api.post(`/admin/mantenimientos/dias-bloqueados?fecha=${diaSeleccionado.value}&motivo=${encodeURIComponent(motivo)}`)
    await cargar()
  } catch (e) { alert(e.response?.data?.message || 'Error al bloquear') }
}

async function desbloquearDia() {
  if (!diaSeleccionado.value) return
  if (!confirm('¿Desbloquear este día?')) return
  try {
    await api.delete(`/admin/mantenimientos/dias-bloqueados?fecha=${diaSeleccionado.value}`)
    await cargar()
  } catch (e) { alert(e.response?.data?.message || 'Error al desbloquear') }
}

// ── Carga de datos ────────────────────────────────────────────────────────────
async function cargar() {
  loading.value = true
  const desde = `${anio.value}-${String(mes.value + 1).padStart(2,'0')}-01`
  const lastDay = new Date(anio.value, mes.value + 1, 0).getDate()
  const hasta   = `${anio.value}-${String(mes.value + 1).padStart(2,'0')}-${lastDay}`
  try {
    const [citasRes, bloqRes] = await Promise.all([
      api.get(`/admin/mantenimientos/rango?desde=${desde}&hasta=${hasta}`),
      api.get(`/admin/mantenimientos/disponibilidad?fecha=${desde}`).catch(() => null),
    ])
    citasMes.value   = citasRes.data.data || []
    // Intentar obtener días bloqueados del mes
    bloqueados.value = []
    // Recorrer cada día y verificar disponibilidad para obtener bloqueados
    // Simplificado: si hay error en rango de bloqueados, se ignora
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

// Cargar días bloqueados del mes de forma eficiente
async function cargarBloqueados() {
  const meses = [`${anio.value}-${String(mes.value + 1).padStart(2,'0')}`]
  const promises = []
  for (let d = 1; d <= new Date(anio.value, mes.value + 1, 0).getDate(); d++) {
    const fecha = `${meses[0]}-${String(d).padStart(2,'0')}`
    promises.push(
      api.get(`/admin/mantenimientos/disponibilidad?fecha=${fecha}`)
        .then(r => ({ fecha, bloqueado: r.data.data?.diaBloqueado || false, motivo: r.data.data?.motivoBloqueo }))
        .catch(() => ({ fecha, bloqueado: false }))
    )
  }
  const results = await Promise.all(promises)
  bloqueados.value = results.filter(r => r.bloqueado)
}

watch([anio, mes], () => cargar())
onMounted(async () => { await cargar() })

// ── Modal Nueva Cita ─────────────────────────────────────────────────────────
const showModal     = ref(false)
const loadingCita   = ref(false)
const errorCita     = ref('')
const loadingSlots  = ref(false)
const slotsDisponibles = ref([])

const formCita = ref({
  fecha: '', horaInicio: '',
  nombreCliente: '', emailCliente: '', celularCliente: '',
  tipoBicicleta: '', marcaBicicleta: '', modeloBicicleta: '',
  problemaReportado: '',
})

function abrirModal(fecha) {
  errorCita.value = ''
  slotsDisponibles.value = []
  formCita.value = {
    fecha: fecha || '',
    horaInicio: '',
    nombreCliente: '', emailCliente: '', celularCliente: '',
    tipoBicicleta: '', marcaBicicleta: '', modeloBicicleta: '',
    problemaReportado: '',
  }
  if (fecha) cargarSlots()
  showModal.value = true
}

async function cargarSlots() {
  if (!formCita.value.fecha) return
  loadingSlots.value = true
  slotsDisponibles.value = []
  formCita.value.horaInicio = ''
  try {
    const { data } = await api.get(`/public/mantenimientos/disponibilidad?fecha=${formCita.value.fecha}`)
    const slots = data.data?.slots || []
    slotsDisponibles.value = slots.filter(s => s.disponible)
  } catch { slotsDisponibles.value = [] }
  finally { loadingSlots.value = false }
}

async function crearCita() {
  errorCita.value = ''
  loadingCita.value = true
  try {
    await api.post('/admin/mantenimientos', {
      ...formCita.value,
      // El backend espera LocalTime — enviamos "HH:mm:ss"
      horaInicio: formCita.value.horaInicio.length === 5
        ? formCita.value.horaInicio + ':00'
        : formCita.value.horaInicio,
    })
    showModal.value = false
    await cargar()           // refrescar el calendario
  } catch (e) {
    errorCita.value = e.response?.data?.message || 'Error al agendar la cita.'
  } finally {
    loadingCita.value = false
  }
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
