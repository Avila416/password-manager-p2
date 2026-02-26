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

    this.authService.register(this.form).subscribe({
      next: (resp) => {
        this.authService.saveToken(resp.token);
        this.message = resp.message;
        this.router.navigate(['/dashboard']);
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
}
