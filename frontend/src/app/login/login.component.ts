import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  form = {
    email: '',
    password: '',
    otp: ''
  };

  message = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.error = '';
    this.message = '';
    this.authService.login(this.form).subscribe({
      next: (resp) => {
        this.authService.saveToken(resp.token);
        this.router.navigate(['/master-password']);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Login failed';
      }
    });
  }

  requestOtp() {
    this.error = '';
    this.message = '';
    if (!this.form.email) {
      this.error = 'Email is required to request OTP';
      return;
    }
    this.authService.requestOtp(this.form.email).subscribe({
      next: (resp) => {
        this.message = resp.message;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to request OTP';
      }
    });
  }
}
