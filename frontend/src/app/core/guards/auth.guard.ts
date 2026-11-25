import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthStore } from '../auth/auth.store';
import { Permiso } from '../auth/auth.model';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthStore);
  const router = inject(Router);
  if (auth.autenticado()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthStore);
  const router = inject(Router);
  if (!auth.autenticado()) {
    return true;
  }
  return router.createUrlTree([auth.rutaPorDefecto()]);
};

export function permisoGuard(...permisos: Permiso[]): CanActivateFn {
  return () => {
    const auth = inject(AuthStore);
    const router = inject(Router);
    const messageService = inject(MessageService);

    if (!auth.autenticado()) {
      return router.createUrlTree(['/login']);
    }
    if (auth.tienePermiso(...permisos)) {
      return true;
    }
    messageService.add({
      severity: 'warn',
      summary: 'Acceso no habilitado',
      detail: 'Tu rol actual no incluye permisos para operar en esa sección.',
      life: 4500,
    });
    return router.createUrlTree([auth.rutaPorDefecto()]);
  };
}
