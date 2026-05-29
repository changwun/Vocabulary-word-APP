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

interface WinnerDetail {
  username: string;
  email: string;
  phoneNumber: string;
  wonAt: string;
}

const AdminDashboard: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [winners, setWinners] = useState<WinnerDetail[]>([]);
  const [showRealInfo, setShowRealInfo] = useState(false);
  const [selectedEventTitle, setSelectedEventTitle] = useState('');
  const [historyDate, setHistoryDate] = useState(new Date().toISOString().split('T')[0]);

  useEffect(() => {
    fetchStats();
    fetchHistory(historyDate);
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

  const fetchHistory = async (date: string) => {
    try {
      const response = await api.get(`/admin/winners/history?date=${date}`);
      setWinners(response.data);
      setSelectedEventTitle(`${date} 추첨 결과`);
    } catch (err) {
      setWinners([]);
    }
  };

  const handleDraw = async (eventId: number, title: string) => {
    if (!window.confirm(`'${title}' 이벤트를 추첨하시겠습니까?`)) return;

    try {
      const response = await api.post(`/admin/event/${eventId}/draw?count=10`);
      if (response.data.length === 0) {
        alert('응모자가 없어 추첨이 진행되지 않았습니다.');
      } else {
        setWinners(response.data);
        setSelectedEventTitle(title);
        alert('🎉 추첨이 완료되었습니다!');
      }
      fetchStats();
    } catch (err: any) {
      alert(err.response?.data?.message || '추첨 중 오류가 발생했습니다.');
    }
  };

  const fetchWinnersByEvent = async (eventId: number, title: string) => {
    try {
      const response = await api.get(`/admin/event/${eventId}/winners`);
      setWinners(response.data);
      setSelectedEventTitle(title);
    } catch (err) {
      alert('당첨자 정보를 불러올 수 없습니다.');
    }
  };

  const downloadExcel = () => {
    if (winners.length === 0) return;
    const headers = ['순번', '이름', '이메일', '전화번호', '당첨일시'];
    const rows = winners.map((w, idx) => [idx + 1, w.username, w.email, w.phoneNumber, new Date(w.wonAt).toLocaleString()]);
    const csvContent = "\uFEFF" + [headers, ...rows].map(e => e.join(",")).join("\n");
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", `당첨자명단_${selectedEventTitle}.csv`);
    link.click();
  };

  const maskText = (text: string, type: 'name' | 'email' | 'phone') => {
    if (showRealInfo) return text;
    if (type === 'name') return text[0] + '*' + text.slice(-1);
    if (type === 'email') return text.split('@')[0].slice(0, 3) + '****@' + text.split('@')[1];
    return text.slice(0, 3) + '-****-' + text.slice(-4);
  };

  if (loading) return <div className="text-center mt-20 font-bold">관리자 화면 로딩 중...</div>;

  return (
    <div className="max-w-6xl mx-auto mt-10 p-4 pb-40">
      <h2 className="text-4xl font-black text-gray-800 mb-10 tracking-tighter">관리자 제어 센터</h2>
      
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

      {/* 실시간 당첨자 정보 및 이력 관리 */}
      <section className="mb-12 bg-white rounded-3xl border-2 border-blue-100 shadow-xl overflow-hidden">
        <div className="p-8 border-b border-gray-100 flex flex-col md:flex-row justify-between gap-6">
          <div>
            <h3 className="text-2xl font-black text-gray-800 mb-1">📋 당첨자 관리 및 이력</h3>
            <p className="text-sm text-gray-500 font-medium">날짜별 추첨 결과를 확인하고 엑셀로 추출합니다.</p>
          </div>
          <div className="flex items-center gap-4">
            <input 
              type="date" 
              value={historyDate} 
              onChange={(e) => {setHistoryDate(e.target.value); fetchHistory(e.target.value);}}
              className="p-3 border-2 border-gray-100 rounded-xl font-bold focus:border-blue-500 outline-none"
            />
            <button 
              onClick={() => setShowRealInfo(!showRealInfo)}
              className={`px-4 py-3 rounded-xl text-sm font-bold transition-all ${showRealInfo ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-blue-50 text-blue-600 border border-blue-100'}`}
            >
              {showRealInfo ? '정보 숨기기' : '정보 보기'}
            </button>
            <button 
              onClick={downloadExcel}
              disabled={winners.length === 0}
              className="px-4 py-3 bg-green-600 text-white rounded-xl text-sm font-bold hover:bg-green-700 disabled:opacity-50"
            >
              엑셀 저장
            </button>
          </div>
        </div>
        
        <div className="max-h-[400px] overflow-y-auto">
          {winners.length > 0 ? (
            <table className="w-full text-left">
              <thead className="bg-gray-50 text-xs font-bold text-gray-400 sticky top-0">
                <tr>
                  <th className="p-4">이름</th>
                  <th className="p-4">이메일</th>
                  <th className="p-4">전화번호</th>
                  <th className="p-4">당첨시간</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {winners.map((winner, idx) => (
                  <tr key={idx} className="text-sm font-bold text-gray-700 hover:bg-gray-50/50 transition-colors">
                    <td className="p-4">{maskText(winner.username, 'name')}</td>
                    <td className="p-4 text-gray-500">{maskText(winner.email, 'email')}</td>
                    <td className="p-4 text-blue-600">{maskText(winner.phoneNumber, 'phone')}</td>
                    <td className="p-4 text-gray-400 text-xs">{new Date(winner.wonAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-20 text-center text-gray-400 font-bold">선택한 날짜에 추첨 기록이 없습니다.</div>
          )}
        </div>
      </section>

      {/* 이벤트 현황 */}
      <div className="bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
        <div className="p-6 border-b border-gray-50 bg-gray-50/50">
          <h3 className="text-xl font-black text-gray-800">이벤트별 응모 및 추첨 현황</h3>
        </div>
        <table className="w-full text-left">
          <thead className="bg-gray-50 text-gray-500 text-sm font-bold uppercase">
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
                    {stat.participantCount}명 응모
                  </span>
                </td>
                <td className="p-6">
                  {stat.isDrawn ? <span className="text-gray-400 font-bold">추첨 완료</span> : <span className="text-green-500 font-bold">진행 중</span>}
                </td>
                <td className="p-6 text-center">
                  {stat.isDrawn ? (
                    <button onClick={() => fetchWinnersByEvent(stat.eventId, stat.eventTitle)} className="px-6 py-2 bg-gray-100 text-gray-600 rounded-xl text-sm font-black hover:bg-gray-200 transition">기록 조회</button>
                  ) : (
                    <button onClick={() => handleDraw(stat.eventId, stat.eventTitle)} className="px-6 py-2 bg-gray-800 text-white rounded-xl text-sm font-black hover:bg-black transition">추첨하기</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminDashboard;
