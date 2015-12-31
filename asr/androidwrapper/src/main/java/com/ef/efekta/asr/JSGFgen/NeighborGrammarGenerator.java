package com.ef.efekta.asr.JSGFgen;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ef.efekta.asr.JSGFgen.GenerateJSGF.RuleGenerator;
import com.englishtown.android.asr.utils.Logger;


public class NeighborGrammarGenerator implements RuleGenerator {
    private static final String TAG = NeighborGrammarGenerator.class.getSimpleName();
    private HashMap<String, String> allWordsAndPhones;
    private List<List<String>> allPhonemes;

    static String dicPath;
    static LCModelBuilder builder;

    public interface LCModelBuilder {
        int newSentence(String normalizedSentence);

        void addWordPhonemes(int index, String word, String phonemesLine);
    }

    public NeighborGrammarGenerator(LCModelBuilder builder, String dicPath) {
        this.dicPath = dicPath;
        this.builder = builder;
        allPhonemes = new ArrayList<List<String>>();
    }

    @Override
    public void generator(PrintWriter printWriter, List<String> items) {

//        try {
//            allWordsAndPhones = GlobalDictionary.loadWordsAndPhonesFromGlobalDic(dicPath);
//
//            String sentences = "<sentences> = <e0>";
//            String options = "";
//            int i = 0;
//            int index = -1;
//
//            for (String item : items) {
//                String normalizedText = GenerateJSGF.normalizer.normalize(item, true);
//                normalizedText = normalizedText.toUpperCase();
//
//                if (builder != null) {
//                    index = builder.newSentence(normalizedText);
//                }
//
//                String e = "<e" + i + ">";
//
//                String resultText = "";
//
//                for (String word : normalizedText.split(" ")) {
//
//                    String line =  allWordsAndPhones.get(word);
//                    if (line == null) {
//                        Logger.i(TAG, String.format("Missing Word: {%s}", word));
//                        continue;
//                    }
//                    if (builder != null) {
//                        builder.addWordPhonemes(index, word, line);
//                    }
//
//                    Logger.i(TAG, String.format("Word:{%s} Line:{%s}", word, line));
//
//                    String[] phonemes = line.split(" ");
//                    resultText += "";
//                    for (String phone : phonemes) {
//                        resultText += Neighbors.getGrammarForPhoneme(phone) + " ";
//                    }
//                    resultText = resultText.substring(0, resultText.lastIndexOf(" "));
//                    resultText += " ";
//                }
//
//
//                if (i > 0) {
//                    sentences += " | " + e;
//                }
//
//                options += e + " = " + resultText + ";\n";
//
//                ++i;
//            }
//            sentences += ";\n";

//            printWriter.format("%s", sentences);
//            printWriter.format("%s", options);


//            Logger.i(TAG, String.format("sentences:{%s} options:{%s}", sentences, options));
//        } catch (FileNotFoundException e) {
//            Logger.e(TAG, e.getMessage());
//        }
    }

    public  List<String> getSentenceNeighbors(List<String> sentence) {

        try {
            allWordsAndPhones = GlobalDictionary.loadWordsAndPhonesFromGlobalDic(dicPath);
        } catch (FileNotFoundException e) {
            Logger.e(TAG, e.getMessage());
        }

        List<String> res = new ArrayList<String>();
        if (sentence == null || sentence.size() < 1) {
            return res;
        }

        for (String item : sentence) {
            if (allWordsAndPhones == null || !allWordsAndPhones.containsKey(item.toUpperCase())) {
                continue;
            }
            allPhonemes.clear();
            res.add(getWordNeighbors(item));
        }
        allWordsAndPhones.clear();
        allWordsAndPhones = null;
        return res;
    }

    private String getWordNeighbors(String word) {

        word = word.toUpperCase();

        if (allWordsAndPhones == null || !allWordsAndPhones.containsKey(word.toUpperCase())) {
            return null;
        }

        List<String> originalPhonemes = java.util.Arrays.asList(allWordsAndPhones.get(word).split(" "));
//        allPhonemes.add(originalPhonemes);

        generatePhonemeNeighbors(originalPhonemes, 0, 3);

        Map<String, String> wordNeighbors = generateWordNeighbors(word, allPhonemes);
        addNewNeighbors(wordNeighbors);

        Set<String> set = wordNeighbors.keySet();

        StringBuilder res = new StringBuilder(word);
        for (String str : set) {
            res.append(" " + str);
        }


        return res.toString();
    }

    private Map<String, String> generateWordNeighbors(String originalWord, List<List<String>> phonemes) {

        Map<String, String> wordNeighbors= new HashMap<String, String>();

        if (phonemes == null || phonemes.size() < 1) {
            return wordNeighbors;
        }

        for (int i = 0; i < phonemes.size(); i++) {
            String errorWord_name = originalWord + "_ERR_" + String.valueOf(i);
            wordNeighbors.put(errorWord_name, listToString(phonemes.get(i), " "));
        }
        return wordNeighbors;
    }

    private void generatePhonemeNeighbors(final List<String> originalPhonemes, int index, int limit) {

        if (allPhonemes.size() >= limit
            || originalPhonemes == null || originalPhonemes.size() < 1
                || index < 0 || index > originalPhonemes.size() - 1) {
            return;
        }

        if (PhonemeSet.VowelPhonemes.contains(originalPhonemes.get(index))) {
            List<String> phonemeNeighbors = Neighbors.getNeighbors().get(originalPhonemes.get(index));

            for (int i = 0; i < phonemeNeighbors.size(); i++) {
                ArrayList<String> newPhonemes = new ArrayList<String>(originalPhonemes);
                newPhonemes.set(index, phonemeNeighbors.get(i));

                allPhonemes.add(newPhonemes);
                generatePhonemeNeighbors(newPhonemes, index + 1, limit);
            }
        }

        generatePhonemeNeighbors(originalPhonemes, index + 1, limit);
    }

    private String listToString(List<String> list, String separator) {

        StringBuilder str = new StringBuilder("");
        if (list == null || list.size() < 1) {
            return str.toString();
        }

        for (int i = 0; i < list.size(); i++) {
            str.append(list.get(i) + separator);
        }

        return str.substring(0, str.length() - 1);
    }

    private void addNewNeighbors(Map<String, String> neighbors) {
        if (neighbors == null) {
            return;
        }

        addNewErrorWordsToFile(neighbors);
    }

    private void addNewErrorWordsToFile(Map<String, String> neighbors) {

        if (neighbors == null) {
            return;
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(dicPath, true);

            for (Map.Entry<String, String> entry : neighbors.entrySet())
            {

                if(allWordsAndPhones.containsKey(entry.getKey())) {
                    continue;
                }

            //  fw.write(System.lineSeparator());
                fw.write(System.getProperty("line.separator", "\n"));
                fw.write(entry.getKey() + "\t" + entry.getValue());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    throw new RuntimeException("close error!");
                }
        }
    }

}