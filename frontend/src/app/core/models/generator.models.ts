export interface GeneratePasswordRequest {
  length: number;
  uppercase: boolean;
  lowercase: boolean;
  numbers: boolean;
  specialChars: boolean;
  excludeSimilar: boolean;
  count: number;
}

export interface PasswordResponse {
  password: string;
  strength: 'WEAK' | 'MEDIUM' | 'STRONG' | 'VERY_STRONG';
}

export interface SavePasswordRequest {
  username: string;
  password: string;
}

export interface PasswordEntryResponse {
  id: number;
  username: string;
  password: string;
  strength: 'WEAK' | 'MEDIUM' | 'STRONG' | 'VERY_STRONG';
  createdAt: string;
}

export interface AuditResponse {
  total: number;
  weak: number;
  reused: number;
  old: number;
  reportId: number;
  alertCount: number;
  generatedAt: string;
}

export interface AlertResponse {
  id: number;
  message: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  type: 'WEAK' | 'REUSED' | 'OLD';
  createdAt: string;
}

export interface StoredPasswordAnalysisResponse {
  entryId: number;
  username: string;
  website?: string;
  strength: 'WEAK' | 'MEDIUM' | 'STRONG' | 'VERY_STRONG';
  weak: boolean;
  reused: boolean;
  old: boolean;
  createdAt: string;
}
