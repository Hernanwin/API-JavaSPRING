const pptxgen = require('pptxgenjs');
const fs = require('fs');
const path = require('path');

const pptx = new pptxgen();
pptx.layout = 'LAYOUT_16x9';
pptx.author = 'StockCommerce Team';
pptx.company = 'StockCommerce Enterprise';
pptx.title = 'StockCommerce - Arquitectura, Clases y Suite de Pruebas';

// Paleta de colores de alto contraste
const C_BG = '0B132B';        // Fondo azul noche profundo
const C_CARD = '1C2541';      // Tarjeta slate oscuro
const C_CARD_LIGHT = '243256';// Tarjeta secundaria
const C_PRIMARY = '00D2D3';   // Cyan turquesa brillante
const C_ACCENT = '10B981';    // Verde esmeralda
const C_AMBER = 'F59E0B';     // Ámbar para advertencias/estados
const C_TEXT = 'FFFFFF';      // Blanco puro para títulos y texto principal
const C_TEXT_MUTED = 'CBD5E1';// Gris claro para texto secundario
const C_BORDER = '3A506B';

function applySlideDefaults(slide, title, category) {
  // Background
  slide.background = { color: C_BG };

  // Header banner / Category
  if (category) {
    slide.addText(category.toUpperCase(), {
      x: 0.8, y: 0.4, w: 10, h: 0.3,
      fontSize: 11, bold: true, color: C_PRIMARY, fontFace: 'Calibri'
    });
  }

  // Slide Title
  if (title) {
    slide.addText(title, {
      x: 0.8, y: 0.7, w: 11.5, h: 0.6,
      fontSize: 22, bold: true, color: C_TEXT, fontFace: 'Calibri'
    });
  }

  // Footer
  slide.addText('StockCommerce — Monorepo Enterprise Full-Stack', {
    x: 0.8, y: 7.1, w: 6, h: 0.3,
    fontSize: 9, color: '64748B', fontFace: 'Calibri'
  });
  slide.addText('Spring Boot 4 · Angular 20 · RBAC · 17/17 Tests Pass', {
    x: 7.5, y: 7.1, w: 5, h: 0.3,
    fontSize: 9, align: 'right', color: '64748B', fontFace: 'Calibri'
  });
}

// ==========================================
// SLIDE 1: PORTADA
// ==========================================
{
  const slide = pptx.addSlide();
  slide.background = { color: C_BG };

  // Tag superior
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.8, y: 1.2, w: 2.8, h: 0.4,
    fill: { color: '1E293B' }, line: { color: C_PRIMARY, width: 1.5 }, rectRadius: 0.1
  });
  slide.addText('PROYECTO MONOREPO 2026', {
    x: 0.8, y: 1.2, w: 2.8, h: 0.4,
    fontSize: 10, bold: true, color: C_PRIMARY, align: 'center', fontFace: 'Calibri'
  });

  // Título Principal
  slide.addText('StockCommerce', {
    x: 0.8, y: 1.8, w: 11.5, h: 1.2,
    fontSize: 48, bold: true, color: C_TEXT, fontFace: 'Calibri'
  });

  // Subtítulo
  slide.addText('Plataforma Integral de Gestión de Inventario, POS Multitienda, Logística y Gobierno de Seguridad RBAC', {
    x: 0.8, y: 3.0, w: 11, h: 0.8,
    fontSize: 18, color: C_TEXT_MUTED, fontFace: 'Calibri'
  });

  // 3 Tarjetas resumen en portada
  const cards = [
    { title: 'Backend Robusto', desc: 'Java 17, Spring Boot 4, JPA, Spring Security 6, JWT, Arquitectura Limpia.', color: C_PRIMARY },
    { title: 'Frontend Moderno', desc: 'Angular 20, Signals Reactivas, Zoneless, PrimeNG Aura, Tailwind v4.', color: C_ACCENT },
    { title: 'Calidad & Pruebas', desc: '17 Tests de Integración (100% PASS), Validación RBAC y Flujo de Stock.', color: C_AMBER }
  ];

  cards.forEach((c, idx) => {
    const xPos = 0.8 + idx * 3.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 4.2, w: 3.6, h: 2.2,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.1
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: xPos, y: 4.2, w: 3.6, h: 0.1,
      fill: { color: c.color }
    });
    slide.addText(c.title, {
      x: xPos + 0.25, y: 4.5, w: 3.1, h: 0.4,
      fontSize: 14, bold: true, color: C_TEXT, fontFace: 'Calibri'
    });
    slide.addText(c.desc, {
      x: xPos + 0.25, y: 5.0, w: 3.1, h: 1.2,
      fontSize: 11, color: C_TEXT_MUTED, fontFace: 'Calibri', lineSpacing: 14
    });
  });
}

