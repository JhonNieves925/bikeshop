<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between gap-3">
      <p class="text-[#a0a0a0] text-sm">{{ total }} productos registrados</p>
      <button @click="abrirModal()"
        class="shrink-0 bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        + Nuevo producto
      </button>
    </div>

    <!-- Búsqueda -->
    <div class="relative">
      <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#a0a0a0]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
      </svg>
      <input v-model="busqueda" type="text" placeholder="Buscar por nombre..."
        class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl pl-10 pr-4 py-3 text-white text-sm placeholder-[#a0a0a0] focus:outline-none focus:border-[#E31837] transition-colors" />
    </div>

    <!-- Contenido -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-[#a0a0a0]">Cargando...</div>
      <div v-else-if="productos.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">🚲</p>
        <p class="text-[#a0a0a0]">No hay productos. Crea el primero.</p>
      </div>
      <div v-else-if="productosFiltrados.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">🔍</p>
        <p class="text-[#a0a0a0]">No se encontraron productos con ese nombre.</p>
      </div>

      <template v-else>
        <!-- MÓVIL: cards -->
        <div class="md:hidden divide-y divide-[#2a2a2a]">
          <div v-for="p in productosFiltrados" :key="p.id"
            class="flex items-start gap-3 px-4 py-4 hover:bg-[#2a2a2a]/40 transition-colors">
            <div class="w-14 h-14 bg-[#0a0a0a] rounded-lg flex items-center justify-center overflow-hidden shrink-0">
              <img v-if="p.imagenPrincipalUrl" :src="imgUrl(p.imagenPrincipalUrl)" class="w-full h-full object-cover" />
              <span v-else class="text-2xl">🚲</span>
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex items-start justify-between gap-2">
                <p class="text-white text-sm font-semibold truncate">{{ p.nombre }}</p>
                <span v-if="p.tieneTallas"
                  class="shrink-0 text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#E31837]/10 text-[#E31837] border border-[#E31837]/20 uppercase tracking-wide">
                  Tallas
                </span>
              </div>
              <p class="text-[#a0a0a0] text-xs mt-0.5">{{ p.categoriaNombre || 'Sin categoría' }}</p>
              <div class="flex items-center gap-2 mt-1 flex-wrap">
                <span class="text-[#E31837] font-bold text-xs">{{ formatPrice(p.precio) }}</span>
                <span class="text-xs" :class="p.stock <= 3 ? 'text-red-400' : 'text-[#a0a0a0]'">Stock: {{ p.stock }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                  :class="p.activo ? 'bg-green-500/20 text-green-400' : 'bg-[#2a2a2a] text-[#a0a0a0]'">
                  {{ p.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </div>
              <div class="flex items-center gap-3 mt-2">
                <button @click="abrirModal(p)" class="text-[#a0a0a0] hover:text-white text-xs transition-colors">Editar</button>
                <button @click="toggleActivo(p)" class="text-[#a0a0a0] hover:text-yellow-400 text-xs transition-colors">
                  {{ p.activo ? 'Desactivar' : 'Activar' }}
                </button>
                <button @click="eliminar(p)" class="text-[#a0a0a0] hover:text-red-400 text-xs transition-colors">Eliminar</button>
              </div>
            </div>
          </div>
        </div>

        <!-- DESKTOP: tabla -->
        <table class="hidden md:table w-full">
          <thead class="border-b border-[#2a2a2a]">
            <tr class="text-left">
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Producto</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Categoría</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Precio</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Stock</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Estado</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#2a2a2a]">
            <tr v-for="p in productosFiltrados" :key="p.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-[#0a0a0a] rounded-lg flex items-center justify-center overflow-hidden shrink-0">
                    <img v-if="p.imagenPrincipalUrl" :src="imgUrl(p.imagenPrincipalUrl)" class="w-full h-full object-cover" />
                    <span v-else class="text-xl">🚲</span>
                  </div>
                  <div>
                    <span class="text-white text-sm font-medium">{{ p.nombre }}</span>
                    <span v-if="p.tieneTallas"
                      class="ml-2 text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#E31837]/10 text-[#E31837] border border-[#E31837]/20 uppercase tracking-wide">
                      Tallas
                    </span>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-[#a0a0a0] text-sm">{{ p.categoriaNombre || '—' }}</td>
              <td class="px-6 py-4 text-[#E31837] font-semibold text-sm">{{ formatPrice(p.precio) }}</td>
              <td class="px-6 py-4 text-sm" :class="p.stock <= 3 ? 'text-red-400' : 'text-white'">{{ p.stock }}</td>
              <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                  :class="p.activo ? 'bg-green-500/20 text-green-400' : 'bg-[#2a2a2a] text-[#a0a0a0]'">
                  {{ p.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-3">
                <button @click="abrirModal(p)" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">Editar</button>
                <button @click="toggleActivo(p)" class="text-[#a0a0a0] hover:text-yellow-400 text-sm transition-colors">
                  {{ p.activo ? 'Desactivar' : 'Activar' }}
                </button>
                <button @click="eliminar(p)" class="text-[#a0a0a0] hover:text-red-400 text-sm transition-colors">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </template>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-[#2a2a2a]">
        <button @click="page--; cargar()" :disabled="page === 0"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
        <span class="text-sm text-[#a0a0a0]">Página {{ page + 1 }} de {{ totalPages }}</span>
        <button @click="page++; cargar()" :disabled="page >= totalPages - 1"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
      </div>
    </div>

    <!-- Modal -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-start justify-center p-4 overflow-y-auto">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-lg my-8">
          <h2 class="text-xl font-black mb-6">{{ editando ? 'Editar producto' : 'Nuevo producto' }}</h2>
          <form @submit.prevent="guardar" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre *</label>
                <input v-model="form.nombre" type="text" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Nombre del producto" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Precio *</label>
                <input v-model="form.precio" type="number" required min="0" step="100"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="0" />
              </div>
              <!-- Stock: solo visible cuando NO hay tallas -->
              <div v-if="!form.usaTallas">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Stock</label>
                <input v-model="form.stock" type="number" min="0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="0" />
              </div>
              <div v-else>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Stock total</label>
                <div class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-[#a0a0a0] text-sm">
                  {{ stockTotalTallas }} <span class="text-xs">(suma de tallas)</span>
                </div>
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Stock mínimo</label>
                <input v-model="form.stockMinimo" type="number" min="0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="0" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Categoría</label>
                <select v-model="form.categoriaId"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                  <option value="">Sin categoría</option>
                  <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
                </select>
              </div>
              <div class="col-span-2">
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Descripción</label>
                <textarea v-model="form.descripcion" rows="3"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none"
                  placeholder="Descripción del producto..." />
              </div>
            </div>

            <!-- ── Tallas ─────────────────────────────────────────────── -->
            <div class="border-t border-[#2a2a2a] pt-4">
              <div class="flex items-center justify-between mb-1">
                <div>
                  <p class="text-xs text-[#a0a0a0] uppercase tracking-wider font-semibold">Tallas / Medidas</p>
                  <p class="text-[#5a5a5a] text-[11px] mt-0.5">Opcional. Actívalo si el producto tiene diferentes tallas.</p>
                </div>
                <!-- Toggle switch -->
                <button type="button" @click="form.usaTallas = !form.usaTallas"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors shrink-0"
                  :class="form.usaTallas ? 'bg-[#E31837]' : 'bg-[#3a3a3a]'">
                  <span class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform shadow-sm"
                    :class="form.usaTallas ? 'translate-x-6' : 'translate-x-1'" />
                </button>
              </div>

              <template v-if="form.usaTallas">
                <!-- Chips rápidos -->
                <div class="flex flex-wrap gap-2 mt-3 mb-3">
                  <button v-for="chip in tallasRapidas" :key="chip" type="button"
                    @click="agregarTallaRapida(chip)"
                    :disabled="form.tallas.some(t => t.talla.toUpperCase() === chip)"
                    class="px-3 py-1 rounded-lg text-xs border transition-colors font-medium"
                    :class="form.tallas.some(t => t.talla.toUpperCase() === chip)
                      ? 'border-[#E31837] text-[#E31837] bg-[#E31837]/10 cursor-not-allowed'
                      : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-white hover:text-white'">
                    {{ chip }}
                  </button>
                </div>

                <!-- Lista de tallas -->
                <div class="space-y-2 mb-2">
                  <div v-for="(t, i) in form.tallas" :key="i" class="flex items-center gap-2">
                    <input v-model="t.talla" type="text" placeholder="Ej: L, XL, 29..."
                      @blur="t.talla = t.talla.trim().toUpperCase()"
                      class="flex-1 bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors uppercase placeholder-[#555]" />
                    <input v-model.number="t.stock" type="number" min="0" placeholder="Stock"
                      class="w-24 bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors text-center" />
                    <button type="button" @click="form.tallas.splice(i, 1)"
                      class="w-8 h-8 flex items-center justify-center text-[#a0a0a0] hover:text-[#E31837] hover:bg-[#E31837]/10 rounded-lg transition-colors text-lg leading-none shrink-0">
                      ×
                    </button>
                  </div>
                </div>

                <!-- Agregar talla personalizada -->
                <button type="button" @click="form.tallas.push({ talla: '', stock: 0 })"
                  class="flex items-center gap-1.5 text-sm text-[#a0a0a0] hover:text-white transition-colors py-1">
                  <span class="text-lg leading-none">+</span> Agregar otra talla
                </button>

                <p v-if="form.tallas.length > 0" class="text-xs text-[#a0a0a0] mt-2 border-t border-[#2a2a2a] pt-2">
                  Stock total calculado: <span class="text-white font-semibold">{{ stockTotalTallas }}</span>
                </p>
              </template>
            </div>

            <!-- ── Galería de imágenes ────────────────────────── -->
            <div class="border-t border-[#2a2a2a] pt-4">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">
                Fotos del producto
                <span v-if="galeriaProducto.length > 0" class="ml-1 text-white">({{ galeriaProducto.length }})</span>
              </p>

              <!-- Imágenes existentes -->
              <div v-if="galeriaProducto.length > 0" class="grid grid-cols-4 gap-2 mb-3">
                <div v-for="img in galeriaProducto" :key="img.id"
                  class="relative group aspect-square rounded-xl overflow-hidden border-2 transition-colors cursor-pointer"
                  :class="img.esPrincipal ? 'border-[#E31837]' : 'border-[#2a2a2a] hover:border-[#a0a0a0]'"
                  @click="marcarPrincipal(img)">
                  <img :src="imgUrl(img.url)" class="w-full h-full object-cover" />
                  <!-- Badge principal -->
                  <span v-if="img.esPrincipal"
                    class="absolute top-1 left-1 bg-[#E31837] text-white text-[9px] font-bold px-1.5 py-0.5 rounded-full leading-none">
                    ★
                  </span>
                  <!-- Botón eliminar -->
                  <button type="button" @click.stop="eliminarFoto(img)"
                    class="absolute top-1 right-1 bg-black/70 text-white rounded-full w-5 h-5 text-xs items-center justify-center hidden group-hover:flex transition-all hover:bg-red-600">
                    ×
                  </button>
                </div>
              </div>

              <p v-if="galeriaProducto.length > 0" class="text-[#a0a0a0] text-xs mb-2">
                Toca una foto para marcarla como <span class="text-[#E31837]">★ principal</span>
              </p>

              <!-- Subir nueva foto -->
              <label class="flex items-center gap-3 cursor-pointer bg-[#0a0a0a] border border-dashed border-[#2a2a2a] hover:border-[#E31837] rounded-xl px-4 py-3 transition-colors group">
                <span class="text-2xl group-hover:scale-110 transition-transform">📷</span>
                <div>
                  <p class="text-white text-sm font-medium">
                    {{ subiendoFoto ? 'Subiendo...' : editando ? 'Agregar foto' : 'Foto principal' }}
                  </p>
                  <p class="text-[#a0a0a0] text-xs">JPG, PNG o WEBP · máx 5MB</p>
                </div>
                <input type="file" accept="image/*" class="hidden"
                  @change="editando ? subirFoto($event) : onFileChange($event)" :disabled="subiendoFoto" />
              </label>
            </div>

            <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>
            <div class="flex gap-3 pt-2">
              <button type="button" @click="modal = false"
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
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'
import { imgUrl } from '@/utils/imgUrl'

const TALLAS_RAPIDAS = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'ÚNICA']

const productos = ref([])
const categorias = ref([])
const loading = ref(true)
const modal = ref(false)
const editando = ref(null)
const saving = ref(false)
const error = ref('')
const page = ref(0)
const total = ref(0)
const totalPages = ref(0)
const imagenFile = ref(null)
const busqueda = ref('')
const galeriaProducto = ref([])
const subiendoFoto = ref(false)

const tallasRapidas = TALLAS_RAPIDAS

const form = ref({
  nombre: '', descripcion: '', precio: '', stock: '', stockMinimo: '2', categoriaId: '',
  usaTallas: false, tallas: []
})

const stockTotalTallas = computed(() =>
  form.value.tallas.reduce((s, t) => s + (Number(t.stock) || 0), 0)
)

const productosFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return productos.value
  return productos.value.filter(p => p.nombre.toLowerCase().includes(q))
})

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(price)
}

function agregarTallaRapida(chip) {
  if (form.value.tallas.some(t => t.talla.toUpperCase() === chip)) return
  form.value.tallas.push({ talla: chip, stock: 0 })
}

async function cargar() {
  loading.value = true
  try {
    const { data } = await api.get(`/admin/productos?page=${page.value}&size=10`)
    productos.value = data.data?.content || []
    total.value = data.data?.totalElements || 0
    totalPages.value = data.data?.totalPages || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function cargarCategorias() {
  const { data } = await api.get('/admin/categorias')
  categorias.value = data.data || []
}

async function abrirModal(prod = null) {
  editando.value = prod
  imagenFile.value = null
  galeriaProducto.value = []
  form.value = {
    nombre: prod?.nombre || '',
    descripcion: prod?.descripcion || '',
    precio: prod?.precio || '',
    stock: prod?.stock ?? '',
    stockMinimo: prod?.stockMinimo ?? '2',
    categoriaId: prod?.categoriaId || '',
    usaTallas: prod?.tieneTallas || false,
    tallas: []
  }
  error.value = ''
  modal.value = true
  if (prod) await cargarGaleria(prod.id)
}

async function cargarGaleria(id) {
  try {
    const { data } = await api.get(`/admin/productos/${id}`)
    galeriaProducto.value = data.data?.imagenes || []
    if (data.data?.tieneTallas) {
      form.value.tallas = (data.data?.tallas || []).map(t => ({ talla: t.talla, stock: t.stock }))
    }
  } catch (e) { console.error('Error cargando galería', e) }
}

async function subirFoto(event) {
  const file = event.target.files[0]
  if (!file || !editando.value) return
  subiendoFoto.value = true
  try {
    const fd = new FormData()
    fd.append('archivo', file)
    fd.append('esPrincipal', galeriaProducto.value.length === 0 ? 'true' : 'false')
    await api.post(`/admin/productos/${editando.value.id}/imagenes`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await cargarGaleria(editando.value.id)
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al subir imagen'
  } finally {
    subiendoFoto.value = false
    event.target.value = ''
  }
}

async function eliminarFoto(img) {
  if (!confirm('¿Eliminar esta foto?')) return
  try {
    await api.delete(`/admin/productos/${editando.value.id}/imagenes/${img.id}`)
    await cargarGaleria(editando.value.id)
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al eliminar imagen'
  }
}

async function marcarPrincipal(img) {
  if (img.esPrincipal) return
  try {
    await api.patch(`/admin/productos/${editando.value.id}/imagenes/${img.id}/principal`)
    await cargarGaleria(editando.value.id)
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al marcar como principal'
  }
}

function onFileChange(e) { imagenFile.value = e.target.files[0] || null }

async function guardar() {
  error.value = ''
  saving.value = true
  try {
    const tallasValidas = form.value.usaTallas
      ? form.value.tallas.filter(t => t.talla?.trim())
      : null

    const payload = {
      nombre: form.value.nombre,
      descripcion: form.value.descripcion || '',
      precio: Number(form.value.precio),
      stock: form.value.usaTallas ? 0 : Number(form.value.stock),
      stockMinimo: Number(form.value.stockMinimo) || 2,
      categoriaId: form.value.categoriaId || null,
      tallas: tallasValidas
    }
    let productoId
    if (editando.value) {
      await api.put(`/admin/productos/${editando.value.id}`, { ...payload, activo: true })
      productoId = editando.value.id
    } else {
      const { data } = await api.post('/admin/productos', payload)
      productoId = data.data?.id
    }
    if (imagenFile.value && productoId) {
      const fd = new FormData()
      fd.append('archivo', imagenFile.value)
      fd.append('esPrincipal', 'true')
      await api.post(`/admin/productos/${productoId}/imagenes`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    }
    modal.value = false
    await cargar()
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al guardar'
  } finally { saving.value = false }
}

async function toggleActivo(prod) {
  try {
    await api.patch(`/admin/productos/${prod.id}/toggle`)
    await cargar()
  } catch { alert('Error al cambiar estado') }
}

async function eliminar(prod) {
  if (!confirm(`¿Seguro que deseas eliminar "${prod.nombre}"?\n\nEsta acción no se puede deshacer.`)) return
  try {
    await api.delete(`/admin/productos/${prod.id}`)
    await cargar()
  } catch (e) {
    alert(e.response?.data?.message || 'No se pudo eliminar el producto')
  }
}

onMounted(async () => { await Promise.all([cargar(), cargarCategorias()]) })
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
