import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GeneratorComponent } from './pages/generator/generator.component';
import { AuditComponent } from './pages/audit/audit.component';
import { AuthRequiredComponent } from './pages/auth-required/auth-required.component';
import { moduleAuthGuard } from './core/guards/module-auth.guard';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'generator' },
  { path: 'generator', component: GeneratorComponent, canActivate: [moduleAuthGuard] },
  { path: 'audit', component: AuditComponent, canActivate: [moduleAuthGuard] },
  { path: 'auth-required', component: AuthRequiredComponent },
  { path: '**', redirectTo: 'generator' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
