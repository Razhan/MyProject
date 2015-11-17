package com.ef.efekta.asr.textnormalizer;

import com.englishtown.android.asr.utils.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNormalizer {
    private static final String TAG = TextNormalizer.class.getSimpleName();

    private Pattern _pattern;

    public TextNormalizer(String pattern) {
        try {
            _pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (Exception ex) {
            Logger.e(TAG, ex.getMessage());
        }
    }

    public String normalize(String text) {
        return normalize(text, false);
    }

    public String normalize(String text, boolean removePunctuation) {
        ReplaceLogic logic = new ReplaceLogic(text, removePunctuation);
        Matcher matcher = _pattern.matcher(text);
        int count = matcher.groupCount();

        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String value = logic.apply(matcher);
            builder.append(value);
        }

        return builder.toString();
    }
}
