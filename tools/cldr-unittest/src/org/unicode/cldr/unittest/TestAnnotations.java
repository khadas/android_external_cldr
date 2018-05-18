package org.unicode.cldr.unittest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.unicode.cldr.util.Annotations;
import org.unicode.cldr.util.Annotations.AnnotationSet;
import org.unicode.cldr.util.CLDRConfig;
import org.unicode.cldr.util.CLDRFile;
import org.unicode.cldr.util.CLDRPaths;
import org.unicode.cldr.util.Counter;
import org.unicode.cldr.util.Emoji;
import org.unicode.cldr.util.Factory;
import org.unicode.cldr.util.PathHeader;
import org.unicode.cldr.util.PathHeader.PageId;
import org.unicode.cldr.util.PathHeader.SectionId;
import org.unicode.cldr.util.SimpleFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.ibm.icu.dev.util.CollectionUtilities;
import com.ibm.icu.dev.util.UnicodeMap;
import com.ibm.icu.dev.util.UnicodeMap.EntryRange;
import com.ibm.icu.text.UnicodeSet;

public class TestAnnotations extends TestFmwkPlus {
    public static void main(String[] args) {
        new TestAnnotations().run(args);
    }

    enum Containment {
        contains, empty, not_contains
    }

    public void TestBasic() {
        String[][] tests = {
            { "en", "[\u2650]", "contains", "sagitarius", "zodiac" },
            { "en", "[\u0020]", "empty" },
            { "en", "[\u2651]", "not_contains", "foobar" },
        };
        for (String[] test : tests) {
            UnicodeMap<Annotations> data = Annotations.getData(test[0]);
            UnicodeSet us = new UnicodeSet(test[1]);
            Set<String> annotations = new LinkedHashSet<>();
            Containment contains = Containment.valueOf(test[2]);
            for (int i = 3; i < test.length; ++i) {
                annotations.add(test[i]);
            }
            for (String s : us) {
                Set<String> set = data.get(s).getKeywords();
                if (set == null) {
                    set = Collections.emptySet();
                }
                switch (contains) {
                case contains:
                    if (Collections.disjoint(set, annotations)) {
                        LinkedHashSet<String> temp = new LinkedHashSet<>(annotations);
                        temp.removeAll(set);
                        assertEquals("Missing items", Collections.EMPTY_SET, temp);
                    }
                    break;
                case not_contains:
                    if (!Collections.disjoint(set, annotations)) {
                        LinkedHashSet<String> temp = new LinkedHashSet<>(annotations);
                        temp.retainAll(set);
                        assertEquals("Extra items", Collections.EMPTY_SET, temp);
                    }
                    break;
                case empty:
                    assertEquals("mismatch", Collections.emptySet(), set);
                    break;
                }
            }
        }
    }

    public void TestList() {
        if (isVerbose()) {
            for (String locale : Annotations.getAvailable()) {
                for (EntryRange<Annotations> s : Annotations.getData(locale).entryRanges()) {
                    logln(s.toString());
                }
            }
        }
    }

