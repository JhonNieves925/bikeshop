import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))
  const isOpen = ref(false)

  // ── Cupón ─────────────────────────────────────────────────────────────────
  const cupon = ref(null) // { codigo, tipo, descuento, montoDescuento, totalConDescuento }

  const subtotal = computed(() =>
    items.value.reduce((sum, item) => sum + item.precio * item.cantidad, 0)
  )

  const total = computed(() => subtotal.value)

  const totalItems = computed(() =>
    items.value.reduce((sum, item) => sum + item.cantidad, 0)
  )

  const descuentoCupon = computed(() =>
    cupon.value ? Number(cupon.value.montoDescuento) : 0
  )

  const totalConDescuento = computed(() =>
    Math.max(0, subtotal.value - descuentoCupon.value)
  )

  /**
   * Agrega un producto al carrito.
   * @param {Object} producto - Datos del producto
   * @param {number} cantidad - Cantidad a agregar (default 1)
   * @param {string|null} talla - Talla seleccionada (null si el producto no tiene tallas)
   */
  function agregar(producto, cantidad = 1, talla = null) {
    // Clave única: "id" o "id_TALLA" para distinguir tallas del mismo producto
    const key = talla ? `${producto.id}_${talla}` : String(producto.id)

    const existente = items.value.find(i => i.key === key)
    if (existente) {
      existente.cantidad += cantidad
    } else {
      items.value.push({
        key,
        id: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        imagenUrl: producto.imagenUrl,
        imagenPrincipalUrl: producto.imagenPrincipalUrl,
        talla: talla || null,
        cantidad
      })
    }
    // Si hay cupón aplicado, quitarlo para re-validar (el subtotal cambió)
    cupon.value = null
    guardar()
  }

  function quitar(key) {
    items.value = items.value.filter(i => i.key !== key)
    cupon.value = null
    guardar()
  }

  function actualizarCantidad(key, cantidad) {
    const item = items.value.find(i => i.key === key)
    if (item) {
      if (cantidad <= 0) quitar(key)
      else item.cantidad = cantidad
    }
    cupon.value = null
    guardar()
  }

  function vaciar() {
    items.value = []
    cupon.value = null
    guardar()
  }

  function aplicarCupon(datos) {
    cupon.value = datos
  }

  function quitarCupon() {
    cupon.value = null
  }

  function guardar() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return {
    items, isOpen, cupon,
    subtotal, total, totalItems, descuentoCupon, totalConDescuento,
    agregar, quitar, actualizarCantidad, vaciar,
    aplicarCupon, quitarCupon
  }
})
