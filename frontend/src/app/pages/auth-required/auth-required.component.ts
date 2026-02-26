import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth-required',
  templateUrl: './auth-required.component.html',
  styleUrls: ['./auth-required.component.css']
})
export class AuthRequiredComponent {
  constructor(private readonly router: Router) {}

  useDemoAccess(): void {
    localStorage.setItem('pm_token', 'demo-token');
    this.router.navigate(['/console']);
  }
}
