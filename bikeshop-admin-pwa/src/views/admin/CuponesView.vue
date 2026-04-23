<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between gap-3">
      <p class="text-[#a0a0a0] text-sm">{{ cupones.length }} cupón{{ cupones.length !== 1 ? 'es' : '' }} registrado{{ cupones.length !== 1 ? 's' : '' }}</p>
      <button @click="abrirModal()"
        class="shrink-0 bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nuevo cupón
      </button>
    </div>

    <!-- Tabla -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-[#a0a0a0]">Cargando...</div>
      <div v-else-if="cupones.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">🏷️</p>
        <p class="text-[#a0a0a0]">No hay cupones. Crea el primero.</p>
      </div>

      <div v-else>
        <!-- Mobile cards -->
        <div class="md:hidden divide-y divide-[#2a2a2a]">
          <div v-for="c in cupones" :key="c.id" class="px-4 py-4 space-y-2">
            <div class="flex items-center justify-between">
              <span class="text-white font-black text-sm tracking-widest">{{ c.codigo }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                :class="c.activo ? 'bg-green-500/20 text-green-400' : 'bg-[#2a2a2a] text-[#a0a0a0]'">
                {{ c.activo ? 'Activo' : 'Inactivo' }}
              </span>
            </div>
            <p class="text-[#E31837] font-semibold text-sm">
              {{ c.tipo === 'PORCENTAJE' ? c.descuento + '% off' : formatPrice(c.descuento) + ' off' }}
            </p>
            <div class="flex gap-1 text-xs text-[#a0a0a0] flex-wrap">
              <span v-if="c.fechaExpira">Expira: {{ formatFecha(c.fechaExpira) }}</span>
              <span v-else>Sin expiración</span>
              <span>·</span>
              <span>{{ c.usosActuales }}{{ c.usosMax ? `/${c.usosMax}` : '' }} usos</span>
            </div>
            <div class="flex gap-4 pt-1">
              <button @click="toggle(c)" class="text-xs text-[#a0a0a0] hover:text-yellow-400 transition-colors">
                {{ c.activo ? 'Desactivar' : 'Activar' }}
              </button>
              <button @click="eliminar(c)" class="text-xs text-[#a0a0a0] hover:text-red-400 transition-colors">Eliminar</button>
            </div>
          </div>
        </div>

        <!-- Desktop table -->
        <table class="hidden md:table w-full">
          <thead class="border-b border-[#2a2a2a]">
            <tr class="text-left">
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Código</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Descuento</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Expiración</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Usos</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Estado</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#2a2a2a]">
            <tr v-for="c in cupones" :key="c.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
              <td class="px-6 py-4 text-white font-black tracking-widest text-sm">{{ c.codigo }}</td>
              <td class="px-6 py-4 text-[#E31837] font-semibold text-sm">
                {{ c.tipo === 'PORCENTAJE' ? c.descuento + '%' : formatPrice(c.descuento) }}
              </td>
              <td class="px-6 py-4 text-[#a0a0a0] text-sm">
                {{ c.fechaExpira ? formatFecha(c.fechaExpira) : '—' }}
              </td>
              <td class="px-6 py-4 text-[#a0a0a0] text-sm">
                {{ c.usosActuales }}{{ c.usosMax ? ` / ${c.usosMax}` : '' }}
              </td>
              <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                  :class="c.activo ? 'bg-green-500/20 text-green-400' : 'bg-[#2a2a2a] text-[#a0a0a0]'">
                  {{ c.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-3">
                <button @click="toggle(c)" class="text-[#a0a0a0] hover:text-yellow-400 text-sm transition-colors">
                  {{ c.activo ? 'Desactivar' : 'Activar' }}
                </button>
                <button @click="eliminar(c)" class="text-[#a0a0a0] hover:text-red-400 text-sm transition-colors">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal crear -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-md">
          <h2 class="text-xl font-black mb-6">Nuevo cupón</h2>
          <form @submit.prevent="guardar" class="space-y-4">
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Código *</label>
              <input v-model="form.codigo" type="text" required maxlength="30"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors uppercase"
                placeholder="Ej: BICI20" />
              <p class="text-[#555] text-xs mt-1">Se guardará en mayúsculas</p>
            </div>

            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Tipo de descuento *</label>
              <div class="flex gap-3">
                <label class="flex-1 flex items-center gap-2 cursor-pointer bg-[#0a0a0a] border rounded-xl px-4 py-3 transition-colors"
                  :class="form.tipo === 'PORCENTAJE' ? 'border-[#E31837] text-[#E31837]' : 'border-[#2a2a2a] text-[#a0a0a0]'">
                  <input v-model="form.tipo" type="radio" value="PORCENTAJE" class="hidden" />
                  <span class="text-sm font-medium">% Porcentaje</span>
                </label>
                <label class="flex-1 flex items-center gap-2 cursor-pointer bg-[#0a0a0a] border rounded-xl px-4 py-3 transition-colors"
                  :class="form.tipo === 'VALOR_FIJO' ? 'border-[#E31837] text-[#E31837]' : 'border-[#2a2a2a] text-[#a0a0a0]'">
                  <input v-model="form.tipo" type="radio" value="VALOR_FIJO" class="hidden" />
                  <span class="text-sm font-medium">$ Valor fijo</span>
                </label>
              </div>
            </div>

            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">
                {{ form.tipo === 'PORCENTAJE' ? 'Porcentaje (%)' : 'Monto fijo (COP)' }} *
              </label>
              <input v-model="form.descuento" type="number" required min="0.01"
                :max="form.tipo === 'PORCENTAJE' ? 100 : undefined"
                :step="form.tipo === 'PORCENTAJE' ? '0.1' : '1000'"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                :placeholder="form.tipo === 'PORCENTAJE' ? 'Ej: 15 (para 15%)' : 'Ej: 20000'" />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Fecha expiración</label>
                <input v-model="form.fechaExpira" type="date" :min="hoy"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
                <p class="text-[#555] text-xs mt-1">Dejar vacío = sin límite</p>
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Usos máximos</label>
                <input v-model="form.usosMax" type="number" min="1"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="∞ ilimitado" />
              </div>
            </div>

            <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>
            <div class="flex gap-3 pt-2">
              <button type="button" @click="modal = false"
                class="flex-1 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button type="submit" :disabled="saving"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                {{ saving ? 'Guardando...' : 'Crear cupón' }}
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

const cupones = ref([])
const loading = ref(true)
const modal   = ref(false)
const saving  = ref(false)
const error   = ref('')
const hoy     = new Date().toISOString().split('T')[0]

const form = ref({ codigo: '', tipo: 'PORCENTAJE', descuento: '', fechaExpira: '', usosMax: '' })

function formatPrice(p) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p)
}
function formatFecha(f) {
  return new Date(f + 'T00:00:00').toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' })
}

async function cargar() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/cupones')
    cupones.value = data.data || []
  } finally { loading.value = false }
}

function abrirModal() {
  form.value = { codigo: '', tipo: 'PORCENTAJE', descuento: '', fechaExpira: '', usosMax: '' }
  error.value = ''
  modal.value = true
}

async function guardar() {
  error.value = ''
  saving.value = true
  try {
    const payload = {
      codigo: form.value.codigo.toUpperCase().trim(),
      tipo: form.value.tipo,
      descuento: Number(form.value.descuento),
      fechaExpira: form.value.fechaExpira || null,
      usosMax: form.value.usosMax ? Number(form.value.usosMax) : null,
    }
    await api.post('/admin/cupones', payload)
    modal.value = false
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al crear el cupón'
  } finally { saving.value = false }
}

async function toggle(c) {
  try {
    await api.patch(`/admin/cupones/${c.id}/toggle`)
    await cargar()
  } catch { alert('Error al cambiar estado') }
}

async function eliminar(c) {
  if (!confirm(`¿Eliminar el cupón "${c.codigo}"? Esta acción no se puede deshacer.`)) return
  try {
    await api.delete(`/admin/cupones/${c.id}`)
    await cargar()
  } catch { alert('Error al eliminar') }
}

onMounted(cargar)
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
