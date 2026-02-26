import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AlertResponse,
  AuditResponse,
  GeneratePasswordRequest,
  PasswordEntryResponse,
  PasswordResponse,
  SavePasswordRequest,
  StoredPasswordAnalysisResponse
} from '../models/generator.models';

@Injectable({
  providedIn: 'root'
})
export class GeneratorApiService {
  private readonly baseUrl = 'http://localhost:8084/api/generator';

  constructor(private readonly http: HttpClient) {}

  generatePasswords(payload: GeneratePasswordRequest): Observable<PasswordResponse[]> {
    return this.http.post<PasswordResponse[]>(`${this.baseUrl}/generate`, payload);
  }

  runAudit(): Observable<AuditResponse> {
    return this.http.get<AuditResponse>(`${this.baseUrl}/audit`);
  }

  getAlerts(): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>(`${this.baseUrl}/audit/alerts`);
  }

  getPasswordAnalysis(): Observable<StoredPasswordAnalysisResponse[]> {
    return this.http.get<StoredPasswordAnalysisResponse[]>(`${this.baseUrl}/audit/passwords-analysis`);
  }

  savePassword(payload: SavePasswordRequest): Observable<PasswordEntryResponse> {
    return this.http.post<PasswordEntryResponse>(`${this.baseUrl}/vault/passwords`, payload);
  }

  getVaultPasswords(): Observable<PasswordEntryResponse[]> {
    return this.http.get<PasswordEntryResponse[]>(`${this.baseUrl}/vault/passwords`);
  }
}
