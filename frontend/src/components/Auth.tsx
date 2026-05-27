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
  const [quizMode, setQuizMode] = useState<'EN_TO_KO' | 'KO_TO_EN'>('EN_TO_KO');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      if (isLogin) {
        const response = await api.post('/auth/login', { email, password });
        const token = response.data;
        localStorage.setItem('token', token);
        onLoginSuccess(token);
      } else {
        await api.post('/auth/signup', { username, email, password, quizMode });
        alert('회원가입 성공! 로그인해 주세요.');
        setIsLogin(true);
      }
    } catch (err: any) {
      setError(err.response?.data || '오류가 발생했습니다.');
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-xl">
      <h2 className="text-2xl font-bold mb-6 text-center">
        {isLogin ? '로그인' : '회원가입'}
      </h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        {!isLogin && (
          <div>
            <label className="block text-sm font-medium">사용자 이름</label>
            <input
              type="text"
              className="w-full p-2 border rounded mt-1"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
        )}
        {!isLogin && (
          <div>
            <label className="block text-sm font-medium mb-2">기본 퀴즈 모드</label>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="quizMode"
                  value="EN_TO_KO"
                  checked={quizMode === 'EN_TO_KO'}
                  onChange={() => setQuizMode('EN_TO_KO')}
                />
                <span className="text-sm">영 ➔ 한</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="quizMode"
                  value="KO_TO_EN"
                  checked={quizMode === 'KO_TO_EN'}
                  onChange={() => setQuizMode('KO_TO_EN')}
                />
                <span className="text-sm">한 ➔ 영</span>
              </label>
            </div>
          </div>
        )}
        <div>
          <label className="block text-sm font-medium">이메일</label>
          <input
            type="email"
            className="w-full p-2 border rounded mt-1"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium">비밀번호</label>
          <input
            type="password"
            className="w-full p-2 border rounded mt-1"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <button
          type="submit"
          className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700 transition"
        >
          {isLogin ? '로그인' : '회원가입'}
        </button>
      </form>
      <div className="mt-4 text-center">
        <button
          onClick={() => setIsLogin(!isLogin)}
          className="text-blue-600 hover:underline text-sm"
        >
          {isLogin ? '계정이 없으신가요? 회원가입' : '이미 계정이 있으신가요? 로그인'}
        </button>
      </div>
    </div>
  );
};

export default Auth;
