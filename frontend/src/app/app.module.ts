import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { ConsoleComponent } from './pages/console/console.component';
import { AuthRequiredComponent } from './pages/auth-required/auth-required.component';
import { AuthTokenInterceptor } from './core/interceptors/auth-token.interceptor';
import { GeneratorComponent } from './pages/generator/generator.component';
import { SecurityAuditComponent } from './pages/security-audit/security-audit.component';

@NgModule({
  declarations: [
    AppComponent,
    ConsoleComponent,
    AuthRequiredComponent,
    GeneratorComponent,
    SecurityAuditComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthTokenInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
