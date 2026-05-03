import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import type { Incident, PaginatedResponse } from '../types';

const severityColors: Record<string, string> = {
  CRITICAL: 'bg-red-600',
  HIGH: 'bg-orange-500',
  MEDIUM: 'bg-yellow-500',
  LOW: 'bg-green-500',
};

const statusColors: Record<string, string> = {
  OPEN: 'bg-blue-500',
  TRIAGED: 'bg-indigo-500',
  INVESTIGATING: 'bg-purple-500',
  RESOLVED: 'bg-green-600',
  CLOSED: 'bg-gray-500',
  REOPENED: 'bg-orange-600',
  CANCELLED: 'bg-gray-400',
};

export default function Dashboard() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [severityFilter, setSeverityFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', '20');
    if (severityFilter) params.set('severity', severityFilter);
    if (statusFilter) params.set('status', statusFilter);

    client.get<PaginatedResponse<Incident>>(`/incidents?${params}`).then((res) => {
      setIncidents(res.data.content);
      setTotalElements(res.data.totalElements);
    });
  }, [page, severityFilter, statusFilter]);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-white">Incidents</h1>
        <span className="text-gray-400">{totalElements} total</span>
      </div>

      <div className="flex gap-3 mb-6">
        <select
          value={severityFilter}
          onChange={(e) => { setSeverityFilter(e.target.value); setPage(0); }}
          className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600"
        >
          <option value="">All Severities</option>
          <option value="CRITICAL">Critical</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600"
        >
          <option value="">All Statuses</option>
          <option value="OPEN">Open</option>
          <option value="TRIAGED">Triaged</option>
          <option value="INVESTIGATING">Investigating</option>
          <option value="RESOLVED">Resolved</option>
          <option value="CLOSED">Closed</option>
          <option value="REOPENED">Reopened</option>
        </select>
      </div>

      <div className="bg-gray-800 rounded-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-gray-700 text-gray-400 text-sm">
              <th className="text-left p-4">Title</th>
              <th className="text-left p-4">Severity</th>
              <th className="text-left p-4">Status</th>
              <th className="text-left p-4">Assignee</th>
              <th className="text-left p-4">Team</th>
              <th className="text-left p-4">Created</th>
            </tr>
          </thead>
          <tbody>
            {incidents.map((inc) => (
              <tr key={inc.id} className="border-b border-gray-700 hover:bg-gray-750">
                <td className="p-4">
                  <Link to={`/incidents/${inc.id}`} className="text-blue-400 hover:underline">
                    {inc.title}
                  </Link>
                </td>
                <td className="p-4">
                  <span className={`px-2 py-1 rounded text-xs font-medium text-white ${severityColors[inc.severity]}`}>
                    {inc.severity}
                  </span>
                </td>
                <td className="p-4">
                  <span className={`px-2 py-1 rounded text-xs font-medium text-white ${statusColors[inc.status]}`}>
                    {inc.status}
                  </span>
                </td>
                <td className="p-4 text-gray-300">{inc.assigneeUsername || '—'}</td>
                <td className="p-4 text-gray-300">{inc.teamName || '—'}</td>
                <td className="p-4 text-gray-400 text-sm">
                  {new Date(inc.createdAt).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex justify-between mt-4">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50"
        >
          Previous
        </button>
        <span className="text-gray-400 self-center">Page {page + 1}</span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={incidents.length < 20}
          className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}
