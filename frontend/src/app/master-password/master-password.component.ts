import { Component, OnInit } from '@angular/core';
import { AuthService, UserProfile } from '../services/auth.service';

@Component({
  selector: 'app-master-password',
  templateUrl: './master-password.component.html',
  styleUrls: ['./master-password.component.css']
})
export class MasterPasswordComponent implements OnInit {
  profile: UserProfile | null = null;
  accountError = '';
  setupMessage = '';
  setupError = '';
  changeMessage = '';
  changeError = '';
  twoFaMessage = '';
  twoFaError = '';
  forgotMessage = '';
  forgotError = '';

  setupForm = {
    masterPassword: '',
    confirmMasterPassword: ''
  };

  changeForm = {
    oldMasterPassword: '',
    newMasterPassword: '',
    confirmNewMasterPassword: ''
  };

  forgotForm = {
    email: '',
    verificationCode: '',
    newMasterPassword: '',
    confirmMasterPassword: ''
  };

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount() {
    this.authService.getAccount().subscribe({
      next: (resp) => {
        this.profile = resp;
        this.accountError = '';
      },
      error: (err) => {
        this.accountError = this.extractError(err, 'Failed to load account');
      }
    });
  }

  setupMasterPassword() {
    this.setupError = '';
    this.setupMessage = '';
    this.authService.setupMasterPassword(
      this.setupForm.masterPassword,
      this.setupForm.confirmMasterPassword
    ).subscribe({
      next: (resp) => {
        this.setupMessage = resp.message;
      },
      error: (err) => {
        this.setupError = this.extractError(err, 'Failed to set master password');
      }
    });
  }

  changeMasterPassword() {
    this.changeError = '';
    this.changeMessage = '';

    if (this.changeForm.newMasterPassword !== this.changeForm.confirmNewMasterPassword) {
      this.changeError = 'New master password and confirm password must match';
      return;
    }

    const confirmed = window.confirm('Are you sure you want to change your master password?');
    if (!confirmed) {
      return;
    }

    this.authService.changeMasterPassword(
      this.changeForm.oldMasterPassword,
      this.changeForm.newMasterPassword
    ).subscribe({
      next: (resp) => {
        this.changeMessage = resp.message;
        this.changeForm = {
          oldMasterPassword: '',
          newMasterPassword: '',
          confirmNewMasterPassword: ''
        };
      },
      error: (err) => {
        this.changeError = this.extractError(err, 'Failed to change master password');
      }
    });
  }

  set2fa(enabled: boolean) {
    this.twoFaError = '';
    this.twoFaMessage = '';
    this.authService.update2fa(enabled).subscribe({
      next: () => {
        this.twoFaMessage = `2FA ${enabled ? 'enabled' : 'disabled'}`;
        this.loadAccount();
      },
      error: (err) => {
        this.twoFaError = this.extractError(err, 'Failed to update 2FA');
      }
    });
  }

  requestForgotMasterPasswordCode() {
    this.forgotError = '';
    this.forgotMessage = '';

    if (!this.forgotForm.email) {
      this.forgotError = 'Email is required';
      return;
    }

    this.authService.requestForgotMasterPasswordCode(this.forgotForm.email).subscribe({
      next: (resp) => {
        this.forgotMessage = resp.message;
      },
      error: (err) => {
        this.forgotError = this.extractError(err, 'Failed to send verification code');
      }
    });
  }

  resetForgotMasterPassword() {
    this.forgotError = '';
    this.forgotMessage = '';

    if (this.forgotForm.newMasterPassword !== this.forgotForm.confirmMasterPassword) {
      this.forgotError = 'New master password and confirm master password must match';
      return;
    }

    this.authService.resetForgotMasterPassword({
      email: this.forgotForm.email,
      verificationCode: this.forgotForm.verificationCode,
      newMasterPassword: this.forgotForm.newMasterPassword,
      confirmMasterPassword: this.forgotForm.confirmMasterPassword
    }).subscribe({
      next: (resp) => {
        this.forgotMessage = resp.message;
        this.forgotForm = {
          email: '',
          verificationCode: '',
          newMasterPassword: '',
          confirmMasterPassword: ''
        };
      },
      error: (err) => {
        this.forgotError = this.extractError(err, 'Failed to reset master password');
      }
    });
  }

  private extractError(err: any, fallback: string): string {
    const backendMessage = err?.error?.message;
    if (backendMessage === 'Invalid authentication principal' || backendMessage === 'Authentication required') {
      return 'Session expired. Please login again.';
    }
    return backendMessage ?? fallback;
  }
}
