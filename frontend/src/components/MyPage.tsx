import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface UserInfo {
  username: string;
  email: string;
  phoneNumber: string;
  raffleCount: number;
  quizMode: string;
  notificationTime: string | null;
  notificationEnabled: boolean;
}

interface MyPageProps {
  onNavigateToEvents: () => void;
  onNavigateToWrongAnswers: () => void;
}

const MyPage: React.FC<MyPageProps> = ({ onNavigateToEvents, onNavigateToWrongAnswers }) => {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const [editNotiTime, setEditNotiTime] = useState('09:00');
  const [editNotiEnabled, setEditNotiEnabled] = useState(true);

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await api.get('/user/me');
      const data = response.data;
      setUserInfo(data);
      setEditName(data.username);
      setEditPhone(data.phoneNumber);
      setEditNotiTime(data.notificationTime?.substring(0, 5) || '09:00');
      setEditNotiEnabled(data.notificationEnabled);
    } catch (err) {
      alert('사용자 정보를 가져오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const formatPhoneNumber = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length <= 3) return cleaned;
    if (cleaned.length <= 7) return `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7, 11)}`;
  };

  const handleUpdateProfile = async () => {
    try {
      await api.put('/user/me', { username: editName, phoneNumber: editPhone });
      await api.put('/user/notification', { 
        notificationTime: editNotiTime + ':00', 
        notificationEnabled: editNotiEnabled 
      });
      alert('정보가 성공적으로 수정되었습니다.');
      setIsEditing(false);
      fetchUserInfo();
    } catch (err: any) {
      alert(err.response?.data?.message || '수정에 실패했습니다.');
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

  return (
    <div className="max-w-2xl mx-auto mt-10 p-4 pb-20 space-y-8">
      {/* 프로필 섹션 */}
      <div className="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden">
        <div className="p-8 border-b border-gray-50 flex justify-between items-center bg-gray-50/30">
          <h2 className="text-2xl font-black text-gray-800">내 프로필</h2>
          <button 
            onClick={() => setIsEditing(!isEditing)}
            className={`px-4 py-2 rounded-xl text-sm font-bold transition ${isEditing ? 'bg-gray-100 text-gray-400' : 'bg-blue-50 text-blue-600 hover:bg-blue-100'}`}
          >
            {isEditing ? '취소' : '정보 수정'}
          </button>
        </div>

        <div className="p-8 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p className="text-xs font-black text-gray-400 uppercase tracking-widest mb-1">사용자 이름</p>
              {isEditing ? (
                <input 
                  value={editName} 
                  onChange={(e) => setEditName(e.target.value)}
                  className="w-full p-3 border-2 border-blue-100 rounded-xl focus:border-blue-500 outline-none font-bold"
                />
              ) : (
                <p className="text-xl font-bold text-gray-800">{userInfo?.username}</p>
              )}
            </div>
            <div>
              <p className="text-xs font-black text-gray-400 uppercase tracking-widest mb-1">이메일 (수정불가)</p>
              <p className="text-xl font-bold text-gray-400">{userInfo?.email}</p>
            </div>
            <div>
              <p className="text-xs font-black text-gray-400 uppercase tracking-widest mb-1">전화번호</p>
              {isEditing ? (
                <input 
                  value={editPhone} 
                  onChange={(e) => setEditPhone(formatPhoneNumber(e.target.value))}
                  maxLength={13}
                  className="w-full p-3 border-2 border-blue-100 rounded-xl focus:border-blue-500 outline-none font-bold"
                />
              ) : (
                <p className="text-xl font-bold text-gray-800">{userInfo?.phoneNumber}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 알림 설정 섹션 */}
      <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h3 className="text-xl font-black text-gray-800">퀴즈 알림 설정 🔔</h3>
            <p className="text-sm text-gray-400">원하는 시간에 퀴즈 알림을 보내드려요.</p>
          </div>
          {isEditing && (
            <div className="flex items-center gap-2 bg-gray-50 p-2 rounded-xl border border-gray-100">
              <span className="text-xs font-bold text-gray-500">알림 받기</span>
              <input 
                type="checkbox" 
                className="w-5 h-5 rounded-lg"
                checked={editNotiEnabled}
                onChange={(e) => setEditNotiEnabled(e.target.checked)}
              />
            </div>
          )}
        </div>
        
        <div className="flex items-center gap-4">
          <div className="flex-1 p-4 bg-blue-50 rounded-2xl border border-blue-100">
            <p className="text-xs font-bold text-blue-400 mb-1">희망 알림 시간</p>
            {isEditing ? (
              <input 
                type="time" 
                value={editNotiTime}
                onChange={(e) => setEditNotiTime(e.target.value)}
                className="bg-transparent text-2xl font-black text-blue-600 outline-none"
              />
            ) : (
              <p className="text-2xl font-black text-blue-600">
                {userInfo?.notificationEnabled ? (userInfo?.notificationTime?.substring(0, 5) || '미설정') : '알림 꺼짐'}
              </p>
            )}
          </div>
          <div className="p-4 bg-gray-50 rounded-2xl flex items-center justify-center min-w-[120px]">
             <p className="text-center">
                <span className="block text-xs font-bold text-gray-400 mb-1">마감 임박 알림</span>
                <span className="text-sm font-black text-gray-600">자정 1시간 전</span>
             </p>
          </div>
        </div>
      </div>

      {isEditing && (
        <button 
          onClick={handleUpdateProfile}
          className="w-full py-5 bg-blue-600 text-white rounded-2xl font-black text-xl shadow-lg shadow-blue-100 transition-all hover:bg-blue-700 transform hover:-translate-y-1"
        >
          수정 완료
        </button>
      )}

      {/* 기타 메뉴 */}
      <div className="grid grid-cols-2 gap-4">
        <button 
          onClick={onNavigateToWrongAnswers}
          className="p-6 bg-white rounded-3xl border-2 border-gray-100 hover:border-blue-400 transition-all text-left group"
        >
          <div className="text-3xl mb-2 group-hover:scale-110 transition-transform">📖</div>
          <p className="text-lg font-black text-gray-800">나만의 단어장</p>
          <p className="text-xs text-gray-400 font-medium">틀린 단어 복습하기</p>
        </button>
        <button 
          onClick={onNavigateToEvents}
          className="p-6 bg-white rounded-3xl border-2 border-gray-100 hover:border-blue-400 transition-all text-left group"
        >
          <div className="text-3xl mb-2 group-hover:scale-110 transition-transform">🎟️</div>
          <p className="text-lg font-black text-gray-800">이벤트 응모</p>
          <p className="text-xs text-gray-400 font-medium">경품 확인하러 가기</p>
        </button>
      </div>

      <div className="bg-gray-50 rounded-3xl p-6 border border-gray-100">
         <p className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Quiz Preference</p>
         <div className="grid grid-cols-2 gap-3">
            <button
              onClick={() => updateMode('EN_TO_KO')}
              className={`py-3 rounded-xl border-2 font-black transition-all ${userInfo?.quizMode === 'EN_TO_KO' ? 'border-blue-600 bg-blue-50 text-blue-600' : 'border-gray-100 text-gray-300'}`}
            >
              영 ➔ 한
            </button>
            <button
              onClick={() => updateMode('KO_TO_EN')}
              className={`py-3 rounded-xl border-2 font-black transition-all ${userInfo?.quizMode === 'KO_TO_EN' ? 'border-blue-600 bg-blue-50 text-blue-600' : 'border-gray-100 text-gray-300'}`}
            >
              한 ➔ 영
            </button>
         </div>
      </div>
    </div>
  );
};

export default MyPage;
