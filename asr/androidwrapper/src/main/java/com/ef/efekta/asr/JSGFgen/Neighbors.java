package com.ef.efekta.asr.JSGFgen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neighbors {

    private static Map<String, List<String>> neighbors = new HashMap<>();
	
	public static List<String> get(String phoneme) {
		return neighbors.get(phoneme);
	}
	
	public static String getGrammarForPhoneme(String phoneme) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(");
		List<String> neighbors = get(phoneme);
		int size = neighbors.size();
		int i = 0;
		for (String string : neighbors) {
			stringBuilder.append(string);
			if(i != size - 1) {
				stringBuilder.append(" | ");
			} 
			i++;
		}
		stringBuilder.append(")");
		
		return stringBuilder.toString();
	}
	
	static {
		addNeighbor("IY","IH");
		addNeighbor("IH","IY","EH");
		addNeighbor("EH","IH","ER","AE");
		addNeighbor("AE","EH","ER","AH");
		addNeighbor("AH","AE","ER","AA");
		addNeighbor("AA","AH","ER","AO");
		addNeighbor("AO","AA","ER","UH");
		addNeighbor("UH","AO","UW");
		addNeighbor("UW","UH");
		addNeighbor("ER","EH","AH","AO");
		addNeighbor("EY","EH","IY","AY");
		addNeighbor("OY","AO","IY","AY");
		addNeighbor("AY","AA","IY","OY","EY");
		addNeighbor("AW","AA","UH","OW");
		addNeighbor("OW","AO","UH","AW");
		addNeighbor("P","T","B","HH");
		addNeighbor("T","CH","K","D","P","HH");
		addNeighbor("CH","SH","JH","T");
		addNeighbor("K","G","T","HH");
		addNeighbor("SH","S","ZH","CH");
		addNeighbor("S","SH","Z","TH");
		addNeighbor("B","P","D");
		addNeighbor("D","T","JH","G","B");
		addNeighbor("JH","CH","ZH","D");
		addNeighbor("G","K","D");
		addNeighbor("ZH","SH","Z","JH");
		addNeighbor("Z","S","DH","ZH");
		addNeighbor("TH","S","DH","F","HH");
		addNeighbor("F","HH","TH","V");
		addNeighbor("DH","TH","Z","V");
		addNeighbor("V","F","DH");
		addNeighbor("HH","TH","F","P","T","K");
		addNeighbor("L","R","W");
		addNeighbor("R","Y","L");
		addNeighbor("Y","W","R");
		addNeighbor("W","L","Y");
		addNeighbor("M","N");
		addNeighbor("N","M","NG");
	}
	
	private static void addNeighbor(String phoneme, String... neighbors) {
		Neighbors.neighbors.put(phoneme, Arrays.asList(neighbors));
	}

    public static Map<String, List<String>> getNeighbors() {
        return neighbors;
    }
}
