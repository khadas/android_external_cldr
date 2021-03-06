package org.unicode.cldr.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.util.ULocale;

/**
 * This is a first-cut version just to get going. The data is hard coded until we switch over to real data in ICU.
 */
public class DayPeriodsOld {
    private static final int HOUR = 60 * 60 * 1000;

    public enum DayPeriod {
        MORNING1("EARLY_MORNING"), MORNING2("MORNING"), AFTERNOON1("EARLY_AFTERNOON"), AFTERNOON2("AFTERNOON"), EVENING1("EARLY_EVENING"), EVENING2(
            "EVENING"), NIGHT1("NIGHT"), NIGHT2("LATE_NIGHT");
        public final String name;

        DayPeriod(String name) {
            this.name = name;
        }

        public static DayPeriod get(String dayPeriod) {
            for (DayPeriod d : DayPeriod.values()) {
                if (dayPeriod.equals(d.name)) {
                    return d;
                }
            }
            return DayPeriod.valueOf(dayPeriod);
        }
    }

    /**
     * Get the category for a given time in the day.
     * @param millisInDay
     * @return
     */
    public DayPeriod get(long millisInDay) {
        long hours = millisInDay / HOUR;
        int hoursInDay = (int) (hours % 24);
        if (hoursInDay < 0) {
            hoursInDay += 24;
        }
        return timeMap[hoursInDay];
    }

    /**
     * Get the *actual* locale for the DayPeriods (eg, asking for "en-AU" may get you "en")
     * @param millisInDay
     * @return
     */
    public ULocale getLocale() {
        return locale;
    }

    /**
     * Get a sample, for showing to a localizer. The actual phrase should come out of a SELECT statement, since it may vary by message.
     * @param millisInDay
     * @return
     */
    public String getSample(DayPeriod dayPeriod) {
        return samples.get(dayPeriod);
    }

    /**
     * Return the possible DayPeriod values for this locale.
     * @return
     */
    public Set<DayPeriod> getDayPeriods() {
        return samples.keySet();
    }

    /**
     * Get an instance with a factory method. Right now, returns null if the locale data is not available.
     * @param loc
     * @return
     */
    public static DayPeriodsOld getInstance(ULocale loc) {
        ULocale base = new ULocale(loc.getLanguage());
        DayPeriodsOld result = DATA.get(base);
//        if (result == null) {
//            throw new IllegalArgumentException("No data for locale " + loc);
//        }
        return result;
    }

    /**
     * Returns the available locales. Note that regional/script variants may be mapped by getInstance to a base locale,
     * eg, en-AU => en.
     * @return
     */
    public static Set<ULocale> getAvailable() {
        return DATA.keySet();
    }

    // ===== PRIVATES =====

    private final ULocale locale;
    private final DayPeriod[] timeMap;
    private final Map<DayPeriod, String> samples;

    private DayPeriodsOld(ULocale base, DayPeriod[] map, EnumMap<DayPeriod, String> samples2) {
        locale = base;
        fix(map, samples2, DayPeriod.MORNING2, DayPeriod.MORNING1);
        fix(map, samples2, DayPeriod.AFTERNOON2, DayPeriod.AFTERNOON1);
        fix(map, samples2, DayPeriod.EVENING2, DayPeriod.EVENING1);
        fix(map, samples2, DayPeriod.NIGHT2, DayPeriod.NIGHT1);
        timeMap = map;
        samples = Collections.unmodifiableMap(samples2);
    }

    private void fix(DayPeriod[] map, EnumMap<DayPeriod, String> samples2, DayPeriod dayPeriod2, DayPeriod dayPeriod1) {
        if (samples2.containsKey(dayPeriod2) && !samples2.containsKey(dayPeriod1)) {
            samples2.put(dayPeriod1, samples2.get(dayPeriod2));
            samples2.remove(dayPeriod2);
            for (int i = 0; i < map.length; ++i) {
                if (map[i] == dayPeriod2) {
                    map[i] = dayPeriod1;
                }
            }
        }
    }

    // HACK TO SET UP DATA
    // Will be replaced by real data table in the future

    private static final Map<ULocale, DayPeriodsOld> DATA = new LinkedHashMap<>();

    private static DayPeriodBuilder make(String locale) {
        return new DayPeriodBuilder(locale);
    }

    private static class DayPeriodBuilder {
        private final ULocale locale;
        private final DayPeriod[] timeMap = new DayPeriod[24];
        private final EnumMap<DayPeriod, String> samples = new EnumMap<>(DayPeriod.class);

