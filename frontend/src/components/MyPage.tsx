import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface UserInfo {
  username: string;
  raffleCount: number;
  quizMode: string;
}

const MyPage: React.FC = () => {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [isusing, setIsUsing] = useState(false);

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

  const handleUseRaffle = async () => {
    if (userInfo && userInfo.raffleCount <= 0) {
      alert('사용 가능한 응모권이 없습니다.');
      return;
    }

    if (!window.confirm('응모권 1개를 사용하여 경품에 응모하시겠습니까?')) {
      return;
    }

    setIsUsing(true);
    try {
      const response = await api.post('/api/raffle/use'); // 실제로는 백엔드 URL 구조에 맞춰야 함
      // 현재 백엔드 WebConfig에 따라 /api가 붙어있으므로 /raffle/use로 호출
      // 만약 axios.ts에 baseURL이 /api까지 되어있다면 '/raffle/use'
      await api.post('/raffle/use'); 
      
      alert('응모 완료! 행운을 빕니다. 🍀');
      fetchUserInfo(); // 개수 갱신
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || '응모 중 오류가 발생했습니다.';
      alert(msg);
    } finally {
      setIsUsing(false);
    }
  };

  if (loading) return <div className="text-center mt-10 text-xl font-bold">로딩 중...</div>;

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
              onClick={handleUseRaffle}
              disabled={isusing || (userInfo?.raffleCount || 0) <= 0}
              className={`mt-6 w-full py-3 bg-white text-blue-600 rounded-xl font-black text-lg shadow-md transition transform active:scale-95 disabled:opacity-50 disabled:active:scale-100 ${isusing ? 'animate-pulse' : 'hover:bg-blue-50'}`}
            >
              {isusing ? 'Processing...' : 'Use Ticket Now'}
            </button>
          </div>
          {/* Decorative Circle */}
          <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-blue-500 rounded-full opacity-50"></div>
        </div>

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
