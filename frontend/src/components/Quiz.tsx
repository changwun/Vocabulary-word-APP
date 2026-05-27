import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface Question {
  wordId: number;
  question: string;
  mode: 'EN_TO_KO' | 'KO_TO_EN';
}

interface WrongDetail {
  wordId: number;
  question: string;
  correctAnswer: string;
  userAnswer: string;
}

interface QuizResult {
  success: boolean;
  wrongDetails: WrongDetail[];
  message: string;
  raffleCount: number;
}

const Quiz: React.FC = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<{ [key: number]: string }>({});
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<QuizResult | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchQuiz();
  }, []);

  const fetchQuiz = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get('/quiz/daily');
      // 방어 코드: 데이터가 배열인지 확인
      if (Array.isArray(response.data)) {
        setQuestions(response.data);
        const initialAnswers = response.data.reduce((acc: any, q: Question) => {
          acc[q.wordId] = '';
          return acc;
        }, {});
        setAnswers(initialAnswers);
      } else {
        throw new Error('데이터 형식이 올바르지 않습니다.');
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || err.message || '퀴즈를 불러오는 데 실패했습니다.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (wordId: number, value: string) => {
    setAnswers(prev => ({ ...prev, [wordId]: value }));
  };

  const handleSubmit = async () => {
    if (!window.confirm('답안을 제출하시겠습니까? 제출 후에는 수정이나 재도전이 불가능합니다.')) {
      return;
    }

    try {
      const mode = questions[0]?.mode;
      const answerList = Object.entries(answers).map(([wordId, answer]) => ({
        wordId: Number(wordId),
        answer: answer.trim()
      }));

      const response = await api.post('/quiz/complete', {
        answers: answerList,
        mode: mode
      });
      setResult(response.data);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || '오류가 발생했습니다.';
      alert(msg);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center mt-20">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mr-3"></div>
      <span className="text-xl font-bold text-blue-600">오늘의 퀴즈를 준비 중...</span>
    </div>
  );
  
  if (error) {
    return (
      <div className="max-w-md mx-auto mt-10 p-10 bg-white rounded-xl shadow-2xl text-center border-t-4 border-blue-500">
        <div className="text-5xl mb-6">📅</div>
        <h2 className="text-2xl font-bold text-gray-800 mb-4">도전 불가</h2>
        <p className="text-lg text-gray-600 mb-8">{error}</p>
        <div className="p-4 bg-blue-50 rounded-lg text-sm text-blue-700 font-medium">
          내일 자정(00:00)에 새로운 퀴즈가 도착합니다!
        </div>
      </div>
    );
  }

  if (result) {
    const isSuccess = result.success;
    return (
      <div className="max-w-2xl mx-auto mt-10 p-10 bg-white rounded-xl shadow-2xl border-t-8 transition-all duration-500 overflow-hidden" 
           style={{ borderTopColor: isSuccess ? '#10B981' : '#EF4444' }}>
        <div className="text-center mb-8">
          <div className="text-6xl mb-4">{isSuccess ? '🎉' : '📖'}</div>
          <h2 className={`text-4xl font-black mb-4 ${isSuccess ? 'text-green-600' : 'text-red-600'}`}>
            {isSuccess ? '퍼펙트! 응모권 획득' : '아쉬워요! 오답 노트'}
          </h2>
          <p className="text-xl text-gray-600">{result.message}</p>
        </div>
        
        {!isSuccess && Array.isArray(result.wrongDetails) && result.wrongDetails.length > 0 && (
          <div className="mt-8 mb-8 overflow-hidden rounded-lg border border-gray-200 shadow-sm">
            <table className="w-full text-left border-collapse">
              <thead className="bg-gray-50">
                <tr>
                  <th className="p-4 text-sm font-bold text-gray-600 border-b">문제</th>
                  <th className="p-4 text-sm font-bold text-gray-600 border-b">내 답변</th>
                  <th className="p-4 text-sm font-bold text-green-600 border-b">정답</th>
                </tr>
              </thead>
              <tbody>
                {result.wrongDetails.map((detail) => (
                  <tr key={detail.wordId} className="hover:bg-gray-50 transition-colors">
                    <td className="p-4 border-b font-medium text-gray-800">{detail.question}</td>
                    <td className="p-4 border-b text-red-500 line-through text-sm">{detail.userAnswer}</td>
                    <td className="p-4 border-b text-green-600 font-bold">{detail.correctAnswer}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="flex flex-col items-center gap-6 mt-10">
          <div className="flex items-center gap-3 px-8 py-4 bg-blue-50 rounded-full border border-blue-100">
            <span className="text-gray-600 font-bold">현재 보유 응모권:</span>
            <span className="text-3xl font-black text-blue-600">{result.raffleCount}개</span>
          </div>
          
          <button
            onClick={() => window.location.reload()}
            className="w-full sm:w-64 py-4 bg-gray-800 text-white rounded-xl font-bold text-lg hover:bg-gray-900 shadow-lg transition-all transform active:scale-95"
          >
            확인
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto mt-10 p-8 bg-white rounded-xl shadow-2xl border-t-8 border-blue-600">
      <div className="flex justify-between items-center mb-10">
        <div>
          <h2 className="text-3xl font-black text-gray-800">Daily Quiz</h2>
          <p className="text-gray-500 font-medium">오늘의 5문제를 모두 맞춰보세요!</p>
        </div>
        {questions.length > 0 && (
          <div className="px-4 py-2 bg-blue-50 text-blue-700 rounded-xl text-sm font-bold border border-blue-100">
            {questions[0].mode === 'EN_TO_KO' ? '영어 ➔ 한국어' : '한국어 ➔ 영어'}
          </div>
        )}
      </div>
      
      <div className="space-y-8 mb-12">
        {Array.isArray(questions) && questions.map((q, index) => (
          <div key={q.wordId} className="relative group">
            <div className="flex items-center gap-4 mb-3">
              <span className="flex-shrink-0 w-10 h-10 flex items-center justify-center bg-gray-800 text-white rounded-xl font-bold shadow-md group-hover:bg-blue-600 transition-colors">
                {index + 1}
              </span>
              <span className="text-2xl font-bold text-gray-800">{q.question}</span>
            </div>
            <input
              type="text"
              placeholder="정답을 입력하세요"
              className="w-full p-4 pl-14 bg-gray-50 border-2 border-transparent rounded-xl focus:bg-white focus:border-blue-500 focus:outline-none transition-all text-lg shadow-inner"
              value={answers[q.wordId] || ''}
              onChange={(e) => handleAnswerChange(q.wordId, e.target.value)}
            />
          </div>
        ))}
      </div>

      <div className="bg-yellow-50 p-5 rounded-2xl border border-yellow-100 mb-8 flex gap-4">
        <span className="text-2xl">⚠️</span>
        <p className="text-sm text-yellow-800 leading-tight font-medium">
          제출 후에는 수정이나 재시도가 불가능합니다. <br/>
          <span className="text-red-600">모든 문제를 맞춰야만 응모권이 지급됩니다.</span>
        </p>
      </div>

      <button
        onClick={handleSubmit}
        disabled={questions.length === 0}
        className="w-full bg-blue-600 text-white py-5 rounded-2xl font-black text-xl hover:bg-blue-700 shadow-xl shadow-blue-100 transition-all transform hover:-translate-y-1 active:translate-y-0 disabled:opacity-50 disabled:transform-none"
      >
        답안 제출하기
      </button>
    </div>
  );
};

export default Quiz;