        DayPeriodBuilder(String locale) {
            this.locale = new ULocale(locale);
        }

        public DayPeriodBuilder add(String dayPeriod, String localeName, int... hours) {
            DayPeriod dayPeriodEnum = DayPeriod.get(dayPeriod);
            String previous = samples.put(dayPeriodEnum, localeName);
            if (previous != null) {
                throw new IllegalArgumentException(locale + " Collision");
            }
            for (int i : hours) {
                if (timeMap[i] != null) {
                    throw new IllegalArgumentException(locale + " Collision " + i + ", " + timeMap[i] + ", " + dayPeriodEnum);
                }
                timeMap[i] = dayPeriodEnum;
            }
            return this;
        }

        public DayPeriodsOld build() {
            for (int i = 0; i < timeMap.length; ++i) {
                DayPeriod dp = timeMap[i];
                if (dp == null) {
                    throw new IllegalArgumentException(locale + " Missing item: " + i);
                }
            }
            DayPeriodsOld item = new DayPeriodsOld(locale, timeMap, samples);
            DATA.put(locale, item);
            return item;
        }
    }

    static {
        make("en")
            .add("NIGHT1", "night", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .add("MORNING1", "morning", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "afternoon", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "evening", 18, 19, 20)
            .build();

        make("af")
            .add("MORNING", "oggend", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "middag", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "aand", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "nag", 0, 1, 2, 3, 4)
            .build();

        make("nl")
            .add("MORNING", "ochtend", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "middag", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "avond", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "nacht", 0, 1, 2, 3, 4, 5)
            .build();

        make("de")
            .add("EARLY_MORNING", "Morgen", 5, 6, 7, 8, 9)
            .add("MORNING", "Vormittag", 10, 11)
            .add("AFTERNOON", "Mittag", 12)
            .add("EVENING", "Nachmittag", 13, 14, 15, 16, 17)
            .add("NIGHT", "Abend", 18, 19, 20, 21, 22, 23)
            .add("LATE_NIGHT", "Nacht", 0, 1, 2, 3, 4)
            .build();

        make("da")
            .add("EARLY_MORNING", "morgen", 5, 6, 7, 8, 9)
            .add("MORNING", "formiddag", 10, 11)
            .add("AFTERNOON", "eftermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "aften", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "nat", 0, 1, 2, 3, 4)
            .build();

        make("nb")
            .add("EARLY_MORNING", "morgen", 6, 7, 8, 9)
            .add("MORNING", "formiddag", 10, 11)
            .add("AFTERNOON", "ettermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "kveld", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "natt", 0, 1, 2, 3, 4, 5)
            .build();

        make("sv")
            .add("EARLY_MORNING", "morgon", 5, 6, 7, 8, 9)
            .add("MORNING", "f??rmiddag", 10, 11)
            .add("AFTERNOON", "eftermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "kv??ll", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "natt", 0, 1, 2, 3, 4)
            .build();

        make("is")
            .add("MORNING", "morgunn", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "eftir h??degi", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "kv??ld", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "n??tt", 0, 1, 2, 3, 4, 5)
            .build();

        make("pt")
            .add("MORNING", "manh??", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "tarde", 12, 13, 14, 15, 16, 17, 18)
            .add("EVENING", "noite", 19, 20, 21, 22, 23)
            .add("NIGHT", "madrugada", 0, 1, 2, 3, 4, 5)
            .build();

        make("gl")
            .add("EARLY_MORNING", "madrugada", 0, 1, 2, 3, 4, 5)
            .add("MORNING", "ma????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "mediod??a", 12)
            .add("EVENING", "tarde", 13, 14, 15, 16, 17, 18, 19, 20)
            .add("NIGHT", "noite", 21, 22, 23)
            .build();

        make("es")
            .add("MORNING", "ma??ana", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "tarde", 12, 13, 14, 15, 16, 17, 18, 19)
            .add("EVENING", "noche", 20, 21, 22, 23)
            .add("NIGHT", "madrugada", 0, 1, 2, 3, 4, 5)
            .build();

        make("ca")
            .add("EARLY_MORNING", "matinada", 0, 1, 2, 3, 4, 5)
            .add("MORNING", "mat??", 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "migdia", 12)
            .add("AFTERNOON", "tarda", 13, 14, 15, 16, 17, 18)
            .add("EVENING", "vespre", 19, 20)
            .add("NIGHT", "nit", 21, 22, 23)
            .build();

        make("it")
            .add("MORNING", "mattina", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "pomeriggio", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "sera", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "notte", 0, 1, 2, 3, 4, 5)
            .build();

        make("ro")
            .add("MORNING", "diminea????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "dup??-amiaz??", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "sear??", 18, 19, 20, 21)
            .add("NIGHT", "noapte", 0, 1, 2, 3, 4, 22, 23)
            .build();

        make("fr")
            .add("MORNING", "matin", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "apr??s-midi", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "soir", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "nuit", 0, 1, 2, 3)
            .build();

        make("hr")
            .add("MORNING", "jutro", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "popodne", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ve??er", 18, 19, 20)
            .add("NIGHT", "no??", 0, 1, 2, 3, 21, 22, 23)
            .build();

        make("bs")
            .add("MORNING", "jutro", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "popodne", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ve??e", 18, 19, 20)
            .add("NIGHT", "no??", 0, 1, 2, 3, 21, 22, 23)
            .build();

        make("sr")
            .add("MORNING", "??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "????????", 18, 19, 20)
            .add("NIGHT", "??????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("sl")
            .add("EARLY_MORNING", "jutro", 6, 7, 8, 9)
            .add("MORNING", "dopoldne", 10, 11)
            .add("AFTERNOON", "popoldne", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ve??er", 18, 19, 20, 21)
            .add("NIGHT", "no??", 0, 1, 2, 3, 4, 5, 22, 23)
            .build();

        make("cs")
            .add("EARLY_MORNING", "r??no", 4, 5, 6, 7, 8)
            .add("MORNING", "dopoledne", 9, 10, 11)
            .add("AFTERNOON", "odpoledne", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ve??er", 18, 19, 20, 21)
            .add("NIGHT", "noc", 0, 1, 2, 3, 22, 23)
            .build();

        make("sk")
            .add("EARLY_MORNING", "r??no", 4, 5, 6, 7, 8)
            .add("MORNING", "dopoludnie", 9, 10, 11)
            .add("AFTERNOON", "popoludnie", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ve??er", 18, 19, 20, 21)
            .add("NIGHT", "noc", 0, 1, 2, 3, 22, 23)
            .build();

        make("pl")
            .add("EARLY_MORNING", "rano", 6, 7, 8, 9)
            .add("MORNING", "przedpo??udnie", 10, 11)
            .add("AFTERNOON", "popo??udnie", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "wiecz??r", 18, 19, 20)
            .add("NIGHT", "noc", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("bg")
            .add("MORNING", "????????????????", 4, 5, 6, 7, 8, 9, 10)
            .add("EARLY_AFTERNOON", "???? ????????", 11, 12, 13)
            .add("AFTERNOON", "????????????????", 14, 15, 16, 17)
            .add("EVENING", "??????????????", 18, 19, 20, 21)
            .add("NIGHT", "??????", 0, 1, 2, 3, 22, 23)
            .build();

        make("mk")
            .add("EARLY_MORNING", "????????????", 4, 5, 6, 7, 8, 9)
            .add("MORNING", "????????????????????", 10, 11)
            .add("AFTERNOON", "????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????????????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "???? ????????????", 0, 1, 2, 3)
            .build();

        make("ru")
            .add("MORNING", "????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "????????", 0, 1, 2, 3)
            .build();

        make("uk")
            .add("MORNING", "??????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "??????", 0, 1, 2, 3)
            .build();

        make("lt")
            .add("MORNING", "rytas", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "diena", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "vakaras", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "naktis", 0, 1, 2, 3, 4, 5)
            .build();

        make("lv")
            .add("MORNING", "r??ts", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "p??cpusdiena", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "vakars", 18, 19, 20, 21, 22)
            .add("NIGHT", "nakts", 0, 1, 2, 3, 4, 5, 23)
            .build();

        make("el")
            .add("EARLY_MORNING", "??????????????????", 0)
            .add("MORNING", "????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????????", 12, 13, 14, 15, 16)
            .add("EVENING", "????????????????", 17, 18, 19)
            .add("NIGHT", "??????????", 1, 2, 3, 20, 21, 22, 23)
            .build();

        make("fa")
            .add("MORNING", "??????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "?????? ???? ??????", 12, 13, 14, 15, 16)
            .add("EVENING", "??????", 17, 18)
            .add("NIGHT", "????", 0, 1, 2, 3, 19, 20, 21, 22, 23)
            .build();

        make("hy")
            .add("MORNING", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "??????????", 0, 1, 2, 3, 4, 5)
            .build();

        make("ka")
            .add("MORNING", "????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "??????????????? ????????????????????????", 12, 13, 14, 15, 16)
            .add("AFTERNOON", "??????????????? ????????????????????????", 17)
            .add("EVENING", "??????????????????", 18, 19, 20)
            .add("NIGHT", "????????????", 0, 1, 2, 3, 4, 21, 22, 23)
            .build();

        make("sq")
            .add("EARLY_MORNING", "m??ngjes", 4, 5, 6, 7, 8)
            .add("MORNING", "paradite", 9, 10, 11)
            .add("AFTERNOON", "pasdite", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "mbr??mje", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "nat??", 0, 1, 2, 3)
            .build();

        make("ur")
            .add("MORNING", "??????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "??????????", 12, 13, 14, 15)
            .add("AFTERNOON", "???? ??????", 16, 17)
            .add("EVENING", "??????", 18, 19)
            .add("NIGHT", "??????", 0, 1, 2, 3, 20, 21, 22, 23)
            .build();

        make("hi")
            .add("MORNING", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "???????????????", 12, 13, 14, 15)
            .add("EVENING", "?????????", 16, 17, 18, 19)
            .add("NIGHT", "?????????", 0, 1, 2, 3, 20, 21, 22, 23)
            .build();

        make("bn")
            .add("EARLY_MORNING", "?????????", 4, 5)
            .add("MORNING", "????????????", 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "???????????????", 12, 13, 14, 15)
            .add("AFTERNOON", "???????????????", 16, 17)
            .add("EVENING", "?????????????????????", 18, 19)
            .add("NIGHT", "??????????????????", 0, 1, 2, 3, 20, 21, 22, 23)
            .build();

        make("gu")
            .add("MORNING", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????", 12, 13, 14, 15)
            .add("EVENING", "????????????", 16, 17, 18, 19)
            .add("NIGHT", "?????????", 0, 1, 2, 3, 20, 21, 22, 23)
            .build();

        make("mr")
            .add("EARLY_MORNING", "???????????????", 4, 5)
            .add("MORNING", "???????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????????????????", 12, 13, 14, 15)
            .add("EVENING", "??????????????????????????????", 16, 17, 18, 19)
            .add("NIGHT", "??????????????????", 0, 1, 2, 20, 21, 22, 23)
            .add("LATE_NIGHT", "???????????????", 3)
            .build();

        make("ne")
            .add("MORNING", "???????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "?????????????????????", 12, 13, 14, 15)
            .add("AFTERNOON", "????????????", 16, 17, 18)
            .add("EVENING", "??????????????????", 19, 20, 21)
            .add("NIGHT", "?????????", 0, 1, 2, 3, 22, 23)
            .build();

        make("pa")
            .add("MORNING", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????????????????", 12, 13, 14, 15)
            .add("EVENING", "?????????", 16, 17, 18, 19, 20)
            .add("NIGHT", "?????????", 0, 1, 2, 3, 21, 22, 23)
            .build();

        make("si")
            .add("EARLY_MORNING", "??????????????????", 1, 2, 3, 4, 5)
            .add("MORNING", "?????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????", 12, 13)
            .add("EVENING", "?????????", 14, 15, 16, 17)
            .add("NIGHT", "??????", 18, 19, 20, 21, 22, 23)
            .add("LATE_NIGHT", "????????????????????? ?????????", 0)
            .build();

        make("ta")
            .add("EARLY_MORNING", "?????????????????????", 3, 4)
            .add("MORNING", "????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????????????????", 12, 13, 16, 17)
            .add("EVENING", "????????????", 14, 15, 18, 19, 20)
            .add("NIGHT", "????????????", 0, 1, 2, 21, 22, 23)
            .build();

        make("te")
            .add("MORNING", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "???????????????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "????????????????????????", 18, 19, 20)
            .add("NIGHT", "??????????????????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("ml")
            .add("EARLY_MORNING", "????????????????????????", 3, 4, 5)
            .add("MORNING", "??????????????????", 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "??????????????????????????????", 12, 13)
            .add("AFTERNOON", "????????????????????????????????????", 14)
            .add("EARLY_EVENING", "??????????????????????????????", 15, 16, 17)
            .add("EVENING", "????????????????????????????????????", 18)
            .add("NIGHT", "??????????????????", 0, 1, 2, 19, 20, 21, 22, 23)
            .build();

        make("kn")
            .add("MORNING", "?????????????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "????????????", 18, 19, 20)
            .add("NIGHT", "??????????????????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("zh")
            .add("EARLY_MORNING", "??????", 5, 6, 7)
            .add("MORNING", "??????", 8, 9, 10, 11)
            .add("AFTERNOON", "??????", 12)
            .add("EVENING", "??????", 13, 14, 15, 16, 17, 18)
            .add("NIGHT", "??????", 19, 20, 21, 22, 23)
            .add("LATE_NIGHT", "??????", 0, 1, 2, 3, 4)
            .build();

        make("ja")
            .add("EARLY_MORNING", "???", 6, 7, 8)
            .add("MORNING", "??????", 9, 10, 11)
            .add("EARLY_AFTERNOON", "??????", 12, 13, 14, 15)
            .add("AFTERNOON", "??????", 16, 17, 18)
            .add("EVENING", "???", 19, 20, 21, 22)
            .add("NIGHT", "??????", 0, 1, 2, 3, 23)
            .add("LATE_NIGHT", "?????????", 4, 5)
            .build();

        make("ko")
            .add("EARLY_MORNING", "??????", 3, 4, 5)
            .add("MORNING", "??????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????", 18, 19, 20)
            .add("NIGHT", "???", 0, 1, 2, 21, 22, 23)
            .build();

        make("tr")
            .add("EARLY_MORNING", "sabah", 6, 7, 8, 9, 10)
            .add("MORNING", "????leden ??nce", 11)
            .add("EARLY_AFTERNOON", "????leden sonra", 12, 13, 14, 15, 16, 17)
            .add("AFTERNOON", "ak??am??st??", 18)
            .add("EVENING", "ak??am", 19, 20)
            .add("NIGHT", "gece", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("az")
            .add("EARLY_MORNING", "s??bh", 4, 5)
            .add("MORNING", "s??h??r", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "g??nd??z", 12, 13, 14, 15, 16)
            .add("EVENING", "ax??am??st??", 17, 18)
            .add("NIGHT", "ax??am", 19, 20, 21, 22, 23)
            .add("LATE_NIGHT", "gec??", 0, 1, 2, 3)
            .build();

        make("kk")
            .add("MORNING", "??????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????", 18, 19, 20)
            .add("NIGHT", "??????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("ky")
            .add("MORNING", "?????????? ??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "???????????? ??????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "????????????????", 18, 19, 20)
            .add("NIGHT", "??????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("uz")
            .add("MORNING", "ertalab", 6, 7, 8, 9, 10)
            .add("AFTERNOON", "kunduz", 11, 12, 13, 14, 15, 16, 17)
            .add("EVENING", "kechqurun", 18, 19, 20, 21)
            .add("NIGHT", "tun", 0, 1, 2, 3, 4, 5, 22, 23)
            .build();

        make("et")
            .add("MORNING", "hommik", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "p??rastl??una", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??htu", 18, 19, 20, 21, 22)
            .add("NIGHT", "????", 0, 1, 2, 3, 4, 23)
            .build();

        make("fi")
            .add("EARLY_MORNING", "aamu", 5, 6, 7, 8, 9)
            .add("MORNING", "aamup??iv??", 10, 11)
            .add("AFTERNOON", "iltap??iv??", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "ilta", 18, 19, 20, 21, 22)
            .add("NIGHT", "y??", 0, 1, 2, 3, 4, 23)
            .build();

        make("hu")
            .add("EARLY_MORNING", "reggel", 6, 7, 8)
            .add("MORNING", "d??lel??tt", 9, 10, 11)
            .add("AFTERNOON", "d??lut??n", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "este", 18, 19, 20)
            .add("NIGHT", "??jjel", 0, 1, 2, 3, 21, 22, 23)
            .add("LATE_NIGHT", "hajnal", 4, 5)
            .build();

        make("th")
            .add("MORNING", "????????????", 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "??????????????????", 12)
            .add("AFTERNOON", "????????????", 13, 14, 15)
            .add("EARLY_EVENING", "????????????", 16, 17)
            .add("EVENING", "?????????", 18, 19, 20)
            .add("NIGHT", "?????????????????????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("lo")
            .add("EARLY_MORNING", "????????????????????????", 0, 1, 2, 3, 4)
            .add("MORNING", "??????????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "???????????????", 12, 13, 14, 15)
            .add("AFTERNOON", "?????????", 16)
            .add("EVENING", "????????????", 17, 18, 19)
            .add("NIGHT", "????????????", 20, 21, 22, 23)
            .build();

        make("ar")
            .add("EARLY_MORNING", "????????", 3, 4, 5)
            .add("MORNING", "??????????", 6, 7, 8, 9, 10, 11)
            .add("EARLY_AFTERNOON", "????????", 12)
            .add("AFTERNOON", "?????? ??????????", 13, 14, 15, 16, 17)
            .add("EVENING", "????????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", " ?????????? ??????????", 0)
            .add("LATE_NIGHT", "????????", 1, 2)
            .build();

        make("he")
            .add("MORNING", "????????", 5, 6, 7, 8, 9, 10)
            .add("EARLY_AFTERNOON", "????????????", 11, 12, 13, 14)
            .add("AFTERNOON", "?????? ??????????????", 15, 16, 17)
            .add("EVENING", "??????", 18, 19, 20, 21)
            .add("NIGHT", "????????", 0, 1, 2, 3, 4, 22, 23)
            .build();

        make("id")
            .add("MORNING", "pagi", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            .add("AFTERNOON", "siang", 10, 11, 12, 13, 14)
            .add("EVENING", "sore", 15, 16, 17)
            .add("NIGHT", "malam", 18, 19, 20, 21, 22, 23)
            .build();

        make("ms")
            .add("EARLY_MORNING", "tengah malam", 0)
            .add("MORNING", "pagi", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "tengah hari", 12, 13)
            .add("EVENING", "petang", 14, 15, 16, 17, 18)
            .add("NIGHT", "malam", 19, 20, 21, 22, 23)
            .build();

        make("fil")
            .add("EARLY_MORNING", "madaling-araw", 0, 1, 2, 3, 4, 5)
            .add("MORNING", "umaga", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "tanghali", 12, 13, 14, 15)
            .add("EVENING", "hapon", 16, 17)
            .add("NIGHT", "gabi", 18, 19, 20, 21, 22, 23)
            .build();

        make("vi")
            .add("MORNING", "s??ng", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "chi???u", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "t???i", 18, 19, 20)
            .add("NIGHT", "????m", 0, 1, 2, 3, 21, 22, 23)
            .build();

        make("km")
            .add("MORNING", "???????????????", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "???????????????", 18, 19, 20)
            .add("NIGHT", "?????????", 21, 22, 23)
            .build();

        make("sw")
            .add("EARLY_MORNING", "alfajiri", 4, 5, 6)
            .add("MORNING", "asubuhi", 7, 8, 9, 10, 11)
            .add("AFTERNOON", "mchana", 12, 13, 14, 15)
            .add("EVENING", "jioni", 16, 17, 18)
            .add("NIGHT", "usiku", 0, 1, 2, 3, 19, 20, 21, 22, 23)
            .build();

        make("zu")
            .add("EARLY_MORNING", "ntathakusa", 0, 1, 2, 3, 4, 5)
            .add("MORNING", "ekuseni", 6, 7, 8, 9)
            .add("AFTERNOON", "emini", 10, 11, 12)
            .add("EVENING", "ntambama", 13, 14, 15, 16, 17, 18)
            .add("NIGHT", "ebusuku", 19, 20, 21, 22, 23)
            .build();

        make("am")
            .add("MORNING", "?????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "???????????? ?????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "??????", 18, 19, 20, 21, 22, 23)
            .add("NIGHT", "?????????", 0, 1, 2, 3, 4, 5)
            .build();

        make("eu")
            .add("EARLY_MORNING", "goizaldea", 0, 1, 2, 3, 4, 5)
            .add("MORNING", "goiza", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "eguerdia", 12, 13)
            .add("EVENING", "arratsaldea", 14, 15, 16, 17, 18, 19, 20)
            .add("NIGHT", "gaua", 21, 22, 23)
            .build();

        make("mn")
            .add("MORNING", "??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING", "????????", 18, 19, 20)
            .add("NIGHT", "????????", 0, 1, 2, 3, 4, 5, 21, 22, 23)
            .build();

        make("my")
            .add("MORNING", "???????????????", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON", "??????????????????", 12, 13, 14, 15)
            .add("EVENING", "?????????", 16, 17, 18)
            .add("NIGHT", "???", 19, 20, 21, 22, 23)
            .build();
    }
}
