import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConsoleComponent } from './pages/console/console.component';
import { AuthRequiredComponent } from './pages/auth-required/auth-required.component';
import { ModuleAuthGuard } from './core/guards/module-auth.guard';
import { GeneratorComponent } from './pages/generator/generator.component';
import { SecurityAuditComponent } from './pages/security-audit/security-audit.component';

const routes: Routes = [
  { path: '', redirectTo: 'console', pathMatch: 'full' },
  { path: 'console', component: ConsoleComponent, canActivate: [ModuleAuthGuard] },
  { path: 'generator', component: GeneratorComponent, canActivate: [ModuleAuthGuard] },
  { path: 'security-audit', component: SecurityAuditComponent, canActivate: [ModuleAuthGuard] },
  { path: 'auth-required', component: AuthRequiredComponent },
  { path: '**', redirectTo: 'console' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    anchorScrolling: 'enabled',
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
