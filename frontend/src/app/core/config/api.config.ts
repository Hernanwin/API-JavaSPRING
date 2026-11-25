// URL base de la API. Es relativa (no "http://localhost:8080/api") a proposito:
// asi, en dev, el proxy.conf.json de Angular intercepta /api y lo reenvia al
// backend sin que el navegador vea un origen distinto (evita el error de CORS,
// ya que el backend no tiene CORS configurado). Cuando el proyecto necesite
// build por ambiente, esto se reemplaza por los environment files de Angular.
export const API_BASE_URL = '/api';
