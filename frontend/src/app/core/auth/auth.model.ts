export type TipoUbicacion = 'TIENDA' | 'DEPOSITO';
export type TipoAlcance = 'GLOBAL' | 'UBICACION';
export type NivelRiesgo = 'OPERATIVO' | 'MEDIO' | 'ALTO' | 'CRITICO';

export type RolCodigo =
  | 'ADMIN'
  | 'ENCARGADO_DEPOSITO'
  | 'CAJERO_VENDEDOR'
  | 'SUPERVISOR_COMPRAS';

export type Permiso =
  | 'CATALOGO_VER'
  | 'COSTOS_VER'
  | 'CATALOGO_GESTIONAR'
  | 'PRECIOS_MODIFICAR'
  | 'INVENTARIO_VER'
  | 'STOCK_MINIMO_CONFIGURAR'
  | 'ENTRADA_REGISTRAR'
  | 'SALIDA_MANUAL_REGISTRAR'
  | 'TRANSFERENCIA_SOLICITAR'
  | 'TRANSFERENCIA_APROBAR'
  | 'TRANSFERENCIA_OPERAR'
  | 'AJUSTE_CONTEO_EJECUTAR'
  | 'POS_COBRAR'
  | 'VENTA_ADMINISTRAR'
  | 'APARTADO_GESTIONAR'
  | 'PROVEEDOR_GESTIONAR'
  | 'ORDEN_COMPRA_VER'
  | 'ORDEN_COMPRA_CREAR'
  | 'ORDEN_COMPRA_APROBAR'
  | 'REPORTE_OPERATIVO_VER'
  | 'REPORTE_ROTACION_MARGENES_VER'
  | 'REPORTE_FINANCIERO_CONSOLIDADO_VER'
  | 'TIENDA_CONFIGURAR'
  | 'USUARIO_GESTIONAR';

export interface AsignacionRol {
  id?: number;
  rol: RolCodigo;
  nivelRiesgo?: NivelRiesgo;
  tipoAlcance?: TipoAlcance;
  idTienda?: number | null;
  nombreTienda?: string | null;
  tipoUbicacion?: TipoUbicacion | null;
  caja?: string | null;
  activo?: boolean;
}

export interface UsuarioPerfil {
  id: number;
  username: string;
  nombreCompleto: string;
  activo: boolean;
  asignaciones: AsignacionRol[];
  permisosEfectivos: Permiso[];
}

export interface RolDetalle {
  id: number;
  codigo: RolCodigo;
  nombre: string;
  descripcion: string;
  nivelRiesgo: NivelRiesgo;
  tipoAlcance: TipoAlcance;
  tipoUbicacionRequerido: TipoUbicacion | null;
  permisos: Permiso[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiracionSegundos: number;
  usuario: UsuarioPerfil;
}

export interface CrearUsuarioInput {
  username: string;
  password: string;
  nombreCompleto: string;
  asignaciones: AsignacionRol[];
}

export const ROL_PERMISO_MAP: Record<RolCodigo, readonly Permiso[]> = {
  ENCARGADO_DEPOSITO: [
    'CATALOGO_VER',
    'INVENTARIO_VER',
    'ENTRADA_REGISTRAR',
    'SALIDA_MANUAL_REGISTRAR',
    'TRANSFERENCIA_SOLICITAR',
    'TRANSFERENCIA_OPERAR',
    'AJUSTE_CONTEO_EJECUTAR',
    'ORDEN_COMPRA_VER',
    'REPORTE_OPERATIVO_VER',
  ],
  CAJERO_VENDEDOR: ['CATALOGO_VER', 'INVENTARIO_VER', 'POS_COBRAR', 'APARTADO_GESTIONAR'],
  SUPERVISOR_COMPRAS: [
    'CATALOGO_VER',
    'COSTOS_VER',
    'INVENTARIO_VER',
    'STOCK_MINIMO_CONFIGURAR',
    'PROVEEDOR_GESTIONAR',
    'ORDEN_COMPRA_VER',
    'ORDEN_COMPRA_CREAR',
    'ORDEN_COMPRA_APROBAR',
    'TRANSFERENCIA_SOLICITAR',
    'TRANSFERENCIA_APROBAR',
    'REPORTE_OPERATIVO_VER',
    'REPORTE_ROTACION_MARGENES_VER',
  ],
  ADMIN: [
    'CATALOGO_VER',
    'COSTOS_VER',
    'CATALOGO_GESTIONAR',
    'PRECIOS_MODIFICAR',
    'INVENTARIO_VER',
    'STOCK_MINIMO_CONFIGURAR',
    'ENTRADA_REGISTRAR',
    'SALIDA_MANUAL_REGISTRAR',
    'TRANSFERENCIA_SOLICITAR',
    'TRANSFERENCIA_APROBAR',
    'TRANSFERENCIA_OPERAR',
    'AJUSTE_CONTEO_EJECUTAR',
    'POS_COBRAR',
    'VENTA_ADMINISTRAR',
    'APARTADO_GESTIONAR',
    'PROVEEDOR_GESTIONAR',
    'ORDEN_COMPRA_VER',
    'ORDEN_COMPRA_CREAR',
    'ORDEN_COMPRA_APROBAR',
    'REPORTE_OPERATIVO_VER',
    'REPORTE_ROTACION_MARGENES_VER',
    'REPORTE_FINANCIERO_CONSOLIDADO_VER',
    'TIENDA_CONFIGURAR',
    'USUARIO_GESTIONAR',
  ],
};

export const ETIQUETA_ROL: Record<RolCodigo, string> = {
  ADMIN: 'Administrador General',
  ENCARGADO_DEPOSITO: 'Encargado de Depósito',
  CAJERO_VENDEDOR: 'Cajero / Vendedor',
  SUPERVISOR_COMPRAS: 'Supervisor de Compras',
};
