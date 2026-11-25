import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { catchError, throwError } from 'rxjs';
import { AuthStore } from '../auth/auth.store';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);
  const authStore = inject(AuthStore);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const esLogin = req.url.endsWith('/api/auth/login');

      if (error.status === 401 && !esLogin) {
        authStore.logout(true);
        messageService.add({
          severity: 'warn',
          summary: 'Sesión expirada',
          detail: 'Volvé a iniciar sesión para continuar operando.',
          life: 4500,
        });
        return throwError(() => error);
      }

      const mensaje = error.error?.message ?? 'Ocurrió un error inesperado. Intentá de nuevo.';
      const resumen =
        error.status === 403
          ? 'Permiso o alcance restringido'
          : error.status === 401
            ? 'Credenciales inválidas'
            : `Error ${error.status}`;

      messageService.add({
        severity: error.status === 403 ? 'warn' : 'error',
        summary: resumen,
        detail: mensaje,
        life: 5000,
      });

      return throwError(() => error);
    }),
  );
};
