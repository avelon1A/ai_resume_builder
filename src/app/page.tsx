import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen bg-neutral-900 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-white mb-4">AI Resume Builder</h1>
        <p className="text-neutral-400 text-lg mb-8">Your intelligent resume creation assistant</p>
        <Link
          href="/admin"
          className="px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white rounded-lg font-medium transition"
        >
          Admin Dashboard
        </Link>
      </div>
    </main>
  );
}
