import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [HttpClientTestingModule, FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('refreshAll should clear audit filters and request dashboard, entries, and audit logs', () => {
    component.auditFilter = { action: 'delete', status: 'failed', ip: '127.0.0.1' };

    component.refreshAll();

    expect(component.auditFilter).toEqual({ action: '', status: '', ip: '' });
    httpMock.expectOne('/api/dashboard');
    httpMock.expectOne('/api/vault/entries');
    httpMock.expectOne((req) => req.url === '/api/audit' && req.params.keys().length === 0);
  });

  it('fetchAuditLogs should trim and send only non-empty params', () => {
    component.auditFilter = { action: '  delete ', status: '   ', ip: ' 127.0 ' };

    component.fetchAuditLogs();

    const req = httpMock.expectOne((request) => request.url === '/api/audit');
    expect(req.request.params.get('action')).toBe('delete');
    expect(req.request.params.get('ip')).toBe('127.0');
    expect(req.request.params.has('status')).toBeFalse();
    req.flush([]);
  });
});