// ==========================================
// SLIDE 2: ARQUITECTURA GLOBAL DEL SISTEMA
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Arquitectura del Sistema y Diseño Monorepo', 'Arquitectura & Stack');

  const layers = [
    {
      name: '1. Frontend (Angular 20)',
      color: C_PRIMARY,
      items: [
        'Single Page Application (SPA) con Standalone Components',
        'State Management reactivo con Signals (AuthStore, PosStore, etc.)',
        'PrimeNG Aura Theme + Tailwind CSS v4 optimizado para contraste',
        'Interceptores JWT automáticos y Route Guards por rol'
      ]
    },
    {
      name: '2. API Gateway & Seguridad',
      color: C_ACCENT,
      items: [
        'Filtro JwtAuthenticationFilter stateless con Bearer Token',
        'AccessControlService: validación de jerarquía y nivel de riesgo',
        'Data Scoping: Aislamiento por tienda o depósito asignado',
        'OpenAPI 3.1 & Scalar Interactive API Docs en /docs'
      ]
    },
    {
      name: '3. Backend (Spring Boot 4)',
      color: C_AMBER,
      items: [
        'Arquitectura por Capas: Controllers, Services, Repositories, DTOs',
        'Transaccionalidad ACID con control estricto de concurrencia',
        'Eventos AfterCommit para alertas reactivas de stock mínimo',
        'Base de datos desacoplada (H2 para dev/tests, PostgreSQL/MySQL prod)'
      ]
    }
  ];

  layers.forEach((l, idx) => {
    const xPos = 0.8 + idx * 3.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 1.5, w: 3.6, h: 5.2,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.15
    });
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos + 0.2, y: 1.7, w: 3.2, h: 0.5,
      fill: { color: C_CARD_LIGHT }, line: { color: l.color, width: 1.5 }, rectRadius: 0.08
    });
    slide.addText(l.name, {
      x: xPos + 0.2, y: 1.7, w: 3.2, h: 0.5,
      fontSize: 12, bold: true, color: l.color, align: 'center', fontFace: 'Calibri'
    });

    const bulletRows = l.items.map(it => ({
      text: it,
      options: { bullet: true, fontSize: 11, color: C_TEXT_MUTED, lineSpacing: 18 }
    }));
    slide.addText(bulletRows, {
      x: xPos + 0.25, y: 2.4, w: 3.1, h: 4.0,
      fontFace: 'Calibri'
    });
  });
}

