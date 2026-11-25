// funciones puras, sin estado y sin injeccion - por eso SI se comparten
// entre stores sin volver a acoplar nada. No guardan nada, solo transforman
// lo que les pasan.

export function filtrarPorTienda<T extends { idTienda: number }>(
  items: T[],
  idTienda: number | null,
): T[] {
  return idTienda == null ? items : items.filter((item) => item.idTienda === idTienda);
}

export function fechaISO(offsetDias: number): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + offsetDias);
  return fecha.toISOString().slice(0, 10);
}

export function calcularVariacionPorcentual(actual: number, anterior: number): number | null {
  if (anterior === 0) return null; // sin base de comparacion valida
  return Math.round(((actual - anterior) / anterior) * 100);
}
