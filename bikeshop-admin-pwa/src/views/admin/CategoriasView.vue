<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <p class="text-[#a0a0a0] text-sm">{{ categorias.length }} categorías registradas</p>
      <button @click="abrirModal()"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nueva categoría
      </button>
    </div>

    <!-- Tabla -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-[#a0a0a0]">Cargando...</div>
      <div v-else-if="categorias.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">🏷️</p>
        <p class="text-[#a0a0a0]">No hay categorías. Crea la primera.</p>
      </div>
      <table v-else class="w-full">
        <thead class="border-b border-[#2a2a2a]">
          <tr class="text-left">
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Imagen</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Nombre</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Slug</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Descripción</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-[#2a2a2a]">
          <tr v-for="cat in categorias" :key="cat.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
            <td class="px-6 py-4">
              <div class="w-12 h-12 rounded-lg overflow-hidden bg-[#0a0a0a] border border-[#2a2a2a] flex items-center justify-center shrink-0">
                <img v-if="cat.iconoUrl" :src="cat.iconoUrl" class="w-full h-full object-cover" />
                <span v-else class="text-[#555] text-xs">Sin img</span>
              </div>
            </td>
            <td class="px-6 py-4 font-semibold text-white">{{ cat.nombre }}</td>
            <td class="px-6 py-4 text-[#a0a0a0] text-sm font-mono">{{ cat.slug }}</td>
            <td class="px-6 py-4 text-[#a0a0a0] text-sm">{{ cat.descripcion || '—' }}</td>
            <td class="px-6 py-4 text-right">
              <button @click="abrirModal(cat)" class="text-[#a0a0a0] hover:text-white text-sm mr-3 transition-colors">
                Editar
              </button>
              <button @click="eliminar(cat.id)" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                Eliminar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-md max-h-[90vh] overflow-y-auto">
          <h2 class="text-xl font-black mb-6">
            {{ editando ? 'Editar categoría' : 'Nueva categoría' }}
          </h2>
          <form @submit.prevent="guardar" class="space-y-4">
            <!-- Nombre -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre *</label>
              <input v-model="form.nombre" type="text" required
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="Ej: Bicicletas de Montaña" />
            </div>

            <!-- Imagen -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Imagen de portada</label>

              <!-- Previsualización -->
              <div v-if="previewUrl" class="mb-3 w-full h-32 rounded-xl overflow-hidden border border-[#2a2a2a] relative group">
                <img :src="previewUrl" class="w-full h-full object-cover" />
                <button type="button" @click="limpiarImagen"
                  class="absolute top-2 right-2 bg-black/60 hover:bg-[#E31837] text-white rounded-full w-7 h-7 text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  ✕
                </button>
              </div>

              <!-- Subir archivo -->
              <label
                class="flex items-center gap-3 w-full bg-[#0a0a0a] border border-dashed border-[#2a2a2a] hover:border-[#E31837] rounded-xl px-4 py-3 cursor-pointer transition-colors mb-2">
                <span class="text-lg">📷</span>
                <span class="text-sm text-[#a0a0a0]">
                  {{ archivoSeleccionado ? archivoSeleccionado.name : 'Subir foto desde tu dispositivo' }}
                </span>
                <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" class="hidden"
                  @change="onArchivoSeleccionado" />
              </label>

              <!-- Separador -->
              <div class="flex items-center gap-3 my-2">
                <div class="flex-1 h-px bg-[#2a2a2a]" />
                <span class="text-xs text-[#555]">o usa URL</span>
                <div class="flex-1 h-px bg-[#2a2a2a]" />
              </div>

              <!-- URL manual -->
              <input v-model="form.iconoUrl" type="url" @input="onUrlInput"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="https://..." />
            </div>

            <!-- Descripción -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">
                Descripción <span class="normal-case text-[#555]">(opcional)</span>
              </label>
              <textarea v-model="form.descripcion" rows="2"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
                placeholder="Descripción breve..." />
            </div>

            <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>

            <div class="flex gap-3 pt-2">
              <button type="button" @click="cerrarModal"
                class="flex-1 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button type="submit" :disabled="saving"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                {{ saving ? 'Guardando...' : 'Guardar' }}
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

const categorias = ref([])
const loading = ref(true)
const modal = ref(false)
const editando = ref(null)
const saving = ref(false)
const error = ref('')
const form = ref({ nombre: '', descripcion: '', iconoUrl: '' })

// Imagen
const fileInput = ref(null)
const archivoSeleccionado = ref(null)
const previewUrl = ref('')

async function cargar() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/categorias')
    categorias.value = data.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function abrirModal(cat = null) {
  editando.value = cat
  form.value = {
    nombre: cat?.nombre || '',
    descripcion: cat?.descripcion || '',
    iconoUrl: cat?.iconoUrl || ''
  }
  archivoSeleccionado.value = null
  previewUrl.value = cat?.iconoUrl || ''
  error.value = ''
  modal.value = true
}

function cerrarModal() {
  modal.value = false
  archivoSeleccionado.value = null
  previewUrl.value = ''
}

function onArchivoSeleccionado(e) {
  const file = e.target.files[0]
  if (!file) return
  archivoSeleccionado.value = file
  form.value.iconoUrl = '' // el archivo tiene prioridad sobre la URL
  // Generar preview local
  const reader = new FileReader()
  reader.onload = ev => { previewUrl.value = ev.target.result }
  reader.readAsDataURL(file)
}

function onUrlInput() {
  // Si escriben una URL, descartan el archivo
  if (form.value.iconoUrl) {
    archivoSeleccionado.value = null
    if (fileInput.value) fileInput.value.value = ''
    previewUrl.value = form.value.iconoUrl
  }
}

function limpiarImagen() {
  form.value.iconoUrl = ''
  archivoSeleccionado.value = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function guardar() {
  error.value = ''
  saving.value = true
  try {
    let categoriaId

    if (editando.value) {
      const { data } = await api.put(`/admin/categorias/${editando.value.id}`, form.value)
      categoriaId = data.data?.id ?? editando.value.id
    } else {
      const { data } = await api.post('/admin/categorias', form.value)
      categoriaId = data.data?.id
    }

    // Si hay archivo seleccionado, subirlo ahora
    if (archivoSeleccionado.value && categoriaId) {
      const fd = new FormData()
      fd.append('archivo', archivoSeleccionado.value)
      await api.post(`/admin/categorias/${categoriaId}/imagen`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }

    cerrarModal()
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta categoría?')) return
  try {
    await api.delete(`/admin/categorias/${id}`)
    await cargar()
  } catch (e) {
    alert(e.response?.data?.message || 'No se pudo eliminar')
  }
}

onMounted(cargar)
</script>
