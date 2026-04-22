<template>
  <div>
    <!-- Hero -->
    <section class="relative min-h-[90vh] flex items-center overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-r from-[#0a0a0a] via-[#0a0a0a]/80 to-transparent z-10" />
      <div class="absolute inset-0 bg-[#141414]">
        <div class="absolute inset-0 opacity-20"
          style="background-image: url('data:image/svg+xml,%3Csvg width=\'60\' height=\'60\' viewBox=\'0 0 60 60\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cg fill=\'none\' fill-rule=\'evenodd\'%3E%3Cg fill=\'%23E31837\' fill-opacity=\'0.4\'%3E%3Ccircle cx=\'1\' cy=\'1\' r=\'1\'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')">
        </div>
      </div>

      <div class="relative z-20 max-w-7xl mx-auto px-6 py-20">
        <Transition name="slide-up" appear>
          <div>
            <p class="text-[#E31837] text-sm font-semibold tracking-[0.3em] uppercase mb-4">Nueva temporada</p>
            <h1 class="text-6xl md:text-8xl font-black leading-none tracking-tight mb-6">
              RIDE<br>
              <span class="text-[#E31837]">BEYOND</span><br>
              LIMITS
            </h1>
            <p class="text-[#a0a0a0] text-lg max-w-md mb-10 leading-relaxed">
              Las mejores bicicletas y accesorios para llevar tu experiencia al siguiente nivel.
            </p>
            <div class="flex gap-4 flex-wrap">
              <RouterLink to="/catalogo"
                class="bg-[#E31837] hover:bg-[#b5112a] text-white px-8 py-4 rounded-xl font-bold text-sm tracking-wide uppercase transition-all hover:scale-105">
                Ver catálogo
              </RouterLink>
              <RouterLink to="/mantenimiento"
                class="border border-[#2a2a2a] hover:border-white text-white px-8 py-4 rounded-xl font-bold text-sm tracking-wide uppercase transition-all">
                Agendar servicio
              </RouterLink>
            </div>
          </div>
        </Transition>
      </div>
    </section>

    <!-- Categorías -->
    <section class="py-20">
      <div class="max-w-7xl mx-auto px-6 flex items-end justify-between mb-10">
        <div>
          <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Explora</p>
          <h2 class="text-3xl font-black">CATEGORÍAS</h2>
        </div>
        <RouterLink to="/catalogo" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">
          Ver todo →
        </RouterLink>
      </div>

      <div v-if="loadingCategorias" class="flex overflow-x-auto snap-x snap-mandatory gap-4 pb-4 carousel-pl pr-6 scrollbar-hide">
        <div v-for="i in 4" :key="i" class="h-80 w-[70vw] md:w-72 shrink-0 bg-[#141414] rounded-2xl animate-pulse" />
      </div>

      <div v-else ref="carouselCats" class="flex overflow-x-auto snap-x snap-mandatory gap-4 pb-4 carousel-pl pr-6 scrollbar-hide">
        <RouterLink
          v-for="cat in categorias"
          :key="cat.id"
          :to="`/catalogo?categoria=${cat.slug}`"
          class="group relative h-80 w-[70vw] md:w-72 shrink-0 snap-start rounded-2xl overflow-hidden"
        >
          <!-- Imagen de fondo -->
          <img v-if="cat.iconoUrl" :src="cat.iconoUrl" :alt="cat.nombre"
            class="absolute inset-0 w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-105 transition-all duration-500" />
          <!-- Fondo fallback si no hay imagen -->
          <div v-else class="absolute inset-0 bg-[#1a1a1a] flex items-center justify-center">
            <span class="text-5xl opacity-20">🚲</span>
          </div>
          <!-- Overlay degradado -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/85 via-black/20 to-transparent" />
          <!-- Borde rojo hover -->
          <div class="absolute inset-0 ring-2 ring-transparent group-hover:ring-[#E31837] rounded-2xl transition-all duration-300" />
          <!-- Texto + flecha -->
          <div class="absolute bottom-0 left-0 right-0 p-5 flex items-end justify-between">
            <p class="text-white font-bold text-base leading-tight group-hover:text-[#E31837] transition-colors">
              {{ cat.nombre }}
            </p>
            <span class="w-8 h-8 rounded-full bg-white/10 group-hover:bg-[#E31837] flex items-center justify-center text-white text-sm transition-all duration-300 shrink-0 ml-2">
              →
            </span>
          </div>
        </RouterLink>
      </div>
      <!-- Flechas desktop -->
      <div class="hidden md:flex justify-end gap-2 mt-5 max-w-7xl mx-auto px-6">
        <button @click="scrollCarousel(carouselCats, -1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">‹</button>
        <button @click="scrollCarousel(carouselCats,  1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">›</button>
      </div>
    </section>

    <!-- Productos destacados -->
    <section id="destacados" class="bg-[#141414] py-20">
      <div class="max-w-7xl mx-auto px-6 flex items-end justify-between mb-10">
        <div>
          <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Lo mejor</p>
          <h2 class="text-3xl font-black">DESTACADOS</h2>
        </div>
        <RouterLink to="/catalogo" class="text-[#a0a0a0] hover:text-white text-sm transition-colors">
          Ver todo →
        </RouterLink>
      </div>

      <div v-if="loadingProductos" class="flex overflow-x-auto snap-x snap-mandatory gap-6 pb-4 carousel-pl pr-6 scrollbar-hide">
        <div v-for="i in 4" :key="i" class="h-[370px] w-[72vw] md:w-64 shrink-0 bg-[#1a1a1a] rounded-xl animate-pulse" />
      </div>

      <div v-else ref="carouselDest" class="flex overflow-x-auto snap-x snap-mandatory gap-6 pb-4 carousel-pl pr-6 scrollbar-hide">
        <div v-for="p in productos" :key="p.id" class="w-[72vw] md:w-64 shrink-0 snap-start">
          <ProductCard :producto="p" />
        </div>
      </div>

      <!-- Flechas desktop -->
      <div class="hidden md:flex justify-end gap-2 mt-5 max-w-7xl mx-auto px-6">
        <button @click="scrollCarousel(carouselDest, -1)" class="w-9 h-9 rounded-full border border-[#3a3a3a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">‹</button>
        <button @click="scrollCarousel(carouselDest,  1)" class="w-9 h-9 rounded-full border border-[#3a3a3a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">›</button>
      </div>
    </section>

    <!-- Novedades -->
    <section id="novedades" class="py-20">
      <div class="max-w-7xl mx-auto px-6 mb-10">
        <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Mantente informado</p>
        <h2 class="text-3xl font-black">NOVEDADES</h2>
      </div>

      <!-- Skeleton -->
      <div v-if="loadingNovedades" class="flex overflow-x-auto snap-x snap-mandatory gap-6 pb-4 carousel-pl pr-6 scrollbar-hide">
        <div v-for="i in 3" :key="i" class="h-[420px] w-[80vw] md:w-80 shrink-0 bg-[#141414] rounded-2xl animate-pulse" />
      </div>

      <!-- Sin novedades -->
      <div v-else-if="novedades.length === 0"
        class="max-w-7xl mx-auto px-6 text-center py-12 text-[#555] border border-dashed border-[#2a2a2a] rounded-xl">
        <p class="text-3xl mb-2">📰</p>
        <p class="text-sm">Próximamente novedades y eventos.</p>
      </div>

      <!-- Cards full-bleed -->
      <div v-else ref="carouselNov" class="flex overflow-x-auto snap-x snap-mandatory gap-6 pb-4 carousel-pl pr-6 scrollbar-hide">
        <article v-for="n in novedades" :key="n.id"
          @click="abrirNovedad(n)"
          class="group relative w-[80vw] md:w-80 h-[420px] shrink-0 snap-start rounded-2xl overflow-hidden cursor-pointer">
          <!-- Imagen full-bleed -->
          <img v-if="n.imagenUrl" :src="n.imagenUrl" :alt="n.titulo"
            class="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
          <!-- Fallback sin imagen -->
          <div v-else class="absolute inset-0 bg-[#1a1a1a] flex items-center justify-center">
            <span class="text-8xl opacity-10">{{ tipoIcono(n.tipo) }}</span>
          </div>
          <!-- Gradiente de desvanecido (fuerte abajo, transparente arriba) -->
          <div class="absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent" />
          <!-- Badge tipo arriba izquierda -->
          <span :class="tipoBadge(n.tipo)"
            class="absolute top-4 left-4 px-2.5 py-1 rounded-full text-xs font-bold">
            {{ tipoLabel(n.tipo) }}
          </span>
          <!-- Badge destacado arriba derecha -->
          <span v-if="n.destacada"
            class="absolute top-4 right-4 bg-[#E31837] text-white px-2 py-1 rounded-full text-xs font-bold">
            ⭐
          </span>
          <!-- Contenido superpuesto abajo -->
          <div class="absolute bottom-0 left-0 right-0 p-6 pr-16">
            <p class="text-white/50 text-xs mb-2 uppercase tracking-wider">{{ formatFecha(n.creadoEn) }}</p>
            <h3 class="text-white font-black text-xl leading-tight mb-2 group-hover:text-[#E31837] transition-colors line-clamp-3">
              {{ n.titulo }}
            </h3>
            <p v-if="n.resumen" class="text-white/65 text-sm leading-relaxed line-clamp-2">
              {{ n.resumen }}
            </p>
          </div>
          <!-- Botón + abajo derecha -->
          <div class="absolute bottom-6 right-5 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm group-hover:bg-[#E31837] flex items-center justify-center text-white text-xl font-bold transition-all duration-300">
            +
          </div>
        </article>
      </div>
      <!-- Flechas desktop novedades -->
      <div v-if="novedades.length > 0" class="hidden md:flex justify-end gap-2 mt-5 max-w-7xl mx-auto px-6">
        <button @click="scrollCarousel(carouselNov, -1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">‹</button>
        <button @click="scrollCarousel(carouselNov,  1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">›</button>
      </div>
    </section>

    <!-- Modal novedad -->
    <Transition name="fade">
      <div v-if="novedadActiva" class="fixed inset-0 bg-black/80 z-50 flex items-start justify-center p-4 overflow-y-auto"
        @click.self="novedadActiva = null">
        <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl w-full max-w-2xl my-8 overflow-hidden">
          <!-- Header -->
          <div class="relative">
            <!-- Imagen de portada grande -->
            <div v-if="novedadActiva.imagenUrl" class="h-64 overflow-hidden">
              <img :src="novedadActiva.imagenUrl" :alt="novedadActiva.titulo"
                class="w-full h-full object-cover" />
            </div>
            <!-- Gradiente sobre la imagen -->
            <div v-if="novedadActiva.imagenUrl"
              class="absolute inset-0 bg-gradient-to-t from-[#141414] via-[#141414]/20 to-transparent" />
            <!-- Botón cerrar -->
            <button @click="novedadActiva = null"
              class="absolute top-4 right-4 bg-black/60 hover:bg-[#E31837] text-white rounded-full w-9 h-9 flex items-center justify-center transition-colors text-lg">
              ✕
            </button>
            <!-- Badge tipo -->
            <span :class="tipoBadge(novedadActiva.tipo)"
              class="absolute top-4 left-4 px-3 py-1 rounded-full text-xs font-bold">
              {{ tipoLabel(novedadActiva.tipo) }}
            </span>
          </div>

          <!-- Contenido -->
          <div class="p-8">
            <p class="text-xs text-[#555] mb-2">{{ formatFecha(novedadActiva.creadoEn) }}</p>
            <h2 class="text-2xl font-black text-white mb-3 leading-tight">{{ novedadActiva.titulo }}</h2>
            <p v-if="novedadActiva.resumen" class="text-[#a0a0a0] text-base mb-6 leading-relaxed border-l-2 border-[#E31837] pl-4">
              {{ novedadActiva.resumen }}
            </p>
            <div v-if="novedadActiva.contenido"
              class="text-[#c0c0c0] text-sm leading-relaxed whitespace-pre-line mb-8">
              {{ novedadActiva.contenido }}
            </div>

            <!-- Galería de fotos -->
            <div v-if="novedadActiva.galeria?.length" class="space-y-4">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-widest font-semibold border-t border-[#2a2a2a] pt-6">
                Fotos
              </p>
              <div v-for="(foto, idx) in novedadActiva.galeria" :key="foto.id ?? idx"
                class="rounded-xl overflow-hidden border border-[#2a2a2a] cursor-zoom-in"
                @click="fotoAmpliada = foto.imagenUrl">
                <img :src="foto.imagenUrl" :alt="`Foto ${idx + 1}`"
                  class="w-full object-cover hover:scale-[1.01] transition-transform duration-300" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Lightbox foto ampliada -->
    <Transition name="fade">
      <div v-if="fotoAmpliada" class="fixed inset-0 bg-black/95 z-[60] flex items-center justify-center p-4"
        @click="fotoAmpliada = null">
        <button class="absolute top-4 right-4 text-white text-2xl hover:text-[#E31837] transition-colors">✕</button>
        <img :src="fotoAmpliada" class="max-w-full max-h-[90vh] object-contain rounded-xl" />
      </div>
    </Transition>

    <!-- Testimonios -->
    <section class="py-20">
      <div class="max-w-7xl mx-auto px-6 text-center mb-12">
        <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Opiniones</p>
        <h2 class="text-3xl font-black">LO QUE DICEN NUESTROS CLIENTES</h2>
        <div class="flex items-center justify-center gap-2 mt-3">
          <div class="flex gap-0.5 text-yellow-400 text-lg">★★★★★</div>
          <span class="text-white font-bold">4.9</span>
          <span class="text-[#a0a0a0] text-sm">· +320 reseñas verificadas</span>
        </div>
      </div>

      <div ref="carouselTest" class="flex overflow-x-auto snap-x snap-mandatory gap-6 pb-4 carousel-pl pr-6 scrollbar-hide">
        <article v-for="r in testimonios" :key="r.nombre"
          class="w-[82vw] md:w-96 shrink-0 snap-start bg-[#141414] border border-[#2a2a2a] rounded-2xl p-6 hover:border-[#E31837]/40 transition-colors">
          <!-- Estrellas -->
          <div class="flex gap-0.5 text-yellow-400 text-sm mb-4">
            <span v-for="i in r.estrellas" :key="i">★</span>
          </div>
          <!-- Comentario -->
          <p class="text-[#c0c0c0] text-sm leading-relaxed mb-5">"{{ r.comentario }}"</p>
          <!-- Autor -->
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-full flex items-center justify-center text-white text-sm font-black shrink-0"
              :style="`background-color: ${r.color}`">
              {{ r.inicial }}
            </div>
            <div>
              <p class="text-white text-sm font-semibold">{{ r.nombre }}</p>
              <p class="text-[#555] text-xs">{{ r.producto }}</p>
            </div>
          </div>
        </article>
      </div>
      <!-- Flechas desktop testimonios -->
      <div class="hidden md:flex justify-end gap-2 mt-5 max-w-7xl mx-auto px-6">
        <button @click="scrollCarousel(carouselTest, -1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">‹</button>
        <button @click="scrollCarousel(carouselTest,  1)" class="w-9 h-9 rounded-full border border-[#2a2a2a] hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all text-lg">›</button>
      </div>
    </section>

    <!-- Sobre Nosotros -->
    <section class="relative py-28 overflow-hidden">
      <!-- Imagen de fondo — reemplaza la URL por tu propia foto -->
      <img
        src="https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=1600&q=80"
        alt=""
        aria-hidden="true"
        class="absolute inset-0 w-full h-full object-cover object-center scale-105"
      />
      <!-- Overlay degradado: oscuro a la izquierda donde está el texto, semiopaco a la derecha -->
      <div class="absolute inset-0 bg-gradient-to-r from-black/90 via-black/70 to-black/50" />
      <!-- Línea roja decorativa superior -->
      <div class="absolute top-0 left-0 right-0 h-1 bg-[#0a0a0a]" />

      <div class="relative z-10 max-w-7xl mx-auto px-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <!-- Texto -->
          <div>
            <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-3">Nuestra historia</p>
            <h2 class="text-4xl font-black leading-tight mb-6 text-white">
              PASIÓN POR<br>
              <span class="text-[#E31837]">LAS RUEDAS</span>
            </h2>
            <p class="text-white/70 leading-relaxed mb-5">
              Somos una tienda especializada en bicicletas, fundada con la misión de acercar el ciclismo a todos.
              Desde bicicletas de montaña hasta ruta, pasando por accesorios y repuestos, tenemos todo lo que
              necesitas para rodar.
            </p>
            <p class="text-white/70 leading-relaxed mb-8">
              Además de vender productos de calidad, contamos con un taller de mantenimiento donde nuestro
              equipo de mecánicos expertos deja tu bici como nueva.
            </p>
            <RouterLink to="/catalogo"
              class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-8 py-4 rounded-xl font-bold text-sm tracking-wide uppercase transition-all hover:scale-105">
              Conoce nuestros productos
            </RouterLink>
          </div>

          <!-- Stats -->
          <div class="grid grid-cols-2 gap-4">
            <div class="bg-black/50 backdrop-blur-sm border border-white/10 rounded-xl p-6 text-center hover:border-[#E31837] transition-colors">
              <p class="text-4xl font-black text-[#E31837] mb-1">+500</p>
              <p class="text-white/60 text-sm">Clientes satisfechos</p>
            </div>
            <div class="bg-black/50 backdrop-blur-sm border border-white/10 rounded-xl p-6 text-center hover:border-[#E31837] transition-colors">
              <p class="text-4xl font-black text-[#E31837] mb-1">+200</p>
              <p class="text-white/60 text-sm">Productos disponibles</p>
            </div>
            <div class="bg-black/50 backdrop-blur-sm border border-white/10 rounded-xl p-6 text-center hover:border-[#E31837] transition-colors">
              <p class="text-4xl font-black text-[#E31837] mb-1">+5</p>
              <p class="text-white/60 text-sm">Años de experiencia</p>
            </div>
            <div class="bg-black/50 backdrop-blur-sm border border-white/10 rounded-xl p-6 text-center hover:border-[#E31837] transition-colors">
              <p class="text-4xl font-black text-[#E31837] mb-1">+20</p>
              <p class="text-white/60 text-sm">Marcas reconocidas</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Banner mantenimiento -->
    <section class="max-w-7xl mx-auto px-6 py-20">
      <div class="bg-[#E31837] rounded-2xl p-10 md:p-16 flex flex-col md:flex-row items-center justify-between gap-8">
        <div>
          <h2 class="text-3xl md:text-4xl font-black text-white mb-3">¿Tu bici necesita servicio?</h2>
          <p class="text-white/80 text-lg">Agenda tu cita de mantenimiento en minutos.</p>
        </div>
        <RouterLink to="/mantenimiento"
          class="bg-white text-[#E31837] px-8 py-4 rounded-xl font-black text-sm tracking-wide uppercase hover:bg-gray-100 transition-all shrink-0">
          Agendar ahora
        </RouterLink>
      </div>
    </section>

    <!-- Ubicación -->
    <section class="bg-[#141414] py-20">
      <div class="max-w-7xl mx-auto px-6">
        <div class="mb-10 text-center">
          <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Visítanos</p>
          <h2 class="text-3xl font-black">DÓNDE ESTAMOS</h2>
          <p class="text-[#a0a0a0] mt-3 max-w-md mx-auto">
            Estamos en el corazón de Cali, listos para atenderte de lunes a sábado.
          </p>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
          <!-- Mapa + link -->
          <div class="space-y-2">
            <div class="rounded-2xl overflow-hidden border border-[#2a2a2a] h-80">
              <iframe
                src="https://www.openstreetmap.org/export/embed.html?bbox=-76.5756%2C3.3416%2C-76.4244%2C3.5616&layer=mapnik&marker=3.4516%2C-76.5000"
                width="100%" height="100%" style="border:0" loading="lazy"
                title="Ubicación BikeShop — Cali, Colombia">
              </iframe>
            </div>
            <p class="text-xs text-[#555] text-right pr-1">
              <a href="https://www.openstreetmap.org/?mlat=3.4516&mlon=-76.5000#map=13/3.4516/-76.5000"
                target="_blank" rel="noopener" class="hover:text-[#a0a0a0] transition-colors">
                Ver mapa más grande ↗
              </a>
            </p>
          </div>

          <!-- Info -->
          <div class="space-y-4">
            <div class="flex items-start gap-4 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 hover:border-[#E31837] transition-colors">
              <span class="text-2xl shrink-0">📍</span>
              <div>
                <p class="text-white font-bold mb-1">Dirección</p>
                <p class="text-[#a0a0a0] text-sm">Cali, Valle del Cauca, Colombia</p>
              </div>
            </div>
            <div class="flex items-start gap-4 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 hover:border-[#E31837] transition-colors">
              <span class="text-2xl shrink-0">📞</span>
              <div>
                <p class="text-white font-bold mb-1">Teléfono</p>
                <p class="text-[#a0a0a0] text-sm">+57 (2) 123-4567</p>
              </div>
            </div>
            <div class="flex items-start gap-4 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 hover:border-[#E31837] transition-colors">
              <span class="text-2xl shrink-0">✉️</span>
              <div>
                <p class="text-white font-bold mb-1">Email</p>
                <p class="text-[#a0a0a0] text-sm">info@bikeshop.com</p>
              </div>
            </div>
            <div class="flex items-start gap-4 bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 hover:border-[#E31837] transition-colors">
              <span class="text-2xl shrink-0">🕐</span>
              <div>
                <p class="text-white font-bold mb-1">Horario</p>
                <p class="text-[#a0a0a0] text-sm">Lunes – Sábado: 8:00 am – 6:00 pm</p>
                <p class="text-[#a0a0a0] text-sm">Domingos: 9:00 am – 2:00 pm</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'
import ProductCard from '@/components/common/ProductCard.vue'

const categorias = ref([])
const productos = ref([])
const novedades = ref([])

const testimonios = [
  { nombre: 'Andrés Molina', inicial: 'A', color: '#E31837', estrellas: 5,
    comentario: 'Excelente atención, me ayudaron a elegir la bicicleta perfecta para mis rutas de montaña. El servicio de mantenimiento es top.',
    producto: 'Bicicleta MTB Pro 29"' },
  { nombre: 'Laura Castillo', inicial: 'L', color: '#7c3aed', estrellas: 5,
    comentario: 'Compré unos pedales y llegaron en perfectas condiciones. El envío fue rapidísimo y el empaque impecable. Volvería a comprar sin duda.',
    producto: 'Pedales de aluminio' },
  { nombre: 'Carlos Reyes', inicial: 'C', color: '#059669', estrellas: 5,
    comentario: 'Llevé mi bici al taller y quedó como nueva. Los mecánicos saben lo que hacen, precios justos y muy amables. 100% recomendado.',
    producto: 'Servicio de mantenimiento' },
  { nombre: 'Valentina Torres', inicial: 'V', color: '#d97706', estrellas: 4,
    comentario: 'Gran variedad de productos y buenos precios. La app es muy fácil de usar para hacer pedidos. Me encantó la experiencia de compra.',
    producto: 'Casco urbano + luces' },
  { nombre: 'Diego Herrera', inicial: 'D', color: '#0891b2', estrellas: 5,
    comentario: 'Llevo 2 años comprando aquí. Siempre tienen stock, los productos son originales y el trato es muy personalizado.',
    producto: 'Kit de herramientas' },
  { nombre: 'Sofía Gómez', inicial: 'S', color: '#be185d', estrellas: 5,
    comentario: 'Agendé el mantenimiento en línea y fue muy fácil. Me recordaron la cita por WhatsApp. ¡Servicio 10/10!',
    producto: 'Mantenimiento completo' },
]
const loadingCategorias = ref(true)
const loadingProductos = ref(true)
const loadingNovedades = ref(true)
const novedadActiva = ref(null)
const fotoAmpliada = ref(null)

// Refs para navegación con flechas en desktop
const carouselCats = ref(null)
const carouselDest = ref(null)
const carouselNov  = ref(null)
const carouselTest = ref(null)

function scrollCarousel(el, dir) {
  if (!el) return
  el.scrollBy({ left: dir * 340, behavior: 'smooth' })
}

function abrirNovedad(n) {
  novedadActiva.value = n
  fotoAmpliada.value = null
}

onMounted(async () => {
  // Categorías y productos juntos
  try {
    const [catRes, prodRes] = await Promise.all([
      api.get('/public/categorias'),
      api.get('/public/productos?page=0&size=8')
    ])
    categorias.value = catRes.data.data?.slice(0, 8) || []
    productos.value = prodRes.data.data?.content || []
  } catch (e) {
    console.error(e)
  } finally {
    loadingCategorias.value = false
    loadingProductos.value = false
  }

  // Novedades por separado — si fallan no rompen el resto
  try {
    const novRes = await api.get('/public/novedades')
    novedades.value = (novRes.data.data || []).slice(0, 3)
  } catch (e) {
    // tabla aún no creada o sin conexión — sección queda vacía
  } finally {
    loadingNovedades.value = false
  }
})

function tipoBadge(tipo) {
  const m = { DESCUENTO: 'bg-yellow-500/90 text-black', EVENTO: 'bg-purple-500/90 text-white', NOVEDAD: 'bg-blue-500/90 text-white' }
  return m[tipo] || m.NOVEDAD
}
function tipoLabel(tipo) {
  const m = { DESCUENTO: '🏷️ Descuento', EVENTO: '🎉 Evento', NOVEDAD: '📢 Novedad' }
  return m[tipo] || tipo
}
function tipoIcono(tipo) {
  const m = { DESCUENTO: '🏷️', EVENTO: '🎉', NOVEDAD: '📢' }
  return m[tipo] || '📰'
}
function formatFecha(f) {
  if (!f) return ''
  return new Date(f).toLocaleDateString('es-CO', { day: '2-digit', month: 'long', year: 'numeric' })
}
</script>
