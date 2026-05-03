import { NavLink, Outlet } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

export default function Layout() {
  const { username, role, logout } = useAuthStore();

  return (
    <div className="min-h-screen bg-gray-900 flex">
      <aside className="w-64 bg-gray-800 border-r border-gray-700 flex flex-col">
        <div className="p-6">
          <h1 className="text-xl font-bold text-white">ShieldWatch</h1>
          <p className="text-gray-500 text-sm mt-1">Incident Management</p>
        </div>
        <nav className="flex-1 px-4 space-y-1">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `block px-4 py-2 rounded text-sm ${isActive ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-700'}`
            }
          >
            Incidents
          </NavLink>
          <NavLink
            to="/teams"
            className={({ isActive }) =>
              `block px-4 py-2 rounded text-sm ${isActive ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-700'}`
            }
          >
            Teams
          </NavLink>
        </nav>
        <div className="p-4 border-t border-gray-700">
          <p className="text-white text-sm">{username}</p>
          <p className="text-gray-500 text-xs">{role}</p>
          <button
            onClick={logout}
            className="mt-2 text-sm text-red-400 hover:text-red-300"
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 p-8">
        <Outlet />
      </main>
    </div>
  );
}
