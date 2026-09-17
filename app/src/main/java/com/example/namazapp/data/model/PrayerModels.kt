package com.example.namazapp.data.model

enum class Madhhab(val displayName: String, val shortDesc: String, val regionNote: String) {
    HANAFI(
        displayName = "Ханафитский",
        shortDesc = "Мазхаб имама Абу Ханифы",
        regionNote = "Самый распространенный в РФ, Татарстане, Башкортостане, Центральной Азии и Турции"
    ),
    SHAFII(
        displayName = "Шафиитский",
        shortDesc = "Мазхаб имама аш-Шафии",
        regionNote = "Распространен на Северном Кавказе (Дагестан, Чечня, Ингушетия), в Египте, Индонезии"
    ),
    MALIKI(
        displayName = "Маликитский",
        shortDesc = "Мазхаб имама Малика",
        regionNote = "Распространен в странах Магриба, Северной и Западной Африки, ОАЭ"
    ),
    HANBALI(
        displayName = "Ханбалитский",
        shortDesc = "Мазхаб имама Ахмада ибн Ханбаля",
        regionNote = "Распространен на Аравийском полуострове, в Саудовской Аравии, Катаре"
    ),
    SUNNAH_SALAFI(
        displayName = "По Сунне / Без мазхаба",
        shortDesc = "Следование прямым хадисам (Саляфитский путь)",
        regionNote = "Опирается на наиболее достоверные хадисы без привязки к конкретной богословской школе"
    )
}

enum class Gender(val displayName: String) {
    MALE("Мужчина"),
    FEMALE("Женщина")
}

enum class PostureType {
    INTENTION,
    TAKBEER,
    QIYAM_FOLDED,
    QIYAM_SADL,
    RUKU,
    ITIDAL,
    SUJUD,
    SITTING_JALSA,
    SITTING_TASHAHHUD,
    SALAM_RIGHT,
    SALAM_LEFT
}

data class PrayerInfo(
    val id: String,
    val nameArabic: String,
    val nameRu: String,
    val fardRakahs: Int,
    val sunnahBeforeRakahs: Int = 0,
    val sunnahAfterRakahs: Int = 0,
    val timeDescription: String,
    val description: String
)

data class PrayerStep(
    val stepId: String,
    val rakahNumber: Int,
    val stepNumber: Int,
    val totalStepsInRakah: Int,
    val title: String,
    val posture: PostureType,
    val arabicText: String,
    val transcription: String,
    val translation: String,
    val instruction: String,
    val madhhabSpecificNote: String? = null,
    val repetitions: Int = 1
)

data class SurahItem(
    val number: Int,
    val nameArabic: String,
    val nameRu: String,
    val meaningRu: String,
    val versesCount: Int,
    val verses: List<VerseItem>
)

data class VerseItem(
    val verseNumber: Int,
    val arabic: String,
    val transcription: String,
    val translation: String
)

data class DuaItem(
    val id: String,
    val title: String,
    val occasion: String,
    val arabic: String,
    val transcription: String,
    val translation: String,
    val note: String? = null
)

data class WuduStepItem(
    val stepNumber: Int,
    val title: String,
    val actionText: String,
    val isFard: Boolean,
    val duaArabic: String? = null,
    val duaTranscription: String? = null,
    val duaTranslation: String? = null
)

data class QuizQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
