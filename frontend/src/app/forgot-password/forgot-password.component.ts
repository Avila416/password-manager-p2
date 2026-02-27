import { Component } from '@angular/core';
import { Router } from '@angular/router';
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

  constructor(private authService: AuthService, private router: Router) {}

  requestCode() {
    this.error = '';
    this.message = '';
    const email = this.form.email.trim();
    if (!email) {
      this.error = 'Email is required';
      return;
    }
    if (!this.isValidEmail(email)) {
      this.error = 'Enter a valid email address';
      return;
    }

    this.authService.requestForgotPasswordCode(email).subscribe({
      next: (resp) => (this.message = resp.message),
      error: (err) => (this.error = err?.error?.message ?? 'Failed to request code')
    });
  }

  resetPassword() {
    this.error = '';
    this.message = '';

    const email = this.form.email.trim();
    const verificationCode = this.form.verificationCode.trim();
    const newPassword = this.form.newPassword.trim();
    const confirmPassword = this.form.confirmPassword.trim();

    if (!email) {
      this.error = 'Email is required';
      return;
    }

    if (!this.isValidEmail(email)) {
      this.error = 'Enter a valid email address';
      return;
    }

    if (!verificationCode) {
      this.error = 'Verification code is required';
      return;
    }

    if (!newPassword || !confirmPassword) {
      this.error = 'New password and confirm password are required';
      return;
    }

    if (newPassword.length < 6) {
      this.error = 'New password must be at least 6 characters';
      return;
    }

    if (newPassword !== confirmPassword) {
      this.error = 'New password and confirm password must match';
      return;
    }

    this.form = { email, verificationCode, newPassword, confirmPassword };

    this.authService.resetForgotPassword(this.form).subscribe({
      next: (resp) => {
        this.message = `${resp.message}. You can login now.`;
        this.router.navigate(['/login']);
      },
      error: (err) => (this.error = err?.error?.message ?? 'Failed to reset password')
    });
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
