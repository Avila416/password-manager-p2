import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { VaultComponent } from './vault/vault.component';
import { AppRoutingModule } from './app-routing.module';
import { AuthRequiredComponent } from './auth-required/auth-required.component';
import { AuthTokenInterceptor } from './interceptors/auth-token.interceptor';
import { RegisterComponent } from './register/register.component';

import { LoginComponent } from './login/login.component';
import { MasterPasswordComponent } from './master-password/master-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';

@NgModule({
  declarations: [AppComponent, VaultComponent, AuthRequiredComponent,RegisterComponent,
    LoginComponent,
    MasterPasswordComponent,
    ForgotPasswordComponent],
  imports: [BrowserModule, ReactiveFormsModule, FormsModule, HttpClientModule, AppRoutingModule],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthTokenInterceptor,
      multi: true
    }
  ],
})
export class AppModule {}
