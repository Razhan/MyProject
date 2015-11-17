package com.ef.efekta.asr.JSGFgen;

import android.util.Log;

import com.englishtown.android.asr.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class GlobalDictionary {
    private static final String TAG = GlobalDictionary.class.getSimpleName();

    public static HashMap<String, String> loadWordsAndPhonesFromGlobalDic2(String dicPath)
            throws FileNotFoundException {

        String word = "";
        String phones = "";

        File dicFile = new File(dicPath);

        HashMap<String, String> result = new HashMap<>();

        Scanner scanner = new Scanner(dicFile);


        int i = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

//            Scanner lineScanner = new Scanner(line);
//
//            String word = lineScanner.next();
//            String phones = lineScanner.nextLine().trim();

            int pos = line.indexOf("\t");

            if (pos == -1) {
                pos = line.indexOf(" ");
            }

            word = line.substring(0, pos);
            phones = line.substring(word.length()).trim();



            result.put(word, phones);

//            Logger.i(TAG, String.format("{%s}-   -{%s}", word, phones));
            i++;
        }

        return result;
    }

    public static HashMap<String, String> loadWordsAndPhonesFromGlobalDic(String dicPath)
            throws FileNotFoundException {

        HashMap<String, String> result = new HashMap<>();

        File file = new File(dicPath);
        InputStream inputStream = new FileInputStream(file);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();

        try {
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line + "\n");
            }
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
                try {
                    r.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
        }

        String[] list = total.toString().split("\n");

        String word = "";
        String phones = "";
        for (String line : list) {

            int pos = line.indexOf("\t");

            if (pos == -1) {
                pos = line.indexOf(" ");
            }

            if (pos == -1) {
                continue;
            }

            word = line.substring(0, pos);
            phones = line.substring(word.length()).trim();
            result.put(word, phones);
        }

        Log.d("linecount", String.valueOf(result.size()));
        return result;
    }

}
