import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VaultComponent } from './vault/vault.component';
import { AuthRequiredComponent } from './auth-required/auth-required.component';
import { vaultGuard } from './guards/vault.guard';

const routes: Routes = [
  { path: '', redirectTo: 'vault', pathMatch: 'full' },
  { path: 'vault', component: VaultComponent, canActivate: [vaultGuard] },
  { path: 'auth-required', component: AuthRequiredComponent },
  { path: '**', redirectTo: 'vault' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
