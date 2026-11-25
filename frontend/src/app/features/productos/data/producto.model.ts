export interface Producto {
  id: number;
  nombre: string;
  categoria: string;
  precio: number;
  // Solo viene presente si el usuario autenticado posee Permiso.COSTOS_VER
  costo?: number | null;
  idProveedor?: number | null;
  nombreProveedor?: string | null;
}

export interface ProductoInput {
  nombre: string;
  categoria: string;
  precio: number;
  costo?: number | null;
  idProveedor?: number | null;
}
