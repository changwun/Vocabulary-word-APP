import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface WrongAnswer {
  id: number;
  wordId: number;
  english: string;
  korean: string;
  wrongCount: number;
  lastAttemptAt: string;
}

const WrongAnswerNote: React.FC = () => {
  const [list, setList] = useState<WrongAnswer[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchList();
  }, []);

  const fetchList = async () => {
    try {
      // Using endpoint from prompt: GET /api/wrong-answer/list
      const response = await api.get('/wrong-answer/list');
      setList(response.data);
    } catch (err) {
      console.error('Failed to fetch wrong answers', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      // Using endpoint from prompt: DELETE /api/wrong-answer/{id}
      await api.delete(`/wrong-answer/${id}`);
      setList(prev => prev.filter(item => item.id !== id));
    } catch (err) {
      alert('학습 완료 처리에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-10">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-500 font-bold">불러오는 중...</span>
      </div>
    );
  }

  if (list.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
        <p className="text-5xl mb-4">👏</p>
        <p className="text-gray-500 font-black text-lg">틀린 단어가 없습니다. 완벽해요!</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center px-1">
        <h3 className="text-xl font-black text-gray-800">
          오답 노트 <span className="text-blue-600 ml-1">{list.length}</span>
        </h3>
      </div>
      
      <div className="grid gap-4">
        {list.map((item) => (
          <div 
            key={item.id} 
            className="group p-5 bg-white rounded-2xl border-2 border-gray-100 flex justify-between items-center transition-all hover:border-blue-400 hover:shadow-xl hover:shadow-blue-50"
          >
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <span className="text-2xl font-black text-gray-800 group-hover:text-blue-600 transition-colors">
                  {item.english}
                </span>
                <span className="px-2.5 py-1 bg-red-100 text-red-600 text-[10px] font-black rounded-lg uppercase tracking-tight">
                  {item.wrongCount}회 틀림
                </span>
              </div>
              <p className="text-gray-500 font-bold text-lg">{item.korean}</p>
              <div className="flex items-center mt-3 text-[10px] text-gray-400 font-bold uppercase tracking-widest">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                최근 오답: {new Date(item.lastAttemptAt).toLocaleDateString()}
              </div>
            </div>
            
            <button
              onClick={() => handleDelete(item.id)}
              className="ml-4 p-4 bg-gray-50 text-gray-400 rounded-2xl hover:bg-green-500 hover:text-white transition-all transform active:scale-90 group/btn"
              title="학습 완료"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
              </svg>
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default WrongAnswerNote;
