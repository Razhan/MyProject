package com.ef.efekta.asr.textnormalizer;

public class WordFinder {
	static public String[] patterns = {
			"(((\\d{1,2}\\.\\d{2} *(pm|p.m|a.m|am)+)|(\\d{1,2}:\\d{2} *(pm|p.m|a.m|am)?)))",
			"(\\\"+|\\b'+|'+\\b|'+$|^'+|\\(.*\\)|;+|:+)",
			"(\\u002B|\\u0025)",
			"((\\d+)([ -]\\d+)+)",
			"((\\u0024)?(((\\d{1,3}((,?\\d{3})|\\d{0,3}){0,3}))(\\.\\d+)?)(st|rd|nd|th)*)",
			"([a-zA-Z]+'*[a-zA-Z]*)", "([,!.?:;-]+)", "(_+)", "(\\s+)",
			"(\\[.+=[a-z' ]+\\])",
			"(<.+=(([a-z]{1}[a-z0-9]?|\\u002B|\\.|_\\^|_!|_&|_,|_\\.|_\\?|_s) ?)+>)" };
	static public String getPattern() {
		String res = patterns[0];
		int l = patterns.length;
		for (int i = 1; i < l; i++) {
			res += "|" + patterns[i];
		}
		return res;
	}
}