// ==========================================
// SLIDE 3: SEGURIDAD RBAC Y DATA SCOPING
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Modelo de Seguridad RBAC y Gobierno por Alcance', 'Seguridad & Permisos');

  // Tabla con los 5 Roles
  const headers = [
    { text: 'Rol (RolCodigo)', options: { bold: true, color: C_TEXT, fill: { color: '1E293B' }, fontSize: 10 } },
    { text: 'Nivel Riesgo', options: { bold: true, color: C_TEXT, fill: { color: '1E293B' }, fontSize: 10 } },
    { text: 'Tipo de Alcance', options: { bold: true, color: C_TEXT, fill: { color: '1E293B' }, fontSize: 10 } },
    { text: 'Responsabilidad y Operaciones Permitidas', options: { bold: true, color: C_TEXT, fill: { color: '1E293B' }, fontSize: 10 } },
    { text: 'Restricciones Clave', options: { bold: true, color: C_TEXT, fill: { color: '1E293B' }, fontSize: 10 } }
  ];

  const rows = [
    [
      { text: 'ADMIN', options: { bold: true, color: C_PRIMARY } },
      { text: 'ALTO', options: { bold: true, color: 'EF4444' } },
      { text: 'GLOBAL (Todas)', options: { color: C_TEXT } },
      { text: 'Control total de usuarios, roles, catálogo maestro, costos y sucursales.', options: { color: C_TEXT_MUTED } },
      { text: 'Ninguna (Superusuario del sistema)', options: { color: C_TEXT_MUTED } }
    ],
    [
      { text: 'GERENTE_TIENDA', options: { bold: true, color: C_PRIMARY } },
      { text: 'MEDIO', options: { bold: true, color: C_AMBER } },
      { text: 'POR TIENDA / DEP.', options: { color: C_TEXT } },
      { text: 'Gestión comercial de su tienda, compras, precios, inventarios y ventas.', options: { color: C_TEXT_MUTED } },
      { text: 'No gestiona usuarios ni roles globales.', options: { color: C_TEXT_MUTED } }
    ],
    [
      { text: 'ENCARGADO_DEPOSITO', options: { bold: true, color: C_PRIMARY } },
      { text: 'MEDIO', options: { bold: true, color: C_AMBER } },
      { text: 'SOLO DEPÓSITOS', options: { color: C_TEXT } },
      { text: 'Recepción de compras, despacho y recepción de transferencias, conteos.', options: { color: C_TEXT_MUTED } },
      { text: 'No accede a ventas POS ni ve costos/precios.', options: { color: 'F87171' } }
    ],
    [
      { text: 'CAJERO_VENDEDOR', options: { bold: true, color: C_PRIMARY } },
      { text: 'BAJO', options: { bold: true, color: C_ACCENT } },
      { text: 'SOLO TIENDAS POS', options: { color: C_TEXT } },
      { text: 'Facturación POS, emisión de tickets, reservas y apartados de clientes.', options: { color: C_TEXT_MUTED } },
      { text: 'Bloqueado en depósitos, reportes y compras.', options: { color: 'F87171' } }
    ],
    [
      { text: 'AUDITOR', options: { bold: true, color: C_PRIMARY } },
      { text: 'BAJO', options: { bold: true, color: C_ACCENT } },
      { text: 'GLOBAL (Solo lectura)', options: { color: C_TEXT } },
      { text: 'Inspección de movimientos, kardex, reportes ejecutivos y trazabilidad.', options: { color: C_TEXT_MUTED } },
      { text: 'Solo lectura: 403 en todo POST, PUT, DELETE.', options: { color: 'F87171' } }
    ]
  ];

  const tableData = [headers, ...rows.map(r => r.map(cell => ({
    text: cell.text,
    options: Object.assign({ fontSize: 9.5, fill: { color: C_CARD } }, cell.options)
  })))];

  slide.addTable(tableData, {
    x: 0.8, y: 1.5, w: 11.7, h: 3.4,
    border: { color: C_BORDER, width: 1 },
    colW: [2.0, 1.4, 2.0, 3.8, 2.5],
    align: 'left',
    valign: 'middle'
  });

  // Callout inferior sobre AccessControlService
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.8, y: 5.2, w: 11.7, h: 1.6,
    fill: { color: C_CARD_LIGHT }, line: { color: C_PRIMARY, width: 1 }, rectRadius: 0.1
  });
  slide.addText('Mecanismo de Verificación en Runtime (AccessControlService):', {
    x: 1.0, y: 5.3, w: 11.3, h: 0.35,
    fontSize: 11, bold: true, color: C_PRIMARY, fontFace: 'Calibri'
  });
  slide.addText([
    { text: '1. Jerarquía de Riesgo: ', options: { bold: true, color: C_TEXT } },
    { text: 'Un usuario no puede alterar configuraciones de nivel igual o superior al suyo.\n', options: { color: C_TEXT_MUTED } },
    { text: '2. Validación de Tipo de Ubicación: ', options: { bold: true, color: C_TEXT } },
    { text: 'El sistema rechaza si se intenta asignar a un Cajero en un DEPOSITO o a un Encargado en una TIENDA comercial (HTTP 400).\n', options: { color: C_TEXT_MUTED } },
    { text: '3. Aislamiento de Consultas: ', options: { bold: true, color: C_TEXT } },
    { text: 'Los endpoints de inventario y movimientos filtran automáticamente para que cada usuario solo vea los almacenes que tiene autorizados.', options: { color: C_TEXT_MUTED } }
  ], {
    x: 1.0, y: 5.65, w: 11.3, h: 1.0,
    fontSize: 9.5, fontFace: 'Calibri', lineSpacing: 13
  });
}

