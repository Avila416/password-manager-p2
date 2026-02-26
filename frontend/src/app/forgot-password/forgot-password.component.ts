import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  form = {
    email: '',
    verificationCode: '',
    newPassword: '',
    confirmPassword: ''
  };

  message = '';
  error = '';

  constructor(private authService: AuthService) {}

  requestCode() {
    this.error = '';
    this.message = '';
    this.authService.requestForgotPasswordCode(this.form.email).subscribe({
      next: (resp) => (this.message = resp.message),
      error: (err) => (this.error = err?.error?.message ?? 'Failed to request code')
    });
  }

  resetPassword() {
    this.error = '';
    this.message = '';
    this.authService.resetForgotPassword(this.form).subscribe({
      next: (resp) => (this.message = resp.message),
      error: (err) => (this.error = err?.error?.message ?? 'Failed to reset password')
    });
  }
}
