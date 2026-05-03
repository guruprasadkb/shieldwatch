import { useEffect, useState } from 'react';
import client from '../api/client';
import type { Team } from '../types';

interface TeamMember {
  id: string;
  username: string;
  displayName: string;
  role: string;
}

export default function TeamView() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [selectedTeam, setSelectedTeam] = useState<string | null>(null);
  const [members, setMembers] = useState<TeamMember[]>([]);

  useEffect(() => {
    client.get<Team[]>('/teams').then((res) => setTeams(res.data));
  }, []);

  useEffect(() => {
    if (!selectedTeam) return;
    client.get<TeamMember[]>(`/teams/${selectedTeam}/members`).then((res) => setMembers(res.data));
  }, [selectedTeam]);

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-6">Teams</h1>
      <div className="grid grid-cols-3 gap-4 mb-8">
        {teams.map((team) => (
          <button
            key={team.id}
            onClick={() => setSelectedTeam(team.id)}
            className={`p-4 rounded-lg text-left transition ${
              selectedTeam === team.id ? 'bg-blue-600' : 'bg-gray-800 hover:bg-gray-750'
            }`}
          >
            <h3 className="text-white font-semibold">{team.name}</h3>
            <p className="text-gray-400 text-sm">{team.description}</p>
          </button>
        ))}
      </div>

      {selectedTeam && (
        <div className="bg-gray-800 rounded-lg p-6">
          <h2 className="text-lg font-semibold text-white mb-4">Members</h2>
          <div className="space-y-3">
            {members.map((member) => (
              <div key={member.id} className="flex items-center gap-4 border-b border-gray-700 pb-3">
                <span className="text-white font-medium">{member.displayName}</span>
                <span className="text-gray-400">@{member.username}</span>
                <span className="px-2 py-1 rounded text-xs bg-gray-700 text-gray-300">{member.role}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
