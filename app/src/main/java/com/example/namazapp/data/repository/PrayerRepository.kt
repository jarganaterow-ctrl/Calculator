package com.example.namazapp.data.repository

import com.example.namazapp.data.model.*

object PrayerRepository {

    val prayersList: List<PrayerInfo> = listOf(
        PrayerInfo(
            id = "fajr",
            nameArabic = "صَلَاةُ الفَجْر",
            nameRu = "Фаджр (Утренний)",
            fardRakahs = 2,
            sunnahBeforeRakahs = 2,
            sunnahAfterRakahs = 0,
            timeDescription = "От рассвета (истинного утреннего света) до начала восхода солнца",
            description = "Начинается день с утренней молитвы. Включает 2 ракаата сунны (муаккада) и 2 ракаата фарда с чтением Корана вслух."
        ),
        PrayerInfo(
            id = "dhuhr",
            nameArabic = "صَلَاةُ الظُّهْر",
            nameRu = "Зухр (Полуденный)",
            fardRakahs = 4,
            sunnahBeforeRakahs = 4,
            sunnahAfterRakahs = 2,
            timeDescription = "От момента, когда солнце прошло зенит, до времени Асра",
            description = "Дневная молитва из 4 ракаатов фарда. Читается про себя (шепотом). До фарда совершается 4 ракаата сунны, после — 2 ракаата."
        ),
        PrayerInfo(
            id = "asr",
            nameArabic = "صَلَاةُ العَصْر",
            nameRu = "Аср (Послеполуденный)",
            fardRakahs = 4,
            sunnahBeforeRakahs = 0,
            sunnahAfterRakahs = 0,
            timeDescription = "От удвоения тени предметов (или одинарной тени) до начала заката солнца",
            description = "Послеполуденная молитва из 4 ракаатов фарда. Читается про себя. Особо подчеркнута в Коране как «средняя молитва»."
        ),
        PrayerInfo(
            id = "maghrib",
            nameArabic = "صَلَاةُ المَغْرِب",
            nameRu = "Магриб (Вечерний)",
            fardRakahs = 3,
            sunnahBeforeRakahs = 0,
            sunnahAfterRakahs = 2,
            timeDescription = "С момента полного захода солнца за горизонт до исчезновения вечерней зари",
            description = "Вечерняя молитва из 3 ракаатов фарда (первые 2 ракаата вслух, 3-й про себя) и 2 ракаатов сунны после фарда."
        ),
        PrayerInfo(
            id = "isha",
            nameArabic = "صَلَاةُ العِشَاء",
            nameRu = "Иша (Ночной)",
            fardRakahs = 4,
            sunnahBeforeRakahs = 0,
            sunnahAfterRakahs = 2,
            timeDescription = "От исчезновения зари до наступления истинного рассвета",
            description = "Ночная молитва из 4 ракаатов фарда (первые 2 ракаата вслух) и 2 ракаатов сунны, после которой читается Витр."
        ),
        PrayerInfo(
            id = "witr",
            nameArabic = "صَلَاةُ الوِتْر",
            nameRu = "Витр (Завершающий)",
            fardRakahs = 3,
            sunnahBeforeRakahs = 0,
            sunnahAfterRakahs = 0,
            timeDescription = "После намаза Иша до наступления утренней зари (Фаджр)",
            description = "Особо важная молитва с нечетным количеством ракаатов (1 или 3 ракаата). Включает чтение дуа Кунут."
        )
    )

    fun getPrayerById(id: String): PrayerInfo {
        return prayersList.find { it.id == id } ?: prayersList.first()
    }

