import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  login() {
    this.error = '';
    this.message = '';

    const email = this.form.email.trim();
    const password = this.form.password.trim();

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

    this.authService.login(this.form).subscribe({
      next: (resp) => {
        this.authService.saveToken(resp.token);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl && returnUrl.startsWith('/') ? returnUrl : '/master-password');
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Login failed';
      }
    });
  }

  requestOtp() {
    this.error = '';
    this.message = '';
    const email = this.form.email.trim();
    if (!email) {
      this.error = 'Email is required to request OTP';
      return;
    }
    if (!this.isValidEmail(email)) {
      this.error = 'Enter a valid email address';
      return;
    }

    this.authService.requestOtp(email).subscribe({
      next: (resp) => {
        this.message = resp.message;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to request OTP';
      }
    });
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
