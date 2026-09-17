import React from 'react';

export default function PostureIllustration({ posture, madhhabId, gender }) {
  const isFemale = gender === 'FEMALE';
  const isHanafi = madhhabId === 'HANAFI';

  // Лейбл позы
  const postureLabels = {
    INTENTION: 'Намерение (Ният)',
    TAKBEER: 'Вступительный такбир',
    QIYAM_FOLDED: isHanafi && !isFemale ? 'Кыям (Руки ниже пупка)' : 'Кыям (Руки сложены на груди)',
    QIYAM_SADL: 'Кыям (Руки опущены вдоль тела — Садль)',
    RUKU: isFemale ? 'Рукуъ (Умеренный женский наклон)' : 'Рукуъ (Поясной поклон 90°)',
    ITIDAL: 'И‘тидаль (Выпрямление стоя)',
    SUJUD: isFemale ? 'Саджда (Компактный женский поклон)' : 'Саджда (Земной поклон на 7 точек)',
    SITTING_JALSA: isFemale ? 'Джалса (Поза Таваррук)' : 'Джалса (Поза Ифтираш)',
    SITTING_TASHAHHUD: isFemale ? 'Ташаххуд (Поза Таваррук)' : (isHanafi ? 'Ташаххуд (Поза Ифтираш)' : 'Ташаххуд (Поза Таваррук)'),
    SALAM_RIGHT: 'Таслим (Поворот головы направо)',
    SALAM_LEFT: 'Таслим (Поворот головы налево)'
  };

  return (
    <div className="relative w-full h-44 rounded-2xl bg-gradient-to-b from-[#13281e] to-[#0d1b14] border border-[#d4af37]/30 p-3 flex flex-col items-center justify-center overflow-hidden shadow-inner">
      {/* Декоративный геометрический узор на фоне */}
      <div className="absolute inset-0 opacity-10 flex items-center justify-center pointer-events-none">
        <svg width="240" height="240" viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="45" stroke="#d4af37" strokeWidth="0.8" fill="none" />
          <polygon points="50,10 90,50 50,90 10,50" stroke="#d4af37" strokeWidth="0.8" fill="none" />
          <polygon points="50,15 85,50 50,85 15,50" stroke="#d4af37" strokeWidth="0.5" fill="none" />
          <circle cx="50" cy="50" r="28" stroke="#d4af37" strokeWidth="0.6" fill="none" />
        </svg>
      </div>

      {/* SVG Фигура молящегося */}
      <div className="relative z-10 flex items-center justify-center">
        <svg width="180" height="110" viewBox="0 0 200 130" className="overflow-visible">
          <defs>
            <linearGradient id="bodyGrad" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#4ade80" />
              <stop offset="100%" stopColor="#22c55e" />
            </linearGradient>
            <linearGradient id="accentGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#fde047" />
              <stop offset="100%" stopColor="#d4af37" />
            </linearGradient>
          </defs>

          {/* Пол */}
          <line x1="20" y1="120" x2="180" y2="120" stroke="#1f4432" strokeWidth="2" strokeDasharray="3,3" />

          {/* В зависимости от позы рисуем стилизованные векторные линии */}
          {(posture === 'INTENTION' || posture === 'TAKBEER') && (
            <g transform="translate(100, 10)">
              {/* Голова */}
              <circle cx="0" cy="22" r="11" fill="#e2f5ea" />
              {/* Тело */}
              <line x1="0" y1="33" x2="0" y2="76" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              {/* Ноги */}
              <line x1="0" y1="76" x2="-8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              <line x1="0" y1="76" x2="8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              {/* Руки подняты к ушам/плечам */}
              <line x1="0" y1="42" x2="-18" y2="35" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
              <line x1="-18" y1="35" x2="-18" y2="16" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
              <circle cx="-18" cy="14" r="3" fill="#fde047" />

              <line x1="0" y1="42" x2="18" y2="35" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
              <line x1="18" y1="35" x2="18" y2="16" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
              <circle cx="18" cy="14" r="3" fill="#fde047" />
            </g>
          )}

          {posture === 'QIYAM_FOLDED' && (
            <g transform="translate(100, 10)">
              <circle cx="0" cy="22" r="11" fill="#e2f5ea" />
              <line x1="0" y1="33" x2="0" y2="76" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              <line x1="0" y1="76" x2="-8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              <line x1="0" y1="76" x2="8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              {/* Руки сложены: высота зависит от мазхаба и пола */}
              {isHanafi && !isFemale ? (
                // Ниже пупка
                <>
                  <line x1="0" y1="42" x2="-14" y2="65" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
                  <line x1="0" y1="42" x2="14" y2="65" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
                  <line x1="-14" y1="65" x2="14" y2="65" stroke="url(#accentGrad)" strokeWidth="6" strokeLinecap="round" />
                </>
              ) : (
                // На груди
                <>
                  <line x1="0" y1="42" x2="-14" y2="52" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
                  <line x1="0" y1="42" x2="14" y2="52" stroke="#4ade80" strokeWidth="4.5" strokeLinecap="round" />
                  <line x1="-14" y1="52" x2="14" y2="52" stroke="url(#accentGrad)" strokeWidth="6" strokeLinecap="round" />
                </>
              )}
            </g>
          )}

          {posture === 'QIYAM_SADL' && (
            <g transform="translate(100, 10)">
              <circle cx="0" cy="22" r="11" fill="#e2f5ea" />
              <line x1="0" y1="33" x2="0" y2="76" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              <line x1="0" y1="76" x2="-8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              <line x1="0" y1="76" x2="8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              {/* Руки свободно опущены вдоль тела (Садль) */}
              <line x1="0" y1="42" x2="-12" y2="75" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
              <line x1="0" y1="42" x2="12" y2="75" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
            </g>
          )}

          {posture === 'RUKU' && (
            <g transform="translate(120, 30)">
              {isFemale ? (
                // Женский умеренный наклон (45°)
                <>
                  <circle cx="-35" cy="20" r="10" fill="#e2f5ea" />
                  <line x1="-28" y1="28" x2="0" y2="55" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
                  <line x1="0" y1="55" x2="0" y2="90" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
                  <line x1="-15" y1="38" x2="0" y2="68" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
                </>
              ) : (
                // Мужской горизонтальный наклон (90°)
                <>
                  <circle cx="-50" cy="35" r="10" fill="#e2f5ea" />
                  <line x1="-40" y1="35" x2="5" y2="35" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
                  <line x1="5" y1="35" x2="5" y2="90" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
                  <line x1="-30" y1="35" x2="5" y2="65" stroke="url(#accentGrad)" strokeWidth="4.5" strokeLinecap="round" />
                </>
              )}
            </g>
          )}

          {posture === 'ITIDAL' && (
            <g transform="translate(100, 10)">
              <circle cx="0" cy="22" r="11" fill="#e2f5ea" />
              <line x1="0" y1="33" x2="0" y2="76" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              <line x1="0" y1="76" x2="-8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              <line x1="0" y1="76" x2="8" y2="110" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              <line x1="0" y1="42" x2="-14" y2="65" stroke="#4ade80" strokeWidth="4" strokeLinecap="round" />
              <line x1="0" y1="42" x2="14" y2="65" stroke="#4ade80" strokeWidth="4" strokeLinecap="round" />
            </g>
          )}

          {posture === 'SUJUD' && (
            <g transform="translate(50, 45)">
              {isFemale ? (
                // Компактный женский суджуд (локти к полу и бокам, живот к бедрам)
                <>
                  <circle cx="35" cy="65" r="10" fill="#e2f5ea" />
                  <path d="M 45 65 Q 65 45 95 65 L 115 72" fill="none" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
                  <line x1="48" y1="68" x2="35" y2="73" stroke="url(#accentGrad)" strokeWidth="4" strokeLinecap="round" />
                </>
              ) : (
                // Мужской суджуд (локти оторваны от пола и разведены)
                <>
                  <circle cx="30" cy="64" r="10" fill="#e2f5ea" />
                  <line x1="38" y1="60" x2="68" y2="35" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
                  <line x1="68" y1="35" x2="105" y2="72" stroke="#4ade80" strokeWidth="5.5" strokeLinecap="round" />
                  {/* Локти приподняты */}
                  <line x1="45" y1="52" x2="35" y2="40" stroke="url(#accentGrad)" strokeWidth="4" strokeLinecap="round" />
                  <line x1="35" y1="40" x2="28" y2="72" stroke="url(#accentGrad)" strokeWidth="4" strokeLinecap="round" />
                </>
              )}
            </g>
          )}

          {(posture === 'SITTING_JALSA' || posture === 'SITTING_TASHAHHUD') && (
            <g transform="translate(100, 35)">
              <circle cx="-10" cy="20" r="10" fill="#e2f5ea" />
              <line x1="-10" y1="30" x2="-10" y2="65" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              {/* Бедра и колени */}
              <line x1="-10" y1="65" x2="-35" y2="82" stroke="#4ade80" strokeWidth="5.5" strokeLinecap="round" />
              <line x1="-10" y1="65" x2="20" y2="82" stroke="#4ade80" strokeWidth="5" strokeLinecap="round" />
              {/* Руки на коленях */}
              <line x1="-10" y1="42" x2="-30" y2="76" stroke="#4ade80" strokeWidth="4" strokeLinecap="round" />
              {/* Указательный палец для ташаххуда */}
              {posture === 'SITTING_TASHAHHUD' && (
                <line x1="-30" y1="76" x2="-40" y2="70" stroke="url(#accentGrad)" strokeWidth="3" strokeLinecap="round" />
              )}
            </g>
          )}

          {posture === 'SALAM_RIGHT' && (
            <g transform="translate(100, 35)">
              <circle cx="-5" cy="20" r="10" fill="#e2f5ea" />
              <line x1="-5" y1="30" x2="-5" y2="65" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              <line x1="-5" y1="65" x2="-30" y2="82" stroke="#4ade80" strokeWidth="5.5" strokeLinecap="round" />
              {/* Стрелка поворота направо */}
              <path d="M 10 18 Q 30 18 35 25" fill="none" stroke="url(#accentGrad)" strokeWidth="3" strokeLinecap="round" />
              <polygon points="37,25 31,23 35,20" fill="#fde047" />
            </g>
          )}

          {posture === 'SALAM_LEFT' && (
            <g transform="translate(100, 35)">
              <circle cx="-5" cy="20" r="10" fill="#e2f5ea" />
              <line x1="-5" y1="30" x2="-5" y2="65" stroke="#4ade80" strokeWidth="6" strokeLinecap="round" />
              <line x1="-5" y1="65" x2="25" y2="82" stroke="#4ade80" strokeWidth="5.5" strokeLinecap="round" />
              {/* Стрелка поворота налево */}
              <path d="M -20 18 Q -40 18 -45 25" fill="none" stroke="url(#accentGrad)" strokeWidth="3" strokeLinecap="round" />
              <polygon points="-47,25 -41,23 -45,20" fill="#fde047" />
            </g>
          )}
        </svg>
      </div>

      {/* Нижний бейдж позы */}
      <div className="absolute bottom-2 left-3 bg-black/60 px-2.5 py-1 rounded-md text-[11px] font-semibold text-[#d4af37] tracking-wide border border-[#d4af37]/20">
        {postureLabels[posture] || 'Положение тела'}
      </div>

      {/* Верхний бейдж мазхаба */}
      <div className="absolute top-2 right-3 bg-[#194030]/80 px-2 py-0.5 rounded text-[10px] text-[#a7d8be] border border-[#256045]">
        {gender === 'FEMALE' ? 'Женщина' : 'Мужчина'}
      </div>
    </div>
  );
}
