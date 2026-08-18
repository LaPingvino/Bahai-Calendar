package com.example.devotional

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object DevotionalRepository {

    val PRESET_LOCATIONS = listOf(
        CityLocation("Haifa / Acre (Bahjí)", "Israel", 32.9433, 35.0922, "Asia/Jerusalem"),
        CityLocation("Tehran", "Iran", 35.6892, 51.3890, "Asia/Tehran"),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        CityLocation("Frankfurt (Langenhain)", "Germany", 50.1109, 8.6821, "Europe/Berlin"),
        CityLocation("Wilmette / Chicago", "United States", 42.0748, -87.6901, "America/Chicago"),
        CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
        CityLocation("San Francisco", "United States", 37.7749, -122.4194, "America/Los_Angeles"),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        CityLocation("Sydney (Ingleside)", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        CityLocation("New Delhi (Lotus Temple)", "India", 28.5535, 77.2588, "Asia/Kolkata"),
        CityLocation("Panama City", "Panama", 9.0714, -79.5188, "America/Panama"),
        CityLocation("Santiago", "Chile", -33.4489, -70.6693, "America/Santiago"),
        CityLocation("Kampala", "Uganda", 0.3476, 32.5825, "Africa/Kampala"),
        CityLocation("Apia", "Samoa", -13.8333, -171.7667, "Pacific/Apia"),
        CityLocation("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo")
    )

    val ALL_WRITINGS: List<HolyWriting> = listOf(
        // --- OBLIGATORY PRAYERS ---
        HolyWriting(
            id = "obl_short",
            title = "Short Obligatory Prayer",
            author = "Bahá'u'lláh",
            category = WritingCategory.OBLIGATORY,
            textEnglish = "I bear witness, O my God, that Thou hast created me to know Thee and to worship Thee. I testify, at this moment, to my powerlessness and to Thy might, to my poverty and to Thy wealth.\n\nThere is none other God but Thee, the Help in Peril, the Self-Subsisting.",
            textArabicPersian = "أَشْهَدُ يَا إِلَهِي بِأَنَّكَ خَلَقْتَنِي لِعِرْفَانِكَ وَعِبَادَتِكَ، أَشْهَدُ فِي هَذَا الْحِينِ بِعَجْزِي وَقُوَّتِكَ، وَضَعْفِي وَاقْتِدَارِكَ، وَفَقْرِي وَغَنَائِكَ.\n\nلا إِلَهَ إِلا أَنْتَ الْمُهَيْمِنُ الْقَيُّومُ.",
            sourceReference = "Prayers and Meditations, CLXXXI",
            instructions = "To be recited once in twenty-four hours, at noon (between solar noon and sunset), while facing the Qiblih."
        ),
        HolyWriting(
            id = "obl_medium",
            title = "Medium Obligatory Prayer",
            author = "Bahá'u'lláh",
            category = WritingCategory.OBLIGATORY,
            textEnglish = "Whoso wisheth to pray, let him wash his hands, and while he washeth, let him say:\n\nStrengthen my hand, O my God, that it may take hold of Thy Book with such steadfastness that the hosts of the world shall have no power over it. Guard it, then, from meddling with whatsoever doth not belong unto it. Thou art, verily, the Almighty, the Most Powerful.\n\nAnd while washing his face, let him say:\n\nI have turned my face unto Thee, O my Lord! illumine it with the light of Thy countenance. Protect it, then, from turning to any one but Thee.\n\nThen standing up and facing the Qiblih, let him say:\n\nGod testifieth that there is none other God but Him. His are the kingdoms of Revelation and of creation. He, in truth, hath manifested Him Who is the Dayspring of Revelation, Who conversed on Sinai, through Whom the Supreme Horizon hath been made to shine, and the Lote-Tree beyond which there is no passing hath spoken...",
            sourceReference = "Kitáb-i-Aqdas",
            instructions = "To be recited three times a day: morning, noon, and evening, with ablutions and facing the Qiblih."
        ),
        HolyWriting(
            id = "obl_long",
            title = "Long Obligatory Prayer",
            author = "Bahá'u'lláh",
            category = WritingCategory.OBLIGATORY,
            textEnglish = "Whoso wisheth to recite this prayer, let him stand up and turn unto God, and, as he standeth in his place, let him gaze to the right and to the left, as if awaiting the mercy of his Lord, the Most Merciful, the Compassionate. Then let him say:\n\nO Thou Who art the Lord of all names and the Maker of the heavens! I beseech Thee by them Who are the Day-Springs of Thine invisible Essence, the Most Exalted, the All-Glorious, to make of my prayer a fire that will burn away the veils which have shut me out from Thy beauty, and a light that will lead me unto the ocean of Thy Presence...",
            sourceReference = "Prayers and Meditations",
            instructions = "To be recited once in twenty-four hours at any time with full prostrations and movements facing the Qiblih."
        ),

        // --- FASTING & DAWN ---
        HolyWriting(
            id = "fast_dawn_1",
            title = "Prayer for the Dawn of the Fast",
            author = "Bahá'u'lláh",
            category = WritingCategory.FASTING,
            textEnglish = "I beseech Thee, O my God, by Thy mighty Sign, and by the revelation of Thy grace amongst men, to cast me not away from the gate of the city of Thy presence, and to disappoint not the hopes I have set on the manifestations of Thy grace amidst Thy creatures. Thou seest me, O my God, holding to Thy Name, the Most Holy, the Most Luminous, the Most Mighty, the Most Great, the Most Exalted, the Most Glorious, and clinging to the hem of the robe of Thy sanctity...",
            textArabicPersian = "إلهِي إلهِي أَسْئَلُكَ بِآيَتِكَ الْكُبْرَى وَظُهُورِ فَضْلِكَ بَيْنَ الْوَرَى أَنْ لا تَطْرُدَنِي عَنْ بَابِ مَدِينَةِ لِقَائِكَ...",
            sourceReference = "Prayers and Meditations, CLXXVII",
            instructions = "Recited during the dawn hours before sunrise during the Nineteen Day Fast ('Alá')."
        ),
        HolyWriting(
            id = "fast_meditation",
            title = "Meditation for the Fast",
            author = "Bahá'u'lláh",
            category = WritingCategory.FASTING,
            textEnglish = "Praise be unto Thee, O Lord my God! We have observed the Fast in conformity with Thy bidding and do now break it through Thy love and Thy good-pleasure. Deign to accept, O my God, the deeds that we have performed in Thy path wholly for the sake of Thy beauty, with our gazes set towards Thy Cause, unhindered by the names of such as have rebelled against Thee.",
            sourceReference = "Prayers and Meditations, LVI",
            instructions = "Recited at sunset upon the breaking of the Fast."
        ),
        HolyWriting(
            id = "fast_abdulbaha",
            title = "The Purpose of the Fast",
            author = "'Abdu'l-Bahá",
            category = WritingCategory.FASTING,
            textEnglish = "Fasting is a symbol. Fasting signifieth abstinence from lust. Physical fast is a symbol of that abstinence, and is a reminder; that is, just as a person abstaineth from physical appetites, he is to abstain from self-appetites and passions. But mere abstention from food hath no effect on the spirit. It is a mere symbol, a reminder. Otherwise it is of no importance.",
            sourceReference = "Star of the West, Vol. IV",
            instructions = "Spiritual reflection for the Fast."
        ),

        // --- THE HIDDEN WORDS ---
        HolyWriting(
            id = "hw_arabic_1",
            title = "From the Hidden Words (Arabic #1)",
            author = "Bahá'u'lláh",
            category = WritingCategory.HIDDEN_WORDS,
            textEnglish = "O SON OF SPIRIT!\nMy first counsel is this: Possess a pure, kindly and radiant heart, that thine may be a sovereignty ancient, imperishable and everlasting.",
            textArabicPersian = "يَا ابْنَ الرُّوحِ!\nفِي أَوَّلِ الْقَوْلِ امْلِكْ قَلْبًا جَيِّدًا حَسَنًا مُنِيرًا لِتَمْلِكَ مُلْكًا دَائِمًا بَاقِيًا قَدِيمًا أَزَلاً.",
            sourceReference = "The Hidden Words of Bahá'u'lláh, Arabic 1"
        ),
        HolyWriting(
            id = "hw_arabic_2",
            title = "Justice (Arabic #2)",
            author = "Bahá'u'lláh",
            category = WritingCategory.HIDDEN_WORDS,
            textEnglish = "O SON OF SPIRIT!\nThe best beloved of all things in My sight is Justice; turn not away therefrom if thou desirest Me, and neglect it not that I may confide in thee. By its aid thou shalt see with thine own eyes and not through the eyes of others, and shalt know of thine own knowledge and not through the knowledge of thy neighbor. Ponder this in thy heart; how it behooveth thee to be. Verily justice is My gift to thee and the sign of My loving-kindness. Set it then before thine eyes.",
            textArabicPersian = "يَا ابْنَ الرُّوحِ!\nأَحَبُّ الأَشْيَاءِ عِنْدِي الإِنْصَافُ. لا تَرْغَبْ عَنْهُ إِنْ تَكُنْ إِلَيَّ رَاغِبًا وَلا تَغْفُلْ مِنْهُ لِتَكُونَ لِي أَمِينًا...",
            sourceReference = "The Hidden Words of Bahá'u'lláh, Arabic 2"
        ),
        HolyWriting(
            id = "hw_arabic_3",
            title = "Creation & Love (Arabic #3 & #4)",
            author = "Bahá'u'lláh",
            category = WritingCategory.HIDDEN_WORDS,
            textEnglish = "O SON OF MAN!\nVeiled in My immemorial being and in the ancient eternity of My essence, I knew My love for thee; therefore I created thee, have engraved on thee Mine image and revealed to thee My beauty.\n\nO SON OF MAN!\nI loved thy creation, hence I created thee. Wherefore, do thou love Me, that I may name thy name and fill thy soul with the spirit of life.",
            sourceReference = "The Hidden Words of Bahá'u'lláh, Arabic 3-4"
        ),
        HolyWriting(
            id = "hw_persian_1",
            title = "From the Hidden Words (Persian #1)",
            author = "Bahá'u'lláh",
            category = WritingCategory.HIDDEN_WORDS,
            textEnglish = "O YE PEOPLE THAT HAVE MINDS TO KNOW AND EARS TO HEAR!\nThe first call of the Beloved is this: O mystic nightingale! Abide not but in the rose garden of the spirit. O messenger of the Solomon of love! Seek thou no shelter except in the Sheba of the well-beloved, and O immortal phoenix! dwell not save on the mount of faithfulness. Therein is thy true abode, if on the wings of the soul thou soarest unto the realm of the infinite and seekest to reach thy goal.",
            sourceReference = "The Hidden Words of Bahá'u'lláh, Persian 1"
        ),

        // --- HEALING & SOLACE ---
        HolyWriting(
            id = "heal_general",
            title = "Prayer for Healing",
            author = "Bahá'u'lláh",
            category = WritingCategory.HEALING,
            textEnglish = "Thy name is my healing, O my God, and remembrance of Thee is my remedy. Nearness to Thee is my hope, and love for Thee is my companion. Thy mercy to me is my healing and my succor in both this world and the world to come. Thou, verily, art the All-Bountiful, the All-Knowing, the All-Wise.",
            textArabicPersian = "بِسْمِكَ الشَّافِي يَا شَافِي، بِسْمِكَ الْكَافِي يَا كَافِي...\nاسْمُكَ شِفَائِي يَا إِلَهِي وَذِكْرُكَ دَوَائِي وَقُرْبُكَ رَجَائِي وَحُبُّكَ مُؤْنِسِي وَرَحْمَتُكَ طَبِيبِي وَمُعِينِي فِي الدُّنْيَا وَالآخِرَةِ، وَإِنَّكَ أَنْتَ الْمُعْطِي الْعَلِيمُ الْحَكِيمُ.",
            sourceReference = "Prayers and Meditations"
        ),
        HolyWriting(
            id = "heal_long_excerpt",
            title = "The Long Healing Prayer (Excerpt)",
            author = "Bahá'u'lláh",
            category = WritingCategory.HEALING,
            textEnglish = "He is the Healer, the Sufficer, the Helper, the All-Forgiving, the All-Merciful.\n\nI call on Thee O Exalted One, O Refulgent One, O Beauteous One, O Bounteous One!\nThou the Sufficing, Thou the Healing, Thou the Abiding, O Thou Abiding One!\n\nI call on Thee O Sovereign, O Upraiser, O Judge, O Beloved!\nThou the Sufficing, Thou the Healing, Thou the Abiding, O Thou Abiding One!...",
            sourceReference = "Bahá'í Prayers"
        ),

        // --- ASSISTANCE & TESTS ---
        HolyWriting(
            id = "remover_difficulties",
            title = "The Remover of Difficulties",
            author = "The Báb",
            category = WritingCategory.ASSISTANCE,
            textEnglish = "Is there any Remover of difficulties save God? Say: Praised be God! He is God! All are His servants, and all abide by His bidding!",
            textArabicPersian = "هَلْ مِنْ مُفَرِّجٍ لِلْكُرَبِ غَيْرُ اللهِ، قُلْ سُبْحَانَ اللهِ هُوَ اللهُ كُلٌّ عِبَادٌ لَهُ وَكُلٌّ بِأَمْرِهِ قَائِمُونَ.",
            sourceReference = "Selections from the Writings of the Báb"
        ),
        HolyWriting(
            id = "say_god_sufficeth",
            title = "Say: God Sufficeth",
            author = "The Báb",
            category = WritingCategory.ASSISTANCE,
            textEnglish = "Say: God sufficeth all things above all things, and nothing in the heavens or in the earth but God sufficeth. Verily, He is in Himself the Knower, the Sustainer, the Omnipotent.",
            textArabicPersian = "قُلِ اللهُ يَكْفِي كُلَّ شَيْءٍ عَنْ كُلِّ شَيْءٍ وَلا يَكْفِي عَنِ اللهِ شَيْءٌ فِي السَّمَاوَاتِ وَلا فِي الأَرْضِ وَلا مَا بَيْنَهُمَا إِنَّهُ كَانَ عَلِيمًا قَدِيرًا قَيُّومًا.",
            sourceReference = "Selections from the Writings of the Báb"
        ),

        // --- MORNING & EVENING ---
        HolyWriting(
            id = "morning_prayer",
            title = "Morning Prayer",
            author = "Bahá'u'lláh",
            category = WritingCategory.MORNING_EVENING,
            textEnglish = "I have awakened in Thy shelter, O my God, and it becometh him that seeketh that shelter to abide within the Sanctuary of Thy protection and the Stronghold of Thy defense. Illumine my inner being, O my Lord, with the splendors of the Dayspring of Thy Revelation, even as Thou didst illumine my outer being with the morning light of Thy favor.",
            sourceReference = "Prayers and Meditations"
        ),
        HolyWriting(
            id = "evening_prayer",
            title = "Evening Prayer",
            author = "The Báb",
            category = WritingCategory.MORNING_EVENING,
            textEnglish = "I have risen this evening, O my Lord, by Thy grace, and have sought refuge beneath the shadow of Thy protection, and have turned my heart unto Thee, trusting in Thy mercy and depending upon Thy providence. Send down upon me, O my God, out of the heaven of Thy bounty that which will make me independent of all else besides Thee.",
            sourceReference = "Bahá'í Prayers"
        ),

        // --- UNITY & PEACE ---
        HolyWriting(
            id = "unity_all_men",
            title = "The Tabernacle of Unity",
            author = "Bahá'u'lláh",
            category = WritingCategory.UNITY_PEACE,
            textEnglish = "The tabernacle of unity hath been raised; regard ye not one another as strangers. Ye are the fruits of one tree, and the leaves of one branch. We cherish the hope that the light of justice may shine upon the world and sanctify it from tyranny.\n\nConsort with the followers of all religions in a spirit of friendliness and fellowship.",
            sourceReference = "Gleanings from the Writings of Bahá'u'lláh, CXII"
        ),
        HolyWriting(
            id = "unity_prayer_abdulbaha",
            title = "Prayer for All Mankind",
            author = "'Abdu'l-Bahá",
            category = WritingCategory.UNITY_PEACE,
            textEnglish = "O Thou kind Lord! Thou hast created all humanity from the same stock. Thou hast decreed that all shall belong to the same household. In Thy holy Presence they are all Thy servants, and all mankind are sheltered beneath Thy Tabernacle; all have gathered at Thy Table of Bounty; all are illumined through the light of Thy Providence.\n\nO God! Raise aloft the banner of the oneness of mankind. O God! Establish the Most Great Peace.",
            sourceReference = "Promulgation of Universal Peace"
        ),

        // --- HOLY DAYS & TABLETS ---
        HolyWriting(
            id = "tablet_of_ahmad",
            title = "Tablet of Aḥmad",
            author = "Bahá'u'lláh",
            category = WritingCategory.HOLY_DAYS,
            textEnglish = "He is the King, the All-Knowing, the Wise!\n\nLo, the Nightingale of Paradise singeth upon the twigs of the Tree of Eternity, with holy and sweet melodies, proclaiming to the sincere ones the glad tidings of the nearness of God, calling the believers in the Divine Unity to the court of the Presence of the Generous One...\n\nO Aḥmad! Forget not My bounties while I am absent. Remember My days during thy days, and My distress and banishment in this remote prison. And be thou so steadfast in My love that thy heart shall not waver, even if the swords of the enemies rain blows upon thee and all the heavens and the earth arise against thee...\n\nLearn well this Tablet, O Aḥmad. Chant it during thy days and withhold not thyself therefrom. For verily, God hath ordained for the one who chanteth it, the reward of a hundred martyrs and a service in both worlds.",
            sourceReference = "Bahá'í Prayers"
        ),
        HolyWriting(
            id = "tablet_of_visitation",
            title = "Tablet of Visitation",
            author = "Bahá'u'lláh",
            category = WritingCategory.HOLY_DAYS,
            textEnglish = "The praise which hath dawned from Thy most august Self, and the glory which hath shone forth from Thy most resplendent Beauty, rest upon Thee, O Thou Who art the Manifestation of the Grandeur, and the King of Eternity, and the Lord of all who are in heaven and on earth!\n\nI testify that through Thee the sovereignty of God and His dominion, and the majesty of God and His grandeur, were revealed, and the Day-Stars of ancient splendor have shed their radiance in the heaven of Thine irrevocable decree, and the Beauty of the Unseen One hath illumined the empyrean of creation...",
            sourceReference = "Recited at the Shrines of Bahá'u'lláh and the Báb, and during Holy Day commemorations."
        )
    )

    val PRESET_PROGRAMS: List<DevotionalProgram> = listOf(
        DevotionalProgram(
            id = "prog_dawn_fast",
            title = "Dawn Devotional & The Fast",
            description = "Reflective readings and prayers for the dawn hours before sunrise and during the Nineteen Day Fast.",
            themeEmoji = "🌅",
            writingIds = listOf("morning_prayer", "fast_dawn_1", "hw_arabic_1", "fast_abdulbaha", "say_god_sufficeth")
        ),
        DevotionalProgram(
            id = "prog_feast",
            title = "Nineteen Day Feast Devotions",
            description = "Spiritual portion for the Nineteen Day Feast gatherings featuring praise, unity, and consultation reflections.",
            themeEmoji = "🕊️",
            writingIds = listOf("hw_arabic_2", "unity_all_men", "unity_prayer_abdulbaha", "remover_difficulties")
        ),
        DevotionalProgram(
            id = "prog_healing",
            title = "Healing & Spiritual Solace",
            description = "Devotional selections dedicated to physical and spiritual healing, comfort, and peace.",
            themeEmoji = "🌿",
            writingIds = listOf("heal_general", "heal_long_excerpt", "say_god_sufficeth", "hw_arabic_1")
        ),
        DevotionalProgram(
            id = "prog_holy_day",
            title = "Holy Day Commemoration",
            description = "Solemn readings for Holy Day commemorations and Ascension anniversaries.",
            themeEmoji = "📜",
            writingIds = listOf("tablet_of_ahmad", "hw_persian_1", "tablet_of_visitation")
        )
    )
}
