<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">

      <!-- Header -->
      <div class="text-center mb-12">
        <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-3">Taller BikeShop</p>
        <h1 class="text-4xl md:text-5xl font-black mb-4">AGENDAR SERVICIO</h1>
        <p class="text-[#a0a0a0] text-lg max-w-md mx-auto">
          Selecciona un día disponible y completa el formulario.
        </p>
      </div>

      <!-- Confirmación exitosa -->
      <div v-if="exito" class="max-w-md mx-auto bg-[#1a1a1a] border border-green-500/30 rounded-2xl p-10 text-center">
        <div class="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg class="w-8 h-8 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
          </svg>
        </div>
        <h2 class="text-2xl font-black text-white mb-2">¡Cita agendada!</h2>
        <p class="text-[#a0a0a0] text-sm mb-2">
          <strong class="text-white">{{ form.nombreCliente }}</strong>, tu cita ha sido registrada para el
        </p>
        <p class="text-[#E31837] font-bold text-lg mb-6">{{ fechaFormateada(fechaSeleccionada) }}</p>
        <p class="text-[#a0a0a0] text-xs mb-8">
          Recibirás confirmación al correo <strong class="text-white">{{ form.emailCliente }}</strong>
        </p>
        <button @click="resetForm"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white px-8 py-3 rounded-xl font-bold text-sm transition-colors">
          Agendar otra cita
        </button>
      </div>

      <!-- Layout principal -->
      <div v-else class="flex flex-col lg:flex-row gap-8">

        <!-- Calendario -->
        <div class="lg:w-[55%]">
          <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6">
            <!-- Encabezado mes -->
            <div class="flex items-center justify-between mb-6">
              <button @click="prevMes"
                class="w-9 h-9 flex items-center justify-center rounded-lg bg-[#2a2a2a] hover:bg-[#3a3a3a] transition-colors">
                <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
              </button>
              <h2 class="text-lg font-black capitalize">{{ mesAnio }}</h2>
              <button @click="nextMes"
                class="w-9 h-9 flex items-center justify-center rounded-lg bg-[#2a2a2a] hover:bg-[#3a3a3a] transition-colors">
                <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
              </button>
            </div>

            <!-- Días de la semana -->
            <div class="grid grid-cols-7 mb-2">
              <div v-for="d in diasSemana" :key="d"
                class="text-center text-xs font-semibold text-[#a0a0a0] uppercase tracking-wider py-2">
                {{ d }}
              </div>
            </div>

            <!-- Grid días -->
            <div v-if="loadingCalendario" class="grid grid-cols-7 gap-1">
              <div v-for="i in 35" :key="i" class="h-10 bg-[#2a2a2a] rounded-lg animate-pulse" />
            </div>
            <div v-else class="grid grid-cols-7 gap-1">
              <!-- Espacios vacíos inicio -->
              <div v-for="i in primerDiaSemana" :key="`e${i}`" />
              <!-- Días del mes -->
              <button
                v-for="dia in diasDelMes"
                :key="dia.fecha"
                @click="dia.disponible && dia.slotsDisponibles > 0 && !dia.pasado ? seleccionarFecha(dia.fecha) : null"
                class="relative h-10 rounded-lg text-sm font-semibold transition-all flex items-center justify-center"
                :class="[
                  dia.fecha === fechaSeleccionada ? 'bg-[#E31837] text-white ring-2 ring-[#E31837]/50' :
                  dia.pasado || !dia.disponible ? 'text-[#3a3a3a] cursor-not-allowed' :
                  dia.slotsDisponibles === 0 ? 'bg-red-500/10 text-red-400/50 cursor-not-allowed' :
                  'bg-green-500/10 text-green-400 hover:bg-green-500/20 cursor-pointer'
                ]"
              >
                {{ dia.numero }}
                <span v-if="!dia.pasado && dia.disponible && dia.slotsDisponibles > 0 && dia.fecha !== fechaSeleccionada"
                  class="absolute bottom-1 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full bg-green-400" />
              </button>
            </div>

            <!-- Leyenda -->
            <div class="flex items-center justify-center gap-4 mt-5 pt-5 border-t border-[#2a2a2a]">
              <div class="flex items-center gap-1.5 text-xs text-[#a0a0a0]">
                <span class="w-3 h-3 rounded bg-green-500/20" />Disponible
              </div>
              <div class="flex items-center gap-1.5 text-xs text-[#a0a0a0]">
                <span class="w-3 h-3 rounded bg-red-500/10" />Completo
              </div>
              <div class="flex items-center gap-1.5 text-xs text-[#a0a0a0]">
                <span class="w-3 h-3 rounded bg-[#E31837]" />Seleccionado
              </div>
            </div>
          </div>
        </div>

        <!-- Formulario -->
        <div class="lg:w-[45%]">
          <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6">
            <h3 class="text-lg font-black mb-1">Datos de la cita</h3>
            <p v-if="!fechaSeleccionada" class="text-[#a0a0a0] text-sm mb-6">
              Selecciona un día en el calendario.
            </p>
            <p v-else class="text-[#E31837] font-semibold text-sm mb-6">
              📅 {{ fechaFormateada(fechaSeleccionada) }}
              <span v-if="slotsDisponiblesHoy > 0" class="text-[#a0a0a0] font-normal">
                — {{ slotsDisponiblesHoy }} slot{{ slotsDisponiblesHoy !== 1 ? 's' : '' }} disponible{{ slotsDisponiblesHoy !== 1 ? 's' : '' }}
              </span>
            </p>

            <form @submit.prevent="agendar" class="space-y-4">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre completo *</label>
                <input v-model="form.nombreCliente" type="text" required :disabled="!fechaSeleccionada"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40 placeholder-[#4a4a4a]"
                  placeholder="Tu nombre" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Email *</label>
                <input v-model="form.emailCliente" type="email" required :disabled="!fechaSeleccionada"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40 placeholder-[#4a4a4a]"
                  placeholder="tu@email.com" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Teléfono / Celular *</label>
                <input v-model="form.celularCliente" type="tel" required :disabled="!fechaSeleccionada"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40 placeholder-[#4a4a4a]"
                  placeholder="3XX XXX XXXX" />
              </div>
              <!-- Selector de bici registrada (solo para clientes logueados con bicis) -->
              <div v-if="auth.isLoggedIn && missBicis.length > 0">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">¿Para cuál bicicleta?</label>
                <div class="space-y-2">
                  <!-- Sin seleccionar / limpiar -->
                  <button type="button" @click="bicicletaSeleccionada = null; form.bicicletaId = null"
                    class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl border-2 text-sm transition-all text-left"
                    :class="!bicicletaSeleccionada
                      ? 'border-[#E31837] bg-[#E31837]/10 text-white'
                      : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-[#555]'">
                    <span class="text-lg">🔧</span>
                    <span class="font-medium">No especificada / bici nueva</span>
                  </button>
                  <button v-for="b in missBicis" :key="b.id" type="button" @click="seleccionarBici(b)"
                    class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl border-2 text-sm transition-all text-left"
                    :class="bicicletaSeleccionada?.id === b.id
                      ? 'border-[#E31837] bg-[#E31837]/10 text-white'
                      : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-[#555]'">
                    <span class="text-lg">🚲</span>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold truncate">{{ b.marca }} {{ b.modelo }}</p>
                      <p class="text-xs opacity-70">{{ b.tipo }} <span v-if="b.anio">· {{ b.anio }}</span></p>
                    </div>
                  </button>
                </div>
              </div>

              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Tipo de bicicleta *</label>
                <select v-model="form.tipoBicicleta" required :disabled="!fechaSeleccionada"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40">
                  <option value="">Seleccionar tipo</option>
                  <option>MTB</option>
                  <option>Ruta</option>
                  <option>Urbana</option>
                  <option>Eléctrica</option>
                  <option>Otra</option>
                </select>
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Hora de la cita *</label>
                <select v-model="form.horaInicio" required :disabled="!fechaSeleccionada || slotsDisponibles.length === 0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40">
                  <option value="">— Seleccionar hora —</option>
                  <option v-for="slot in slotsDisponibles" :key="slot.hora" :value="slot.hora">
                    {{ slot.hora }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Descripción del problema *</label>
                <textarea v-model="form.problemaReportado" rows="3" required :disabled="!fechaSeleccionada"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors disabled:opacity-40 resize-none placeholder-[#4a4a4a]"
                  placeholder="Describe el problema de tu bicicleta..." />
              </div>

              <p v-if="errorAgendar" class="text-red-400 text-sm">{{ errorAgendar }}</p>

              <button type="submit" :disabled="!fechaSeleccionada || agendando"
                class="w-full py-3.5 rounded-xl font-bold text-sm transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-[#E31837] hover:bg-[#b5112a] text-white">
                {{ agendando ? 'Agendando...' : 'Confirmar cita' }}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

// Bicicletas registradas del cliente
const missBicis = ref([])
const bicicletaSeleccionada = ref(null)

const diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// Fecha actual
const hoy = new Date()
hoy.setHours(0, 0, 0, 0)
const mesActual = ref(hoy.getMonth())
const anioActual = ref(hoy.getFullYear())
const fechaSeleccionada = ref(null)
const disponibilidad = ref([])
const loadingCalendario = ref(false)
const slotsDisponiblesHoy = ref(0)
const slotsDisponibles = ref([])
const exito = ref(false)
const agendando = ref(false)
const errorAgendar = ref('')

const form = ref({
  nombreCliente: '',
  emailCliente: '',
  celularCliente: '',
  tipoBicicleta: '',
  marcaBicicleta: '',
  modeloBicicleta: '',
  horaInicio: '',
  problemaReportado: '',
  bicicletaId: null,
})

const mesAnio = computed(() => {
  return new Date(anioActual.value, mesActual.value, 1)
    .toLocaleDateString('es-CO', { month: 'long', year: 'numeric' })
})

const primerDiaSemana = computed(() => {
  const d = new Date(anioActual.value, mesActual.value, 1).getDay()
  // 0=Dom→6, 1=Lun→0
  return d === 0 ? 6 : d - 1
})

const diasDelMes = computed(() => {
  const year = anioActual.value
  const month = mesActual.value
  const total = new Date(year, month + 1, 0).getDate()
  return Array.from({ length: total }, (_, i) => {
    const num = i + 1
    const fecha = `${year}-${String(month + 1).padStart(2, '0')}-${String(num).padStart(2, '0')}`
    const disp = disponibilidad.value.find(d => d.fecha === fecha)
    const pasado = new Date(year, month, num) < hoy
    return {
      numero: num,
      fecha,
      pasado,
      disponible: disp ? disp.diaDisponible : false,
      slotsDisponibles: disp ? (disp.slots?.filter(s => s.disponible).length ?? 0) : 0,
    }
  })
})

async function cargarDisponibilidad() {
  loadingCalendario.value = true
  try {
    const year = anioActual.value
    const month = mesActual.value
    const ultimoDia = new Date(year, month + 1, 0).getDate()
    const desde = `${year}-${String(month + 1).padStart(2, '0')}-01`
    const hasta = `${year}-${String(month + 1).padStart(2, '0')}-${String(ultimoDia).padStart(2, '0')}`
    const { data } = await api.get(`/public/mantenimientos/disponibilidad/rango?desde=${desde}&hasta=${hasta}`)
    disponibilidad.value = data.data || []
  } catch { disponibilidad.value = [] }
  finally { loadingCalendario.value = false }
}

function prevMes() {
  if (mesActual.value === 0) { mesActual.value = 11; anioActual.value-- }
  else mesActual.value--
  cargarDisponibilidad()
}

function nextMes() {
  if (mesActual.value === 11) { mesActual.value = 0; anioActual.value++ }
  else mesActual.value++
  cargarDisponibilidad()
}

async function seleccionarFecha(fecha) {
  fechaSeleccionada.value = fecha
  form.value.horaInicio = ''
  slotsDisponibles.value = []
  try {
    const { data } = await api.get(`/public/mantenimientos/disponibilidad?fecha=${fecha}`)
    const slots = data.data?.slots || []
    slotsDisponibles.value = slots.filter(s => s.disponible)
    slotsDisponiblesHoy.value = slotsDisponibles.value.length
  } catch {
    slotsDisponiblesHoy.value = 0
    slotsDisponibles.value = []
  }
}

function fechaFormateada(f) {
  if (!f) return ''
  return new Intl.DateTimeFormat('es-CO', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
    .format(new Date(f + 'T00:00:00'))
}

async function agendar() {
  agendando.value = true
  errorAgendar.value = ''
  try {
    // Solo clientes registrados usan el endpoint autenticado;
    // admin/empleados y visitantes usan el público (sin vinculación de cuenta)
    const endpoint = auth.isCliente ? '/clientes/mantenimientos' : '/public/mantenimientos'
    await api.post(endpoint, {
      ...form.value,
      fecha: fechaSeleccionada.value,
    })
    exito.value = true
  } catch (e) {
    errorAgendar.value = e.response?.data?.message || 'Error al agendar la cita. Intenta de nuevo.'
  } finally { agendando.value = false }
}

function resetForm() {
  exito.value = false
  fechaSeleccionada.value = null
  slotsDisponibles.value = []
  slotsDisponiblesHoy.value = 0
  bicicletaSeleccionada.value = null
  form.value = {
    nombreCliente: auth.user?.nombre || '',
    emailCliente: auth.user?.email || '',
    celularCliente: auth.user?.celular || '',
    tipoBicicleta: '', marcaBicicleta: '', modeloBicicleta: '',
    horaInicio: '', problemaReportado: '', bicicletaId: null,
  }
}

onMounted(async () => {
  cargarDisponibilidad()
  // Pre-llenar datos solo si es un CLIENTE registrado (no admin/empleado)
  if (auth.isCliente && auth.user) {
    form.value.nombreCliente = auth.user.nombre || ''
    form.value.emailCliente = auth.user.email || ''
    form.value.celularCliente = auth.user.celular || ''
    // Cargar bicicletas registradas
    try {
      const res = await api.get('/cliente/bicicletas')
      missBicis.value = res.data?.data || res.data || []
    } catch (e) { /* no crítico */ }
    // Si viene preseleccionada desde "Mis Bicicletas"
    const presel = sessionStorage.getItem('biciPreseleccionada')
    if (presel) {
      const b = JSON.parse(presel)
      sessionStorage.removeItem('biciPreseleccionada')
      seleccionarBici(b)
    }
  }
})

function seleccionarBici(bici) {
  bicicletaSeleccionada.value = bici
  form.value.bicicletaId = bici.id
  form.value.marcaBicicleta = bici.marca || ''
  form.value.modeloBicicleta = bici.modelo || ''
  if (bici.tipo) form.value.tipoBicicleta = bici.tipo
}
</script>
