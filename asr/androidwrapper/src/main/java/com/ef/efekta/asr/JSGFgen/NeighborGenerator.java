package com.ef.efekta.asr.JSGFgen;

import com.englishtown.android.asr.utils.Logger;

import java.io.PrintWriter;
import java.util.List;

/**
 * Created by ranzh on 11/12/2015.
 */
public class NeighborGenerator implements GenerateJSGF.RuleGenerator {
    private static final String TAG = NeighborGenerator.class.getSimpleName();
    public void generator(PrintWriter printWriter, List<String> items) {
        String sentences = "<sentences> = ";
        String options = "";
        int i = 0;

        for (String item : items) {
//            String normalizedText = GenerateJSGF.normalizer.normalize(item, true);
            String normalizedText = item.toUpperCase();

            String[] strs = normalizedText.split(" ");
            normalizedText = "";
            for (String s : strs) {
                normalizedText += s + "|";
            }
            normalizedText = normalizedText.substring(0, normalizedText.length() - 1);

            String e = "<e" + i + ">";

            if (i >= 0) {
                sentences += "(" + e + ")*";
            }

            options += e + " = " + normalizedText + ";\n";

            ++i;
        }
        sentences += ";\n";

        printWriter.format("%s", sentences);
        printWriter.format("%s", options);

        Logger.i(TAG, String.format("Write: {%s}", sentences));
        Logger.i(TAG, String.format("Write: {%s}", options));
    }
}
