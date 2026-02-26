import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VaultComponent } from './vault/vault.component';
import { AuthRequiredComponent } from './auth-required/auth-required.component';
import { vaultGuard } from './guards/vault.guard';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { MasterPasswordComponent } from './master-password/master-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { authGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: 'vault', pathMatch: 'full' },
  { path: 'vault', component: VaultComponent, canActivate: [vaultGuard] },
  { path: 'auth-required', component: AuthRequiredComponent },
  { path: '**', redirectTo: 'vault' },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'master-password', component: MasterPasswordComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }

]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