    public void TestNames() {
        AnnotationSet eng = Annotations.getDataSet("en");
        String[][] tests = {
            {"🇪🇺","European Union","flag"},
            {"#️⃣","keycap: #","keycap"},
            {"9️⃣","keycap: 9","keycap"},
            {"💏","kiss","couple | kiss"},
            {"👩‍❤️‍💋‍👩","kiss: woman, woman","couple | kiss | woman"},
            {"💑","couple with heart","couple | couple with heart | love"},
            {"👩‍❤️‍👩","couple with heart: woman, woman","couple | couple with heart | love | woman"},
            {"👪","family","family"},
            {"👩‍👩‍👧","family: woman, woman, girl","family | woman | girl"},
            {"👦🏻","boy: light skin tone","boy | young | light skin tone"},
            {"👩🏿","woman: dark skin tone","woman | dark skin tone"},
            {"👨‍⚖","man judge","justice | man | man judge | scales"},
            {"👨🏿‍⚖","man judge: dark skin tone","justice | man | man judge | scales | dark skin tone"},
            {"👩‍⚖","woman judge","judge | scales | woman | woman judge"},
            {"👩🏼‍⚖","woman judge: medium-light skin tone","judge | scales | woman | woman judge | medium-light skin tone"},
            {"👮","police officer","cop | officer | police | police officer"},
            {"👮🏿","police officer: dark skin tone","cop | officer | police | police officer | dark skin tone"},
            {"👮‍♂️","man police officer","cop | man | man police officer | officer | police"},
            {"👮🏼‍♂️","man police officer: medium-light skin tone","cop | man | man police officer | officer | police | medium-light skin tone"},
            {"👮‍♀️","woman police officer","cop | officer | police | woman | woman police officer"},
            {"👮🏿‍♀️","woman police officer: dark skin tone","cop | officer | police | woman | woman police officer | dark skin tone"},
            {"🚴","person biking","bicycle | biking | cyclist | person biking"},
            {"🚴🏿","person biking: dark skin tone","bicycle | biking | cyclist | person biking | dark skin tone"},
            {"🚴‍♂️","man biking","bicycle | biking | cyclist | man | man biking"},
            {"🚴🏿‍♂️","man biking: dark skin tone","bicycle | biking | cyclist | man | man biking | dark skin tone"},
            {"🚴‍♀️","woman biking","bicycle | biking | cyclist | woman | woman biking"},
            {"🚴🏿‍♀️","woman biking: dark skin tone","bicycle | biking | cyclist | woman | woman biking | dark skin tone"},
        };

        Splitter BAR = Splitter.on('|').trimResults();
        boolean ok = true;
        for (String[] test : tests) {
            String emoji = test[0];
            String expectedName = test[1];
            Set<String> expectedKeywords = new HashSet<>(BAR.splitToList(test[2]));
            final String shortName = eng.getShortName(emoji);
            final Set<String> keywords = eng.getKeywords(emoji);
            ok &=assertEquals("short name for " + emoji, expectedName, shortName);
            ok &= assertEquals("keywords for " + emoji, expectedKeywords, keywords);
        }
        if (!ok) {
            System.out.println("Possible replacement, but check");
            for (String[] test : tests) {
                String emoji = test[0];
                final String shortName = eng.getShortName(emoji);
                final Set<String> keywords = eng.getKeywords(emoji);
                System.out.println("{\"" + emoji 
                    + "\",\"" + shortName 
                    + "\",\"" + CollectionUtilities.join(keywords, " | ") 
                    + "\"},");
            }
        }

    }

    // comment this out, since we now have console check for this.
    public void oldTestUniqueness() {
//        if (logKnownIssue("cldrbug:10104", "Disable until the uniqueness problems are fixed")) {
//            return;
//        }
        LinkedHashSet<String> locales = new LinkedHashSet<>();
        locales.add("en");
        locales.addAll(Annotations.getAvailable());
        locales.remove("root");
        locales.remove("sr_Latn");
        Multimap<String, String> localeToNameToEmoji = TreeMultimap.create();
        Multimap<String, String> nameToEmoji = TreeMultimap.create();
        UnicodeMap<Annotations> english = Annotations.getData("en");
        UnicodeSet englishKeys = getCurrent(english.keySet());
        Map<String, UnicodeSet> localeToMissing = new TreeMap<>();

        for (String locale : locales) {
            UnicodeMap<Annotations> data = Annotations.getData(locale);
            nameToEmoji.clear();
            localeToMissing.put(locale, new UnicodeSet(englishKeys).removeAll(data.keySet()).freeze());
            for (Entry<String, Annotations> value : data.entrySet()) {
                String emoji = value.getKey();
                String name = value.getValue().getShortName();
                if (name == null) {
                    continue;
                }
                nameToEmoji.put(name, emoji);
            }
            for ( Entry<String, Collection<String>> entry : nameToEmoji.asMap().entrySet()) {
                String name = entry.getKey();
                Collection<String> emojis = entry.getValue();
                if (emojis.size() > 1) {
                    errln("Duplicate name in " + locale + ": “" + name + "” for " 
                        + CollectionUtilities.join(emojis, " & "));
                    localeToNameToEmoji.putAll(locale + "\t" + name, emojis);
                }
            }
        }
        if (isVerbose() && !localeToNameToEmoji.isEmpty()) {
            System.out.println("\nCollisions");
            for ( Entry<String, String> entry : localeToNameToEmoji.entries()) {
                String locale = entry.getKey();
                String emoji = entry.getValue();
                System.out.println(locale
                    + "\t" + english.get(emoji).getShortName()
                    + "\t" + emoji
                    );
            }
        }
        if (isVerbose() && !localeToMissing.isEmpty()) {
            System.out.println("\nMissing");
            int count = 2;
            for (Entry<String, UnicodeSet> entry : localeToMissing.entrySet()) {
                String locale = entry.getKey();
                for (String emoji : entry.getValue()) {
                    System.out.println(locale
                        + "\t" + emoji
                        + "\t" + english.get(emoji).getShortName()
                        + "\t" + "=GOOGLETRANSLATE(C"+count+",\"en\",A"+count+")"
                        // =GOOGLETRANSLATE(C2,"en",A2)
                        );
                    ++count;
                }
            }
        }

    }

    private UnicodeSet getCurrent(UnicodeSet keySet) {
        UnicodeSet currentAge = new UnicodeSet("[:age=9.0:]");
        UnicodeSet result = new UnicodeSet();
        for (String s : keySet) {
            if (currentAge.containsAll(s)) {
                result.add(s);
            }
        }
        return result.freeze();
    }