// ==========================================
// SLIDE 4: MAPEO DE CLASES BACKEND - MODELOS
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Mapeo de Clases Backend: Entidades de Dominio (JPA)', 'Backend · models/');

  const entityGroups = [
    {
      title: 'Catálogo & Ubicaciones',
      color: C_PRIMARY,
      classes: [
        'Producto: id, sku, nombre, precio, costo, categoria, idProveedor, activo',
        'Tienda: id, nombre, direccion, tipo (TIENDA | DEPOSITO), activo',
        'Proveedor: id, nombre, identificacionFiscal, telefono, email, activo',
        'Inventario: id, idProducto, idTienda, cantidad, stockReservado, stockMinimo'
      ]
    },
    {
      title: 'Ventas & Reservas (POS)',
      color: C_ACCENT,
      classes: [
        'Venta: id, idTienda, fechaHora, total, estado (COMPLETADA, CANCELADA)',
        'DetalleVenta: id, idVenta, idProducto, cantidad, precioUnitario, subtotal',
        'Apartado: id, idTienda, cliente, senia, saldo, fechaLimite, estado',
        'DetalleApartado: id, idApartado, idProducto, cantidad, precioUnitario'
      ]
    },
    {
      title: 'Compras & Logística',
      color: C_AMBER,
      classes: [
        'OrdenCompra: id, idProveedor, idTienda, estado, total, fechaCreacion',
        'DetalleOrdenCompra: id, idOrden, idProducto, cantidadPedida, recibida, costo',
        'Transferencia: id, origenId, destinoId, estado (PENDIENTE, EN_TRANSITO, ...)',
        'AjusteConteo: id, idTienda, idProducto, stockFisico, stockLogico, diferencia',
        'Salida: id, idTienda, idProducto, cantidad, motivo (MERMA, ROTURA, etc.)'
      ]
    },
    {
      title: 'Seguridad & Alertas',
      color: '818CF8',
      classes: [
        'Usuario: id, username, password, nombreCompleto, activo, asignaciones',
        'AsignacionRol: id, usuario, rol (RolCodigo), tienda (Scope id)',
        'Notificacion: id, idProducto, idTienda, cantidadActual, leida, fecha'
      ]
    }
  ];

  entityGroups.forEach((g, idx) => {
    const col = idx % 2;
    const row = Math.floor(idx / 2);
    const xPos = 0.8 + col * 5.9;
    const yPos = 1.5 + row * 2.7;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: yPos, w: 5.7, h: 2.5,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.1
    });

    slide.addText(g.title, {
      x: xPos + 0.3, y: yPos + 0.15, w: 5.1, h: 0.35,
      fontSize: 12, bold: true, color: g.color, fontFace: 'Calibri'
    });

    const items = g.classes.map(c => {
      const parts = c.split(':');
      return [
        { text: parts[0] + ':', options: { bold: true, color: C_TEXT, fontSize: 9.5 } },
        { text: parts[1] + '\n', options: { color: C_TEXT_MUTED, fontSize: 9 } }
      ];
    }).flat();

    slide.addText(items, {
      x: xPos + 0.3, y: yPos + 0.55, w: 5.1, h: 1.8,
      fontFace: 'Calibri', lineSpacing: 12
    });
  });
}

