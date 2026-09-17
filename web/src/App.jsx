import React, { useState, useEffect } from 'react';
import {
  Compass,
  Water,
  BookOpen,
  CheckCircle,
  HelpCircle,
  RotateCcw,
  Volume2,
  ChevronRight,
  ChevronLeft,
  Settings,
  Sparkles,
  Info,
  Smartphone,
  Maximize2,
  Minimize2,
  Check,
  AlertTriangle,
  Play,
  Square,
  Repeat
} from 'lucide-react';

import { MADHHABS } from './data/madhhabs';
import { PRAYERS, generatePrayerSteps } from './data/prayers';
import { SURAHS, DUAS } from './data/surahsAndDuas';
import { WUDU_DATA } from './data/wudu';
import { QUIZ_QUESTIONS } from './data/quiz';
import PostureIllustration from './components/PostureIllustration';
import { soundManager } from './components/AudioPlayer';

export default function App() {
  // Настройки
  const [activeMadhhab, setActiveMadhhab] = useState('HANAFI');
  const [activeGender, setActiveGender] = useState('MALE'); // 'MALE' | 'FEMALE'
  const [isPhoneFrame, setIsPhoneFrame] = useState(true);

  // Навигация
  const [currentTab, setCurrentTab] = useState('home'); // home | simulator | wudu | surahs | tasbih | quiz | reference
  const [showMadhhabModal, setShowMadhhabModal] = useState(false);

  // Симулятор
  const [simPrayerId, setSimPrayerId] = useState('fajr');
  const [simIsFard, setSimIsFard] = useState(true);
  const [simStepIndex, setSimStepIndex] = useState(0);
  const [isSpeaking, setIsSpeaking] = useState(false);

  // Тасбих
  const [tasbihPreset, setTasbihPreset] = useState(0);
  const [tasbihCount, setTasbihCount] = useState(0);
  const [totalTasbih, setTotalTasbih] = useState(0);

  // Квиз
  const [quizIndex, setQuizIndex] = useState(0);
  const [quizSelected, setQuizSelected] = useState(null);
  const [quizScore, setQuizScore] = useState(0);
  const [quizFinished, setQuizFinished] = useState(false);

  // Вкладки омовения
  const [wuduTab, setWuduTab] = useState('wudu'); // wudu | ghusl | tayammum | nullifiers

  // Вкладки сур
  const [surahTab, setSurahTab] = useState('surahs'); // surahs | duas
  const [expandedSurah, setExpandedSurah] = useState(1);

  // Генерация шагов
  const currentSteps = generatePrayerSteps(simPrayerId, simIsFard, activeMadhhab, activeGender);
  const currentStep = currentSteps[simStepIndex] || currentSteps[0];
  const activePrayer = PRAYERS.find(p => p.id === simPrayerId) || PRAYERS[0];
  const madhhabInfo = MADHHABS[activeMadhhab];

  // Звуковое воспроизведение
  const handlePlayAudio = (text, isArabic = true) => {
    setIsSpeaking(true);
    soundManager.speak(text, isArabic, () => {
      setIsSpeaking(false);
    });
  };

  const handleStopAudio = () => {
    soundManager.stop();
    setIsSpeaking(false);
  };

  // Запуск симулятора
  const startSimulator = (prayerId, isFard) => {
    setSimPrayerId(prayerId);
    setSimIsFard(isFard);
    setSimStepIndex(0);
    setCurrentTab('simulator');
    soundManager.playChime('bell');
  };

  // Пресеты тасбиха
  const tasbihPresets = [
    { title: 'Субханаллах', ar: 'سُبْحَانَ اللَّهِ', target: 33 },
    { title: 'Альхамдулиллях', ar: 'الْحَمْدُ لِلَّهِ', target: 33 },
    { title: 'Аллаху Акбар', ar: 'اللَّهُ أَكْبَرُ', target: 33 },
    { title: 'Астагфируллах', ar: 'أَسْتَغْفِرُ اللَّهَ', target: 33 },
    { title: 'Ля иляха илля-Ллах', ar: 'لَا إِلَٰهَ إِلَّا اللَّهُ', target: 100 },
    { title: 'Салават Пророку ﷺ', ar: 'اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ', target: 100 }
  ];

  const handleTasbihTap = () => {
    soundManager.playChime('bead');
    const newCount = tasbihCount + 1;
    const newTotal = totalTasbih + 1;
    setTotalTasbih(newTotal);

    if (newCount >= tasbihPresets[tasbihPreset].target) {
      if (tasbihPreset < 2) {
        setTasbihPreset(tasbihPreset + 1);
        setTasbihCount(0);
      } else {
        setTasbihCount(newCount);
      }
    } else {
      setTasbihCount(newCount);
    }
  };

  return (
    <div className="min-h-screen bg-[#070e0a] text-slate-100 flex flex-col items-center justify-start p-0 md:p-4 selection:bg-[#d4af37]/30 selection:text-white">
      {/* Верхняя строка управления предпросмотром (на десктопе) */}
      <div className="w-full max-w-4xl hidden md:flex items-center justify-between py-2 px-4 mb-2 bg-[#102219]/90 border border-[#1e3c2b] rounded-xl text-xs text-[#8cb09d]">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
          <span className="font-semibold text-white">Обучение Намазу (Android Jetpack Compose & Web Live Preview)</span>
        </div>
        <div className="flex items-center gap-3">
          <span>Режим экрана:</span>
          <button
            onClick={() => setIsPhoneFrame(true)}
            className={`px-2.5 py-1 rounded flex items-center gap-1.5 transition ${
              isPhoneFrame ? 'bg-[#d4af37] text-black font-bold' : 'bg-[#1b3527] text-white hover:bg-[#234332]'
            }`}
          >
            <Smartphone size={13} />
            Смартфон (Android Frame)
          </button>
          <button
            onClick={() => setIsPhoneFrame(false)}
            className={`px-2.5 py-1 rounded flex items-center gap-1.5 transition ${
              !isPhoneFrame ? 'bg-[#d4af37] text-black font-bold' : 'bg-[#1b3527] text-white hover:bg-[#234332]'
            }`}
          >
            <Maximize2 size={13} />
            Полный экран
          </button>
        </div>
      </div>

      {/* Контейнер приложения (в виде смартфона или растянутый) */}
      <div
        className={`w-full bg-[#0b1611] flex flex-col justify-between overflow-hidden shadow-2xl transition-all duration-300 ${
          isPhoneFrame
            ? 'max-w-[430px] min-h-[860px] md:rounded-[40px] md:border-[7px] md:border-[#1d3829] md:my-2 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.8)]'
            : 'max-w-4xl min-h-[92vh] md:rounded-2xl md:border md:border-[#1c3829]'
        }`}
      >
        {/* Верхняя строка статуса смартфона */}
        {isPhoneFrame && (
          <div className="bg-[#0b1611] px-6 pt-2 pb-1 flex justify-between items-center text-[11px] text-[#7ea18f] font-mono select-none">
            <span>12:00</span>
            <div className="w-20 h-4 bg-black/60 rounded-full mx-auto" />
            <div className="flex items-center gap-1.5">
              <span>5G</span>
              <div className="w-5 h-2.5 border border-[#7ea18f] rounded-sm p-0.5 flex items-center">
                <div className="w-full h-full bg-[#7ea18f] rounded-xs" />
              </div>
            </div>
          </div>
        )}

        {/* Шапка приложения */}
        <header className="bg-gradient-to-r from-[#12281e] via-[#163527] to-[#12281e] border-b border-[#1e3c2c] px-4 py-3 sticky top-0 z-30 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#1b5e39] to-[#0e3b22] border border-[#d4af37]/40 flex items-center justify-center text-[#d4af37] shadow">
              <Sparkles size={18} />
            </div>
            <div>
              <h1 className="text-base font-extrabold tracking-tight text-white leading-tight">
                Обучение Намазу
              </h1>
              <p className="text-[10px] text-[#8db39f] font-medium">
                Все 4 Мазхаба + По Сунне
              </p>
            </div>
          </div>

          {/* Быстрые плашки выбора Мазхаба и Пола */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setShowMadhhabModal(true)}
              className="bg-[#1b3d2b] hover:bg-[#235038] border border-[#d4af37]/40 px-2.5 py-1 rounded-lg text-xs font-semibold text-[#f1d87e] flex items-center gap-1 shadow-sm transition"
            >
              <span>{madhhabInfo.name.split(' ')[0]}</span>
              <Settings size={12} className="opacity-70" />
            </button>

            <button
              onClick={() => {
                setActiveGender(activeGender === 'MALE' ? 'FEMALE' : 'MALE');
                soundManager.playChime('click');
              }}
              className="bg-[#1b3d2b] hover:bg-[#235038] border border-[#2c6145] px-2 py-1 rounded-lg text-xs text-white font-medium transition"
              title="Нажмите для переключения: Мужчина / Женщина"
            >
              {activeGender === 'MALE' ? '👨 М' : '🧕 Ж'}
            </button>
          </div>
        </header>

        {/* ОСНОВНОЙ КОНТЕНТ В ЗАВИСИМОСТИ ОТ ВКЛАДКИ */}
        <main className="flex-1 overflow-y-auto pb-6">
          {/* ВКЛАДКА: ГЛАВНАЯ */}
          {currentTab === 'home' && (
            <div className="p-4 space-y-4">
              {/* Баннер активных настроек */}
              <div className="rounded-2xl bg-gradient-to-br from-[#163a29] to-[#0f241a] p-4 border border-[#d4af37]/30 shadow-lg relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-[#d4af37]/5 rounded-full blur-2xl pointer-events-none" />
                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-[10px] font-bold tracking-wider text-[#d4af37] uppercase">
                      Текущий мазхаб и режим
                    </span>
                    <h2 className="text-xl font-bold text-white mt-0.5">
                      {madhhabInfo.name}
                    </h2>
                    <p className="text-xs text-[#a3c9b4] mt-1 leading-relaxed">
                      {madhhabInfo.region}
                    </p>
                  </div>
                  <span className="text-xs px-2.5 py-1 rounded-full bg-[#1b4330] border border-[#2e6e4e] text-emerald-300 font-semibold">
                    {activeGender === 'MALE' ? 'Мужской намаз' : 'Женский намаз'}
                  </span>
                </div>

                <div className="mt-3 pt-3 border-t border-[#1e4833] flex justify-between items-center text-xs">
                  <span className="text-[#88b09a] italic text-[11px]">
                    {madhhabInfo.rules.handsQiyamMale.substring(0, 48)}...
                  </span>
                  <button
                    onClick={() => setCurrentTab('reference')}
                    className="text-[#d4af37] font-semibold flex items-center gap-0.5 hover:underline"
                  >
                    Подробнее <ChevronRight size={14} />
                  </button>
                </div>
              </div>

              {/* Быстрые разделы */}
              <div>
                <h3 className="text-xs font-bold text-[#d4af37] uppercase tracking-wider mb-2">
                  Быстрый доступ
                </h3>
                <div className="grid grid-cols-2 gap-2.5">
                  <button
                    onClick={() => setCurrentTab('wudu')}
                    className="flex items-center gap-3 p-3 bg-[#13271d] hover:bg-[#193326] border border-[#1e3e2d] rounded-xl text-left transition shadow"
                  >
                    <div className="w-10 h-10 rounded-xl bg-[#1b4731] flex items-center justify-center text-emerald-300 shrink-0">
                      <Water size={20} />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">Омовение</div>
                      <div className="text-[11px] text-[#7ea18f]">Вуду, Гусль, Таяммум</div>
                    </div>
                  </button>

                  <button
                    onClick={() => setCurrentTab('surahs')}
                    className="flex items-center gap-3 p-3 bg-[#13271d] hover:bg-[#193326] border border-[#1e3e2d] rounded-xl text-left transition shadow"
                  >
                    <div className="w-10 h-10 rounded-xl bg-[#1b4731] flex items-center justify-center text-[#d4af37] shrink-0">
                      <BookOpen size={20} />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">Суры и Дуа</div>
                      <div className="text-[11px] text-[#7ea18f]">Текст, транскрипция</div>
                    </div>
                  </button>

                  <button
                    onClick={() => setCurrentTab('tasbih')}
                    className="flex items-center gap-3 p-3 bg-[#13271d] hover:bg-[#193326] border border-[#1e3e2d] rounded-xl text-left transition shadow"
                  >
                    <div className="w-10 h-10 rounded-xl bg-[#1b4731] flex items-center justify-center text-amber-300 shrink-0">
                      <Repeat size={20} />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">Тасбих</div>
                      <div className="text-[11px] text-[#7ea18f]">Зикр 33 / 99</div>
                    </div>
                  </button>

                  <button
                    onClick={() => setCurrentTab('quiz')}
                    className="flex items-center gap-3 p-3 bg-[#13271d] hover:bg-[#193326] border border-[#1e3e2d] rounded-xl text-left transition shadow"
                  >
                    <div className="w-10 h-10 rounded-xl bg-[#1b4731] flex items-center justify-center text-teal-300 shrink-0">
                      <HelpCircle size={20} />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">Тест знаний</div>
                      <div className="text-[11px] text-[#7ea18f]">10 вопросов</div>
                    </div>
                  </button>
                </div>
              </div>

              {/* Список 5 намазов + Витр */}
              <div>
                <div className="flex justify-between items-center mb-2">
                  <h3 className="text-xs font-bold text-[#d4af37] uppercase tracking-wider">
                    5 Обязательных Намазов
                  </h3>
                  <span className="text-[11px] text-[#7ea18f]">Нажмите для обучения</span>
                </div>

                <div className="space-y-3">
                  {PRAYERS.map(prayer => (
                    <div
                      key={prayer.id}
                      className="bg-[#12241b] border border-[#1d3b2b] rounded-2xl p-3.5 shadow hover:border-[#2d5d44] transition"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <h4 className="text-base font-bold text-white flex items-center gap-1.5">
                            {prayer.nameRu}
                          </h4>
                          <p className="text-xs text-[#8cb09d] mt-0.5">{prayer.time}</p>
                        </div>
                        <span className="font-arabic text-lg font-bold text-[#d4af37]">
                          {prayer.nameArabic}
                        </span>
                      </div>

                      {/* Бейджи ракаатов */}
                      <div className="flex flex-wrap gap-1.5 mt-2.5">
                        {prayer.sunnahBefore > 0 && (
                          <span className="text-[11px] px-2 py-0.5 bg-[#1a3828] border border-[#244f38] text-[#9fc4af] rounded-md">
                            {prayer.sunnahBefore} ракаата сунны до
                          </span>
                        )}
                        <span className="text-[11px] px-2 py-0.5 bg-[#17442d] border border-[#29704c] text-emerald-300 font-bold rounded-md">
                          {prayer.fardRakahs} ракаата фард
                        </span>
                        {prayer.sunnahAfter > 0 && (
                          <span className="text-[11px] px-2 py-0.5 bg-[#1a3828] border border-[#244f38] text-[#9fc4af] rounded-md">
                            {prayer.sunnahAfter} ракаата сунны после
                          </span>
                        )}
                      </div>

                      {/* Кнопки симуляции */}
                      <div className="flex gap-2 mt-3 pt-2.5 border-t border-[#1c392a]">
                        <button
                          onClick={() => startSimulator(prayer.id, true)}
                          className="flex-1 bg-gradient-to-r from-[#175c37] to-[#124d2c] hover:from-[#1b6d41] hover:to-[#175c37] text-white text-xs font-bold py-2 rounded-xl flex items-center justify-center gap-1.5 shadow transition"
                        >
                          <Play size={13} fill="currentColor" />
                          Учить Фард ({prayer.fardRakahs} рак.)
                        </button>

                        {(prayer.sunnahBefore > 0 || prayer.sunnahAfter > 0) && (
                          <button
                            onClick={() => startSimulator(prayer.id, false)}
                            className="px-3 bg-[#173023] hover:bg-[#1e3f2e] text-[#d4af37] border border-[#d4af37]/30 text-xs font-semibold py-2 rounded-xl transition"
                          >
                            Сунна
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Цитата / Хадис дня */}
              <div className="bg-[#102018] border border-[#1b3628] rounded-2xl p-3.5 flex gap-3 items-start">
                <div className="p-2 rounded-xl bg-[#173926] text-[#d4af37] shrink-0">
                  <Info size={18} />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-[#d4af37] uppercase tracking-wide">
                    Хадис Пророка ﷺ
                  </h4>
                  <p className="text-xs text-slate-200 mt-1 italic leading-relaxed">
                    «Первое, за что будет спрошен раб Аллаха в Судный день — это его молитва (намаз). Если она будет исправной, то исправными будут и все остальные его дела».
                  </p>
                  <span className="text-[10px] text-[#7ca08e] mt-1 block">
                    (Ат-Тирмизи, Сахих)
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* ВКЛАДКА: СИМУЛЯТОР НАМАЗА */}
          {currentTab === 'simulator' && (
            <div className="flex flex-col h-full">
              {/* Верхняя строка симулятора */}
              <div className="bg-[#12251c] px-4 py-2.5 border-b border-[#1c3a2a] flex justify-between items-center">
                <button
                  onClick={() => {
                    handleStopAudio();
                    setCurrentTab('home');
                  }}
                  className="flex items-center gap-1 text-xs text-[#a2c8b4] hover:text-white"
                >
                  <ChevronLeft size={16} /> Назад
                </button>
                <div className="text-center">
                  <span className="text-xs font-bold text-white">
                    {activePrayer.nameRu} ({simIsFard ? 'Фард' : 'Сунна'})
                  </span>
                  <div className="text-[10px] text-[#d4af37]">
                    Ракаат {currentStep.rakah} • Шаг {simStepIndex + 1} из {currentSteps.length}
                  </div>
                </div>
                <div className="text-[10px] bg-[#1a3a2a] text-[#86b49c] px-2 py-0.5 rounded border border-[#27593f]">
                  {madhhabInfo.name.substring(0, 6)}
                </div>
              </div>

              {/* Прогресс бар */}
              <div className="w-full bg-[#13271d] h-1.5">
                <div
                  className="bg-gradient-to-r from-[#198754] to-[#d4af37] h-1.5 transition-all duration-300"
                  style={{ width: `${((simStepIndex + 1) / currentSteps.length) * 100}%` }}
                />
              </div>

              {/* Тело шага */}
              <div className="p-4 space-y-3.5 flex-1 overflow-y-auto">
                {/* Иллюстрация позы */}
                <PostureIllustration
                  posture={currentStep.posture}
                  madhhabId={activeMadhhab}
                  gender={activeGender}
                />

                {/* Название шага */}
                <div className="flex justify-between items-center">
                  <h3 className="text-base font-bold text-white">
                    {currentStep.title}
                  </h3>
                  {currentStep.repetitions > 1 && (
                    <span className="text-xs px-2 py-0.5 bg-[#d4af37]/20 border border-[#d4af37] text-[#d4af37] font-bold rounded-full">
                      × {currentStep.repetitions} раза
                    </span>
                  )}
                </div>

                {/* Карточка арабского текста */}
                <div className="bg-[#13231b] border border-[#1f422f] rounded-2xl p-4 shadow">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-[10px] font-bold text-[#d4af37] uppercase tracking-wider">
                      Арабский текст
                    </span>
                    <button
                      onClick={() => {
                        if (isSpeaking) {
                          handleStopAudio();
                        } else {
                          handlePlayAudio(currentStep.arabic, true);
                        }
                      }}
                      className={`px-2.5 py-1 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition ${
                        isSpeaking
                          ? 'bg-red-600 text-white animate-pulse'
                          : 'bg-[#1b5535] hover:bg-[#226e45] text-white shadow'
                      }`}
                    >
                      {isSpeaking ? <Square size={12} fill="currentColor" /> : <Volume2 size={14} />}
                      <span>{isSpeaking ? 'Стоп' : 'Слушать'}</span>
                    </button>
                  </div>

                  <p className="font-arabic text-xl sm:text-2xl text-right leading-loose text-white py-1">
                    {currentStep.arabic}
                  </p>
                </div>

                {/* Транскрипция */}
                <div className="bg-[#14261d] border border-[#1f3f2e] rounded-xl p-3">
                  <span className="text-[10px] font-bold text-[#7deba9] uppercase tracking-wider block mb-1">
                    Транскрипция на русском
                  </span>
                  <p className="text-xs sm:text-sm text-emerald-100 font-medium leading-relaxed">
                    {currentStep.transcription}
                  </p>
                </div>

                {/* Перевод */}
                <div className="bg-[#112018] border border-[#1a3326] rounded-xl p-3">
                  <span className="text-[10px] font-bold text-[#8faea1] uppercase tracking-wider block mb-1">
                    Смысловой перевод
                  </span>
                  <p className="text-xs text-slate-300 leading-relaxed">
                    {currentStep.translation}
                  </p>
                </div>

                {/* Инструкция положения тела */}
                <div className="bg-[#13281e] border border-[#234d36] rounded-xl p-3 flex gap-2.5 items-start">
                  <div className="p-1.5 rounded-lg bg-[#1a442e] text-[#d4af37] shrink-0 mt-0.5">
                    <Compass size={16} />
                  </div>
                  <div>
                    <span className="text-[10px] font-bold text-[#d4af37] uppercase tracking-wider block">
                      Положение тела ({activeGender === 'MALE' ? 'Мужчина' : 'Женщина'})
                    </span>
                    <p className="text-xs text-slate-100 mt-1 leading-relaxed">
                      {currentStep.instruction}
                    </p>
                  </div>
                </div>

                {/* Особенность мазхаба */}
                {currentStep.madhhabNote && (
                  <div className="bg-[#242914] border border-[#d4af37]/40 rounded-xl p-3 flex gap-2.5 items-start">
                    <div className="p-1 rounded-md bg-[#d4af37]/20 text-[#d4af37] shrink-0 mt-0.5">
                      <Info size={15} />
                    </div>
                    <div>
                      <span className="text-[10px] font-bold text-[#d4af37] uppercase tracking-wider block">
                        Особенность ({madhhabInfo.name})
                      </span>
                      <p className="text-xs text-amber-100 mt-0.5 leading-relaxed">
                        {currentStep.madhhabNote}
                      </p>
                    </div>
                  </div>
                )}
              </div>

              {/* Нижняя панель навигации симулятора */}
              <div className="bg-[#102018] border-t border-[#1d3c2b] p-3 flex gap-2.5">
                <button
                  disabled={simStepIndex === 0}
                  onClick={() => {
                    handleStopAudio();
                    if (simStepIndex > 0) setSimStepIndex(simStepIndex - 1);
                  }}
                  className={`flex-1 py-2.5 rounded-xl border flex items-center justify-center gap-1 text-xs font-semibold transition ${
                    simStepIndex === 0
                      ? 'border-[#1b3427] text-slate-600 opacity-50 cursor-not-allowed'
                      : 'border-[#2d5c43] bg-[#162f22] text-[#c0e0ce] hover:bg-[#1d3d2c]'
                  }`}
                >
                  <ChevronLeft size={16} />
                  Назад
                </button>

                <button
                  onClick={() => {
                    handlePlayAudio(currentStep.arabic, true);
                  }}
                  className="px-3 py-2.5 rounded-xl border border-[#2d5c43] bg-[#162f22] text-[#d4af37] hover:bg-[#1d3d2c] transition"
                  title="Повторить произношение"
                >
                  <RotateCcw size={16} />
                </button>

                {simStepIndex < currentSteps.length - 1 ? (
                  <button
                    onClick={() => {
                      handleStopAudio();
                      setSimStepIndex(simStepIndex + 1);
                    }}
                    className="flex-[1.5] py-2.5 rounded-xl bg-gradient-to-r from-[#176239] to-[#124d2c] hover:from-[#1b7444] hover:to-[#176239] text-white text-xs font-bold flex items-center justify-center gap-1 shadow-lg transition"
                  >
                    Следующий шаг
                    <ChevronRight size={16} />
                  </button>
                ) : (
                  <button
                    onClick={() => {
                      handleStopAudio();
                      soundManager.playChime('bell');
                      setCurrentTab('home');
                    }}
                    className="flex-[1.5] py-2.5 rounded-xl bg-gradient-to-r from-[#d4af37] to-[#b39023] hover:from-[#e2be47] hover:to-[#d4af37] text-black text-xs font-extrabold flex items-center justify-center gap-1.5 shadow-lg transition"
                  >
                    <Check size={16} />
                    Завершить намаз!
                  </button>
                )}
              </div>
            </div>
          )}

          {/* ВКЛАДКА: ОМОВЕНИЕ (ТАХАРАТ) */}
          {currentTab === 'wudu' && (
            <div className="p-4 space-y-3.5">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="text-base font-bold text-white">Омовение (Тахарат)</h3>
                  <p className="text-xs text-[#8cb09d]">Порядок совершения и правила чистоты</p>
                </div>
                <div className="p-2 rounded-xl bg-[#143223] text-emerald-300">
                  <Water size={20} />
                </div>
              </div>

              {/* Вкладки омовения */}
              <div className="grid grid-cols-4 gap-1 p-1 bg-[#102018] rounded-xl border border-[#1b3829] text-xs">
                {[
                  { id: 'wudu', label: 'Вуду' },
                  { id: 'ghusl', label: 'Гусль' },
                  { id: 'tayammum', label: 'Таяммум' },
                  { id: 'nullifiers', label: 'Нарушает' }
                ].map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setWuduTab(tab.id)}
                    className={`py-1.5 rounded-lg font-semibold transition ${
                      wuduTab === tab.id
                        ? 'bg-[#1b4e33] text-[#d4af37] shadow'
                        : 'text-[#87aa98] hover:text-white'
                    }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              {/* Контент: Вуду */}
              {wuduTab === 'wudu' && (
                <div className="space-y-3">
                  {/* 4 Фарда */}
                  <div className="bg-[#153424] border border-[#2b6d49] rounded-2xl p-3.5 shadow">
                    <span className="text-[10px] font-bold text-[#d4af37] uppercase tracking-wider block mb-1">
                      4 Обязательных Фарда (Сура Аль-Маида, 6)
                    </span>
                    <ul className="text-xs text-emerald-100 space-y-1">
                      {WUDU_DATA.fardList.map((f, i) => (
                        <li key={i}>{f}</li>
                      ))}
                    </ul>
                  </div>

                  {/* Пошаговые действия */}
                  <div className="space-y-2">
                    {WUDU_DATA.steps.map(step => (
                      <div
                        key={step.num}
                        className="bg-[#12241b] border border-[#1d3b2b] rounded-xl p-3 flex gap-3 items-start"
                      >
                        <div
                          className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs shrink-0 ${
                            step.isFard
                              ? 'bg-[#1b5e39] text-[#d4af37] border border-[#d4af37]/40'
                              : 'bg-[#173022] text-[#86aa97]'
                          }`}
                        >
                          {step.num}
                        </div>
                        <div className="flex-1">
                          <div className="flex justify-between items-center">
                            <h4 className="text-sm font-bold text-white">{step.title}</h4>
                            {step.isFard && (
                              <span className="text-[9px] px-1.5 py-0.5 rounded bg-emerald-900/60 border border-emerald-500/50 text-emerald-300 font-bold">
                                ФАРД
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-[#a9ceba] mt-1 leading-relaxed">
                            {step.action}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Дуа после омовения */}
                  <div className="bg-[#13271d] border border-[#d4af37]/30 rounded-2xl p-4">
                    <div className="flex justify-between items-center mb-2">
                      <span className="text-xs font-bold text-[#d4af37]">
                        Дуа после совершения омовения
                      </span>
                      <button
                        onClick={() => handlePlayAudio(WUDU_DATA.afterWuduDua.ar, true)}
                        className="p-1.5 rounded-lg bg-[#1a4a30] text-[#d4af37] hover:bg-[#236341]"
                      >
                        <Volume2 size={16} />
                      </button>
                    </div>
                    <p className="font-arabic text-base sm:text-lg text-right leading-loose text-white mb-2">
                      {WUDU_DATA.afterWuduDua.ar}
                    </p>
                    <p className="text-xs text-[#7deba9] mb-1 font-medium">
                      {WUDU_DATA.afterWuduDua.tr}
                    </p>
                    <p className="text-[11px] text-[#9dbcb0]">
                      {WUDU_DATA.afterWuduDua.ru}
                    </p>
                  </div>
                </div>
              )}

              {/* Контент: Гусль */}
              {wuduTab === 'ghusl' && (
                <div className="space-y-3">
                  <div className="bg-[#12251c] border border-[#1d3b2b] rounded-2xl p-3.5">
                    <h4 className="text-xs font-bold text-[#d4af37] uppercase tracking-wide mb-1">
                      Когда гусль обязателен?
                    </h4>
                    <p className="text-xs text-slate-200 leading-relaxed">
                      После интимной близости (джанаба), поллюции во сне, по окончании менструации (хайд) и послеродовых кровотечений (нифас), а также при принятии Ислама.
                    </p>
                  </div>

                  <div className="space-y-2">
                    {WUDU_DATA.ghuslSteps.map((step, i) => (
                      <div key={i} className="bg-[#12241b] border border-[#1c3829] rounded-xl p-3 text-xs text-slate-200 leading-relaxed">
                        {step}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Контент: Таяммум */}
              {wuduTab === 'tayammum' && (
                <div className="space-y-3">
                  <div className="bg-[#12251c] border border-[#1d3b2b] rounded-2xl p-3.5">
                    <h4 className="text-xs font-bold text-[#d4af37] uppercase tracking-wide mb-1">
                      Очищение землей (Таяммум)
                    </h4>
                    <p className="text-xs text-slate-200 leading-relaxed">
                      Применяется, когда воды нет в радиусе около 2 км, либо ее использование опасно для здоровья по причине болезни или сильного холода.
                    </p>
                  </div>

                  <div className="space-y-2">
                    {WUDU_DATA.tayammumSteps.map((step, i) => (
                      <div key={i} className="bg-[#12241b] border border-[#1c3829] rounded-xl p-3 text-xs text-slate-200 leading-relaxed">
                        {step}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Контент: Что нарушает */}
              {wuduTab === 'nullifiers' && (
                <div className="space-y-2.5">
                  <span className="text-xs font-bold text-[#d4af37] uppercase tracking-wide block">
                    Что аннулирует омовение
                  </span>
                  {WUDU_DATA.nullifiers.map((n, i) => (
                    <div
                      key={i}
                      className="bg-[#16221c] border border-[#2b3a31] rounded-xl p-3 flex gap-2.5 items-start text-xs text-slate-200"
                    >
                      <div className="p-1 rounded-full bg-red-900/40 text-red-400 shrink-0 mt-0.5">
                        <AlertTriangle size={13} />
                      </div>
                      <span className="leading-relaxed">{n}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ВКЛАДКА: СУРЫ И ДУА */}
          {currentTab === 'surahs' && (
            <div className="p-4 space-y-3.5">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="text-base font-bold text-white">Суры и Дуа для намаза</h3>
                  <p className="text-xs text-[#8cb09d]">Арабский оригинал, транскрипция и перевод</p>
                </div>
                <div className="p-2 rounded-xl bg-[#143223] text-[#d4af37]">
                  <BookOpen size={20} />
                </div>
              </div>

              {/* Переключатель: Суры / Дуа */}
              <div className="grid grid-cols-2 gap-1 p-1 bg-[#102018] rounded-xl border border-[#1b3829] text-xs">
                <button
                  onClick={() => setSurahTab('surahs')}
                  className={`py-2 rounded-lg font-semibold transition ${
                    surahTab === 'surahs' ? 'bg-[#1b4e33] text-[#d4af37] shadow' : 'text-[#87aa98]'
                  }`}
                >
                  Короткие суры ({SURAHS.length})
                </button>
                <button
                  onClick={() => setSurahTab('duas')}
                  className={`py-2 rounded-lg font-semibold transition ${
                    surahTab === 'duas' ? 'bg-[#1b4e33] text-[#d4af37] shadow' : 'text-[#87aa98]'
                  }`}
                >
                  Молитвенные дуа ({DUAS.length})
                </button>
              </div>

              {/* Список сур */}
              {surahTab === 'surahs' && (
                <div className="space-y-3">
                  {SURAHS.map(s => {
                    const isExp = expandedSurah === s.number;
                    return (
                      <div
                        key={s.number}
                        className="bg-[#12241b] border border-[#1d3b2b] rounded-2xl p-3.5 shadow transition"
                      >
                        <div
                          onClick={() => setExpandedSurah(isExp ? null : s.number)}
                          className="flex justify-between items-center cursor-pointer select-none"
                        >
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-xl bg-[#1b4530] text-[#d4af37] font-bold text-xs flex items-center justify-center border border-[#d4af37]/30">
                              {s.number}
                            </div>
                            <div>
                              <h4 className="text-sm font-bold text-white">{s.nameRu}</h4>
                              <p className="text-[11px] text-[#7ea18f]">
                                {s.meaningRu} • {s.versesCount} аята
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            <span className="font-arabic text-lg font-bold text-[#d4af37]">
                              {s.nameArabic}
                            </span>
                            <ChevronRight
                              size={16}
                              className={`text-[#7ea18f] transition-transform ${isExp ? 'rotate-90' : ''}`}
                            />
                          </div>
                        </div>

                        {/* Развернутый список аятов */}
                        {isExp && (
                          <div className="mt-3 pt-3 border-t border-[#1c3929] space-y-3">
                            <button
                              onClick={() => {
                                const allArabic = s.verses.map(v => v.ar).join(' ');
                                handlePlayAudio(allArabic, true);
                              }}
                              className="w-full bg-[#17462c] hover:bg-[#1d5737] text-white py-1.5 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 shadow"
                            >
                              <Volume2 size={14} />
                              Слушать всю суру
                            </button>

                            {s.verses.map(v => (
                              <div key={v.num} className="bg-[#0e1d16] p-3 rounded-xl space-y-1.5 border border-[#163023]">
                                <div className="flex justify-between items-center">
                                  <span className="text-[10px] font-bold text-[#d4af37] px-1.5 py-0.5 rounded bg-[#173324]">
                                    Аят {v.num}
                                  </span>
                                  <button
                                    onClick={() => handlePlayAudio(v.ar, true)}
                                    className="p-1 rounded text-[#7deba9] hover:text-white"
                                  >
                                    <Volume2 size={14} />
                                  </button>
                                </div>
                                <p className="font-arabic text-lg sm:text-xl text-right leading-loose text-white">
                                  {v.ar}
                                </p>
                                <p className="text-xs text-[#7deba9] font-medium leading-relaxed">
                                  {v.tr}
                                </p>
                                <p className="text-[11px] text-[#9fc0b2]">
                                  {v.ru}
                                </p>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Список дуа */}
              {surahTab === 'duas' && (
                <div className="space-y-3">
                  {DUAS.map(d => (
                    <div
                      key={d.id}
                      className="bg-[#12241b] border border-[#1d3b2b] rounded-2xl p-3.5 shadow space-y-2"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <h4 className="text-sm font-bold text-white">{d.title}</h4>
                          <span className="text-[10px] text-[#d4af37] font-semibold">{d.occasion}</span>
                        </div>
                        <button
                          onClick={() => handlePlayAudio(d.arabic, true)}
                          className="p-1.5 rounded-lg bg-[#18442c] text-[#d4af37] hover:bg-[#205739]"
                        >
                          <Volume2 size={16} />
                        </button>
                      </div>

                      <p className="font-arabic text-base sm:text-lg text-right leading-loose text-white bg-[#0e1d16] p-2.5 rounded-xl border border-[#183425]">
                        {d.arabic}
                      </p>

                      <p className="text-xs text-[#7deba9] font-medium leading-relaxed">
                        {d.transcription}
                      </p>

                      <p className="text-xs text-[#9fc0b2] leading-relaxed">
                        {d.translation}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ВКЛАДКА: ЭЛЕКТРОННЫЙ ТАСБИХ */}
          {currentTab === 'tasbih' && (
            <div className="p-4 flex flex-col items-center justify-between min-h-[520px]">
              {/* Верхняя строка */}
              <div className="w-full flex justify-between items-center mb-3">
                <div>
                  <h3 className="text-base font-bold text-white">Электронный Тасбих</h3>
                  <p className="text-xs text-[#8cb09d]">Зикр и поминание Аллаха после намаза</p>
                </div>
                <button
                  onClick={() => {
                    setTasbihCount(0);
                    soundManager.playChime('click');
                  }}
                  className="px-2.5 py-1.5 bg-[#173626] border border-[#26573e] text-[#d4af37] text-xs font-semibold rounded-xl flex items-center gap-1 hover:bg-[#1e4531]"
                >
                  <RotateCcw size={12} />
                  Сброс
                </button>
              </div>

              {/* Выбор фразы зикра */}
              <div className="w-full flex gap-1.5 overflow-x-auto pb-2 scrollbar-none">
                {tasbihPresets.map((p, i) => (
                  <button
                    key={i}
                    onClick={() => {
                      setTasbihPreset(i);
                      setTasbihCount(0);
                      soundManager.playChime('click');
                    }}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition shrink-0 ${
                      tasbihPreset === i
                        ? 'bg-[#d4af37] text-black font-bold shadow'
                        : 'bg-[#12251c] text-[#9fc4af] border border-[#1c3829] hover:bg-[#173225]'
                    }`}
                  >
                    {p.title}
                  </button>
                ))}
              </div>

              {/* Интерактивный круг тасбиха */}
              <div
                onClick={handleTasbihTap}
                className="my-auto cursor-pointer select-none group flex flex-col items-center justify-center text-center p-4"
              >
                <div className="relative w-64 h-64 rounded-full flex items-center justify-center shadow-2xl transition active:scale-95">
                  {/* SVG прогресс круга */}
                  <svg className="w-full h-full transform -rotate-90">
                    <circle
                      cx="128"
                      cy="128"
                      r="110"
                      stroke="#13271d"
                      strokeWidth="14"
                      fill="#0d1b14"
                    />
                    <circle
                      cx="128"
                      cy="128"
                      r="110"
                      stroke="#d4af37"
                      strokeWidth="14"
                      fill="none"
                      strokeDasharray={2 * Math.PI * 110}
                      strokeDashoffset={
                        2 * Math.PI * 110 * (1 - (tasbihCount % tasbihPresets[tasbihPreset].target) / tasbihPresets[tasbihPreset].target)
                      }
                      strokeLinecap="round"
                      className="transition-all duration-150"
                    />
                  </svg>

                  {/* Текст внутри круга */}
                  <div className="absolute inset-0 flex flex-col items-center justify-center p-4 pointer-events-none">
                    <span className="font-arabic text-2xl font-bold text-[#d4af37] mb-1">
                      {tasbihPresets[tasbihPreset].ar}
                    </span>
                    <span className="text-4xl sm:text-5xl font-extrabold text-white tracking-tight">
                      {tasbihCount}
                    </span>
                    <span className="text-xs text-[#8cb09d] mt-1 font-semibold">
                      из {tasbihPresets[tasbihPreset].target}
                    </span>
                  </div>
                </div>

                <p className="text-xs text-[#7ea18f] mt-4 flex items-center gap-1.5 animate-pulse">
                  <span>👆</span> Нажимайте в любом месте для счета
                </p>
              </div>

              {/* Нижняя статистика */}
              <div className="w-full bg-[#12251c] border border-[#1e3c2c] rounded-2xl p-3 flex justify-between items-center text-xs">
                <div>
                  <span className="text-[#7ea18f] block text-[10px]">Всего зикров за сессию:</span>
                  <span className="font-bold text-white text-sm">{totalTasbih} раз</span>
                </div>
                <div className="text-right">
                  <span className="text-[#7ea18f] block text-[10px]">Комплекс после намаза:</span>
                  <span className="font-semibold text-[#d4af37]">
                    {tasbihPreset < 3 ? `Шаг ${tasbihPreset + 1} из 3` : 'Свободный зикр'}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* ВКЛАДКА: ТЕСТ ЗНАНИЙ (КВИЗ) */}
          {currentTab === 'quiz' && (
            <div className="p-4 space-y-4">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="text-base font-bold text-white">Проверка знаний</h3>
                  <p className="text-xs text-[#8cb09d]">10 вопросов по ракаатам, омовению и мазхабам</p>
                </div>
                <div className="text-xs px-2.5 py-1 rounded-full bg-[#183f2a] text-[#d4af37] font-bold border border-[#2b6d49]">
                  Баллы: {quizScore}
                </div>
              </div>

              {!quizFinished ? (
                <div className="space-y-3.5">
                  <div className="flex justify-between items-center text-xs text-[#7ea18f]">
                    <span>Вопрос {quizIndex + 1} из {QUIZ_QUESTIONS.length}</span>
                    <span className="text-[#d4af37] font-semibold">
                      {QUIZ_QUESTIONS[quizIndex].category}
                    </span>
                  </div>

                  {/* Вопрос */}
                  <div className="bg-[#13271d] border border-[#1e422f] rounded-2xl p-4 shadow">
                    <h4 className="text-sm sm:text-base font-bold text-white leading-relaxed">
                      {QUIZ_QUESTIONS[quizIndex].question}
                    </h4>
                  </div>

                  {/* Варианты ответов */}
                  <div className="space-y-2">
                    {QUIZ_QUESTIONS[quizIndex].options.map((opt, i) => {
                      const isAnswered = quizSelected !== null;
                      const isCorrect = i === QUIZ_QUESTIONS[quizIndex].correct;
                      const isChosen = quizSelected === i;

                      let btnStyle = 'bg-[#12241b] border-[#1d3b2b] text-slate-200 hover:bg-[#183324]';
                      if (isAnswered) {
                        if (isCorrect) {
                          btnStyle = 'bg-emerald-800/80 border-emerald-400 text-white font-bold';
                        } else if (isChosen) {
                          btnStyle = 'bg-red-800/80 border-red-400 text-white font-bold';
                        } else {
                          btnStyle = 'bg-[#101d16] border-[#182b20] text-slate-500 opacity-60';
                        }
                      }

                      return (
                        <button
                          key={i}
                          disabled={isAnswered}
                          onClick={() => {
                            setQuizSelected(i);
                            if (i === QUIZ_QUESTIONS[quizIndex].correct) {
                              setQuizScore(quizScore + 1);
                              soundManager.playChime('bell');
                            } else {
                              soundManager.playChime('click');
                            }
                          }}
                          className={`w-full p-3.5 rounded-xl border text-left text-xs sm:text-sm transition flex justify-between items-center ${btnStyle}`}
                        >
                          <span>{opt}</span>
                          {isAnswered && isCorrect && <CheckCircle size={16} className="text-emerald-300" />}
                        </button>
                      );
                    })}
                  </div>

                  {/* Объяснение после ответа */}
                  {quizSelected !== null && (
                    <div className="bg-[#173224] border border-[#d4af37]/40 rounded-xl p-3.5 space-y-2">
                      <span className="text-[10px] font-bold text-[#d4af37] uppercase tracking-wider block">
                        Пояснение
                      </span>
                      <p className="text-xs text-emerald-100 leading-relaxed">
                        {QUIZ_QUESTIONS[quizIndex].explanation}
                      </p>

                      <button
                        onClick={() => {
                          if (quizIndex < QUIZ_QUESTIONS.length - 1) {
                            setQuizIndex(quizIndex + 1);
                            setQuizSelected(null);
                          } else {
                            setQuizFinished(true);
                          }
                        }}
                        className="w-full mt-2 bg-gradient-to-r from-[#196d3f] to-[#13522f] hover:from-[#1e7e4a] hover:to-[#196d3f] text-white py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1 shadow"
                      >
                        {quizIndex < QUIZ_QUESTIONS.length - 1 ? 'Следующий вопрос' : 'Посмотреть результаты'}
                        <ChevronRight size={14} />
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                /* Результат квиза */
                <div className="bg-[#12251c] border border-[#d4af37]/30 rounded-2xl p-6 text-center space-y-4 shadow-xl">
                  <div className="w-16 h-16 rounded-full bg-[#1b4e33] text-[#d4af37] flex items-center justify-center mx-auto border-2 border-[#d4af37]">
                    <Sparkles size={32} />
                  </div>

                  <h4 className="text-xl font-extrabold text-white">Тест успешно завершен!</h4>

                  <div className="text-3xl font-extrabold text-[#d4af37]">
                    {quizScore} из {QUIZ_QUESTIONS.length}
                  </div>

                  <p className="text-xs text-[#a2c8b4] leading-relaxed max-w-sm mx-auto">
                    {quizScore >= 8
                      ? 'Ма ша Аллах! Отличный результат! Вы превосходно знаете правила и порядок намаза.'
                      : quizScore >= 5
                      ? 'Хорошая основа! Рекомендуем повторить разделы омовения и различий мазхабов.'
                      : 'Начало положено! Приложение создано для регулярного повторения. Пройдите симулятор намаза еще раз.'}
                  </p>

                  <button
                    onClick={() => {
                      setQuizIndex(0);
                      setQuizSelected(null);
                      setQuizScore(0);
                      setQuizFinished(false);
                    }}
                    className="px-6 py-2.5 bg-[#d4af37] hover:bg-[#e2bd47] text-black font-extrabold text-xs rounded-xl shadow-lg transition"
                  >
                    Пройти тест заново
                  </button>
                </div>
              )}
            </div>
          )}

          {/* ВКЛАДКА: СПРАВОЧНИК И МАЗХАБЫ */}
          {currentTab === 'reference' && (
            <div className="p-4 space-y-4">
              <div>
                <h3 className="text-base font-bold text-white">Сравнительный гид по 5 направлениям</h3>
                <p className="text-xs text-[#8cb09d]">
                  Ханафи, Шафии, Малики, Ханбали и следование Сунне (Саляфитский путь)
                </p>
              </div>

              {/* Таблица различий */}
              <div className="space-y-3">
                {Object.values(MADHHABS).map(m => (
                  <div
                    key={m.id}
                    className={`rounded-2xl p-4 border transition ${
                      activeMadhhab === m.id
                        ? 'bg-[#153425] border-[#d4af37] shadow-lg'
                        : 'bg-[#12241b] border-[#1d3b2b]'
                    }`}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <h4 className="text-base font-bold text-white flex items-center gap-2">
                          {m.name}
                          {activeMadhhab === m.id && (
                            <span className="text-[10px] bg-[#d4af37] text-black px-2 py-0.5 rounded font-extrabold">
                              АКТИВНЫЙ
                            </span>
                          )}
                        </h4>
                        <span className="text-[11px] text-[#d4af37]">{m.founder}</span>
                      </div>

                      {activeMadhhab !== m.id && (
                        <button
                          onClick={() => {
                            setActiveMadhhab(m.id);
                            soundManager.playChime('click');
                          }}
                          className="text-xs bg-[#1a442e] text-[#d4af37] border border-[#d4af37]/40 px-3 py-1 rounded-lg font-semibold hover:bg-[#21573b]"
                        >
                          Выбрать
                        </button>
                      )}
                    </div>

                    <p className="text-xs text-[#9fc0b2] mb-3 leading-relaxed">{m.region}</p>

                    {/* Свойства */}
                    <div className="grid grid-cols-1 gap-1.5 text-xs">
                      <div className="bg-[#0e1d16] p-2 rounded-lg flex justify-between">
                        <span className="text-[#7ea18f]">Положение рук:</span>
                        <span className="text-white text-right font-medium max-w-[240px]">
                          {activeGender === 'MALE' ? m.rules.handsQiyamMale : m.rules.handsQiyamFemale}
                        </span>
                      </div>
                      <div className="bg-[#0e1d16] p-2 rounded-lg flex justify-between">
                        <span className="text-[#7ea18f]">Раф‘уль-ядейн:</span>
                        <span className="text-white text-right font-medium max-w-[240px]">
                          {m.rules.rafulYadayn}
                        </span>
                      </div>
                      <div className="bg-[#0e1d16] p-2 rounded-lg flex justify-between">
                        <span className="text-[#7ea18f]">Палец в ташаххуде:</span>
                        <span className="text-white text-right font-medium max-w-[240px]">
                          {m.rules.fingerTashahhud}
                        </span>
                      </div>
                      <div className="bg-[#0e1d16] p-2 rounded-lg flex justify-between">
                        <span className="text-[#7ea18f]">Дуа Кунут:</span>
                        <span className="text-white text-right font-medium max-w-[240px]">
                          {m.rules.qunut}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Условия и столпы намаза */}
              <div className="bg-[#12241b] border border-[#1d3b2b] rounded-2xl p-4 space-y-2 text-xs">
                <h4 className="text-sm font-bold text-[#d4af37] uppercase tracking-wide">
                  9 Условий намаза (до молитвы)
                </h4>
                <p className="text-[#a5cdb8] leading-relaxed">
                  1) Ислам. 2) Рассудок. 3) Различение (возраст). 4) Очищение от осквернений (Вуду/Гусль).
                  5) Чистота тела, одежды и места. 6) Сокрытие аурата. 7) Наступление времени намаза.
                  8) Направление на Киблу. 9) Искреннее намерение (Ният).
                </p>
              </div>
            </div>
          )}
        </main>

        {/* НИЖНЕЕ МЕНЮ НАВИГАЦИИ (6 ВКЛАДОК) */}
        <nav className="bg-[#0f1f17] border-t border-[#1a3828] px-2 py-2 sticky bottom-0 z-20 flex justify-around items-center">
          {[
            { id: 'home', label: 'Главная', icon: Compass },
            { id: 'wudu', label: 'Омовение', icon: Water },
            { id: 'surahs', label: 'Суры', icon: BookOpen },
            { id: 'tasbih', label: 'Тасбих', icon: Repeat },
            { id: 'quiz', label: 'Тест', icon: HelpCircle },
            { id: 'reference', label: 'Мазхабы', icon: Settings }
          ].map(tab => {
            const IconComponent = tab.icon;
            const isActive = currentTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => {
                  handleStopAudio();
                  setCurrentTab(tab.id);
                  soundManager.playChime('click');
                }}
                className={`flex flex-col items-center py-1 px-2 rounded-xl transition ${
                  isActive
                    ? 'text-[#d4af37] font-bold scale-105'
                    : 'text-[#6c8f7d] hover:text-[#a0c5b3]'
                }`}
              >
                <IconComponent size={18} strokeWidth={isActive ? 2.5 : 1.8} />
                <span className="text-[10px] mt-1">{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* МОДАЛЬНОЕ ОКНО ВЫБОРА МАЗХАБА */}
      {showMadhhabModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-[#0f2118] border border-[#d4af37]/40 w-full max-w-md rounded-3xl p-5 space-y-4 shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center">
              <div>
                <h3 className="text-lg font-bold text-white">Выберите мазхаб</h3>
                <p className="text-xs text-[#8cb09d]">Все 4 суннитские школы + путь прямого следования Сунне</p>
              </div>
              <button
                onClick={() => setShowMadhhabModal(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2.5">
              {Object.values(MADHHABS).map(m => {
                const isSelected = activeMadhhab === m.id;
                return (
                  <div
                    key={m.id}
                    onClick={() => {
                      setActiveMadhhab(m.id);
                      soundManager.playChime('click');
                      setShowMadhhabModal(false);
                    }}
                    className={`p-3.5 rounded-2xl border cursor-pointer transition flex items-center justify-between ${
                      isSelected
                        ? 'bg-[#1b4c33] border-[#d4af37] shadow'
                        : 'bg-[#12251c] border-[#1f3f2f] hover:bg-[#173024]'
                    }`}
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className={`text-sm font-bold ${isSelected ? 'text-[#d4af37]' : 'text-white'}`}>
                          {m.name}
                        </span>
                        {isSelected && <Check size={16} className="text-[#d4af37]" />}
                      </div>
                      <span className="text-[11px] text-[#a4c9b5] block mt-0.5">{m.shortDesc}</span>
                    </div>
                  </div>
                );
              })}
            </div>

            <button
              onClick={() => setShowMadhhabModal(false)}
              className="w-full bg-[#183d29] hover:bg-[#205237] text-white py-2.5 rounded-xl text-xs font-bold transition"
            >
              Закрыть
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
