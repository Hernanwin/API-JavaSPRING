import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, InputTextModule, PasswordModule, ButtonModule, TagModule],
  templateUrl: './login-page.html',
})
export class LoginPage {
  protected readonly authStore = inject(AuthStore);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly errorLocal = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected completarDemoAdmin(): void {
    this.form.patchValue({ username: 'admin', password: 'admin123' });
  }

  protected async ingresar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorLocal.set(null);
    try {
      await this.authStore.login(this.form.getRawValue());
      await this.router.navigateByUrl(this.authStore.rutaPorDefecto());
    } catch {
      this.errorLocal.set('No se pudo iniciar sesión. Verificá tu usuario y contraseña.');
    }
  }
}
