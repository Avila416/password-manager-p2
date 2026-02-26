import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Category, SearchPayload, VaultEntry, VaultEntryPayload } from '../models/vault.models';
import { VaultApiService } from '../services/vault-api.service';

@Component({
  selector: 'app-vault',
  templateUrl: './vault.component.html',
  styleUrls: ['./vault.component.css']
})
export class VaultComponent implements OnInit {
  categories: Category[] = ['SOCIAL', 'BANKING', 'WORK', 'SHOPPING', 'OTHER'];
  entries: VaultEntry[] = [];
  favorites: VaultEntry[] = [];

  viewMode: 'grid' | 'list' = 'grid';
  activeTab: 'all' | 'favorites' = 'all';
  loading = false;
  error = '';
  success = '';

  editingId: number | null = null;
  selectedEntry: VaultEntry | null = null;
  verifyMasterPassword = '';
  revealedPassword = '';

  entryForm = this.fb.group({
    title: ['', Validators.required],
    username: ['', Validators.required],
    password: [''],
    website: ['', Validators.required],
    category: ['OTHER', Validators.required]
  });

  searchForm = this.fb.group({
    keyword: [''],
    category: [''],
    sortBy: ['title'],
    direction: ['asc']
  });

  constructor(private fb: FormBuilder, private api: VaultApiService) {}

  ngOnInit(): void {
    this.loadAllEntries();
    this.loadFavorites();
  }

  loadAllEntries(): void {
    this.loading = true;
    this.api.getAllEntries().subscribe({
      next: (data) => {
        this.entries = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Failed to load entries');
        this.loading = false;
      }
    });
  }

  loadFavorites(): void {
    this.api.getFavorites().subscribe({
      next: (data) => (this.favorites = data),
      error: (err) => (this.error = this.extractErrorMessage(err, 'Failed to load favorites'))
    });
  }

  saveEntry(): void {
    this.error = '';
    this.success = '';

    if (this.entryForm.invalid) {
      this.entryForm.markAllAsTouched();
      return;
    }

    const payload: VaultEntryPayload = {
      title: this.entryForm.value.title || '',
      username: this.entryForm.value.username || '',
      website: this.entryForm.value.website || '',
      category: (this.entryForm.value.category as Category) || 'OTHER'
    };

    const rawPassword = this.entryForm.value.password || '';
    if (rawPassword) {
      payload.password = rawPassword;
    }

    if (!this.editingId && !rawPassword) {
      this.error = 'Password is required for new entry';
      return;
    }

    const request = this.editingId
      ? this.api.updateEntry(this.editingId, payload)
      : this.api.addEntry(payload);

    request.subscribe({
      next: () => {
        this.success = this.editingId ? 'Entry updated' : 'Entry added';
        this.resetForm();
        this.loadAllEntries();
        this.loadFavorites();
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Failed to save entry');
      }
    });
  }

  editEntry(entry: VaultEntry): void {
    this.editingId = entry.id;
    this.entryForm.patchValue({
      title: entry.title,
      username: entry.username,
      website: entry.website,
      category: entry.category,
      password: ''
    });
    this.success = '';
    this.error = '';
  }

  resetForm(): void {
    this.editingId = null;
    this.entryForm.reset({ category: 'OTHER' });
  }

  markFavorite(entry: VaultEntry): void {
    this.api.markFavorite(entry.id).subscribe({
      next: () => {
        this.loadAllEntries();
        this.loadFavorites();
      },
      error: (err) => (this.error = this.extractErrorMessage(err, 'Failed to mark favorite'))
    });
  }

  removeEntry(entry: VaultEntry): void {
    const masterPassword = window.prompt('Enter master password to delete this entry:');
    if (!masterPassword) {
      return;
    }

    this.api.deleteEntry(entry.id, masterPassword).subscribe({
      next: () => {
        this.success = 'Entry deleted';
        this.loadAllEntries();
        this.loadFavorites();
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Failed to delete entry');
      }
    });
  }

  search(): void {
    const payload: SearchPayload = {
      keyword: this.searchForm.value.keyword || undefined,
      category: (this.searchForm.value.category as Category) || undefined,
      sortBy: this.searchForm.value.sortBy || undefined
    };

    const direction = this.searchForm.value.direction || 'asc';

    this.api.searchEntries(payload, payload.sortBy, direction).subscribe({
      next: (data) => {
        this.entries = data;
        this.activeTab = 'all';
      },
      error: (err) => (this.error = this.extractErrorMessage(err, 'Search failed'))
    });
  }

  clearSearch(): void {
    this.searchForm.reset({ sortBy: 'title', direction: 'asc' });
    this.loadAllEntries();
  }

  openVerify(entry: VaultEntry): void {
    this.selectedEntry = entry;
    this.verifyMasterPassword = '';
    this.revealedPassword = '';
  }

  closeVerify(): void {
    this.selectedEntry = null;
    this.verifyMasterPassword = '';
    this.revealedPassword = '';
  }

  verifyAndReveal(): void {
    if (!this.selectedEntry || !this.verifyMasterPassword) {
      return;
    }

    this.api.verifyAndGet(this.selectedEntry.id, this.verifyMasterPassword).subscribe({
      next: (entry) => {
        this.revealedPassword = entry.password;
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Verification failed');
      }
    });
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const err = error as HttpErrorResponse | undefined;
    if (!err) {
      return fallback;
    }

    if (err.status === 0) {
      return 'Cannot connect to backend. Start Spring Boot on localhost:8083.';
    }

    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }

    if (err.error && typeof err.error === 'object' && 'message' in err.error) {
      const message = (err.error as { message?: unknown }).message;
      if (typeof message === 'string' && message.trim()) {
        return message;
      }
    }

    if (typeof err.message === 'string' && err.message.trim()) {
      return err.message;
    }

    return fallback;
  }
}



