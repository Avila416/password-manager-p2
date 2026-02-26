import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

interface DashboardResponse {
  totalPasswords: number;
  weakPasswords: number;
  recentPasswords: number;
  actionStats?: Record<string, number>;
}

interface VaultEntry {
  id: number;
  title: string;
  username: string;
  password: string;
  createdAt: string;
}

interface AuditLog {
  action: string;
  ip: string;
  status: string;
  time: string;
}

interface LatestBackupInfo {
  exists: boolean;
  fileName: string;
  filePath: string;
  createdAt: string;
}

interface BackupValidateResponse {
  message: string;
  checksum: string;
  encryptedLength: number;
  payloadLength: number;
  validatedAt: string;
}

interface BackupUpdateResponse {
  message: string;
  fileName: string;
  filePath: string;
  checksum: string;
  payloadLength: number;
  updatedAt: string;
}

@Component({
  selector: 'app-console',
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.css']
})
export class ConsoleComponent implements OnInit {
  dashboard: DashboardResponse | null = null;
  entries: VaultEntry[] = [];
  auditLogs: AuditLog[] = [];
  revealedPasswords: Record<number, string> = {};

  newEntry = {
    title: '',
    username: '',
    password: ''
  };

  auditFilter = {
    action: '',
    status: '',
    ip: ''
  };

  loginMonitor = {
    success: true,
    ip: '127.0.0.1'
  };

  backupContent = '';
  backupExport = '';
  backupValidationResult: BackupValidateResponse | null = null;
  backupUpdateResult: BackupUpdateResponse | null = null;
  latestBackupInfo: LatestBackupInfo | null = null;

