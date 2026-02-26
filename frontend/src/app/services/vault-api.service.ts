import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SearchPayload, VaultEntry, VaultEntryPayload } from '../models/vault.models';

@Injectable({ providedIn: 'root' })
export class VaultApiService {
  private readonly baseUrl = 'http://localhost:8083/api/vault';

  constructor(private http: HttpClient) {}

  getAllEntries(): Observable<VaultEntry[]> {
    return this.http.get<VaultEntry[]>(this.baseUrl);
  }

  addEntry(payload: VaultEntryPayload): Observable<VaultEntry> {
    return this.http.post<VaultEntry>(this.baseUrl, payload);
  }

  updateEntry(id: number, payload: VaultEntryPayload): Observable<VaultEntry> {
    return this.http.put<VaultEntry>(`${this.baseUrl}/${id}`, payload);
  }

  deleteEntry(id: number, masterPassword: string): Observable<void> {
    const params = new HttpParams().set('masterPassword', masterPassword);
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { params });
  }

  markFavorite(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/favorite`, {});
  }

  getFavorites(): Observable<VaultEntry[]> {
    return this.http.get<VaultEntry[]>(`${this.baseUrl}/favorites`);
  }

  verifyAndGet(id: number, masterPassword: string): Observable<VaultEntry> {
    return this.http.post<VaultEntry>(`${this.baseUrl}/${id}/verify`, { masterPassword });
  }

  searchEntries(payload: SearchPayload, sortBy?: string, direction: string = 'asc'): Observable<VaultEntry[]> {
    let params = new HttpParams();
    if (sortBy) {
      params = params.set('sortBy', sortBy);
    }
    params = params.set('direction', direction);

    return this.http.post<VaultEntry[]>(`${this.baseUrl}/search`, payload, { params });
  }
}

