import React, { useState } from 'react';
import api from '../api/axios';

interface AuthProps {
  onLoginSuccess: (token: string) => void;
}

const Auth: React.FC<AuthProps> = ({ onLoginSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [quizMode, setQuizMode] = useState<'EN_TO_KO' | 'KO_TO_EN'>('EN_TO_KO');
  const [notificationTime, setNotificationTime] = useState('09:00');
  const [privacyAgreed, setPrivacyAgreed] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // 전화번호 자동 하이픈 포맷팅 함수
  const formatPhoneNumber = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length <= 3) return cleaned;
    if (cleaned.length <= 7) return `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7, 11)}`;
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatPhoneNumber(e.target.value);
    setPhoneNumber(formatted);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!isLogin && !privacyAgreed) {
      setError('개인정보 수집 및 이용에 동의해주세요.');
      return;
    }

    setLoading(true);
    try {
      if (isLogin) {
        const response = await api.post('/auth/login', { email, password });
        const token = response.data;
        localStorage.setItem('token', token);
        onLoginSuccess(token);
      } else {
        await api.post('/auth/signup', { 
          username, 
          email, 
          phoneNumber, 
          password, 
          quizMode,
          notificationTime: notificationTime + ':00', // HH:mm:ss 형식으로 전송
          privacyPolicyAgreed: privacyAgreed 
        });
        alert('회원가입 성공! 로그인해 주세요.');
        setIsLogin(true);
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data || '오류가 발생했습니다.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSocialLogin = (provider: 'KAKAO' | 'GOOGLE') => {
    // 실제 환경에서는 백엔드의 OAuth2 엔드포인트로 리다이렉트하거나
    // SDK를 사용하여 토큰을 받아 백엔드 /api/auth/social-login으로 전달합니다.
    alert(`${provider} 로그인은 현재 인프라 설정(콘솔 등록)이 필요합니다. \n백엔드 로직은 이미 준비되어 있습니다!`);
    
    /* 
    // 예시: 백엔드 API 연동 흐름
    const mockSocialData = {
       provider: provider,
       providerId: "12345",
       email: "social@example.com",
       username: "소셜유저"
    };
    const response = await api.post('/auth/social-login', mockSocialData);
    onLoginSuccess(response.data);
    */
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-8 bg-white rounded-2xl shadow-2xl border-t-8 border-blue-600">
      <h2 className="text-3xl font-black mb-8 text-center text-gray-800">
        {isLogin ? 'Welcome Back!' : 'Create Account'}
      </h2>
      
      <form onSubmit={handleSubmit} className="space-y-5">
        {!isLogin && (
          <>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">사용자 이름</label>
              <input
                type="text"
                className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 focus:outline-none transition font-bold"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                placeholder="Your name"
              />
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">전화번호 (당첨 알림용)</label>
              <input
                type="tel"
                maxLength={13}
                className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 focus:outline-none transition font-bold"
                value={phoneNumber}
                onChange={handlePhoneChange}
                required
                placeholder="010-0000-0000"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
               <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">알림 희망 시간</label>
                  <input
                    type="time"
                    className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 outline-none font-bold"
                    value={notificationTime}
                    onChange={(e) => setNotificationTime(e.target.value)}
                    required
                  />
               </div>
               <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">퀴즈 모드</label>
                  <select 
                    className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 outline-none font-bold bg-white"
                    value={quizMode}
                    onChange={(e) => setQuizMode(e.target.value as any)}
                  >
                    <option value="EN_TO_KO">영 ➔ 한</option>
                    <option value="KO_TO_EN">한 ➔ 영</option>
                  </select>
               </div>
            </div>
          </>
        )}

        <div>
          <label className="block text-sm font-bold text-gray-700 mb-1">이메일 주소</label>
          <input
            type="email"
            className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 focus:outline-none transition font-bold"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="example@email.com"
          />
        </div>

        <div>
          <label className="block text-sm font-bold text-gray-700 mb-1">비밀번호</label>
          <input
            type="password"
            className="w-full p-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 focus:outline-none transition font-bold"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="••••••••"
          />
        </div>

        {!isLogin && (
          <div className="flex items-start gap-2 p-4 bg-gray-50 rounded-xl border border-gray-100">
            <input
              type="checkbox"
              id="privacy"
              className="mt-1 w-4 h-4"
              checked={privacyAgreed}
              onChange={(e) => setPrivacyAgreed(e.target.checked)}
            />
            <label htmlFor="privacy" className="text-xs text-gray-600 leading-tight">
              (필수) 당첨 알림 전송 및 본인 확인을 위한 <strong>개인정보 수집 및 이용</strong>에 동의합니다.
            </label>
          </div>
        )}

        {error && (
          <div className="p-4 bg-red-50 border-l-4 border-red-500 rounded-r-xl">
            <p className="text-red-700 text-sm font-bold">⚠️ {error}</p>
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white py-4 rounded-xl font-black text-xl hover:bg-blue-700 shadow-lg transition transform active:scale-95 disabled:opacity-50"
        >
          {loading ? 'Processing...' : (isLogin ? '로그인' : '회원가입')}
        </button>
      </form>

      {/* 소셜 로그인 구분선 */}
      <div className="relative my-8">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-200"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-4 bg-white text-gray-400 font-bold uppercase tracking-widest">Or Continue With</span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <button 
          onClick={() => handleSocialLogin('KAKAO')}
          className="flex items-center justify-center gap-2 p-3 border-2 border-gray-100 rounded-xl font-bold hover:bg-yellow-50 hover:border-yellow-200 transition"
        >
          <span className="text-xl">🟡</span> Kakao
        </button>
        <button 
          onClick={() => handleSocialLogin('GOOGLE')}
          className="flex items-center justify-center gap-2 p-3 border-2 border-gray-100 rounded-xl font-bold hover:bg-red-50 hover:border-red-200 transition"
        >
          <span className="text-xl">🔴</span> Google
        </button>
      </div>

      <div className="mt-8 text-center border-t pt-6">
        <button
          onClick={() => {
            setIsLogin(!isLogin);
            setError('');
          }}
          className="text-blue-600 font-bold hover:underline"
        >
          {isLogin ? '계정이 없으신가요? 회원가입' : '이미 계정이 있으신가요? 로그인'}
        </button>
      </div>
    </div>
  );
};

export default Auth;
