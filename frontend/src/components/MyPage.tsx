import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import WrongAnswerNote from './WrongAnswerNote';

interface UserInfo {
  username: string;
  raffleCount: number;
  quizMode: string;
}

interface MyPageProps {
  onNavigateToEvents: () => void;
}

const MyPage: React.FC<MyPageProps> = ({ onNavigateToEvents }) => {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [showWrongAnswers, setShowWrongAnswers] = useState(false);

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await api.get('/user/me');
      setUserInfo(response.data);
    } catch (err) {
      alert('사용자 정보를 가져오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const updateMode = async (newMode: 'EN_TO_KO' | 'KO_TO_EN') => {
    try {
      await api.put('/user/mode', { quizMode: newMode });
      setUserInfo(prev => prev ? { ...prev, quizMode: newMode } : null);
      alert('퀴즈 모드가 변경되었습니다.');
    } catch (err) {
      alert('모드 변경에 실패했습니다.');
    }
  };

  if (loading) return <div className="text-center mt-10 text-xl font-bold">로딩 중...</div>;

  if (showWrongAnswers) {
    return (
      <div className="max-w-md mx-auto mt-10 p-8 bg-white rounded-3xl shadow-2xl border-b-8 border-blue-100">
        <button 
          onClick={() => setShowWrongAnswers(false)}
          className="mb-8 flex items-center text-gray-400 font-black hover:text-blue-600 transition group"
        >
          <div className="p-2 bg-gray-50 rounded-lg mr-3 group-hover:bg-blue-50 transition">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M15 19l-7-7 7-7" />
            </svg>
          </div>
          BACK TO DASHBOARD
        </button>
        <WrongAnswerNote />
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto mt-10 p-8 bg-white rounded-2xl shadow-2xl border-b-8 border-blue-100">
      <h2 className="text-3xl font-black mb-10 text-center text-gray-800">My Dashboard</h2>
      
      <div className="space-y-8">
        <div className="p-6 bg-gray-50 rounded-2xl border border-gray-100">
          <p className="text-sm text-gray-500 font-bold mb-1 uppercase tracking-wider">User Profile</p>
          <p className="text-2xl font-black text-gray-800">{userInfo?.username}</p>
        </div>

        <div className="p-6 bg-blue-600 rounded-2xl shadow-lg shadow-blue-200 text-white relative overflow-hidden">
          <div className="relative z-10">
            <p className="text-sm font-bold mb-2 opacity-80 uppercase tracking-wider">Available Raffles</p>
            <div className="flex items-baseline gap-2">
              <span className="text-5xl font-black">{userInfo?.raffleCount}</span>
              <span className="text-xl font-bold">Tickets</span>
            </div>
            <button
              onClick={onNavigateToEvents}
              className="mt-6 w-full py-3 bg-white text-blue-600 rounded-xl font-black text-lg shadow-md transition transform active:scale-95 hover:bg-blue-50"
            >
              Go to Events
            </button>
          </div>
          {/* Decorative Circle */}
          <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-blue-500 rounded-full opacity-50"></div>
        </div>

        {/* Wrong Answer Note Button Section */}
        <button
          onClick={() => setShowWrongAnswers(true)}
          className="w-full p-6 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl shadow-lg shadow-indigo-100 text-white flex items-center justify-between group transition transform active:scale-95"
        >
          <div className="text-left">
            <p className="text-xs font-bold opacity-80 uppercase tracking-widest mb-1">Personal Study</p>
            <p className="text-2xl font-black">나만의 단어장</p>
          </div>
          <div className="p-3 bg-white/20 rounded-xl group-hover:bg-white/30 transition">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M9 5l7 7-7 7" />
            </svg>
          </div>
        </button>

        <div className="p-6 bg-white rounded-2xl border-2 border-gray-100">
          <p className="text-sm text-gray-500 font-bold mb-4 uppercase tracking-wider text-center">Quiz Preference</p>
          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={() => updateMode('EN_TO_KO')}
              className={`py-4 rounded-xl border-2 font-black transition-all ${
                userInfo?.quizMode === 'EN_TO_KO' 
                ? 'border-blue-600 bg-blue-50 text-blue-600' 
                : 'border-gray-100 text-gray-400 hover:border-gray-200'
              }`}
            >
              EN ➔ KO
            </button>
            <button
              onClick={() => updateMode('KO_TO_EN')}
              className={`py-4 rounded-xl border-2 font-black transition-all ${
                userInfo?.quizMode === 'KO_TO_EN' 
                ? 'border-blue-600 bg-blue-50 text-blue-600' 
                : 'border-gray-100 text-gray-400 hover:border-gray-200'
              }`}
            >
              KO ➔ EN
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyPage;
