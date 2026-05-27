import { useState } from 'react';
import Auth from './components/Auth';
import Quiz from './components/Quiz';
import MyPage from './components/MyPage';

function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [view, setView] = useState<'QUIZ' | 'MYPAGE'>('QUIZ');

  const handleLoginSuccess = (newToken: string) => {
    setToken(newToken);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setView('QUIZ');
  };

  return (
    <div className="min-h-screen bg-gray-100 py-10">
      <header className="max-w-2xl mx-auto flex justify-between items-center mb-10 px-4">
        <h1 
          className="text-4xl font-extrabold text-blue-600 cursor-pointer"
          onClick={() => setView('QUIZ')}
        >
          BitPop Quiz
        </h1>
        {token && (
          <div className="flex gap-4 items-center">
            <button
              onClick={() => setView(view === 'QUIZ' ? 'MYPAGE' : 'QUIZ')}
              className="text-sm font-bold text-gray-700 bg-white px-4 py-2 rounded-full shadow hover:bg-gray-50"
            >
              {view === 'QUIZ' ? '마이페이지' : '퀴즈로 돌아가기'}
            </button>
            <button
              onClick={handleLogout}
              className="text-sm bg-gray-200 px-3 py-2 rounded-full hover:bg-gray-300"
            >
              로그아웃
            </button>
          </div>
        )}
      </header>

      <main>
        {!token ? (
          <Auth onLoginSuccess={handleLoginSuccess} />
        ) : (
          view === 'QUIZ' ? <Quiz /> : <MyPage />
        )}
      </main>

      <footer className="text-center mt-20 text-gray-500 text-sm">
        &copy; 2026 BitPop Quiz MVP
      </footer>
    </div>
  );
}

export default App;