// ==========================================
// SLIDE 5: MAPEO DE CLASES BACKEND - SERVICIOS Y CONTROLLERS
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Mapeo de Clases Backend: Servicios y Controladores REST', 'Backend · services/ & controller/');

  const components = [
    {
      type: 'Servicios de Negocio (Interfaces & Impl)',
      color: C_PRIMARY,
      items: [
        'InventarioService: Gestión de stock, cálculo de reservados, auditoría.',
        'VentaService: Venta atómica en POS, validación stock, generación salida.',
        'ApartadoService: Bloqueo de stock reservado y liquidación de señas.',
        'OrdenCompraService: Ciclo comercial de compras y recepción en depósito.',
        'TransferenciaService: Máquina de estados (4 fases) entre almacenes.',
        'AjusteConteoService: Conciliación física y generación de movimientos.',
        'SalidaService: Registro de mermas y salidas operativas no comerciales.',
        'ReporteService: Consolidación financiera, rotación y alertas de quiebre.',
        'NotificacionService: Alertas asíncronas disparadas post-commit.',
        'UsuarioService & AccessControlService: Gobernanza y auditoría de accesos.'
      ]
    },
    {
      type: 'Controladores REST (Endpoints Seguros)',
      color: C_ACCENT,
      items: [
        'AuthController: /api/auth/login, /api/auth/perfil (JWT)',
        'InventarioController: /api/inventario (Filtro por alcance de usuario)',
        'VentaController: /api/ventas (Registro POS y consulta por tienda)',
        'ApartadoController: /api/apartados (Reservas y pagos de saldo)',
        'OrdenCompraController: /api/ordenes-compra (Emisión y recepción)',
        'TransferenciaController: /api/transferencias (Despacho y recepción)',
        'AjusteConteoController: /api/ajustes-conteo (Conteos físicos ciegos)',
        'SalidaController: /api/salidas (Bajas por merma o rotura)',
        'ReporteController: /api/reportes (Dashboard ejecutivo y KPIs)',
        'UsuarioController: /api/usuarios (CRUD con control por nivel de riesgo)'
      ]
    }
  ];

  components.forEach((c, idx) => {
    const xPos = 0.8 + idx * 5.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 1.5, w: 5.7, h: 5.3,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.1
    });

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos + 0.25, y: 1.7, w: 5.2, h: 0.45,
      fill: { color: C_CARD_LIGHT }, line: { color: c.color, width: 1 }, rectRadius: 0.08
    });
    slide.addText(c.type, {
      x: xPos + 0.25, y: 1.7, w: 5.2, h: 0.45,
      fontSize: 11, bold: true, color: c.color, align: 'center', fontFace: 'Calibri'
    });

    const rows = c.items.map(it => {
      const parts = it.split(':');
      return [
        { text: '• ' + parts[0] + ':', options: { bold: true, color: C_TEXT, fontSize: 9.5 } },
        { text: parts[1] + '\n', options: { color: C_TEXT_MUTED, fontSize: 9 } }
      ];
    }).flat();

    slide.addText(rows, {
      x: xPos + 0.3, y: 2.3, w: 5.1, h: 4.3,
      fontFace: 'Calibri', lineSpacing: 12
    });
  });
}

// ==========================================
// SLIDE 6: ARQUITECTURA FRONTEND ANGULAR 20
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Arquitectura Frontend: Angular 20 & PrimeNG Aura', 'Frontend · Angular 20');

  const frontLayers = [
    {
      title: 'State Management (Signals)',
      desc: 'Gestión reactiva sin librerías pesadas externas:',
      color: C_PRIMARY,
      items: [
        'AuthStore: Token JWT, roles, alcance y estado de login reactivo.',
        'TiendaSeleccionadaStore: Sincroniza la sucursal activa globalmente.',
        'PosStore: Carrito de ventas, cálculo de subtotales e impuestos en vivo.',
        'InventarioStore / ComprasStore: Caché y filtrado dinámico.'
      ]
    },
    {
      title: 'Seguridad en el Cliente',
      desc: 'Protección multinivel en rutas y llamadas HTTP:',
      color: C_ACCENT,
      items: [
        'AuthGuard: Valida vigencia de sesión antes de activar cualquier ruta.',
        'AuthInterceptor: Inyecta Authorization: Bearer <token> en cada request.',
        'ErrorInterceptor: Captura 401 (redirección a login) y 403 (acceso denegado).',
        'Directivas de Permiso: Oculta botones según rol del usuario.'
      ]
    },
    {
      title: 'UI / UX & Accesibilidad',
      desc: 'Diseño corporativo con alta legibilidad:',
      color: C_AMBER,
      items: [
        'PrimeNG Aura Theme configurado con selector dark explícito (.p-dark).',
        'Tailwind CSS v4 para espaciado y contraste certificado.',
        'Corrección de alto contraste en tablas oscuras (sin textos blancos invisibles).',
        'Layout Shell responsive con Sidebar colapsable y breadcrumbs.'
      ]
    }
  ];

  frontLayers.forEach((fl, idx) => {
    const xPos = 0.8 + idx * 3.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 1.5, w: 3.6, h: 5.2,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.12
    });

    slide.addText(fl.title, {
      x: xPos + 0.2, y: 1.7, w: 3.2, h: 0.4,
      fontSize: 13, bold: true, color: fl.color, fontFace: 'Calibri'
    });
    slide.addText(fl.desc, {
      x: xPos + 0.2, y: 2.1, w: 3.2, h: 0.4,
      fontSize: 10, color: C_TEXT, fontFace: 'Calibri'
    });

    const bRows = fl.items.map(it => {
      const parts = it.split(':');
      return [
        { text: parts[0] + ':\n', options: { bold: true, color: C_TEXT, fontSize: 10 } },
        { text: parts[1] + '\n\n', options: { color: C_TEXT_MUTED, fontSize: 9.5 } }
      ];
    }).flat();

    slide.addText(bRows, {
      x: xPos + 0.2, y: 2.6, w: 3.2, h: 3.9,
      fontFace: 'Calibri', lineSpacing: 11
    });
  });
}