  loading = false;
  message = '';
  error = '';
  lastRefreshedAt = '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.auditFilter = { action: '', status: '', ip: '' };
    this.lastRefreshedAt = new Date().toLocaleTimeString();
    this.message = `Data refreshed at ${this.lastRefreshedAt}`;
    this.error = '';
    this.fetchDashboard();
    this.fetchEntries();
    this.fetchAuditLogs();
    this.fetchLatestBackupInfo();
  }

  fetchDashboard(): void {
    this.http.get<DashboardResponse>('/api/dashboard').subscribe({
      next: (data) => {
        this.dashboard = data;
        this.error = '';
      },
      error: () => {
        this.error = 'Failed to load dashboard.';
      }
    });
  }

  fetchEntries(): void {
    this.http.get<VaultEntry[]>('/api/vault/entries').subscribe({
      next: (data) => {
        this.entries = data;
        this.error = '';
        const validIds = new Set(data.map((entry) => entry.id));
        Object.keys(this.revealedPasswords).forEach((id) => {
          if (!validIds.has(Number(id))) {
            delete this.revealedPasswords[Number(id)];
          }
        });
      },
      error: () => {
        this.error = 'Failed to load vault entries.';
      }
    });
  }

  addEntry(): void {
    if (!this.newEntry.title || !this.newEntry.username || !this.newEntry.password) {
      this.error = 'All entry fields are required.';
      return;
    }

    this.http.post<VaultEntry>('/api/vault/entries', this.newEntry).subscribe({
      next: () => {
        this.newEntry = { title: '', username: '', password: '' };
        this.message = 'Vault entry created.';
        this.error = '';
        this.fetchEntries();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to create vault entry.';
      }
    });
  }

  viewEntry(entry: VaultEntry): void {
    if (this.revealedPasswords[entry.id]) {
      delete this.revealedPasswords[entry.id];
      return;
    }

    const params = new HttpParams().set('ip', this.loginMonitor.ip || '127.0.0.1');
    this.http.get<VaultEntry>(`/api/vault/entries/${entry.id}`, { params }).subscribe({
      next: (viewedEntry) => {
        this.revealedPasswords[viewedEntry.id] = viewedEntry.password;
        this.error = '';
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to view vault entry.';
      }
    });
  }

  deleteEntry(entryId: number): void {
    const params = new HttpParams().set('ip', this.loginMonitor.ip || '127.0.0.1');
    this.http.delete(`/api/vault/entries/${entryId}`, { params, responseType: 'text' }).subscribe({
      next: (res) => {
        delete this.revealedPasswords[entryId];
        this.message = res;
        this.error = '';
        this.fetchEntries();
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to delete vault entry.';
      }
    });
  }

  fetchAuditLogs(): void {
    const action = this.auditFilter.action.trim();
    const status = this.auditFilter.status.trim();
    const ip = this.auditFilter.ip.trim();

    let params = new HttpParams();
    if (action) {
      params = params.set('action', action);
    }
    if (status) {
      params = params.set('status', status);
    }
    if (ip) {
      params = params.set('ip', ip);
    }

    this.http.get<AuditLog[]>('/api/audit', { params }).subscribe({
      next: (data) => {
        this.auditLogs = data;
        this.error = '';
      },
      error: () => {
        this.error = 'Failed to load audit logs.';
      }
    });
  }

  clearAuditFilters(): void {
    this.auditFilter = { action: '', status: '', ip: '' };
    this.fetchAuditLogs();
  }

  logLoginAttempt(): void {
    const params = new HttpParams()
      .set('success', String(this.loginMonitor.success))
      .set('ip', this.loginMonitor.ip || '127.0.0.1');

    this.http.post('/api/vault/monitor/login', null, { params, responseType: 'text' }).subscribe({
      next: (res) => {
        this.message = res;
        this.error = '';
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to log login event.';
      }
    });
  }

  logMasterPasswordChange(): void {
    const params = new HttpParams().set('ip', this.loginMonitor.ip || '127.0.0.1');
    this.http.post('/api/vault/monitor/master-password-change', null, { params, responseType: 'text' }).subscribe({
      next: (res) => {
        this.message = res;
        this.error = '';
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to log master password change.';
      }
    });
  }

  exportBackup(): void {
    this.loading = true;
    this.http.get('/api/backup/export', { responseType: 'text' }).subscribe({
      next: (data) => {
        this.backupExport = data;
        this.backupContent = data;
        this.loading = false;
        this.message = 'Backup exported.';
        this.error = '';
        this.fetchAuditLogs();
        this.fetchDashboard();
        this.fetchLatestBackupInfo();
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to export backup.';
      }
    });
  }

  validateBackup(): void {
    if (!this.ensureBackupContent('validate')) {
      return;
    }
    this.http.patch<BackupValidateResponse>('/api/backup/validate', { fileContent: this.backupContent }).subscribe({
      next: (res) => {
        this.backupValidationResult = res;
        this.backupUpdateResult = null;
        this.message = `${res.message} (checksum: ${res.checksum.slice(0, 12)}...)`;
        this.error = '';
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Backup validation failed.';
      }
    });
  }

  restoreBackup(): void {
    if (!this.ensureBackupContent('restore')) {
      return;
    }
    this.http.post('/api/backup/restore', { fileContent: this.backupContent }, { responseType: 'text' }).subscribe({
      next: (res) => {
        this.message = `Restore result: ${res}`;
        this.error = '';
        this.fetchEntries();
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to restore backup.';
      }
    });
  }

  updateBackup(): void {
    if (!this.ensureBackupContent('update')) {
      return;
    }
    this.http.put<BackupUpdateResponse>('/api/backup/update', { fileContent: this.backupContent }).subscribe({
      next: (res) => {
        this.backupUpdateResult = res;
        this.backupValidationResult = null;
        this.message = `${res.message} (${res.fileName})`;
        this.error = '';
        this.fetchLatestBackupInfo();
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to update backup.';
      }
    });
  }

  deleteBackup(): void {
    this.http.delete('/api/backup/delete', { responseType: 'text' }).subscribe({
      next: (res) => {
        this.message = res;
        this.error = '';
        this.backupUpdateResult = null;
        this.fetchLatestBackupInfo();
        this.fetchAuditLogs();
        this.fetchDashboard();
      },
      error: () => {
        this.error = 'Failed to delete backup.';
      }
    });
  }

  fetchLatestBackupInfo(): void {
    this.http.get<LatestBackupInfo>('/api/backup/latest').subscribe({
      next: (data) => {
        this.latestBackupInfo = data;
      },
      error: () => {
        this.latestBackupInfo = null;
      }
    });
  }

  private ensureBackupContent(action: string): boolean {
    if (this.backupContent.trim()) {
      return true;
    }
    this.error = `Paste backup content in the textarea before ${action}.`;
    return false;
  }
}
