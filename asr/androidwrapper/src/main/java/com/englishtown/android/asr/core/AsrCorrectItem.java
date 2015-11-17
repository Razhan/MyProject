package com.englishtown.android.asr.core;


public class AsrCorrectItem {
    int index;

    String words;

    public AsrCorrectItem(int index, String words) {
        this.index = index;
        this.words = words;
    }

    @Override
    public String toString() {
        return "AsrCorrectItem{" +
                "index=" + index +
                ", words='" + words + '\'' +
                '}';
    }

    public int getIndex() {
        return index;
    }

    public String getWords() {
        return words;
    }
}
