package com.ef.efekta.asr.textnormalizer;

import java.util.List;


public class StringUtil 
{

    public static String join(String delimiter, String s[]) {
        StringBuilder builder = new StringBuilder();

        if(s.length > 0)
        {
	        for(int i = 0; i < s.length - 1; i++) 
	        {
	            builder.append(s[i]);
	            builder.append(delimiter);
	        }
	        
	        builder.append(s[s.length - 1]);
        }
        return builder.toString();
    } 
    
    public static String join(String delimiter, List<String> s) 
    {
    	return join(delimiter, s.toArray(new String[0]));
    }




}
