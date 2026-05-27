import { useState } from 'react';
import Auth from './components/Auth';
import Quiz from './components/Quiz';
import MyPage from './components/MyPage';
import EventList from './components/EventList';

function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [view, setView] = useState<'QUIZ' | 'MYPAGE' | 'EVENT'>('QUIZ');

  const handleLoginSuccess = (newToken: string) => {
    setToken(newToken);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setView('QUIZ');
  };

  const renderContent = () => {
    if (!token) return <Auth onLoginSuccess={handleLoginSuccess} />;
    
    switch (view) {
      case 'MYPAGE': return <MyPage onNavigateToEvents={() => setView('EVENT')} />;
      case 'EVENT': return <EventList />;
      default: return <Quiz />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-6">
      <header className="max-w-4xl mx-auto flex justify-between items-center mb-8 px-4">
        <h1 
          className="text-4xl font-black text-blue-600 cursor-pointer tracking-tighter"
          onClick={() => setView('QUIZ')}
        >
          럭키보카
        </h1>
        
        {token && (
          <nav className="flex gap-2 items-center bg-white p-2 rounded-2xl shadow-sm border border-gray-100">
            <button
              onClick={() => setView('QUIZ')}
              className={`px-4 py-2 rounded-xl text-sm font-bold transition ${view === 'QUIZ' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
            >
              Quiz
            </button>
            <button
              onClick={() => setView('EVENT')}
              className={`px-4 py-2 rounded-xl text-sm font-bold transition ${view === 'EVENT' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
            >
              Events
            </button>
            <button
              onClick={() => setView('MYPAGE')}
              className={`px-4 py-2 rounded-xl text-sm font-bold transition ${view === 'MYPAGE' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
            >
              My
            </button>
            <div className="w-px h-4 bg-gray-200 mx-1"></div>
            <button
              onClick={handleLogout}
              className="px-3 py-2 text-sm font-bold text-red-400 hover:text-red-600 transition"
            >
              Logout
            </button>
          </nav>
        )}
      </header>

      <main>
        {renderContent()}
      </main>

      <footer className="text-center mt-20 text-gray-400 text-xs font-medium uppercase tracking-widest">
        &copy; 2026 럭키보카. Engineering Excellence.
      </footer>
    </div>
  );
}

export default App;
