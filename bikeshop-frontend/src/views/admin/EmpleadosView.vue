<template>
  <div class="space-y-6">

    <!-- Header -->
    <div class="flex items-center justify-between">
      <p class="text-[#a0a0a0] text-sm">{{ empleados.length }} empleados registrados</p>
      <button @click="abrirModal()"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nuevo empleado
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 4" :key="i" class="h-16 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl animate-pulse" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-8 text-center">
      <p class="text-[#a0a0a0]">{{ loadError }}</p>
    </div>

    <!-- Empty -->
    <div v-else-if="empleados.length === 0" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-12 text-center">
      <p class="text-4xl mb-3">👤</p>
      <p class="text-[#a0a0a0]">No hay empleados. Crea el primero.</p>
    </div>

    <template v-else>
      <!-- Desktop table -->
      <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden hidden md:block">
        <table class="w-full">
          <thead class="border-b border-[#2a2a2a]">
            <tr class="text-left">
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Empleado</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Documento</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Rol</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#2a2a2a]">
            <tr v-for="emp in empleados" :key="emp.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
              <td class="px-6 py-4">
                <p class="text-white text-sm font-medium">{{ emp.nombre }} {{ emp.apellido }}</p>
                <p class="text-[#a0a0a0] text-xs">{{ emp.email }}</p>
              </td>
              <td class="px-6 py-4 text-[#a0a0a0] text-sm font-mono">{{ emp.documento }}</td>
              <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                  :class="emp.rol === 'ADMIN' ? 'bg-[#E31837]/20 text-[#E31837]' : 'bg-blue-500/20 text-blue-400'">
                  {{ emp.rol }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-3">
                <RouterLink :to="`/admin/empleados/${emp.id}`" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                  Jornadas/Pagos
                </RouterLink>
                <button @click="abrirModal(emp)" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">
                  Editar
                </button>
                <button @click="eliminar(emp)" class="text-[#a0a0a0] hover:text-red-400 text-sm transition-colors">
                  Eliminar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile cards -->
      <div class="md:hidden space-y-3">
        <div v-for="emp in empleados" :key="emp.id"
          class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 space-y-3">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-white font-semibold">{{ emp.nombre }} {{ emp.apellido }}</p>
              <p class="text-[#a0a0a0] text-xs">{{ emp.email }}</p>
              <p class="text-[#a0a0a0] text-xs font-mono mt-1">{{ emp.documento }}</p>
            </div>
            <div class="flex flex-col items-end gap-1">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                :class="emp.rol === 'ADMIN' ? 'bg-[#E31837]/20 text-[#E31837]' : 'bg-blue-500/20 text-blue-400'">
                {{ emp.rol }}
              </span>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                :class="emp.activo ? 'bg-green-500/20 text-green-400' : 'bg-[#2a2a2a] text-[#a0a0a0]'">
                {{ emp.activo ? 'Activo' : 'Inactivo' }}
              </span>
            </div>
          </div>
          <div class="flex gap-3 pt-1 border-t border-[#2a2a2a]">
            <RouterLink :to="`/admin/empleados/${emp.id}`" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
              Jornadas/Pagos
            </RouterLink>
            <button @click="abrirModal(emp)" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">
              Editar
            </button>
            <button @click="eliminar(emp)" class="text-[#a0a0a0] hover:text-red-400 text-sm transition-colors">
              Eliminar
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- Modal crear/editar -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4 overflow-y-auto"
        @click.self="modal = false">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-lg my-8">
          <h2 class="text-xl font-black text-white mb-6">
            {{ editando ? 'Editar empleado' : 'Nuevo empleado' }}
          </h2>

          <form @submit.prevent="guardar" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">

              <!-- Nombre -->
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre *</label>
                <input v-model="form.nombre" type="text" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Nombre" />
              </div>

              <!-- Apellido -->
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Apellido *</label>
                <input v-model="form.apellido" type="text" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Apellido" />
              </div>

              <!-- Documento -->
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Documento *</label>
                <input v-model="form.documento" type="text" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Número de documento" />
              </div>

              <!-- Rol -->
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Rol *</label>
                <select v-model="form.rol" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                  <option value="EMPLEADO">EMPLEADO</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>

              <!-- Email -->
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Email *</label>
                <input v-model="form.email" type="email" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="correo@ejemplo.com" />
              </div>

              <!-- Teléfono -->
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Teléfono</label>
                <input v-model="form.telefono" type="tel"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="+57 300 000 0000" />
              </div>

              <!-- Clave -->
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">
                  Contraseña {{ editando ? '' : '*' }}
                </label>
                <input v-model="form.clave" type="password" :required="!editando"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  :placeholder="editando ? 'Dejar en blanco para mantener la clave actual' : 'Contraseña'" />
                <p v-if="editando && form.clave" class="text-yellow-400 text-xs mt-1">
                  Se actualizará la contraseña del empleado.
                </p>
              </div>

            </div>

            <p v-if="formError" class="text-[#E31837] text-sm">{{ formError }}</p>

            <div class="flex gap-3 pt-2">
              <button type="button" @click="modal = false"
                class="flex-1 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button type="submit" :disabled="saving"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                {{ saving ? 'Guardando...' : editando ? 'Actualizar' : 'Crear empleado' }}
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

const empleados = ref([])
const loading = ref(true)
const loadError = ref('')
const modal = ref(false)
const editando = ref(null)
const saving = ref(false)
const formError = ref('')

const form = ref({
  nombre: '',
  apellido: '',
  documento: '',
  clave: '',
  rol: 'EMPLEADO',
  email: '',
  telefono: '',
})

async function cargar() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await api.get('/admin/empleados')
    empleados.value = data.data || []
  } catch (e) {
    loadError.value = e.response?.data?.message || 'No se pudo cargar la lista de empleados.'
  } finally {
    loading.value = false
  }
}

function abrirModal(emp = null) {
  editando.value = emp
  formError.value = ''
  form.value = {
    nombre: emp?.nombre || '',
    apellido: emp?.apellido || '',
    documento: emp?.documento || '',
    clave: '',
    rol: emp?.rol || 'EMPLEADO',
    email: emp?.email || '',
    telefono: emp?.telefono || '',
  }
  modal.value = true
}

async function guardar() {
  formError.value = ''
  saving.value = true
  try {
    const payload = {
      nombre: form.value.nombre,
      apellido: form.value.apellido,
      documento: form.value.documento,
      rol: form.value.rol,
      email: form.value.email,
      telefono: form.value.telefono || null,
    }

    // Incluir clave: obligatoria en crear, solo si tiene valor en editar
    if (!editando.value || form.value.clave) {
      payload.contrasena = form.value.clave
    }

    if (editando.value) {
      await api.put(`/admin/empleados/${editando.value.id}`, payload)
    } else {
      await api.post('/admin/empleados', payload)
    }

    modal.value = false
    await cargar()
  } catch (e) {
    formError.value = e.response?.data?.message || 'Error al guardar el empleado.'
  } finally {
    saving.value = false
  }
}

async function eliminar(emp) {
  const nombre = `${emp.nombre} ${emp.apellido || ''}`.trim()
  if (!confirm(`¿Eliminar a ${nombre} permanentemente? Esta acción no se puede deshacer.`)) return
  try {
    await api.delete(`/admin/empleados/${emp.id}`)
    await cargar()
  } catch (e) {
    alert(e.response?.data?.message || 'Error al eliminar el empleado.')
  }
}

onMounted(() => cargar())
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
