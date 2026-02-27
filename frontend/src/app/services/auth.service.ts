import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  token: string;
  message: string;
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  twoFactorEnabled: boolean;
  phone: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = 'http://localhost:8084/auth';
  private readonly tokenKey = 'pm_token';
  private readonly legacyTokenKey = 'token';

  constructor(private http: HttpClient) {}

  register(payload: {
    name: string;
    email: string;
    password: string;
    phone: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: { email: string; password: string; otp?: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload);
  }

  requestOtp(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/2fa/request`, { email });
  }

  requestForgotPasswordCode(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/password/forgot/request`, { email });
  }

  resetForgotPassword(payload: {
    email: string;
    verificationCode: string;
    newPassword: string;
    confirmPassword: string;
  }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/password/forgot/reset`, payload);
  }

  requestForgotMasterPasswordCode(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/master-password/forgot/request`, { email });
  }

  resetForgotMasterPassword(payload: {
    email: string;
    verificationCode: string;
    newMasterPassword: string;
    confirmMasterPassword: string;
  }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/master-password/forgot/reset`, payload);
  }

  logout(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/logout`,
      {},
      { headers: this.authHeaders() }
    );
  }

  getAccount(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/account`, { headers: this.authHeaders() });
  }

  setupMasterPassword(masterPassword: string, confirmMasterPassword: string) {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/master-password/setup`,
      { masterPassword, confirmMasterPassword },
      { headers: this.authHeaders() }
    );
  }

  changeMasterPassword(oldMasterPassword: string, newMasterPassword: string) {
    return this.http.put<{ message: string }>(
      `${this.baseUrl}/master-password/change`,
      { oldMasterPassword, newMasterPassword },
      { headers: this.authHeaders() }
    );
  }

  update2fa(enabled: boolean) {
    return this.http.put<{ enabled: boolean }>(
      `${this.baseUrl}/2fa/status`,
      { enabled },
      { headers: this.authHeaders() }
    );
  }

  saveToken(token: string) {
    // Keep both keys for cross-module compatibility.
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.legacyTokenKey, token);
  }

  clearToken() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.legacyTokenKey);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.tokenKey);
    if (token) {
      return token;
    }

    const legacyToken = localStorage.getItem(this.legacyTokenKey);
    if (legacyToken) {
      // Self-heal old sessions that used `token`.
      localStorage.setItem(this.tokenKey, legacyToken);
      return legacyToken;
    }

    return null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  authHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.getToken() ?? ''}`,
      'Content-Type': 'application/json'
    });
  }
}