    fun generatePrayerSteps(
        prayerId: String,
        isFard: Boolean = true,
        customRakahsCount: Int? = null,
        madhhab: Madhhab,
        gender: Gender
    ): List<PrayerStep> {
        val prayer = getPrayerById(prayerId)
        val totalRakahs = customRakahsCount ?: if (isFard) prayer.fardRakahs else 2
        val steps = mutableListOf<PrayerStep>()

        // 1. Намерение (Ният)
        steps.add(
            PrayerStep(
                stepId = "niyyah",
                rakahNumber = 1,
                stepNumber = 1,
                totalStepsInRakah = 8,
                title = "Ният (Намерение)",
                posture = PostureType.INTENTION,
                arabicText = "نَوَيْتُ أَنْ أُصَلِّيَ لِلَّهِ تَعَالَى",
                transcription = "Навайту ан усаллийа лилляхи та'аля...",
                translation = "Я намереваюсь ради Аллаха совершить ${if (isFard) "фард" else "сунну"} намаза ${prayer.nameRu}.",
                instruction = "Встаньте лицом к Кибле. Сосредоточьтесь, очистите мысли от мирского. Намерение формируется в сердце. Произнесение шепотом допустимо для сосредоточения.",
                madhhabSpecificNote = when (madhhab) {
                    Madhhab.SHAFII -> "В шафиитском мазхабе желательно сопрягать намерение непосредственно с произнесением вступительного такбира."
                    Madhhab.SUNNAH_SALAFI -> "По сунне достаточно искреннего намерения в сердце; произносить ният вслух или шепотом языком не является сунной пророка."
                    else -> "Намерение принимается в сердце перед такбиром."
                }
            )
        )

        // 2. Вступительный такбир (Ифтитах)
        val takbirInstruction = when (madhhab) {
            Madhhab.HANAFI -> if (gender == Gender.MALE) {
                "Мужчины поднимают руки до уровня мочек ушей, касаясь их большими пальцами. Ладони обращены в сторону Киблы."
            } else {
                "Женщины поднимают руки до уровня плеч или верха груди, не высовывая кисти из-под платка. Пальцы сомкнуты."
            }
            Madhhab.SHAFII -> "Руки поднимаются до уровня плеч (кончики пальцев на уровне верха ушей, большие пальцы на уровне мочек). Ладони раскрыты в сторону Киблы."
            Madhhab.MALIKI -> "Руки поднимаются до уровня плеч или груди ладонями вперед при произнесении Аллаху Акбар."
            Madhhab.HANBALI -> "Поднимите руки до уровня плеч или мочек ушей с раскрытыми к Кибле ладонями."
            Madhhab.SUNNAH_SALAFI -> "Поднимите руки на уровень плеч или мочек ушей с умеренно раскрытыми пальцами, направленными в небо и к Кибле."
        }

        steps.add(
            PrayerStep(
                stepId = "takbeer_tahreemah",
                rakahNumber = 1,
                stepNumber = 2,
                totalStepsInRakah = 8,
                title = "Такбир аль-Ихрам (Вступительный такбир)",
                posture = PostureType.TAKBEER,
                arabicText = "اللَّهُ أَكْبَرُ",
                transcription = "Аллаху Акбар",
                translation = "Аллах Превелик!",
                instruction = takbirInstruction,
                madhhabSpecificNote = "С этого момента начинается намаз. Запрещены любые посторонние движения, разговоры и отвлечения."
            )
        )

        // Проходим по каждому ракаату
        for (rakah in 1..totalRakahs) {
            val isFirstRakah = (rakah == 1)
            val isFinalRakah = (rakah == totalRakahs)
            val hasMiddleTashahhud = (totalRakahs > 2 && rakah == 2)
            val shouldReciteExtraSurah = (rakah <= 2) // В 3-м и 4-м ракаатах фарда читается только Фатиха

            // 1. Стояние (Кыям) и чтение
            val qiyamInstruction = when (madhhab) {
                Madhhab.HANAFI -> if (gender == Gender.MALE) {
                    "Положите правую руку на левую ниже пупка, охватив большим пальцем и мизинцем левое запястье. Взгляд направлен на место земного поклона."
                } else {
                    "Положите правую ладонь на левую руку на уровне груди. Локти прижаты к телу, стопы стоят близко друг к другу."
                }
                Madhhab.SHAFII -> "Сложите руки чуть выше пупка (под грудью, немного сместив влево к сердцу). Правая кисть охватывает левое запястье."
                Madhhab.MALIKI -> "Основное положение в маликитском мазхабе — руки опущены вдоль тела (садль). Также допускается сложить руки на груди (кабд)."
                Madhhab.HANBALI -> "Сложите руки ниже пупка либо на груди, положив правую ладонь на левую кисть."
                Madhhab.SUNNAH_SALAFI -> "Положите правую кисть на левую руку, охватывая запястье и предплечье, строго на груди. Взгляд — на место суджуда."
            }

            val thanaText = if (madhhab == Madhhab.SHAFII) {
                "Вадджахту ваджхийа ли-ллязи фатара-с-самавати валь-арда..."
            } else {
                "Субханака-Ллахумма ва би-хамдика, ва табарака-смука..."
            }

            steps.add(
                PrayerStep(
                    stepId = "qiyam_fatiha_$rakah",
                    rakahNumber = rakah,
                    stepNumber = 1,
                    totalStepsInRakah = 7,
                    title = if (isFirstRakah) "Кыям: Сана и Сура Аль-Фатиха" else "Кыям: Сура Аль-Фатиха",
                    posture = if (madhhab == Madhhab.MALIKI) PostureType.QIYAM_SADL else PostureType.QIYAM_FOLDED,
                    arabicText = if (isFirstRakah) {
                        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ... \n\n بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ..."
                    } else {
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ \n الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ الرَّحْمَٰنِ الرَّحِيمِ مَالِكِ يَوْمِ الدِّينِ..."
                    },
                    transcription = if (isFirstRakah) {
                        "$thanaText \n\n Аузу билляхи мина-ш-шайтани-р-раджим. \n Бисмилляхи-р-Рахмани-р-Рахим. Аль-хамду лилляхи Рабби-ль-'алямин..."
                    } else {
                        "Бисмилляхи-р-Рахмани-р-Рахим. Аль-хамду лилляхи Рабби-ль-'алямин. Ар-Рахмани-р-Рахим. Малики йауми-д-дин..."
                    },
                    translation = "Хвала Аллаху, Господу миров, Милостивому, Милосердному, Владыке Судного дня...",
                    instruction = qiyamInstruction,
                    madhhabSpecificNote = if (isFirstRakah) {
                        "В первом ракаате перед Фатихой читается дуа-истифтах (Сана). После окончания Фатихи тихо (или вслух в джамаате по Шафии/Сунне) произносится «Амиин»."
                    } else null
                )
            )

            // Дополнительная сура (для 1 и 2 ракаатов)
            if (shouldReciteExtraSurah) {
                val surahChoice = if (rakah == 1) "Аль-Ихлас" else "Аль-Фаляк"
                val surahArabic = if (rakah == 1) {
                    "قُلْ هُوَ اللَّهُ أَحَدٌ ۞ اللَّهُ الصَّمَدُ ۞ لَمْ يَلِدْ وَلَمْ يُولَدْ ۞ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ"
                } else {
                    "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۞ مِن شَرِّ مَا خَلَقَ ۞ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ..."
                }
                val surahTranscription = if (rakah == 1) {
                    "Куль хува-Ллаху Ахад. Аллаху-с-Самад. Лям йалид ва лям йуляд. Ва лям йакун ляху куфуван ахад."
                } else {
                    "Куль а'узу би-Рабби-ль-фаляк. Мин шарри ма халяк..."
                }

                steps.add(
                    PrayerStep(
                        stepId = "extra_surah_$rakah",
                        rakahNumber = rakah,
                        stepNumber = 2,
                        totalStepsInRakah = 7,
                        title = "Чтение суры ($surahChoice)",
                        posture = if (madhhab == Madhhab.MALIKI) PostureType.QIYAM_SADL else PostureType.QIYAM_FOLDED,
                        arabicText = surahArabic,
                        transcription = surahTranscription,
                        translation = "Скажи: «Он — Аллах Единый, Аллах Самодостаточный...»",
                        instruction = "Продолжайте стоять смирно. Прочтите любую короткую суру или минимум 3 коротких аята из Корана.",
                        madhhabSpecificNote = "В 1-м и 2-м ракаатах чтение суры после Фатихи является сунной/ваджибом. В 3-м и 4-м ракаатах фарда читается только Аль-Фатиха."
                    )
                )
            }

            // Витр: специальный такбир и Кунут в 3 ракаате
            if (prayerId == "witr" && rakah == 3 && madhhab == Madhhab.HANAFI) {
                steps.add(
                    PrayerStep(
                        stepId = "witr_qunut_hanafi",
                        rakahNumber = 3,
                        stepNumber = 3,
                        totalStepsInRakah = 8,
                        title = "Такбир и Дуа Кунут (Витр по Ханафи)",
                        posture = PostureType.TAKBEER,
                        arabicText = "اللَّهُ أَكْبَرُ \n\n اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ...",
                        transcription = "Аллаху Акбар! \n\n Аллахумма инна наста'инука ва настагфирука...",
                        translation = "Аллах Превелик! О Аллах, мы просим Тебя о помощи и просим о прощении...",
                        instruction = "После чтения суры поднимите руки к ушам/плечам со словами «Аллаху Акбар», снова сложите руки на животе/груди и прочтите дуа Кунут перед уходом в рукуъ.",
                        madhhabSpecificNote = "В ханафитском мазхабе такбир перед Кунутом с поднятием рук является обязательным (ваджиб)."
                    )
                )
            }

            // Рукуъ (Поясной поклон)
            val rukuInstruction = when (madhhab) {
                Madhhab.HANAFI -> if (gender == Gender.MALE) {
                    "Склонитесь со словами «Аллаху Акбар». Спина горизонтальна полу, пальцы рук широко расставлены на коленях, руки прямые."
                } else {
                    "Склонитесь со словами «Аллаху Акбар». Женщины слегка наклоняются (примерно под 45 градусов), пальцы сомкнуты на коленях, локти прижаты к телу."
                }
                Madhhab.SHAFII, Madhhab.HANBALI, Madhhab.SUNNAH_SALAFI -> {
                    "Поднимите руки со словами «Аллаху Акбар» (раф'уль-ядейн) и склонитесь в поясной поклон. Спина ровная, голова на одной линии со спиной, руки охватывают колени."
                }
                Madhhab.MALIKI -> "Склонитесь в рукуъ со словами «Аллаху Акбар». Ладони лежат на коленях, спина выпрямлена."
            }

            steps.add(
                PrayerStep(
                    stepId = "ruku_$rakah",
                    rakahNumber = rakah,
                    stepNumber = if (shouldReciteExtraSurah) 3 else 2,
                    totalStepsInRakah = 7,
                    title = "Рукуъ (Поясной поклон)",
                    posture = PostureType.RUKU,
                    arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                    transcription = "Субхана Раббийа-ль-'Азыйм (3 раза)",
                    translation = "Пречист мой Господь Великий!",
                    instruction = rukuInstruction,
                    madhhabSpecificNote = "Поясной поклон должен быть спокойным, с фиксацией тела минимум на время одного тасбиха (тума'нина).",
                    repetitions = 3
                )
            )

            // Выпрямление (И'тидаль)
            val itidalInstruction = when (madhhab) {
                Madhhab.SHAFII, Madhhab.HANBALI, Madhhab.SUNNAH_SALAFI -> {
                    "Выпрямляясь, поднимите руки до уровня плеч со словами «Сами'а-Ллаху ли-ман хамидах». Полностью выпрямитесь и опустите руки (или сложите на груди по мнению ряда ученых сунны), сказав «Раббана ва ляка-ль-хамд»."
                }
                else -> {
                    "Выпрямляясь, произнесите «Сами'а-Ллаху ли-ман хамидах». Встав прямо, опустите руки вдоль тела и скажите «Раббана ва ляка-ль-хамд»."
                }
            }

            steps.add(
                PrayerStep(
                    stepId = "itidal_$rakah",
                    rakahNumber = rakah,
                    stepNumber = if (shouldReciteExtraSurah) 4 else 3,
                    totalStepsInRakah = 7,
                    title = "И'тидаль (Выпрямление)",
                    posture = PostureType.ITIDAL,
                    arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ ، رَبَّنَا وَلَكَ الْحَمْدُ",
                    transcription = "Сами'а-Ллаху ли-ман хамидах. Раббана ва ляка-ль-хамд",
                    translation = "Услышал Аллах того, кто воздал Ему хвалу. Господь наш, хвала Тебе!",
                    instruction = itidalInstruction,
                    madhhabSpecificNote = if (prayerId == "fajr" && rakah == 2 && madhhab == Madhhab.SHAFII) {
                        "В шафиитском мазхабе во 2-м ракаате Фаджра после выпрямления поднимаются руки ладонями вверх и читается дуа Кунут («Аллахумма-хдини фиман хадайт...»)."
                    } else null
                )
            )

            // Земной поклон 1 (Саджда 1)
            val sujudInstruction = when (madhhab) {
                Madhhab.HANAFI -> if (gender == Gender.MALE) {
                    "Опуститесь со словами «Аллаху Акбар» (сначала колени, затем ладони, затем лоб и нос). Локти приподняты и разведены от боков, живот не касается бедер. Пальцы ног направлены к Кибле."
                } else {
                    "Опуститесь компактно. Женщины прижимают локти к полу и к бокам, живот прижат к бедрам, стопы направлены вправо."
                }
                Madhhab.SHAFII, Madhhab.HANBALI, Madhhab.SUNNAH_SALAFI -> {
                    "Опуститесь со словами «Аллаху Акбар». 7 точек касаются пола: лоб с носом, обе ладони, оба колена и пальцы обеих стоп, обращенные к Кибле. Локти приподняты от пола, руки на уровне плеч или ушей."
                }
                Madhhab.MALIKI -> "Опуститесь в суджуд на 7 частей тела, удерживая локти приподнятыми над ковриком."
            }

            steps.add(
                PrayerStep(
                    stepId = "sujud1_$rakah",
                    rakahNumber = rakah,
                    stepNumber = if (shouldReciteExtraSurah) 5 else 4,
                    totalStepsInRakah = 7,
                    title = "Саджда 1 (Земной поклон)",
                    posture = PostureType.SUJUD,
                    arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                    transcription = "Субхана Раббийа-ль-А'ля (3 раза)",
                    translation = "Пречист мой Господь Всевышний!",
                    instruction = sujudInstruction,
                    madhhabSpecificNote = "Самое близкое положение раба к своему Создателю — во время земного поклона. Сохраняйте полное спокойствие.",
                    repetitions = 3
                )
            )

            // Сидение между сажда (Джалса)
            steps.add(
                PrayerStep(
                    stepId = "jalsa_$rakah",
                    rakahNumber = rakah,
                    stepNumber = if (shouldReciteExtraSurah) 6 else 5,
                    totalStepsInRakah = 7,
                    title = "Джалса (Сидение между двумя суджудами)",
                    posture = PostureType.SITTING_JALSA,
                    arabicText = "رَبِّ اغْفِرْ لِي ، رَبِّ اغْفِرْ لِي",
                    transcription = "Рабби-гфир ли, Рабби-гфир ли",
                    translation = "Господи, прости меня! Господи, прости меня!",
                    instruction = if (gender == Gender.MALE) {
                        "Сядьте со словами «Аллаху Акбар» на левую ногу, поставив правую стопу вертикально пальцами к Кибле (ифтираш). Ладони положите на колени."
                    } else {
                        "Сядьте со словами «Аллаху Акбар», сместив обе ноги вправо и сев на левое бедро (таваррук). Ладони на бедрах/коленях."
                    },
                    madhhabSpecificNote = "Обязательно полностью выпрямить спину и сделать короткую паузу перед вторым суджудом."
                )
            )

            // Земной поклон 2 (Саджда 2)
            steps.add(
                PrayerStep(
                    stepId = "sujud2_$rakah",
                    rakahNumber = rakah,
                    stepNumber = if (shouldReciteExtraSurah) 7 else 6,
                    totalStepsInRakah = 7,
                    title = "Саджда 2 (Второй земной поклон)",
                    posture = PostureType.SUJUD,
                    arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                    transcription = "Субхана Раббийа-ль-А'ля (3 раза)",
                    translation = "Пречист мой Господь Всевышний!",
                    instruction = sujudInstruction,
                    repetitions = 3
                )
            )

            // Промежуточный Ташаххуд (после 2-го ракаата в 3-х и 4-х ракаатных намазах)
            if (hasMiddleTashahhud) {
                val fingerInstruction = getFingerInstruction(madhhab)
                steps.add(
                    PrayerStep(
                        stepId = "middle_tashahhud",
                        rakahNumber = 2,
                        stepNumber = 8,
                        totalStepsInRakah = 8,
                        title = "Первый Ташаххуд (Ат-Тахийят)",
                        posture = PostureType.SITTING_TASHAHHUD,
                        arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ...",
                        transcription = "Ат-тахиййату лилляхи вас-салавату ват-таййибат...",
                        translation = "Все приветствия Аллаху, молитвы и благие деяния...",
                        instruction = "Сядьте в позу сидения. Положите ладони на колени. Прочтите Ат-Тахийят. $fingerInstruction",
                        madhhabSpecificNote = "После чтения ташаххуда встаньте на 3-й ракаат со словами «Аллаху Акбар»."
                    )
                )
            }

            // Заключительное сидение (в последнем ракаате)
            if (isFinalRakah) {
                val fingerInstruction = getFingerInstruction(madhhab)
                val sittingPostureNote = when (madhhab) {
                    Madhhab.HANAFI -> if (gender == Gender.MALE) {
                        "Сидение в позе Ифтираш: сидите на подогнутой левой стопе, правая стопа стоит вертикально носком к Кибле."
                    } else {
                        "Сидение в позе Таваррук: обе ноги выведены вправо, сидение на полу."
                    }
                    else -> "В последнем ракаате сунной является Таваррук: левая нога пропущена под правой голенью, сидящий опускается на пол."
                }

                steps.add(
                    PrayerStep(
                        stepId = "final_tashahhud",
                        rakahNumber = rakah,
                        stepNumber = 8,
                        totalStepsInRakah = 10,
                        title = "Ташаххуд и Салават (Ат-Тахийят, Салават, Дуа)",
                        posture = PostureType.SITTING_TASHAHHUD,
                        arabicText = "التَّحِيَّاتُ لِلَّهِ... \n\n اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ... \n\n رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً...",
                        transcription = "Ат-тахиййату лилляхи... \n\n Аллахумма салли 'аля Мухаммадин... \n\n Раббана атина фид-дунья хасанатан...",
                        translation = "Ат-Тахийят + Салават пророку + Дуа о благе в обоих мирах.",
                        instruction = "$sittingPostureNote $fingerInstruction",
                        madhhabSpecificNote = "Прочтите сначала Ташаххуд, затем Салават (Аллахумма салли и барик), затем завершающее дуа (Раббана атина)."
                    )
                )

                // Приветствие (Салям)
                if (madhhab == Madhhab.MALIKI) {
                    steps.add(
                        PrayerStep(
                            stepId = "salam_maliki",
                            rakahNumber = rakah,
                            stepNumber = 9,
                            totalStepsInRakah = 10,
                            title = "Таслим (Приветствие направо)",
                            posture = PostureType.SALAM_RIGHT,
                            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                            transcription = "Ас-саляму 'алейкум ва рахмату-Ллах",
                            translation = "Мир вам и милость Аллаха!",
                            instruction = "Поверните голову направо, произнося приветствие. На этом намаз завершается.",
                            madhhabSpecificNote = "В маликитском мазхабе один салям направо является обязательным выходом из намаза."
                        )
                    )
                } else {
                    steps.add(
                        PrayerStep(
                            stepId = "salam_right",
                            rakahNumber = rakah,
                            stepNumber = 9,
                            totalStepsInRakah = 10,
                            title = "Таслим 1: Поворот направо",
                            posture = PostureType.SALAM_RIGHT,
                            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                            transcription = "Ас-саляму 'алейкум ва рахмату-Ллах",
                            translation = "Мир вам и милость Аллаха!",
                            instruction = "Поверните голову направо так, чтобы щека была видна сзади, приветствуя ангелов и молящихся.",
                            madhhabSpecificNote = null
                        )
                    )
                    steps.add(
                        PrayerStep(
                            stepId = "salam_left",
                            rakahNumber = rakah,
                            stepNumber = 10,
                            totalStepsInRakah = 10,
                            title = "Таслим 2: Поворот налево",
                            posture = PostureType.SALAM_LEFT,
                            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                            transcription = "Ас-саляму 'алейкум ва рахмату-Ллах",
                            translation = "Мир вам и милость Аллаха!",
                            instruction = "Поверните голову налево, произнося приветствие. Намаз завершен! Альхамдулиллях.",
                            madhhabSpecificNote = "После саляма желательно произнести 3 раза «Астагфируллах» и совершить зикр (Тасбих)."
                        )
                    )
                }
            }
        }

        return steps
    }

    private fun getFingerInstruction(madhhab: Madhhab): String {
        return when (madhhab) {
            Madhhab.HANAFI -> "При словах «Ля иляха» поднимите указательный палец правой руки (сложив остальные в кольцо), а при словах «илля-Ллах» опустите его."
            Madhhab.SHAFII -> "При словах «илля-Ллах» поднимите указательный палец правой руки и удерживайте его направленным к Кибле неподвижно до саляма."
            Madhhab.MALIKI -> "Слегка двигайте указательным пальцем вправо-влево на протяжении всего чтения Ташаххуда."
            Madhhab.HANBALI -> "Поднимайте/шевелите указательным пальцем при каждом упоминании имени Аллаха."
            Madhhab.SUNNAH_SALAFI -> "Направьте указательный палец в сторону Киблы, сфокусируйте взгляд на пальце и делайте легкие движения пальцем во время дуа."
        }
    }
}
