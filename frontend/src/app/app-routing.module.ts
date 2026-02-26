import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GeneratorComponent } from './pages/generator/generator.component';
import { AuditComponent } from './pages/audit/audit.component';
import { AuthRequiredComponent } from './pages/auth-required/auth-required.component';
import { moduleAuthGuard } from './core/guards/module-auth.guard';
import { VaultComponent } from './vault/vault.component';
import { vaultGuard } from './guards/vault.guard';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { MasterPasswordComponent } from './master-password/master-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { authGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'generator' },
  { path: 'generator', component: GeneratorComponent, canActivate: [moduleAuthGuard] },
  { path: 'audit', component: AuditComponent, canActivate: [moduleAuthGuard] },
  { path: 'vault', component: VaultComponent, canActivate: [vaultGuard] },

  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'master-password', component: MasterPasswordComponent, canActivate: [authGuard] },

  { path: 'auth-required', component: AuthRequiredComponent },
  { path: '**', redirectTo: 'generator' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
