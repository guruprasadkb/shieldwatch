export interface User {
  id: string;
  username: string;
  displayName: string;
  role: 'ANALYST' | 'LEAD' | 'ADMIN';
}

export interface Team {
  id: string;
  name: string;
  description: string;
}

export interface Incident {
  id: string;
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'TRIAGED' | 'INVESTIGATING' | 'RESOLVED' | 'CLOSED' | 'REOPENED' | 'CANCELLED';
  reporterUsername: string;
  assigneeUsername: string | null;
  teamName: string | null;
  teamId: string | null;
  triageDeadline: string | null;
  resolutionDeadline: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AuditEntry {
  id: string;
  incidentId: string;
  performedBy: string;
  action: string;
  oldValue: string | null;
  newValue: string | null;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DashboardMetrics {
  totalIncidents: number;
  bySeverity: Record<string, number>;
  byStatus: Record<string, number>;
}
