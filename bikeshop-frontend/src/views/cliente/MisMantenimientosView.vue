<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-black text-white">Mis Mantenimientos</h1>
        <p class="text-[#a0a0a0] text-sm mt-1">{{ citas.length }} cita{{ citas.length !== 1 ? 's' : '' }}</p>
      </div>
      <RouterLink to="/mantenimiento"
        class="shrink-0 bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Agendar cita
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 animate-pulse">
        <div class="flex items-start justify-between gap-4">
          <div class="space-y-2 flex-1">
            <div class="h-4 w-36 bg-[#2a2a2a] rounded" />
            <div class="h-3 w-52 bg-[#2a2a2a] rounded" />
          </div>
          <div class="h-6 w-24 bg-[#2a2a2a] rounded-full shrink-0" />
        </div>
      </div>
    </div>

    <!-- Vacío -->
    <div v-else-if="citas.length === 0"
      class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-16 text-center">
      <p class="text-5xl mb-4">🔧</p>
      <p class="text-white font-semibold text-lg mb-2">Aún no tienes citas agendadas</p>
      <p class="text-[#a0a0a0] text-sm mb-6">¡Agenda tu primera cita de mantenimiento!</p>
      <RouterLink to="/mantenimiento"
        class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl text-sm font-semibold transition-colors">
        Agendar cita
      </RouterLink>
    </div>

    <!-- Lista -->
    <div v-else class="space-y-3">
      <div v-for="cita in citas" :key="cita.id"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden px-5 py-4">
        <div class="flex items-start justify-between gap-3 mb-3">
          <div>
            <p class="text-white font-semibold text-sm">
              {{ formatDate(cita.fecha) }}
              <span v-if="cita.horaInicio" class="text-[#a0a0a0] font-normal"> a las {{ cita.horaInicio }}</span>
            </p>
            <p class="text-[#a0a0a0] text-xs mt-0.5">{{ cita.tipoBicicleta }}</p>
          </div>
          <span class="px-2 py-0.5 rounded-full text-xs font-medium shrink-0"
            :class="estadoBadgeClass(cita.estado)">
            {{ estadoLabel(cita.estado) }}
          </span>
        </div>

        <p class="text-[#a0a0a0] text-sm leading-relaxed line-clamp-2">
          {{ cita.problemaReportado || 'Sin descripción.' }}
        </p>

        <div v-if="cita.estado === 'FINALIZADO'" class="mt-3 pt-3 border-t border-[#2a2a2a] space-y-2">
          <div v-if="cita.diagnostico">
            <p class="text-xs uppercase tracking-widest text-[#a0a0a0] mb-1">Diagnóstico</p>
            <p class="text-white text-sm leading-relaxed">{{ cita.diagnostico }}</p>
          </div>
          <div v-if="cita.costoTotal != null" class="flex items-center justify-between pt-1">
            <span class="text-[#a0a0a0] text-sm">Costo total</span>
            <span class="text-[#E31837] font-bold text-sm">{{ formatPrice(cita.costoTotal) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'

const citas = ref([])
const loading = ref(true)

const ESTADO_LABELS = { PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', EN_CURSO: 'En proceso', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado' }
const ESTADO_CLASSES = { PENDIENTE: 'bg-yellow-500/20 text-yellow-400', CONFIRMADO: 'bg-blue-500/20 text-blue-400', EN_CURSO: 'bg-orange-500/20 text-orange-400', FINALIZADO: 'bg-green-500/20 text-green-400', CANCELADO: 'bg-red-500/20 text-red-400' }

function estadoLabel(e) { return ESTADO_LABELS[e] || e }
function estadoBadgeClass(e) { return ESTADO_CLASSES[e] || 'bg-[#2a2a2a] text-[#a0a0a0]' }
function formatPrice(p) { return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p) }
function formatDate(d) { if (!d) return '—'; return new Intl.DateTimeFormat('es-CO', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(d + 'T00:00:00')) }

async function cargar() {
  loading.value = true
  try {
    const { data } = await api.get('/clientes/mantenimientos')
    citas.value = data.data || data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(cargar)
</script>
