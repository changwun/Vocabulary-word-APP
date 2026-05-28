import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface EventStat {
  eventId: number;
  eventTitle: string;
  participantCount: number;
  isDrawn: boolean;
}

interface DashboardData {
  totalUsers: number;
  totalRafflesUsed: number;
  activeEventsCount: number;
  eventStats: EventStat[];
}

const AdminDashboard: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [winners, setWinners] = useState<string[]>([]);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await api.get('/admin/dashboard');
      setData(response.data);
    } catch (err) {
      alert('관리자 데이터를 가져오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDraw = async (eventId: number) => {
    if (!window.confirm('정말로 추첨을 진행하시겠습니까? 추첨 후에는 해당 이벤트가 종료됩니다.')) {
      return;
    }

    try {
      const response = await api.post(`/admin/event/${eventId}/draw?count=10`);
      setWinners(response.data);
      alert('🎉 추첨이 완료되었습니다!');
      fetchStats(); // 상태 갱신
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || '추첨 중 오류가 발생했습니다.';
      alert(msg);
    }
  };

  if (loading) return <div className="text-center mt-20 font-bold">관리자 화면 로딩 중...</div>;

  return (
    <div className="max-w-6xl mx-auto mt-10 p-4 pb-20">
      <h2 className="text-4xl font-black text-gray-800 mb-10 tracking-tighter">관리자 제어 센터</h2>
      
      {/* 요약 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100">
          <p className="text-sm font-bold text-gray-400 uppercase tracking-widest mb-2">총 회원 수</p>
          <p className="text-4xl font-black text-blue-600">{data?.totalUsers}명</p>
        </div>
        <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100">
          <p className="text-sm font-bold text-gray-400 uppercase tracking-widest mb-2">총 응모 횟수</p>
          <p className="text-4xl font-black text-indigo-600">{data?.totalRafflesUsed}회</p>
        </div>
        <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100">
          <p className="text-sm font-bold text-gray-400 uppercase tracking-widest mb-2">활성 이벤트</p>
          <p className="text-4xl font-black text-green-600">{data?.activeEventsCount}개</p>
        </div>
      </div>

      {/* 추첨 결과 모달 스타일 (간이) */}
      {winners.length > 0 && (
        <div className="mb-10 p-8 bg-yellow-50 rounded-3xl border-4 border-yellow-200 shadow-xl">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-2xl font-black text-yellow-800">🎊 당첨자 명단 (최근 추첨)</h3>
            <button onClick={() => setWinners([])} className="text-yellow-600 font-bold hover:text-yellow-800">닫기</button>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {winners.map((winner, idx) => (
              <div key={idx} className="p-3 bg-white rounded-xl shadow-sm border border-yellow-100 font-bold text-gray-700">
                {idx + 1}. {winner}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 이벤트 현황 테이블 */}
      <div className="bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
        <div className="p-6 border-b border-gray-50 bg-gray-50/50">
          <h3 className="text-xl font-black text-gray-800">이벤트별 응모 및 추첨 현황</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-gray-50 text-gray-500 text-sm font-bold uppercase tracking-tighter">
              <tr>
                <th className="p-6">이벤트 제목</th>
                <th className="p-6">참여 인원</th>
                <th className="p-6">상태</th>
                <th className="p-6 text-center">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 text-gray-700">
              {data?.eventStats.map((stat) => (
                <tr key={stat.eventId} className="hover:bg-gray-50/50 transition-colors">
                  <td className="p-6 font-bold">{stat.eventTitle}</td>
                  <td className="p-6">
                    <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full font-black text-xs">
                      {stat.participantCount}명 응모 중
                    </span>
                  </td>
                  <td className="p-6">
                    {stat.isDrawn ? (
                      <span className="px-3 py-1 bg-gray-100 text-gray-400 rounded-full font-bold text-xs text-center">추첨 완료</span>
                    ) : (
                      <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full font-bold text-xs text-center">진행 중</span>
                    )}
                  </td>
                  <td className="p-6 text-center">
                    <button 
                      onClick={() => handleDraw(stat.eventId)}
                      disabled={stat.isDrawn}
                      className={`px-6 py-2 rounded-xl text-sm font-black shadow-md transition transform active:scale-95 ${stat.isDrawn ? 'bg-gray-100 text-gray-300 cursor-not-allowed' : 'bg-gray-800 text-white hover:bg-black'}`}
                    >
                      {stat.isDrawn ? '추첨 마감' : '추첨하기'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
