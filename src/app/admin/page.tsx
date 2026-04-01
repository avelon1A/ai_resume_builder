"use client";

import { useEffect, useRef, useState } from "react";

interface Stats {
  totalUsers: number;
  totalResumes: number;
  activeToday: number;
  premiumUsers: number;
  freeUsers: number;
  guestUsers: number;
}

interface User {
  id: string;
  email: string;
  name: string;
  subscriptionTier: string;
  apiRequestsToday: number;
  lastRequestDate: string;
  createdAt: string;
}

interface Resume {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  title: string;
  template: string;
  createdAt: string;
}

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export default function AdminDashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [activeTab, setActiveTab] = useState<"overview" | "users" | "resumes">("overview");
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      try {
        const [statsRes, usersRes, resumesRes] = await Promise.all([
          fetch(`${API_BASE}/admin/stats`),
          fetch(`${API_BASE}/admin/users`),
          fetch(`${API_BASE}/admin/resumes`),
        ]);
        const statsData = await statsRes.json();
        const usersData = await usersRes.json();
        const resumesData = await resumesRes.json();
        if (!cancelled) {
          setStats(statsData);
          setUsers(usersData.users || []);
          setResumes(resumesData.resumes || []);
        }
      } catch {
        // Error fetching admin data
      }
      if (!cancelled) setLoading(false);
    }

    loadData();
    return () => { cancelled = true; };
  }, [refreshKey]);

  function refresh() {
    setRefreshKey((k) => k + 1);
  }

  async function deleteUser(id: string) {
    if (!confirm("Delete this user and all their resumes?")) return;
    await fetch(`${API_BASE}/admin/users/${id}`, { method: "DELETE" });
    refresh();
  }

  async function deleteResume(id: string) {
    if (!confirm("Delete this resume?")) return;
    await fetch(`${API_BASE}/admin/resumes/${id}`, { method: "DELETE" });
    refresh();
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <div className="text-neutral-400 text-lg">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-950 text-white">
      <header className="border-b border-neutral-800 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Admin Dashboard</h1>
            <p className="text-neutral-400 text-sm">AI Resume Builder</p>
          </div>
          <button
            onClick={refresh}
            className="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-sm transition"
          >
            Refresh
          </button>
        </div>
      </header>

      <nav className="border-b border-neutral-800 px-6">
        <div className="max-w-7xl mx-auto flex gap-1">
          {(["overview", "users", "resumes"] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-3 text-sm font-medium capitalize transition ${
                activeTab === tab
                  ? "text-blue-400 border-b-2 border-blue-400"
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              {tab}
            </button>
          ))}
        </div>
      </nav>

      <main className="max-w-7xl mx-auto p-6">
        {activeTab === "overview" && stats && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <StatCard label="Total Users" value={stats.totalUsers} color="blue" />
            <StatCard label="Total Resumes" value={stats.totalResumes} color="green" />
            <StatCard label="Active Today" value={stats.activeToday} color="yellow" />
            <StatCard label="Premium Users" value={stats.premiumUsers} color="purple" />
            <StatCard label="Free Users" value={stats.freeUsers} color="neutral" />
            <StatCard label="Guest Users" value={stats.guestUsers} color="orange" />
          </div>
        )}

        {activeTab === "users" && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-neutral-800 text-left text-neutral-400">
                  <th className="py-3 px-4">Name</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Tier</th>
                  <th className="py-3 px-4">Requests Today</th>
                  <th className="py-3 px-4">Created</th>
                  <th className="py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-b border-neutral-800 hover:bg-neutral-900">
                    <td className="py-3 px-4">{user.name}</td>
                    <td className="py-3 px-4 text-neutral-400">{user.email}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-1 rounded text-xs ${
                          user.subscriptionTier === "premium"
                            ? "bg-purple-900 text-purple-300"
                            : "bg-neutral-800 text-neutral-400"
                        }`}
                      >
                        {user.subscriptionTier}
                      </span>
                    </td>
                    <td className="py-3 px-4">{user.apiRequestsToday}</td>
                    <td className="py-3 px-4 text-neutral-400">
                      {new Date(user.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3 px-4">
                      <button
                        onClick={() => deleteUser(user.id)}
                        className="text-red-400 hover:text-red-300 text-xs"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "resumes" && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-neutral-800 text-left text-neutral-400">
                  <th className="py-3 px-4">Title</th>
                  <th className="py-3 px-4">User</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Template</th>
                  <th className="py-3 px-4">Created</th>
                  <th className="py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {resumes.map((resume) => (
                  <tr key={resume.id} className="border-b border-neutral-800 hover:bg-neutral-900">
                    <td className="py-3 px-4">{resume.title}</td>
                    <td className="py-3 px-4">{resume.userName}</td>
                    <td className="py-3 px-4 text-neutral-400">{resume.userEmail}</td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-1 bg-neutral-800 rounded text-xs">
                        {resume.template}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-neutral-400">
                      {new Date(resume.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3 px-4">
                      <button
                        onClick={() => deleteResume(resume.id)}
                        className="text-red-400 hover:text-red-300 text-xs"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}

function StatCard({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  const colors: Record<string, string> = {
    blue: "border-blue-500 text-blue-400",
    green: "border-green-500 text-green-400",
    yellow: "border-yellow-500 text-yellow-400",
    purple: "border-purple-500 text-purple-400",
    orange: "border-orange-500 text-orange-400",
    neutral: "border-neutral-500 text-neutral-400",
  };

  return (
    <div className={`bg-neutral-900 border-l-4 ${colors[color]} rounded-lg p-6`}>
      <div className="text-3xl font-bold">{value}</div>
      <div className="text-neutral-400 text-sm mt-1">{label}</div>
    </div>
  );
}
