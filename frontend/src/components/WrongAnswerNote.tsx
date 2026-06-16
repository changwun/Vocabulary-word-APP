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
  // 각 단어별로 뜻이 보이는지 상태 관리
  const [visibleIds, setVisibleIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    fetchList();
  }, []);

  const fetchList = async () => {
    try {
      const response = await api.get('/wrong-answer/list');
      setList(response.data);
    } catch (err) {
      console.error('Failed to fetch wrong answers', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('정말 이 단어를 마스터하셨나요? 목록에서 제거됩니다.')) return;
    
    try {
      await api.delete(`/wrong-answer/${id}`);
      setList(prev => prev.filter(item => item.id !== id));
    } catch (err) {
      alert('학습 완료 처리에 실패했습니다.');
    }
  };

  const toggleVisibility = (id: number) => {
    setVisibleIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      return newSet;
    });
  };

  const toggleAllVisibility = (show: boolean) => {
    if (show) {
      setVisibleIds(new Set(list.map(item => item.id)));
    } else {
      setVisibleIds(new Set());
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
    <div className="space-y-6 pb-20">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 px-1">
        <div>
          <h3 className="text-2xl font-black text-gray-800">
            나만의 단어장 <span className="text-blue-600 ml-1">{list.length}</span>
          </h3>
          <p className="text-sm text-gray-400 font-medium mt-1">틀린 단어들을 복습하고 마스터하세요!</p>
        </div>
        
        <div className="flex gap-2">
          <button 
            onClick={() => toggleAllVisibility(true)}
            className="px-3 py-1.5 bg-gray-100 text-gray-600 text-xs font-bold rounded-lg hover:bg-gray-200"
          >
            뜻 모두 보기
          </button>
          <button 
            onClick={() => toggleAllVisibility(false)}
            className="px-3 py-1.5 bg-gray-100 text-gray-600 text-xs font-bold rounded-lg hover:bg-gray-200"
          >
            뜻 모두 가리기
          </button>
        </div>
      </div>
      
      <div className="grid gap-4">
        {list.map((item) => (
          <div 
            key={item.id} 
            className="group p-6 bg-white rounded-3xl border-2 border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6 transition-all hover:border-blue-400 hover:shadow-xl hover:shadow-blue-50"
          >
            <div className="flex-1 w-full">
              <div className="flex items-center gap-3 mb-3">
                <span className="text-3xl font-black text-gray-800">
                  {item.english}
                </span>
                <span className="px-2.5 py-1 bg-red-50 text-red-500 text-[10px] font-black rounded-lg uppercase tracking-tight border border-red-100">
                  {item.wrongCount}회 오답
                </span>
              </div>
              
              {/* 뜻 영역: 클릭 시 토글 가능하게 개선 */}
              <div 
                onClick={() => toggleVisibility(item.id)}
                className={`relative overflow-hidden p-4 rounded-2xl cursor-pointer transition-all duration-300 ${visibleIds.has(item.id) ? 'bg-blue-50 border-blue-100' : 'bg-gray-100 hover:bg-gray-200'}`}
              >
                <div className={`text-xl font-bold transition-all duration-300 ${visibleIds.has(item.id) ? 'text-blue-700 blur-0' : 'text-transparent blur-md select-none'}`}>
                  {item.korean}
                </div>
                {!visibleIds.has(item.id) && (
                  <div className="absolute inset-0 flex items-center justify-center text-gray-400 text-xs font-black uppercase tracking-widest">
                    클릭해서 뜻 보기
                  </div>
                )}
              </div>

              <div className="flex items-center mt-4 text-[10px] text-gray-400 font-bold uppercase tracking-widest">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                마지막 오답일: {new Date(item.lastAttemptAt).toLocaleDateString()}
              </div>
            </div>
            
            <div className="flex sm:flex-col gap-2 w-full sm:w-auto">
              <button
                onClick={() => toggleVisibility(item.id)}
                className="flex-1 px-4 py-3 bg-gray-50 text-gray-600 rounded-2xl font-bold text-sm hover:bg-blue-50 hover:text-blue-600 transition-all border border-transparent hover:border-blue-100"
              >
                {visibleIds.has(item.id) ? '숨기기' : '정답확인'}
              </button>
              <button
                onClick={() => handleDelete(item.id)}
                className="flex-1 px-4 py-3 bg-green-50 text-green-600 rounded-2xl font-bold text-sm hover:bg-green-500 hover:text-white transition-all border border-green-100 hover:border-transparent shadow-sm"
              >
                마스터 완료
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default WrongAnswerNote;
