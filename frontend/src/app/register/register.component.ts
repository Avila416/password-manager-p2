import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  form = {
    name: '',
    email: '',
    password: '',
    phone: ''
  };

  message = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    this.error = '';
    this.message = '';

    const name = this.form.name.trim();
    const email = this.form.email.trim();
    const password = this.form.password.trim();
    const phone = this.form.phone.trim();

    if (!name) {
      this.error = 'Name is required';
      return;
    }

    if (!email) {
      this.error = 'Email is required';
      return;
    }

    if (!this.isValidEmail(email)) {
      this.error = 'Enter a valid email address';
      return;
    }

    if (!password) {
      this.error = 'Password is required';
      return;
    }

    if (password.length < 6) {
      this.error = 'Password must be at least 6 characters';
      return;
    }

    if (phone && !this.isValidPhone(phone)) {
      this.error = 'Phone number must be exactly 10 digits';
      return;
    }

    this.form = { name, email, password, phone };

    this.authService.register(this.form).subscribe({
      next: (resp) => {
        this.message = resp.message;
        this.authService.clearToken();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.error = this.extractError(err);
      }
    });
  }

  private extractError(err: any): string {
    const payload = err?.error;
    if (typeof payload === 'string' && payload.trim()) {
      return payload.trim();
    }

    const message = payload?.message ?? payload?.error;
    if (typeof message === 'string' && message.trim()) {
      return message.trim();
    }

    const details = payload?.details;
    if (details && typeof details === 'object') {
      const firstDetail = Object.values(details)[0];
      if (typeof firstDetail === 'string' && firstDetail.trim()) {
        return firstDetail.trim();
      }
    }

    return 'Registration failed';
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  private isValidPhone(phone: string): boolean {
    return /^\d{10}$/.test(phone);
  }
}