// ==========================================
// SLIDE 7: SUITE DE PRUEBAS 1 - RBAC ALCANCE
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Suite de Pruebas: RbacAlcanceIntegrationTest (6 Tests)', 'Testing Backend · Seguridad');

  slide.addText('Verificación de control de acceso por rol, nivel de riesgo y alcance de datos contra Spring Boot Test:', {
    x: 0.8, y: 1.4, w: 11.5, h: 0.35,
    fontSize: 11, color: C_TEXT_MUTED, fontFace: 'Calibri'
  });

  const testsRbac = [
    {
      num: 'Test 1',
      name: 'accesoSinTokenEsRechazadoYAsignacionesInvalidasFallan',
      objective: 'Verifica rechazo 401 sin JWT y bloqueo 400 al asignar roles incompatibles (Cajero en Depósito o Encargado en Tienda).'
    },
    {
      num: 'Test 2',
      name: 'encargadoDepositoOperaSoloSusDepositosSinModificarPreciosNiUsuarios',
      objective: 'Aislamiento estricto de depósitos: oculta depósitos ajenos, no permite ver costos de compra ni crear nuevos usuarios.'
    },
    {
      num: 'Test 3',
      name: 'cajeroVendedorSoloFacturaEnSuTiendaSinVerReportesNiCompras',
      objective: 'Cajero puede registrar ventas y apartados solo en su tienda; reportes financieros y órdenes de compra retornan 403 Forbidden.'
    },
    {
      num: 'Test 4',
      name: 'auditorTieneAccesoLecturaGlobalPeroNoPuedeModificarStock',
      objective: 'Auditor consulta cualquier catálogo, movimiento o reporte globalmente, pero llamadas POST/PUT/DELETE reciben 403.'
    },
    {
      num: 'Test 5',
      name: 'gerenteTiendaNoPuedeGestionarUsuariosAdminNiCambiarAlcances',
      objective: 'Gobernanza de riesgo: usuario con nivel MEDIO no puede promover a ADMIN ni reasignar permisos de nivel ALTO.'
    },
    {
      num: 'Test 6',
      name: 'revocacionDeAsignacionInvalidaAccesoEnTiempoReal',
      objective: 'Al desasignar a un usuario de una tienda, el acceso a sus inventarios queda inmediatamente bloqueado.'
    }
  ];

  testsRbac.forEach((t, idx) => {
    const col = idx % 2;
    const row = Math.floor(idx / 2);
    const xPos = 0.8 + col * 5.9;
    const yPos = 1.85 + row * 1.65;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: yPos, w: 5.7, h: 1.5,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.1
    });

    slide.addShape(pptx.ShapeType.rect, {
      x: xPos, y: yPos, w: 0.1, h: 1.5,
      fill: { color: C_PRIMARY }
    });

    slide.addText([
      { text: t.num + ' · ', options: { bold: true, color: C_PRIMARY, fontSize: 10 } },
      { text: t.name, options: { bold: true, color: C_TEXT, fontSize: 9.5 } }
    ], {
      x: xPos + 0.25, y: yPos + 0.15, w: 5.2, h: 0.4,
      fontFace: 'Calibri'
    });

    slide.addText(t.objective, {
      x: xPos + 0.25, y: yPos + 0.55, w: 5.2, h: 0.85,
      fontSize: 9, color: C_TEXT_MUTED, fontFace: 'Calibri', lineSpacing: 13
    });
  });
}

