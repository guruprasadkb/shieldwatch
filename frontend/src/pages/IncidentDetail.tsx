import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import client from '../api/client';
import type { Incident, AuditEntry } from '../types';

export default function IncidentDetail() {
  const { id } = useParams<{ id: string }>();
  const [incident, setIncident] = useState<Incident | null>(null);
  const [audit, setAudit] = useState<AuditEntry[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    client.get<Incident>(`/incidents/${id}`).then((res) => setIncident(res.data));
    client.get<AuditEntry[]>(`/incidents/${id}/audit`).then((res) => setAudit(res.data));
  }, [id]);

  const handleTransition = async (status: string) => {
    try {
      const res = await client.post(`/incidents/${id}/transition`, { status });
      setIncident(res.data);
      const auditRes = await client.get<AuditEntry[]>(`/incidents/${id}/audit`);
      setAudit(auditRes.data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Transition failed');
    }
  };

  if (!incident) return <div className="text-gray-400">Loading...</div>;

  const transitions: Record<string, string[]> = {
    OPEN: ['TRIAGED'],
    TRIAGED: ['INVESTIGATING'],
    INVESTIGATING: ['RESOLVED'],
    RESOLVED: ['CLOSED', 'REOPENED'],
    REOPENED: ['INVESTIGATING'],
  };

  const availableTransitions = transitions[incident.status] || [];

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-2">{incident.title}</h1>
      <p className="text-gray-400 mb-6">{incident.id}</p>

      {error && <p className="text-red-400 mb-4 bg-red-900/20 p-3 rounded">{error}</p>}

      <div className="grid grid-cols-2 gap-6 mb-8">
        <div className="bg-gray-800 p-6 rounded-lg">
          <h2 className="text-lg font-semibold text-white mb-4">Details</h2>
          <dl className="space-y-3">
            <div><dt className="text-gray-500 text-sm">Severity</dt><dd className="text-white">{incident.severity}</dd></div>
            <div><dt className="text-gray-500 text-sm">Status</dt><dd className="text-white">{incident.status}</dd></div>
            <div><dt className="text-gray-500 text-sm">Reporter</dt><dd className="text-white">{incident.reporterUsername}</dd></div>
            <div><dt className="text-gray-500 text-sm">Assignee</dt><dd className="text-white">{incident.assigneeUsername || 'Unassigned'}</dd></div>
            <div><dt className="text-gray-500 text-sm">Team</dt><dd className="text-white">{incident.teamName || '—'}</dd></div>
            <div><dt className="text-gray-500 text-sm">Created</dt><dd className="text-white">{new Date(incident.createdAt).toLocaleString()}</dd></div>
          </dl>
          {incident.triageDeadline && (
            <div className="mt-4">
              <dt className="text-gray-500 text-sm">Triage Deadline</dt>
              <dd className="text-white">{new Date(incident.triageDeadline).toLocaleString()}</dd>
            </div>
          )}
          {incident.resolutionDeadline && (
            <div className="mt-2">
              <dt className="text-gray-500 text-sm">Resolution Deadline</dt>
              <dd className="text-white">{new Date(incident.resolutionDeadline).toLocaleString()}</dd>
            </div>
          )}
        </div>

        <div className="bg-gray-800 p-6 rounded-lg">
          <h2 className="text-lg font-semibold text-white mb-4">Description</h2>
          <p className="text-gray-300">{incident.description}</p>

          {availableTransitions.length > 0 && (
            <div className="mt-6">
              <h3 className="text-sm text-gray-500 mb-2">Transition to:</h3>
              <div className="flex gap-2">
                {availableTransitions.map((status) => (
                  <button
                    key={status}
                    onClick={() => handleTransition(status)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition text-sm"
                  >
                    {status}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="bg-gray-800 p-6 rounded-lg">
        <h2 className="text-lg font-semibold text-white mb-4">Audit Trail</h2>
        <div className="space-y-3">
          {audit.map((entry) => (
            <div key={entry.id} className="flex items-center gap-4 text-sm border-b border-gray-700 pb-3">
              <span className="text-gray-500 w-40">{new Date(entry.timestamp).toLocaleString()}</span>
              <span className="text-blue-400">{entry.performedBy}</span>
              <span className="text-gray-300">{entry.action}</span>
              {entry.oldValue && <span className="text-red-400">{entry.oldValue}</span>}
              {entry.oldValue && entry.newValue && <span className="text-gray-500">→</span>}
              {entry.newValue && <span className="text-green-400">{entry.newValue}</span>}
            </div>
          ))}
          {audit.length === 0 && <p className="text-gray-500">No audit entries</p>}
        </div>
      </div>
    </div>
  );
}