    public void testAnnotationPaths() {
        assertTrue("", Emoji.getNonConstructed().contains("®"));
        Factory factoryAnnotations = SimpleFactory.make(CLDRPaths.ANNOTATIONS_DIRECTORY, ".*");
        for (String locale : Arrays.asList("en", "root")) {
            CLDRFile enAnnotations = factoryAnnotations.make(locale, false);
            //               //ldml/annotations/annotation[@cp="🧜"][@type="tts"]
            Set<String> annotationPaths = enAnnotations.getPaths("//ldml/anno", 
                Pattern.compile("//ldml/annotations/annotation.*tts.*").matcher(""), new TreeSet<>());
            Set<String> annotationPathsExpected = Emoji.getNamePaths();
            checkAMinusBIsC(locale + ".xml - Emoji.getNamePaths", annotationPaths, annotationPathsExpected, Collections.<String>emptySet());
            checkAMinusBIsC("Emoji.getNamePaths - " + locale + ".xml", annotationPathsExpected, annotationPaths, Collections.<String>emptySet());
        }
    }

    public void testPathHeaderSize() {
        Factory factoryAnnotations = SimpleFactory.make(CLDRPaths.ANNOTATIONS_DIRECTORY, ".*");
        CLDRFile englishAnnotations = factoryAnnotations.make("en", false);

        PathHeader.Factory phf = PathHeader.getFactory(CLDRConfig.getInstance().getEnglish());
        ImmutableSet<String> englishPaths = ImmutableSortedSet.copyOf(englishAnnotations.iterator("//ldml/annotations/"));
        Counter<SectionId> counterSectionId = new Counter<>();
        Counter<PageId> counterPageId = new Counter<>();
        for (String path : englishPaths) {
            PathHeader ph = phf.fromPath(path);
            counterSectionId.add(ph.getSectionId(), 1);
            counterPageId.add(ph.getPageId(), 1);
        }
        for (PageId pageId : counterPageId) {
            long size = counterPageId.get(pageId);
            assertTrue(pageId.toString(), size < 450);
            // System.out.println(pageId + "\t" + size);
        }
    }

    public void testSuperfluousAnnotationPaths() {
        Factory factoryAnnotations = SimpleFactory.make(CLDRPaths.ANNOTATIONS_DIRECTORY, ".*");
        ImmutableSet<String> rootPaths = ImmutableSortedSet.copyOf(factoryAnnotations.make("root", false).iterator("//ldml/annotations/"));

        CLDRFile englishAnnotations = factoryAnnotations.make("en", false);
        ImmutableSet<String> englishPaths = ImmutableSortedSet.copyOf(englishAnnotations.iterator("//ldml/annotations/"));

        Set<String> superfluous2 = setDifference(rootPaths, englishPaths);
        assertTrue("en contains root", superfluous2.isEmpty());
        if (!superfluous2.isEmpty()) {
            for (String path : superfluous2) {
//              XPathParts parts = XPathParts.getFrozenInstance(path);
//              String emoji = parts.getAttributeValue(-1, "cp");
                System.out.println("locale=en; action=add; path=" + path + "; value=XXX");
            }
        }

        Set<String> allSuperfluous = new TreeSet<>();
        for (String locale : factoryAnnotations.getAvailable()) {
            ImmutableSet<String> currentPaths = ImmutableSortedSet.copyOf(factoryAnnotations.make(locale, false).iterator("//ldml/annotations/"));
            Set<String> superfluous = setDifference(currentPaths, rootPaths);
            assertTrue("root contains " + locale, superfluous.isEmpty());
            allSuperfluous.addAll(superfluous);
            for (String s : currentPaths) {
                if (s.contains("\uFE0F")) {
                    errln("Contains FE0F: " + s);
                    break;
                }
            }
        }
        // get items to fix
        if (!allSuperfluous.isEmpty()) {
            for (String path : allSuperfluous) {
//                XPathParts parts = XPathParts.getFrozenInstance(path);
//                String emoji = parts.getAttributeValue(-1, "cp");
                System.out.println("locale=/.*/; action=delete; path=" + path);
            }
        }
    }

    private Set<String> setDifference(ImmutableSet<String> a, ImmutableSet<String> b) {
        Set<String> superfluous = new LinkedHashSet<>(a);
        superfluous.removeAll(b);
        return superfluous;
    }


    private void checkAMinusBIsC(String title, Set<String> a, Set<String> b, Set<String> c) {
        Set<String> aMb = new TreeSet<>(a);
        aMb.removeAll(b); 
        assertEquals(title, c, aMb);
    }
}