package com.ef.efekta.asr.JSGFgen;

import com.englishtown.android.asr.utils.Logger;

import java.io.PrintWriter;
import java.util.List;


public class WildcardGenerator implements GenerateJSGF.RuleGenerator {
    private static final String TAG = WildcardGenerator.class.getSimpleName();

    static String WildcardRule = "<anyword> = <anypronunciation> ";
    static String PronunciationRule = "<anypronunciation> = ";
    static int Repeat = 4;

    static {
        for (int i = 1; i < Repeat; ++i) {
            WildcardRule += " [<anypronunciation>] ";
        }
        WildcardRule += ";\n";

        boolean first = true;
        for (String p : PhonemeSet.AllPhonemes) {
            if (first) {
                PronunciationRule += p;
                first = false;
                continue;
            }

            PronunciationRule += "|" + p;
        }

        PronunciationRule += ";\n";
    }

    @Override
    public void generator(PrintWriter printWriter, List<String> items) {
        String sentences = "<sentences> = <e0>";
        String options = "";
        int i = 0;

        for (String item : items) {
            String normalizedText = GenerateJSGF.normalizer.normalize(item, true);
            normalizedText = normalizedText.toUpperCase();
            String e = "<e" + i + ">";

            if (i > 0) {
                sentences += " | " + e;
            }

            options += e + " = " + normalizedText + ";\n";

            if (options.contains("_")) {
                options = options.replace("_", "<anyword>");
            }

            ++i;
        }
        sentences += ";\n";

        printWriter.format("%s", sentences);
        printWriter.format("%s", options);
        printWriter.format("%s", WildcardRule);
        printWriter.format("%s", PronunciationRule);

        Logger.i(TAG, String.format("Write: {%s}", sentences));
        Logger.i(TAG, String.format("Write: {%s}", options));
        Logger.i(TAG, String.format("Write: {%s}", WildcardRule));
        Logger.i(TAG, String.format("Write: {%s}", PronunciationRule));
    }


}
