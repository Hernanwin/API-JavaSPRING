import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Subscription } from 'rxjs';
import { MessageService } from 'primeng/api';
import { NotificacionService } from './notificacion.service';
import { Notificacion } from './notificacion.model';
import { AuthStore } from '../../../core/auth/auth.store';

@Injectable({ providedIn: 'root' })
export class NotificacionStore {
  private readonly service = inject(NotificacionService);
  private readonly messageService = inject(MessageService);
  private readonly authStore = inject(AuthStore);

  private readonly _notificaciones = signal<Notificacion[]>([]);
  private readonly _cargando = signal(false);
  private readonly _soloNoLeidas = signal(false);

  private streamSub: Subscription | null = null;

  readonly notificaciones = this._notificaciones.asReadonly();
  readonly cargando = this._cargando.asReadonly();
  readonly soloNoLeidas = this._soloNoLeidas.asReadonly();

  readonly notificacionesVisibles = computed(() => {
    const lista = this._notificaciones();
    return this._soloNoLeidas() ? lista.filter((n) => !n.leida) : lista;
  });

  readonly noLeidasCount = computed(
    () => this._notificaciones().filter((n) => !n.leida).length,
  );

  constructor() {
    effect(() => {
      const token = this.authStore.token();
      const puedeRecibir = this.authStore.tienePermiso('INVENTARIO_VER', 'REPORTE_OPERATIVO_VER');

      this.desconectarStream();
      if (token && puedeRecibir) {
        void this.cargar();
        this.conectarStream(token);
      } else {
        this._notificaciones.set([]);
      }
    });
  }

  async cargar(): Promise<void> {
    if (!this.authStore.autenticado()) return;
    this._cargando.set(true);
    try {
      const lista = await firstValueFrom(this.service.listar(false));
      this._notificaciones.set(lista);
    } finally {
      this._cargando.set(false);
    }
  }

  setSoloNoLeidas(valor: boolean): void {
    this._soloNoLeidas.set(valor);
  }

  async marcarLeida(id: number): Promise<void> {
    const actualizada = await firstValueFrom(this.service.marcarLeida(id));
    this._notificaciones.update((actuales) =>
      actuales.map((n) => (n.id === id ? actualizada : n)),
    );
  }

  async marcarTodasLeidas(): Promise<void> {
    await firstValueFrom(this.service.marcarTodasLeidas());
    this._notificaciones.update((actuales) =>
      actuales.map((n) => ({ ...n, leida: true })),
    );
  }

  private conectarStream(token: string): void {
    this.streamSub = this.service.streamStockBajo(token).subscribe((nueva) => {
      this._notificaciones.update((actuales) => {
        if (actuales.some((n) => n.id === nueva.id)) return actuales;
        return [nueva, ...actuales];
      });

      this.messageService.add({
        severity: 'warn',
        summary: 'Alerta de stock bajo',
        detail: nueva.mensaje,
        life: 6000,
      });
    });
  }

  private desconectarStream(): void {
    if (this.streamSub) {
      this.streamSub.unsubscribe();
      this.streamSub = null;
    }
  }
}
