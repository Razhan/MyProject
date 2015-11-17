package com.ef.efekta.asr.JSGFgen;

import java.io.PrintWriter;
import java.util.List;

import com.ef.efekta.asr.JSGFgen.GenerateJSGF.RuleGenerator;
import com.englishtown.android.asr.utils.Logger;

public class GroupGenerator implements RuleGenerator {
	private static final String TAG = GroupGenerator.class.getSimpleName();
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

			++i;
		}
		sentences += ";\n";

		printWriter.format("%s", sentences);
		printWriter.format("%s", options);

		Logger.i(TAG, String.format("Write: {%s}", sentences));
        Logger.i(TAG, String.format("Write: {%s}", options));
	}
}