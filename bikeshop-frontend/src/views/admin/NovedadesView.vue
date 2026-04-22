<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <p class="text-[#a0a0a0] text-sm">{{ total }} novedades registradas</p>
      <button @click="abrirModal()"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nueva novedad
      </button>
    </div>

    <!-- Tabla -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-[#a0a0a0]">Cargando...</div>
      <div v-else-if="novedades.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">📰</p>
        <p class="text-[#a0a0a0]">No hay novedades. Crea la primera.</p>
      </div>
      <table v-else class="w-full">
        <thead class="border-b border-[#2a2a2a]">
          <tr class="text-left">
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Imagen</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Título</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Tipo</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Estado</th>
            <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-[#2a2a2a]">
          <tr v-for="n in novedades" :key="n.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
            <td class="px-6 py-4">
              <div class="w-14 h-10 rounded-lg overflow-hidden bg-[#0a0a0a] border border-[#2a2a2a] flex items-center justify-center shrink-0">
                <img v-if="n.imagenUrl" :src="n.imagenUrl" class="w-full h-full object-cover" />
                <span v-else class="text-[#555] text-xs">Sin img</span>
              </div>
            </td>
            <td class="px-6 py-4 font-semibold text-white max-w-xs">
              <p class="truncate">{{ n.titulo }}</p>
              <p class="text-xs text-[#a0a0a0] truncate mt-0.5">{{ n.resumen }}</p>
            </td>
            <td class="px-6 py-4">
              <span :class="tipoBadge(n.tipo)" class="px-2 py-1 rounded-full text-xs font-semibold">
                {{ tipoLabel(n.tipo) }}
              </span>
            </td>
            <td class="px-6 py-4 text-[#a0a0a0] text-sm">{{ formatFecha(n.creadoEn) }}</td>
            <td class="px-6 py-4">
              <span :class="n.activa ? 'text-green-400' : 'text-[#555]'" class="text-xs font-semibold">
                {{ n.activa ? 'Activa' : 'Oculta' }}
              </span>
            </td>
            <td class="px-6 py-4 text-right space-x-3">
              <button @click="abrirModal(n)" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">
                Editar
              </button>
              <button @click="eliminar(n.id)" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                Eliminar
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="px-6 py-4 border-t border-[#2a2a2a] flex items-center justify-between">
        <button @click="cambiarPagina(paginaActual - 1)" :disabled="paginaActual === 0"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
        <span class="text-xs text-[#a0a0a0]">Página {{ paginaActual + 1 }} de {{ totalPages }}</span>
        <button @click="cambiarPagina(paginaActual + 1)" :disabled="paginaActual >= totalPages - 1"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
      </div>
    </div>

    <!-- Modal -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-lg max-h-[90vh] overflow-y-auto">
          <h2 class="text-xl font-black mb-6">
            {{ editando ? 'Editar novedad' : 'Nueva novedad' }}
          </h2>
          <form @submit.prevent="guardar" class="space-y-4">
            <!-- Título -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Título *</label>
              <input v-model="form.titulo" type="text" required maxlength="150"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="Ej: 20% de descuento en bicicletas de montaña" />
            </div>

            <!-- Tipo -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Tipo *</label>
              <select v-model="form.tipo" required
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors appearance-none">
                <option value="NOVEDAD">📢 Novedad / Noticia</option>
                <option value="DESCUENTO">🏷️ Descuento / Promoción</option>
                <option value="EVENTO">🎉 Evento</option>
              </select>
            </div>

            <!-- Resumen -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Descripción</label>
              <textarea v-model="form.resumen" rows="3"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
                placeholder="Describe brevemente la novedad..." />
            </div>

            <!-- Imagen -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Imagen</label>
              <div v-if="previewUrl" class="mb-3 w-full h-28 rounded-xl overflow-hidden border border-[#2a2a2a] relative group">
                <img :src="previewUrl" class="w-full h-full object-cover" />
                <button type="button" @click="limpiarImagen"
                  class="absolute top-2 right-2 bg-black/60 hover:bg-[#E31837] text-white rounded-full w-7 h-7 text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  ✕
                </button>
              </div>
              <label class="flex items-center gap-3 w-full bg-[#0a0a0a] border border-dashed border-[#2a2a2a] hover:border-[#E31837] rounded-xl px-4 py-3 cursor-pointer transition-colors mb-2">
                <span class="text-lg">📷</span>
                <span class="text-sm text-[#a0a0a0]">{{ archivoSeleccionado ? archivoSeleccionado.name : 'Subir imagen' }}</span>
                <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="onArchivoSeleccionado" />
              </label>
              <div class="flex items-center gap-3 my-2">
                <div class="flex-1 h-px bg-[#2a2a2a]" />
                <span class="text-xs text-[#555]">o usa URL</span>
                <div class="flex-1 h-px bg-[#2a2a2a]" />
              </div>
              <input v-model="form.imagenUrl" type="url" @input="onUrlInput"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="https://..." />
            </div>

            <!-- Contenido completo -->
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">
                Contenido completo <span class="normal-case text-[#555]">(se muestra en el modal)</span>
              </label>
              <textarea v-model="form.contenido" rows="5"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
                placeholder="Escribe aquí el artículo completo..." />
            </div>

            <!-- Opciones -->
            <div class="flex gap-6">
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" v-model="form.activa" class="accent-[#E31837]" />
                <span class="text-sm text-white">Publicar (visible en el sitio)</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" v-model="form.destacada" class="accent-[#E31837]" />
                <span class="text-sm text-white">Destacada</span>
              </label>
            </div>

            <!-- Galería de fotos (solo en modo edición, después de guardar) -->
            <div v-if="editando" class="border-t border-[#2a2a2a] pt-4">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Galería de fotos</p>

              <!-- Fotos existentes -->
              <div v-if="galeriaActual.length" class="grid grid-cols-3 gap-2 mb-3">
                <div v-for="foto in galeriaActual" :key="foto.id"
                  class="relative group h-20 rounded-lg overflow-hidden border border-[#2a2a2a]">
                  <img :src="foto.imagenUrl" class="w-full h-full object-cover" />
                  <button type="button" @click="eliminarFoto(foto.id)"
                    class="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center text-red-400 hover:text-red-300 transition-all text-xl">
                    ✕
                  </button>
                </div>
              </div>
              <p v-else class="text-xs text-[#555] mb-3">Sin fotos en la galería aún.</p>

              <!-- Subir nueva foto -->
              <label class="flex items-center gap-3 w-full bg-[#0a0a0a] border border-dashed border-[#2a2a2a] hover:border-[#E31837] rounded-xl px-4 py-3 cursor-pointer transition-colors">
                <span class="text-lg">📷</span>
                <span class="text-sm text-[#a0a0a0]">{{ subiendoFoto ? 'Subiendo...' : 'Agregar foto a la galería' }}</span>
                <input type="file" accept="image/jpeg,image/png,image/webp" class="hidden"
                  :disabled="subiendoFoto" @change="subirFotoGaleria" />
              </label>
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

const novedades = ref([])
const loading = ref(true)
const modal = ref(false)
const editando = ref(null)
const saving = ref(false)
const error = ref('')
const total = ref(0)
const paginaActual = ref(0)
const totalPages = ref(0)

const form = ref({ titulo: '', resumen: '', contenido: '', imagenUrl: '', tipo: 'NOVEDAD', activa: true, destacada: false })
const fileInput = ref(null)
const archivoSeleccionado = ref(null)
const previewUrl = ref('')
const galeriaActual = ref([])
const subiendoFoto = ref(false)

async function cargar(page = 0) {
  loading.value = true
  paginaActual.value = page
  try {
    const { data } = await api.get(`/admin/novedades?page=${page}&size=15`)
    novedades.value = data.data?.content || []
    total.value = data.data?.totalElements || 0
    totalPages.value = data.data?.totalPages || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function cambiarPagina(p) { if (p >= 0 && p < totalPages.value) cargar(p) }

function abrirModal(n = null) {
  editando.value = n
  form.value = {
    titulo: n?.titulo || '',
    resumen: n?.resumen || '',
    contenido: n?.contenido || '',
    imagenUrl: n?.imagenUrl || '',
    tipo: n?.tipo || 'NOVEDAD',
    activa: n?.activa ?? true,
    destacada: n?.destacada ?? false,
  }
  previewUrl.value = n?.imagenUrl || ''
  archivoSeleccionado.value = null
  galeriaActual.value = n?.galeria || []
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
  form.value.imagenUrl = ''
  const reader = new FileReader()
  reader.onload = ev => { previewUrl.value = ev.target.result }
  reader.readAsDataURL(file)
}

function onUrlInput() {
  if (form.value.imagenUrl) {
    archivoSeleccionado.value = null
    if (fileInput.value) fileInput.value.value = ''
    previewUrl.value = form.value.imagenUrl
  }
}

function limpiarImagen() {
  form.value.imagenUrl = ''
  archivoSeleccionado.value = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function guardar() {
  error.value = ''
  saving.value = true
  try {
    let novedadId
    if (editando.value) {
      const { data } = await api.put(`/admin/novedades/${editando.value.id}`, form.value)
      novedadId = data.data?.id ?? editando.value.id
    } else {
      const { data } = await api.post('/admin/novedades', form.value)
      novedadId = data.data?.id
    }
    if (archivoSeleccionado.value && novedadId) {
      const fd = new FormData()
      fd.append('archivo', archivoSeleccionado.value)
      await api.post(`/admin/novedades/${novedadId}/imagen`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
    cerrarModal()
    await cargar(paginaActual.value)
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

async function subirFotoGaleria(e) {
  const file = e.target.files[0]
  if (!file || !editando.value) return
  subiendoFoto.value = true
  try {
    const fd = new FormData()
    fd.append('archivo', file)
    const { data } = await api.post(`/admin/novedades/${editando.value.id}/galeria`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    galeriaActual.value = data.data?.galeria || []
    // Actualizar la fila en la tabla también
    const idx = novedades.value.findIndex(n => n.id === editando.value.id)
    if (idx >= 0) novedades.value[idx] = data.data
  } catch (e) {
    alert(e.response?.data?.message || 'Error al subir foto')
  } finally {
    subiendoFoto.value = false
    e.target.value = ''
  }
}

async function eliminarFoto(imagenId) {
  if (!editando.value) return
  try {
    await api.delete(`/admin/novedades/${editando.value.id}/galeria/${imagenId}`)
    galeriaActual.value = galeriaActual.value.filter(f => f.id !== imagenId)
  } catch (e) {
    alert(e.response?.data?.message || 'Error al eliminar foto')
  }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta novedad?')) return
  try {
    await api.delete(`/admin/novedades/${id}`)
    await cargar(paginaActual.value)
  } catch (e) {
    alert(e.response?.data?.message || 'No se pudo eliminar')
  }
}

function tipoBadge(tipo) {
  const m = { DESCUENTO: 'bg-yellow-500/20 text-yellow-400', EVENTO: 'bg-purple-500/20 text-purple-400', NOVEDAD: 'bg-blue-500/20 text-blue-400' }
  return m[tipo] || m.NOVEDAD
}
function tipoLabel(tipo) {
  const m = { DESCUENTO: '🏷️ Descuento', EVENTO: '🎉 Evento', NOVEDAD: '📢 Novedad' }
  return m[tipo] || tipo
}
function formatFecha(f) {
  if (!f) return '—'
  return new Date(f).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' })
}

onMounted(() => cargar())
</script>
