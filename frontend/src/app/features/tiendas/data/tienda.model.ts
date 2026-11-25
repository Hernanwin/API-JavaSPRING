import { TipoUbicacion } from '../../../core/auth/auth.model';

export interface Tienda {
  id: number;
  nombre: string;
  direccion: string;
  tipo: TipoUbicacion;
}

export interface TiendaInput {
  nombre: string;
  direccion: string;
  tipo: TipoUbicacion;
}
