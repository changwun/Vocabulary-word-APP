import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface Event {
  id: number;
  title: string;
  description: string;
  prize: string;
  startDate: string;
  endDate: string;
  active: boolean;
}

interface Winner {
  maskedUsername: string;
  maskedEmail: string;
}

const EventList: React.FC = () => {
  const [activeEvents, setActiveEvents] = useState<Event[]>([]);
  const [winners, setWinners] = useState<Winner[]>([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);
  const [entering, setEntering] = useState<number | null>(null);

  useEffect(() => {
    fetchActiveEvents();
    fetchWinnersByDate(selectedDate);
  }, []);

  const fetchActiveEvents = async () => {
    try {
      const response = await api.get('/event/active');
      setActiveEvents(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchWinnersByDate = async (date: string) => {
    try {
      const response = await api.get(`/event/winners/date?date=${date}`);
      setWinners(response.data);
    } catch (err) {
      setWinners([]);
    }
  };

  const handleEnter = async (eventId: number) => {
    if (!window.confirm('응모권 1개를 사용하여 응모하시겠습니까?')) return;
    setEntering(eventId);
    try {
      await api.post(`/event/${eventId}/enter`);
      alert('응모 완료! 행운을 빕니다. 🍀');
    } catch (err: any) {
      alert(err.response?.data?.message || '응모 중 오류가 발생했습니다.');
    } finally {
      setEntering(null);
    }
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = e.target.value;
    setSelectedDate(newDate);
    fetchWinnersByDate(newDate);
  };

  if (loading) return <div className="text-center mt-20 font-bold text-blue-600">이벤트를 불러오는 중...</div>;

  return (
    <div className="max-w-4xl mx-auto mt-10 p-4 pb-40">
      {/* 진행 중인 이벤트 섹션 */}
      <section className="mb-20">
        <div className="text-center mb-12">
          <h2 className="text-4xl font-black text-gray-800 mb-2 tracking-tighter">Live Events</h2>
          <p className="text-gray-500 font-medium">지금 바로 응모 가능한 럭키 찬스!</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {activeEvents.map((event) => (
            <div key={event.id} className="bg-white rounded-3xl shadow-xl overflow-hidden flex flex-col border border-gray-100 hover:shadow-2xl transition-all group">
              <div className="h-3 bg-blue-600 group-hover:bg-indigo-600 transition-colors"></div>
              <div className="p-8 flex-1 flex flex-col">
                <span className="inline-block px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs font-black mb-4 w-fit">RUNNING</span>
                <h3 className="text-2xl font-black text-gray-800 mb-2">{event.title}</h3>
                <p className="text-gray-600 mb-8 line-clamp-2">{event.description}</p>
                <div className="mt-auto space-y-4">
                  <div className="flex justify-between items-center p-4 bg-gray-50 rounded-2xl">
                    <span className="text-sm font-bold text-gray-400 uppercase">Prize</span>
                    <span className="text-lg font-black text-blue-600">{event.prize}</span>
                  </div>
                  <button
                    onClick={() => handleEnter(event.id)}
                    disabled={entering !== null}
                    className="w-full py-4 bg-blue-600 text-white rounded-2xl font-black text-lg shadow-lg hover:bg-blue-700 transition transform active:scale-95 disabled:opacity-50"
                  >
                    {entering === event.id ? '응모 중...' : '응모권 1개 사용'}
                  </button>
                </div>
              </div>
            </div>
          ))}
          {activeEvents.length === 0 && (
            <div className="col-span-full p-20 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200 text-center text-gray-400 font-bold">
              현재 진행 중인 이벤트가 없습니다.
            </div>
          )}
        </div>
      </section>

      {/* 당첨자 확인 섹션 (달력 선택 기능 포함) */}
      <section className="bg-gray-900 rounded-[3rem] p-10 text-white shadow-2xl relative overflow-hidden">
        <div className="relative z-10">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-12">
            <div>
              <h2 className="text-3xl font-black mb-2 tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 to-orange-500">Winner Board</h2>
              <p className="text-gray-400 font-medium text-sm">날짜별 행운의 주인공을 확인하세요.</p>
            </div>
            <div className="flex items-center gap-3 bg-gray-800 p-2 rounded-2xl border border-gray-700">
              <span className="text-xs font-bold text-gray-500 ml-2">날짜 선택:</span>
              <input
                type="date"
                value={selectedDate}
                onChange={handleDateChange}
                className="bg-transparent text-white font-bold outline-none cursor-pointer [color-scheme:dark]"
              />
            </div>
          </div>

          {winners.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
              {winners.map((winner, idx) => (
                <div key={idx} className="flex justify-between items-center p-5 bg-white/5 rounded-2xl border border-white/10 hover:bg-white/10 transition-colors">
                  <span className="font-bold text-lg">{winner.maskedUsername} 님</span>
                  <span className="text-gray-500 text-sm">{winner.maskedEmail}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-20 text-center">
              <div className="text-5xl mb-4 opacity-30">🔍</div>
              <p className="text-gray-500 font-bold">선택하신 날짜({selectedDate})에는<br/>아직 발표된 당첨자가 없습니다.</p>
            </div>
          )}
          
          <p className="mt-10 text-center text-xs text-gray-600 italic">당첨자 분들께는 개별적으로 기프티콘 알림이 전송됩니다.</p>
        </div>
        {/* Background Decorative elements */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-blue-600/10 rounded-full blur-3xl -mr-20 -mt-20"></div>
      </div>
    </div>
  );
};

export default EventList;
