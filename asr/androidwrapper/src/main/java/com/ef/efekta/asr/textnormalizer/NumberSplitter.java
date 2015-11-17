package com.ef.efekta.asr.textnormalizer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class NumberSplitter
{
    private static int[] WholeNumberParts = new int[] {1000, 100, 1 };

    private static HashMap<Integer, String> Replacements;
    
    static
    {
    	Replacements = new HashMap<>();
        Replacements.put(0, "zero");
        Replacements.put(1, "one");
        Replacements.put(2, "two");
        Replacements.put(3, "three");
        Replacements.put(4, "four");
        Replacements.put(5, "five");
        Replacements.put(6, "six");
        Replacements.put(7, "seven");
        Replacements.put(8, "eight");
        Replacements.put(9, "nine");
        Replacements.put(10, "ten");
        Replacements.put(11, "eleven");
        Replacements.put(12, "twelve");
        Replacements.put(13, "thirteen");
        Replacements.put(14, "fourteen");
        Replacements.put(15, "fifteen");
        Replacements.put(16, "sixteen");
        Replacements.put(17, "seventeen");
        Replacements.put(18, "eighteen");
        Replacements.put(19, "nineteen");
        Replacements.put(20, "twenty");
        Replacements.put(30, "thirty");
        Replacements.put(40, "forty");
        Replacements.put(50, "fifty");
        Replacements.put(60, "sixty");
        Replacements.put(70, "seventy");
        Replacements.put(80, "eighty");
        Replacements.put(90, "ninety");
    }


    private static void SplitNumberToSpeech(double number, AtomicReference<int[]> wholeNumbers, AtomicReference<int[]> decimalNumbers)
    {   	   	
        number = Math.abs(number);
        wholeNumbers.set(new int[WholeNumberParts.length]);
        
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        f.setMaximumFractionDigits(9);
        f.setGroupingUsed(false);
        String refinedNumber = f.format(number);
                
        
        String parts[] = refinedNumber.split("\\.");
        int wholePart = Integer.parseInt(parts[0]);

        wholeNumbers.set(Split(wholePart));

        String decimalPart = parts.length > 1 ? parts[1] : "";

        decimalNumbers.set(new int[decimalPart.length()]);

        for (int i = 0; i < decimalNumbers.get().length; i++)
        {
        	char decimalPartChar = decimalPart.charAt(i);
            decimalNumbers.get()[i] = Integer.parseInt(String.valueOf(decimalPartChar));        		
        }
    }

    private static int[] Split(int number)
    {
        int[] rv = new int[WholeNumberParts.length];
        int componentPortion;

        for (int i = 0; i < WholeNumberParts.length; i++)
        {
            if (number >= WholeNumberParts[i])
            {
                componentPortion = number / WholeNumberParts[i];
                rv[i] = componentPortion;
                number -= rv[i] * WholeNumberParts[i];
            }
        }

        return rv;
    }

    public static List<String> GetNumberSpeechKeys(double number, boolean useAnd)
    {
        List<String> rv = new ArrayList<>();

        AtomicReference<int[]> wholeNumbers = new AtomicReference<>();
        AtomicReference<int[]> decimalNumbers = new AtomicReference<>();

        List<String> wholeSpeechKeys;
        List<String> decimalSpeechKeys;

        if (number == 0)
        {
            wholeSpeechKeys = new ArrayList<>();
            wholeSpeechKeys.add("zero");
            decimalSpeechKeys = new ArrayList<>();
        }
        else
        {
            NumberSplitter.SplitNumberToSpeech(number, wholeNumbers, decimalNumbers);
            wholeSpeechKeys = GetSpeechPartsForWholeNumber(wholeNumbers, useAnd);
            decimalSpeechKeys = GetSpeechPartsForDecimalNumber(decimalNumbers);

            if (number > -1d && number < 1d)
            {
                wholeSpeechKeys.add(0, "zero");
            }

            if (number < 0)
            {
                wholeSpeechKeys.add(0, "minus");
            }
        }

        rv.addAll(wholeSpeechKeys);
        rv.addAll(decimalSpeechKeys);

        return rv;
    }

    private static List<String> GetSpeechPartsForWholeNumber(AtomicReference<int[]> wholeNumbers, boolean useAnd)
    {
        List<String> rv = new ArrayList<>();
        int andIndex = wholeNumbers.get()[wholeNumbers.get().length - 1] > 0 ? (wholeNumbers.get().length - 1) : -1;
        boolean canAnd = false;

        for (int i = 0; i < wholeNumbers.get().length; i++)
        {
            int part = wholeNumbers.get()[i];

            if (part == 0)
            {
                continue;
            }
            else
            {
                if (i < andIndex)
                {
                    canAnd = useAnd && true;
                }
            }

            switch (i)
            {
                case 0: // thousand

                    if (part >= 20)
                    {
                        rv.addAll(GetNumberSpeechKeys(part, useAnd));
                    }
                    else
                    {
                        rv.add(Replacements.get(part));
                    }

                    rv.add("thousand");
                    break;

                case 1: // hundred
                    rv.add(Replacements.get(part));
                    rv.add("hundred");
                    break; // hundred

                default:

                    if (i == andIndex && canAnd)
                    {
                        rv.add("and");
                    }

                    if (part < 20)
                    {
                        rv.add(Replacements.get(part));
                    }
                    else
                    {
                        int tens = (part / 10) * 10;
                        int digit = part % 10;

                        rv.add(Replacements.get(tens));

                        if (digit > 0)
                        {
                            rv.add(Replacements.get(digit));
                        }
                    }

                    break;
            }
        }

        return rv;
    }

    private static List<String> GetSpeechPartsForDecimalNumber(AtomicReference<int[]> decimalNumbers)
    {
        List<String> rv = new ArrayList<>();

        if (decimalNumbers.get().length > 0)
        {
            rv.add("point");

            for (int i : decimalNumbers.get())
            {
                rv.add(Replacements.get(i));
            }
        }

        return rv;
    }
}