// ==========================================
// SLIDE 8: SUITE DE PRUEBAS 2 - STOCK FLOW
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Suite de Pruebas: StockFlowIntegrationTest (10 Tests)', 'Testing Backend · Flujo Operativo');

  slide.addText('Validación transaccional ACID, consistencia de inventario, POS, alertas y trazabilidad de movimientos:', {
    x: 0.8, y: 1.4, w: 11.5, h: 0.35,
    fontSize: 11, color: C_TEXT_MUTED, fontFace: 'Calibri'
  });

  const testsFlow = [
    { name: 'entradaAceptaCantidadPequenaYCreaInventarioConMinimo12', desc: 'Entrada inicial crea registro de inventario con stock mínimo por defecto = 12.' },
    { name: 'ventaDescuentaStockRegistraSalidasYUsaPrecioDelProducto', desc: 'Venta descuenta stock disponible de forma atómica y fija precio oficial ignorando frontend.' },
    { name: 'ventaSinStockSuficienteHaceRollbackCompleto', desc: 'Transacción aborta ante falta de existencias sin generar movimientos corruptos.' },
    { name: 'notificaSoloAlCruzarElMinimoDe12HaciaAbajo', desc: 'Alerta reactiva AfterCommit se dispara solo cuando el stock cruza el umbral (12 -> 11).' },
    { name: 'ventaFallidaNoGeneraNotificacion', desc: 'Rollback en venta fallida previene la emisión de notificaciones espurias.' },
    { name: 'transferenciaMueveStockEntreTiendas', desc: 'Despacho descuenta en origen y recepción acredita en destino con trazabilidad.' },
    { name: 'transferenciaSinStockFallaSinModificarNada', desc: 'Intento de transferencia que excede stock físico es rechazado limpiamente.' },
    { name: 'salidaPorMermaDescuentaStock', desc: 'Bajas por deterioro o vencimiento descuentan existencias con motivo auditable.' },
    { name: 'reporteConsolidadoMuestraTotalesCorrectos', desc: 'Agregaciones financieras y métricas de rotación concilian al 100% con movimientos.' },
    { name: 'conteoFisicoCorrigeDiscrepanciasConAjuste', desc: 'Ajuste de inventario concilia sobrantes y faltantes con balance exacto.' }
  ];

  testsFlow.forEach((t, idx) => {
    const col = idx % 2;
    const row = Math.floor(idx / 2);
    const xPos = 0.8 + col * 5.9;
    const yPos = 1.8 + row * 1.0;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: yPos, w: 5.7, h: 0.9,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.08
    });

    slide.addShape(pptx.ShapeType.rect, {
      x: xPos, y: yPos, w: 0.08, h: 0.9,
      fill: { color: C_ACCENT }
    });

    slide.addText('✓ ' + t.name, {
      x: xPos + 0.2, y: yPos + 0.08, w: 5.3, h: 0.35,
      fontSize: 9.5, bold: true, color: C_TEXT, fontFace: 'Calibri'
    });

    slide.addText(t.desc, {
      x: xPos + 0.2, y: yPos + 0.42, w: 5.3, h: 0.45,
      fontSize: 8.5, color: C_TEXT_MUTED, fontFace: 'Calibri'
    });
  });
}

// ==========================================
// SLIDE 9: RESULTADOS Y GARANTÍA DE CALIDAD
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Métricas de Ejecución y Garantía de Calidad', 'Métricas & Validación');

  // 3 Cajas métricas
  const kpis = [
    { val: '17 / 17', label: 'Tests de Integración', sub: '100% aprobados en Spring Boot Test', color: C_ACCENT },
    { val: '0', label: 'Errores en Compilación', sub: 'Angular 20 & Java 17 limpios', color: C_PRIMARY },
    { val: '100%', label: 'Cobertura de Casos Críticos', sub: 'Rollbacks, Concurrencia y RBAC', color: C_AMBER }
  ];

  kpis.forEach((k, idx) => {
    const xPos = 0.8 + idx * 3.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 1.5, w: 3.6, h: 2.0,
      fill: { color: C_CARD }, line: { color: k.color, width: 1.5 }, rectRadius: 0.12
    });
    slide.addText(k.val, {
      x: xPos, y: 1.7, w: 3.6, h: 0.7,
      fontSize: 32, bold: true, color: k.color, align: 'center', fontFace: 'Calibri'
    });
    slide.addText(k.label, {
      x: xPos, y: 2.4, w: 3.6, h: 0.35,
      fontSize: 12, bold: true, color: C_TEXT, align: 'center', fontFace: 'Calibri'
    });
    slide.addText(k.sub, {
      x: xPos, y: 2.75, w: 3.6, h: 0.5,
      fontSize: 9.5, color: C_TEXT_MUTED, align: 'center', fontFace: 'Calibri'
    });
  });

  // Tarjeta grande de conclusiones de calidad
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.8, y: 3.8, w: 11.7, h: 3.0,
    fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.12
  });

  slide.addText('Aspectos Clave Validados en la Suite:', {
    x: 1.1, y: 4.0, w: 11.0, h: 0.4,
    fontSize: 14, bold: true, color: C_PRIMARY, fontFace: 'Calibri'
  });

  const bulletPoints = [
    { text: '• Protección contra Inconsistencias Financieras: ', options: { bold: true, color: C_TEXT } },
    { text: 'Los precios son inmutables desde el cliente; el backend siempre consulta el catálogo maestro.\n', options: { color: C_TEXT_MUTED } },
    { text: '• Atomicidad de Inventario (Anti-Overbooking): ', options: { bold: true, color: C_TEXT } },
    { text: 'Bloqueo a nivel de base de datos que imposibilita ventas con stock menor al disponible.\n', options: { color: C_TEXT_MUTED } },
    { text: '• Aislamiento por Tienda Garantizado: ', options: { bold: true, color: C_TEXT } },
    { text: 'Un usuario con rol en la Tienda A no puede bajo ninguna circunstancia consultar ni alterar datos de la Tienda B.\n', options: { color: C_TEXT_MUTED } },
    { text: '• Persistencia y Notificaciones AfterCommit: ', options: { bold: true, color: C_TEXT } },
    { text: 'Las alertas de reposición solo se disparan si la transacción confirmó exitosamente, evitando alertas falsas.', options: { color: C_TEXT_MUTED } }
  ];

  slide.addText(bulletPoints, {
    x: 1.1, y: 4.45, w: 11.0, h: 2.2,
    fontSize: 10.5, fontFace: 'Calibri', lineSpacing: 14
  });
}

