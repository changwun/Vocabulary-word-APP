import React, { useEffect, useState } from 'react';
import api from '../api/axios';

interface Event {
  id: number;
  title: string;
  description: string;
  prize: string;
  startDate: string;
  endDate: string;
}

const EventList: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [entering, setEntering] = useState<number | null>(null);

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await api.get('/event/active');
      setEvents(response.data);
    } catch (err) {
      alert('이벤트 목록을 가져오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleEnter = async (eventId: number) => {
    if (!window.confirm('응모권 1개를 사용하여 이 이벤트에 응모하시겠습니까?')) {
      return;
    }

    setEntering(eventId);
    try {
      await api.post(`/event/${eventId}/enter`);
      alert('응모가 완료되었습니다! 행운을 빕니다. 🍀');
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || '응모 중 오류가 발생했습니다.';
      alert(msg);
    } finally {
      setEntering(null);
    }
  };

  if (loading) return <div className="text-center mt-20 font-bold">진행 중인 이벤트를 찾는 중...</div>;

  return (
    <div className="max-w-4xl mx-auto mt-10 p-4">
      <div className="flex flex-col mb-10 text-center">
        <h2 className="text-4xl font-black text-gray-800 mb-2">Lucky Events</h2>
        <p className="text-gray-500 font-medium">모아둔 응모권으로 대박 경품에 도전하세요!</p>
      </div>

      {events.length === 0 ? (
        <div className="bg-white p-20 rounded-3xl shadow-xl text-center border-2 border-dashed border-gray-200">
          <p className="text-xl text-gray-400 font-bold">현재 진행 중인 이벤트가 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {events.map((event) => (
            <div key={event.id} className="bg-white rounded-3xl shadow-xl overflow-hidden flex flex-col border border-gray-100 hover:shadow-2xl transition-shadow group">
              <div className="h-3 bg-gradient-to-r from-blue-500 to-indigo-600"></div>
              <div className="p-8 flex-1 flex flex-col">
                <div className="flex justify-between items-start mb-4">
                  <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-black uppercase tracking-tighter">Running Now</span>
                </div>
                <h3 className="text-2xl font-black text-gray-800 mb-2 group-hover:text-blue-600 transition-colors">{event.title}</h3>
                <p className="text-gray-600 mb-6 line-clamp-2">{event.description}</p>
                
                <div className="mt-auto space-y-4">
                  <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 flex items-center justify-between">
                    <span className="text-sm font-bold text-gray-500">Prize</span>
                    <span className="text-lg font-black text-indigo-600">{event.prize}</span>
                  </div>
                  
                  <button
                    onClick={() => handleEnter(event.id)}
                    disabled={entering !== null}
                    className={`w-full py-4 rounded-2xl font-black text-lg shadow-lg transition transform active:scale-95 ${entering === event.id ? 'bg-gray-200 animate-pulse' : 'bg-blue-600 text-white hover:bg-blue-700'}`}
                  >
                    {entering === event.id ? 'Entering...' : 'Use 1 Ticket'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default EventList;
