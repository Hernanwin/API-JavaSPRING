export const STOCK_MINIMO_POR_DEFECTO = 12;

// En el backend, stock bajo y las alertas se disparan cuando cantidad < stockMinimo
// (por defecto 12; ej. pasar de 12 a 11).
export function estaBajoMinimo(cantidad: number, stockMinimo: number | null): boolean {
  const minimo = stockMinimo ?? STOCK_MINIMO_POR_DEFECTO;
  return cantidad < minimo;
}

// aproximacion visual: no hay un "maximo" real en el modelo, asi que usamos
// 3x el stock minimo como referencia de barra llena. Es cosmetico.
export function nivelPorcentaje(cantidad: number, stockMinimo: number | null): number {
  const minimo = stockMinimo && stockMinimo > 0 ? stockMinimo : STOCK_MINIMO_POR_DEFECTO;
  const referencia = minimo * 3;
  return Math.min(100, Math.round((cantidad / referencia) * 100));
}