// ==========================================
// SLIDE 10: DEPLOYMENT Y PUESTA EN MARCHA
// ==========================================
{
  const slide = pptx.addSlide();
  applySlideDefaults(slide, 'Puesta en Marcha, DevOps y Conclusiones', 'DevOps & Conclusiones');

  const devopsBoxes = [
    {
      title: 'Script de Desarrollo dev.ps1',
      color: C_PRIMARY,
      items: [
        'Detección automática de Java 17+ y Node.js en Windows.',
        'Verificación de puertos en uso (8080 y 4200) para evitar colisiones.',
        'Levantamiento simultáneo de Backend y Frontend con un solo comando.',
        'Compatible con PowerShell 5.1 y 7+.'
      ]
    },
    {
      title: 'Documentación y APIs',
      color: C_ACCENT,
      items: [
        'OpenAPI 3.1 con especificación generada en /v3/api-docs.',
        'Scalar Interactive Client embebido en /docs (estilo Swagger moderno).',
        'README completo con diagramas y guías de onboarding.',
        'Monorepo sincronizado en GitHub con historial limpio.'
      ]
    },
    {
      title: 'Conclusión y Valor de Negocio',
      color: C_AMBER,
      items: [
        'Arquitectura empresarial lista para producción.',
        'Modelo de seguridad por riesgo que previene fraudes internos.',
        'Trazabilidad punta a punta desde la compra al proveedor hasta el POS.',
        'Alta mantenibilidad gracias a Clean Architecture y TypeScript tipado.'
      ]
    }
  ];

  devopsBoxes.forEach((b, idx) => {
    const xPos = 0.8 + idx * 3.9;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: 1.5, w: 3.6, h: 5.2,
      fill: { color: C_CARD }, line: { color: C_BORDER, width: 1 }, rectRadius: 0.12
    });

    slide.addText(b.title, {
      x: xPos + 0.25, y: 1.75, w: 3.1, h: 0.5,
      fontSize: 13, bold: true, color: b.color, fontFace: 'Calibri'
    });

    const rows = b.items.map(it => ({
      text: it,
      options: { bullet: true, fontSize: 10.5, color: C_TEXT_MUTED, lineSpacing: 16 }
    }));

    slide.addText(rows, {
      x: xPos + 0.25, y: 2.35, w: 3.1, h: 4.1,
      fontFace: 'Calibri'
    });
  });
}

// Guardar archivo PPTX
const outputPath = path.resolve('c:/Users/herna/OneDrive/Documentos/ProyectosPersonales/stockcomerce/docs/StockCommerce_Presentacion_Proyecto.pptx');
pptx.writeFile({ fileName: outputPath }).then(fileName => {
  console.log('PRESENTACION GENERADA EXITOSAMENTE: ' + fileName);
}).catch(err => {
  console.error('ERROR AL GENERAR PPTX:', err);
  process.exit(1);
});
