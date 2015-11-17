package com.ef.efekta.asr.JSGFgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.ef.efekta.asr.textnormalizer.TextNormalizer;
import com.ef.efekta.asr.textnormalizer.WordFinder;
import com.englishtown.android.asr.utils.Logger;

public class GenerateJSGF {
    private static final String TAG = GenerateJSGF.class.getSimpleName();
    static TextNormalizer normalizer = new TextNormalizer(WordFinder.getPattern());
    private static String FileHeader = "#JSGF V1.0;\ngrammar efektasr;\n\npublic <result> = <sentences>;\n";

    public interface RuleGenerator {
        public void generator(PrintWriter printWriter, List<String> items);
    }

    public static void genJSGF(List<String> items, String jsgfFilePath, String aid, RuleGenerator rule) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new File(jsgfFilePath));

        if (aid != null && aid.length() > 0) {
            FileHeader = "#JSGF V1.0;\ngrammar efektasr;\n\npublic <result" + aid + "> = <sentences>;\n";
        }
        printWriter.format("%s", FileHeader);
        Logger.i("Write: {}", FileHeader);

        rule.generator(printWriter, items);

        printWriter.close();
    }

    public static class SimpleGenerator implements RuleGenerator {
        @Override
        public void generator(PrintWriter printWriter, List<String> items) {
            String sentences = "<sentences> = ";
            boolean bFirst = true;
            for (String item : items) {
                String normalizedText = normalizer.normalize(item, true);
                normalizedText = normalizedText.toUpperCase();

                if (!bFirst)
                    sentences += " | ";
                sentences += normalizedText;

                bFirst = false;
            }
            sentences += ";";
            printWriter.format("%s\n", sentences);

            Logger.i(TAG, "Write: {}" + sentences);
        }
    }

    public static void genCmp(List<String> strs, String cmpFilePath) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new File(cmpFilePath));

        for (String item : strs) {
            String normalizedText = normalizer.normalize(item, true);
            normalizedText = normalizedText.toUpperCase();
            printWriter.format("%s\n", normalizedText);
            printWriter.format("%s\n", item);

            Logger.i(TAG, String.format("Write: {%s}", normalizedText));
            Logger.i(TAG, String.format("Write: {%s}", item));
        }

        printWriter.close();
    }

}
