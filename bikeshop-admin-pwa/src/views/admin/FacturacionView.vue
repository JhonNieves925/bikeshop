<template>
  <div class="space-y-6">

    <!-- Stats HOY -->
    <div>
      <p class="text-xs text-[#a0a0a0] uppercase tracking-widest font-semibold mb-3">
        Resumen de hoy — {{ fechaHoy }}
      </p>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Facturas hoy</p>
          <p class="text-3xl font-black text-white">{{ loadingStats ? '—' : statsHoy.count }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Total facturado hoy</p>
          <p class="text-3xl font-black text-green-400">{{ loadingStats ? '—' : formatPrice(statsHoy.total) }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-6">
          <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-2">Ticket promedio hoy</p>
          <p class="text-3xl font-black text-[#E31837]">{{ loadingStats ? '—' : formatPrice(statsHoy.promedio) }}</p>
        </div>
      </div>
    </div>

    <!-- Historial + filtros -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">

      <!-- Barra de búsqueda y acciones -->
      <div class="px-6 py-4 border-b border-[#2a2a2a] flex flex-col sm:flex-row gap-3 items-start sm:items-end justify-between">
        <div class="flex flex-col sm:flex-row gap-3 flex-1 items-start sm:items-end">

          <!-- Buscar por nombre -->
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#a0a0a0]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z"/>
            </svg>
            <input v-model="filtros.nombre" type="text" placeholder="Buscar cliente..."
              @keyup.enter="buscar"
              class="bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl pl-9 pr-4 py-2.5 text-white text-sm w-48 focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
          </div>

          <!-- Desde -->
          <div>
            <label class="block text-xs text-[#a0a0a0] mb-1">Desde</label>
            <input v-model="filtros.desde" type="date"
              class="bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
          </div>

          <!-- Hasta -->
          <div>
            <label class="block text-xs text-[#a0a0a0] mb-1">Hasta</label>
            <input v-model="filtros.hasta" type="date"
              class="bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
          </div>

          <!-- Botón buscar -->
          <button @click="buscar"
            class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2.5 rounded-xl text-sm font-semibold transition-colors flex items-center gap-2 shrink-0">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z"/>
            </svg>
            Buscar
          </button>

          <!-- Limpiar filtros -->
          <button v-if="hayFiltros" @click="limpiarFiltros"
            class="border border-[#2a2a2a] hover:border-white text-[#a0a0a0] hover:text-white px-4 py-2.5 rounded-xl text-sm font-semibold transition-colors shrink-0">
            Limpiar
          </button>
        </div>

        <!-- Nueva factura -->
        <button @click="abrirNuevaFactura"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white px-5 py-2.5 rounded-xl text-sm font-semibold transition-colors flex items-center gap-2 shrink-0">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nueva factura
        </button>
      </div>

      <!-- Tabla -->
      <div v-if="loading" class="p-8 space-y-3">
        <div v-for="i in 5" :key="i" class="h-12 bg-[#2a2a2a] rounded-lg animate-pulse" />
      </div>

      <div v-else-if="facturas.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">🧾</p>
        <p class="text-white font-semibold mb-1">
          {{ hayFiltros ? 'Sin resultados para ese filtro' : 'Sin facturas todavía' }}
        </p>
        <p class="text-[#a0a0a0] text-sm">
          {{ hayFiltros ? 'Prueba con otras fechas o nombre.' : 'Usa "Nueva factura" para registrar una venta presencial.' }}
        </p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="border-b border-[#2a2a2a]">
            <tr class="text-left">
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">#</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Cliente</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Tipo</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Total</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
              <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Detalle</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#2a2a2a]">
            <tr v-for="f in facturas" :key="f.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
              <td class="px-6 py-4 text-white font-mono font-semibold text-sm">#{{ f.id }}</td>
              <td class="px-6 py-4">
                <p class="text-white text-sm">{{ f.nombreCliente }}</p>
                <p class="text-[#a0a0a0] text-xs">{{ f.emailCliente || '' }}</p>
              </td>
              <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="tipoBadge(f.tipo)">
                  {{ tipoLabel(f.tipo) }}
                </span>
              </td>
              <td class="px-6 py-4 text-[#E31837] font-semibold text-sm">{{ formatPrice(f.total) }}</td>
              <td class="px-6 py-4 text-[#a0a0a0] text-xs">{{ formatFecha(f.fechaEmision) }}</td>
              <td class="px-6 py-4 text-right">
                <button @click="verDetalle(f)" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                  Ver →
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-[#2a2a2a]">
        <button @click="cargar(page - 1)" :disabled="page === 0"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
        <span class="text-sm text-[#a0a0a0]">Página {{ page + 1 }} de {{ totalPages }}</span>
        <button @click="cargar(page + 1)" :disabled="page >= totalPages - 1"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════════════ -->
    <!-- MODAL DETALLE                                                  -->
    <!-- ══════════════════════════════════════════════════════════════ -->
    <Transition name="fade">
      <div v-if="modalDetalle" class="fixed inset-0 bg-black/70 z-50 flex items-start justify-center p-4 overflow-y-auto"
        @click.self="modalDetalle = false">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-8 w-full max-w-lg my-8">
          <div class="flex items-start justify-between mb-6">
            <div>
              <h2 class="text-xl font-black text-white">Factura #{{ facturaActiva?.id }}</h2>
              <p class="text-[#a0a0a0] text-xs mt-1">{{ formatFecha(facturaActiva?.fechaEmision) }}</p>
            </div>
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="tipoBadge(facturaActiva?.tipo)">
              {{ tipoLabel(facturaActiva?.tipo) }}
            </span>
          </div>

          <div class="space-y-2 mb-6">
            <div class="flex justify-between text-sm">
              <span class="text-[#a0a0a0]">Cliente</span>
              <span class="text-white font-medium">{{ facturaActiva?.nombreCliente }}</span>
            </div>
            <div v-if="facturaActiva?.emailCliente" class="flex justify-between text-sm">
              <span class="text-[#a0a0a0]">Email</span>
              <span class="text-white text-xs">{{ facturaActiva?.emailCliente }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#a0a0a0]">Canal</span>
              <span class="text-white">{{ facturaActiva?.canal }}</span>
            </div>
            <div v-if="facturaActiva?.emitidaPorNombre" class="flex justify-between text-sm">
              <span class="text-[#a0a0a0]">Emitida por</span>
              <span class="text-white">{{ facturaActiva?.emitidaPorNombre }}</span>
            </div>
          </div>

          <div class="border border-[#2a2a2a] rounded-xl overflow-hidden mb-4">
            <div class="px-4 py-3 border-b border-[#2a2a2a]">
              <p class="text-xs uppercase tracking-widest text-[#a0a0a0]">Ítems</p>
            </div>
            <div class="divide-y divide-[#2a2a2a]">
              <div v-for="(item, i) in facturaActiva?.items" :key="i"
                class="flex justify-between px-4 py-3 text-sm">
                <div>
                  <p class="text-white">{{ item.descripcion }}</p>
                  <p class="text-[#a0a0a0] text-xs">{{ item.cantidad }} × {{ formatPrice(item.precioUnitario) }}</p>
                </div>
                <span class="text-white font-semibold shrink-0 ml-4">{{ formatPrice(item.subtotal) }}</span>
              </div>
            </div>
          </div>

          <div class="space-y-1 mb-6">
            <div v-if="facturaActiva?.descuento > 0" class="flex justify-between text-sm">
              <span class="text-[#a0a0a0]">Descuento</span>
              <span class="text-yellow-400">- {{ formatPrice(facturaActiva?.descuento) }}</span>
            </div>
            <div class="flex justify-between items-center pt-2 border-t border-[#2a2a2a]">
              <span class="text-white font-semibold uppercase tracking-wider text-sm">Total</span>
              <span class="text-2xl font-black text-[#E31837]">{{ formatPrice(facturaActiva?.total) }}</span>
            </div>
          </div>

          <div v-if="facturaActiva?.observaciones" class="bg-[#0a0a0a] rounded-xl p-4 mb-6">
            <p class="text-xs text-[#a0a0a0] uppercase tracking-widest mb-1">Observaciones</p>
            <p class="text-white text-sm">{{ facturaActiva?.observaciones }}</p>
          </div>

          <button @click="modalDetalle = false"
            class="w-full border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
            Cerrar
          </button>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════════════ -->
    <!-- MODAL NUEVA FACTURA                                           -->
    <!-- ══════════════════════════════════════════════════════════════ -->
    <Transition name="fade">
      <div v-if="modalNueva" class="fixed inset-0 bg-black/70 z-50 flex items-start justify-center p-4 overflow-y-auto"
        @click.self="modalNueva = false">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl w-full max-w-2xl my-8">

          <div class="flex items-center justify-between px-8 pt-8 pb-6 border-b border-[#2a2a2a]">
            <h2 class="text-xl font-black text-white">Nueva Factura Presencial</h2>
            <button @click="modalNueva = false" class="text-[#a0a0a0] hover:text-white transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="px-8 py-6 space-y-6">

            <!-- Datos cliente -->
            <div>
              <p class="text-xs text-[#a0a0a0] uppercase tracking-widest font-semibold mb-3">Datos del cliente</p>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs text-[#a0a0a0] mb-1.5">Nombre *</label>
                  <input v-model="nuevaFactura.nombreCliente" type="text" placeholder="Nombre del cliente"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] mb-1.5">Email (opcional)</label>
                  <input v-model="nuevaFactura.emailCliente" type="email" placeholder="correo@ejemplo.com"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                </div>
              </div>
            </div>

            <!-- Ítems -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <p class="text-xs text-[#a0a0a0] uppercase tracking-widest font-semibold">Ítems</p>
                <div class="flex gap-2">
                  <button @click="agregarItemCatalogo"
                    class="border border-[#2a2a2a] hover:border-[#E31837] text-[#a0a0a0] hover:text-white px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors">
                    + Del catálogo
                  </button>
                  <button @click="agregarItemManual"
                    class="border border-[#2a2a2a] hover:border-[#E31837] text-[#a0a0a0] hover:text-white px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors">
                    + Manual
                  </button>
                </div>
              </div>

              <div v-if="nuevaFactura.items.length === 0"
                class="border border-dashed border-[#2a2a2a] rounded-xl p-8 text-center">
                <p class="text-[#a0a0a0] text-sm">Agrega ítems del catálogo o artículos manuales</p>
              </div>

              <div v-else class="space-y-3">
                <div v-for="(item, idx) in nuevaFactura.items" :key="idx"
                  class="bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl p-4">

                  <div class="flex items-center justify-between mb-3">
                    <span class="text-xs font-semibold px-2 py-0.5 rounded-full"
                      :class="item.tipo === 'catalogo' ? 'bg-purple-500/20 text-purple-400' : 'bg-blue-500/20 text-blue-400'">
                      {{ item.tipo === 'catalogo' ? 'Catálogo' : 'Manual' }}
                    </span>
                    <button @click="eliminarItem(idx)" class="text-[#a0a0a0] hover:text-red-400 transition-colors">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                  </div>

                  <div class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-end">
                    <div v-if="item.tipo === 'catalogo'" class="sm:col-span-5">
                      <label class="block text-xs text-[#a0a0a0] mb-1.5">Producto</label>
                      <select v-model="item.productoId" @change="onProductoSeleccionado(idx)"
                        class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                        <option value="">Seleccionar...</option>
                        <option v-for="p in productos" :key="p.id" :value="p.id">
                          {{ p.nombre }} — {{ formatPrice(p.precio) }} (stock: {{ p.stock }})
                        </option>
                      </select>
                    </div>
                    <div v-else class="sm:col-span-5">
                      <label class="block text-xs text-[#a0a0a0] mb-1.5">Descripción</label>
                      <input v-model="item.descripcion" type="text" placeholder="Ej: Tornillo M8, Grasa cadena..."
                        class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                    </div>
                    <div class="sm:col-span-3">
                      <label class="block text-xs text-[#a0a0a0] mb-1.5">Precio</label>
                      <input v-model.number="item.precioUnitario" type="number" min="0" placeholder="0"
                        class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                    </div>
                    <div class="sm:col-span-2">
                      <label class="block text-xs text-[#a0a0a0] mb-1.5">Cant.</label>
                      <input v-model.number="item.cantidad" type="number" min="1"
                        class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
                    </div>
                    <div class="sm:col-span-2">
                      <label class="block text-xs text-[#a0a0a0] mb-1.5">Subtotal</label>
                      <p class="text-[#E31837] font-semibold text-sm pt-2.5">
                        {{ formatPrice((item.precioUnitario || 0) * (item.cantidad || 1)) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Descuento y observaciones -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs text-[#a0a0a0] mb-1.5">Descuento (opcional)</label>
                <input v-model.number="nuevaFactura.descuento" type="number" min="0" placeholder="0"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] mb-1.5">Observaciones (opcional)</label>
                <input v-model="nuevaFactura.observaciones" type="text" placeholder="Notas internas..."
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
              </div>
            </div>

            <!-- Resumen -->
            <div v-if="nuevaFactura.items.length > 0" class="bg-[#0a0a0a] rounded-xl p-4 space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-[#a0a0a0]">Subtotal</span>
                <span class="text-white">{{ formatPrice(subtotalNueva) }}</span>
              </div>
              <div v-if="nuevaFactura.descuento > 0" class="flex justify-between text-sm">
                <span class="text-[#a0a0a0]">Descuento</span>
                <span class="text-yellow-400">- {{ formatPrice(nuevaFactura.descuento) }}</span>
              </div>
              <div class="flex justify-between items-center pt-2 border-t border-[#2a2a2a]">
                <span class="text-white font-bold">Total a cobrar</span>
                <span class="text-2xl font-black text-[#E31837]">{{ formatPrice(totalNueva) }}</span>
              </div>
            </div>

            <p v-if="errorNueva" class="text-red-400 text-sm">{{ errorNueva }}</p>

            <div class="flex gap-3 pt-2">
              <button @click="modalNueva = false"
                class="flex-1 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
                Cancelar
              </button>
              <button @click="emitirFactura" :disabled="emitiendo"
                class="flex-1 bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl text-sm font-black transition-colors">
                {{ emitiendo ? 'Emitiendo...' : 'Emitir factura' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

// ── Estado ────────────────────────────────────────────────────────────────────
const facturas = ref([])
const loading = ref(true)
const loadingStats = ref(true)
const page = ref(0)
const totalPages = ref(0)

const statsHoy = ref({ count: 0, total: 0, promedio: 0 })

const filtros = ref({ nombre: '', desde: '', hasta: '' })

const modalDetalle = ref(false)
const facturaActiva = ref(null)
const modalNueva = ref(false)
const emitiendo = ref(false)
const errorNueva = ref('')
const productos = ref([])

const nuevaFactura = ref({
  nombreCliente: '', emailCliente: '', descuento: 0, observaciones: '', items: [],
})

// ── Computed ──────────────────────────────────────────────────────────────────
const fechaHoy = computed(() =>
  new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
    .format(new Date())
)
const hayFiltros = computed(() =>
  filtros.value.nombre.trim() || filtros.value.desde || filtros.value.hasta
)
const subtotalNueva = computed(() =>
  nuevaFactura.value.items.reduce((s, i) => s + (i.precioUnitario || 0) * (i.cantidad || 1), 0)
)
const totalNueva = computed(() =>
  Math.max(0, subtotalNueva.value - (nuevaFactura.value.descuento || 0))
)

// ── Stats de hoy ─────────────────────────────────────────────────────────────
async function cargarStatsHoy() {
  loadingStats.value = true
  try {
    // Usar fecha LOCAL (no UTC) para evitar desfase horario con el servidor
    const ahora = new Date()
    const yyyy = ahora.getFullYear()
    const mm = String(ahora.getMonth() + 1).padStart(2, '0')
    const dd = String(ahora.getDate()).padStart(2, '0')
    const hoy = `${yyyy}-${mm}-${dd}`
    const desde = `${hoy}T00:00:00`
    const hasta = `${hoy}T23:59:59`
    const { data } = await api.get(`/admin/facturas?page=0&size=500&desde=${desde}&hasta=${hasta}`)
    const lista = data.data?.content || []
    const total = lista.reduce((s, f) => s + (f.total || 0), 0)
    statsHoy.value = {
      count: lista.length,
      total,
      promedio: lista.length > 0 ? total / lista.length : 0,
    }
  } catch { statsHoy.value = { count: 0, total: 0, promedio: 0 } }
  finally { loadingStats.value = false }
}

// ── Historial ─────────────────────────────────────────────────────────────────
async function cargar(p = 0) {
  page.value = p
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (filtros.value.nombre.trim()) params.set('nombre', filtros.value.nombre.trim())
    if (filtros.value.desde) params.set('desde', `${filtros.value.desde}T00:00:00`)
    if (filtros.value.hasta) params.set('hasta', `${filtros.value.hasta}T23:59:59`)
    const { data } = await api.get(`/admin/facturas?${params}`)
    facturas.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function buscar() { cargar(0) }

function limpiarFiltros() {
  filtros.value = { nombre: '', desde: '', hasta: '' }
  cargar(0)
}

// ── Productos para selector ───────────────────────────────────────────────────
async function cargarProductos() {
  if (productos.value.length > 0) return
  try {
    const { data } = await api.get('/admin/productos?page=0&size=200')
    productos.value = data.data?.content || []
  } catch { productos.value = [] }
}

// ── Nueva factura ─────────────────────────────────────────────────────────────
async function abrirNuevaFactura() {
  nuevaFactura.value = { nombreCliente: '', emailCliente: '', descuento: 0, observaciones: '', items: [] }
  errorNueva.value = ''
  modalNueva.value = true
  await cargarProductos()
}

function agregarItemCatalogo() {
  nuevaFactura.value.items.push({ tipo: 'catalogo', productoId: '', descripcion: '', precioUnitario: 0, cantidad: 1 })
}
function agregarItemManual() {
  nuevaFactura.value.items.push({ tipo: 'manual', productoId: null, descripcion: '', precioUnitario: 0, cantidad: 1 })
}
function onProductoSeleccionado(idx) {
  const item = nuevaFactura.value.items[idx]
  const prod = productos.value.find(p => p.id === item.productoId)
  if (prod) { item.descripcion = prod.nombre; item.precioUnitario = Number(prod.precio) }
}
function eliminarItem(idx) { nuevaFactura.value.items.splice(idx, 1) }

async function emitirFactura() {
  errorNueva.value = ''
  if (!nuevaFactura.value.nombreCliente.trim()) { errorNueva.value = 'El nombre del cliente es obligatorio.'; return }
  if (nuevaFactura.value.items.length === 0) { errorNueva.value = 'Agrega al menos un ítem.'; return }
  for (const item of nuevaFactura.value.items) {
    if (!item.descripcion?.trim()) { errorNueva.value = 'Todos los ítems deben tener descripción.'; return }
    if (!item.precioUnitario || item.precioUnitario <= 0) { errorNueva.value = 'Todos los ítems deben tener precio mayor a 0.'; return }
  }
  emitiendo.value = true
  try {
    const payload = {
      nombreCliente: nuevaFactura.value.nombreCliente.trim(),
      emailCliente: nuevaFactura.value.emailCliente.trim() || null,
      canal: 'PRESENCIAL',
      descuento: nuevaFactura.value.descuento || 0,
      observaciones: nuevaFactura.value.observaciones.trim() || null,
      items: nuevaFactura.value.items.map(i => ({
        productoId: i.tipo === 'catalogo' && i.productoId ? i.productoId : null,
        descripcion: i.descripcion.trim(),
        cantidad: i.cantidad || 1,
        precioUnitario: i.precioUnitario,
      })),
    }
    await api.post('/admin/facturas', payload)
    modalNueva.value = false
    await Promise.all([cargar(0), cargarStatsHoy()])
  } catch (e) {
    errorNueva.value = e.response?.data?.message || 'Error al emitir la factura.'
  } finally { emitiendo.value = false }
}

// ── Detalle ───────────────────────────────────────────────────────────────────
function verDetalle(f) { facturaActiva.value = f; modalDetalle.value = true }

// ── Helpers ───────────────────────────────────────────────────────────────────
function tipoBadge(tipo) {
  return { VENTA: 'bg-purple-500/20 text-purple-400', SERVICIO: 'bg-blue-500/20 text-blue-400', MIXTA: 'bg-orange-500/20 text-orange-400' }[tipo] || 'bg-[#2a2a2a] text-[#a0a0a0]'
}
function tipoLabel(tipo) {
  return { VENTA: 'Venta', SERVICIO: 'Servicio', MIXTA: 'Mixta' }[tipo] || tipo
}
function formatPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(v)
}
function formatFecha(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(iso))
}

onMounted(() => {
  cargarStatsHoy()
  cargar(0)
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
