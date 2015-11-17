package com.ef.efekta.asr.JSGFgen;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ef.efekta.asr.JSGFgen.GenerateJSGF.RuleGenerator;
import com.englishtown.android.asr.utils.Logger;

public class MixedGenerator implements RuleGenerator {
    private static final String TAG = MixedGenerator.class.getSimpleName();

    private static final String MixOptionChar = "[A]|[B]|[C]|[D]|[E]|[F]|[G]|[H]|[I]|[J]|[K]|[L]|[M]|[N]|[O]|[P]|[Q]|[R]|[S]|[T]|[U]|[V]|[W]|[X]|[Y]|[Z]";
    private static final String MixOptionNum = "[ZERO]|[ONE]|[TWO]|[THREE]|[FOUR]|[FIVE]|[SIX]|[SEVEN]|[EIGHT]|[NINE]|[TEN]";
    public static final boolean EnableOptional = true;
    //public static int Weight = 1;

    private class Result {
        String mix;
        int weight;

        public Result() {
            mix = "";
            weight = 0;
        }
    }

    private static HashMap<Integer, String> Replacements;

    static {
        Replacements = new HashMap<>();
        Replacements.put(0, "zero");
        Replacements.put(1, "one");
        Replacements.put(2, "two");
        Replacements.put(3, "three");
        Replacements.put(4, "four");
        Replacements.put(5, "five");
        Replacements.put(6, "six");
        Replacements.put(7, "seven");
        Replacements.put(8, "eight");
        Replacements.put(9, "nine");
        Replacements.put(10, "ten");
        Replacements.put(11, "eleven");
        Replacements.put(12, "twelve");
        Replacements.put(13, "thirteen");
        Replacements.put(14, "fourteen");
        Replacements.put(15, "fifteen");
        Replacements.put(16, "sixteen");
        Replacements.put(17, "seventeen");
        Replacements.put(18, "eighteen");
        Replacements.put(19, "nineteen");
        Replacements.put(20, "twenty");
        Replacements.put(30, "thirty");
        Replacements.put(40, "forty");
        Replacements.put(50, "fifty");
        Replacements.put(60, "sixty");
        Replacements.put(70, "seventy");
        Replacements.put(80, "eighty");
        Replacements.put(90, "ninety");
    }

    private boolean mixWithMum(String item, Result r) {
        boolean b = false;
        String[] items = item.split(" ");
        for (String s : items) {
            if (Replacements.containsValue(s)) {
                b = true;
                break;
            }
        }

        if (b) {
            r.mix = MixOptionNum;
            r.weight = 10;
        }

        return b;
    }

    private boolean mixWithChar(String item, Result r) {
        boolean b = false;
        String[] items = item.split(" ");
        for (String s : items) {
            if (MixOptionChar.contains(s)) {
                b = true;
                break;
            }
        }

        if (b) {
            r.mix = MixOptionChar;
            r.weight = 26;
        }

        return b;
    }

    private boolean mixWithSomeChars(String item, Result r) {
        Set<String> s = new HashSet<>();
        for (char c : item.toCharArray()) {
            if (Character.isLetter(c)) {
                s.add(String.valueOf(c).toUpperCase());
            }
        }

        if (!s.isEmpty()) {
            boolean bFirst = true;

            for (String c : s) {
                if (bFirst) {
                    bFirst = false;
                } else {
                    r.mix += "|";
                }

                r.mix += "[" + c + "]";
            }

            r.weight = s.size() * 10;
        }

        return !s.isEmpty();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void generator(PrintWriter printWriter, List<String> items) {
        if (items.size() != 1) {
            Logger.e(TAG, "Needs just 1 item for FlaExe");
        }

        String normalizedText = GenerateJSGF.normalizer.normalize(items.get(0), true);
        Result r = new Result();

        if (mixWithMum(normalizedText, r)) {
        } else if (mixWithChar(normalizedText, r)) {
        } else {
            mixWithSomeChars(normalizedText, r);
        }

        String sentences = "<sentences> = ";
        String mix = "<mix> = ";
        normalizedText = normalizedText.toUpperCase();
        if (EnableOptional) {
            sentences += "[/" + r.weight + "/(" + normalizedText + ")]|[/1/(<mix>)]";
        } else {
            sentences += "/" + r.weight + "/(" + normalizedText + ")|[/1/(<mix>)]";
        }

        mix += r.mix + ";";
        sentences += ";";
        printWriter.format("%s\n%s\n", mix, sentences);

        Logger.i(TAG, String.format("Write: {%s}", mix));
        Logger.i(TAG, String.format("Write: {%s}", sentences));

    }
}