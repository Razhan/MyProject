package com.ef.efekta.asr.textnormalizer;

import java.util.HashMap;
import java.util.regex.Matcher;

class ReplaceLogic
{
    private static HashMap<String, String> Replacements;
    private static HashMap<String, String> timeReplacements;
    
    static
    {
    	Replacements = new HashMap<String, String>();
        Replacements.put("0", "zero");
        Replacements.put("1", "one");
        Replacements.put("2", "two");
        Replacements.put("3", "three");
        Replacements.put("4", "four");
        Replacements.put("5", "five");
        Replacements.put("6", "six");
        Replacements.put("7", "seven");
        Replacements.put("8", "eight");
        Replacements.put("9", "nine");
        Replacements.put("10", "ten");
        Replacements.put("11", "eleven");
        Replacements.put("12", "twelve");
        Replacements.put("13", "thirteen");
        Replacements.put("14", "fourteen");
        Replacements.put("15", "fifteen");
        Replacements.put("16", "sixteen");
        Replacements.put("17", "seventeen");
        Replacements.put("18", "eighteen");
        Replacements.put("19", "nineteen");
        Replacements.put("20", "twenty");
        Replacements.put("30", "thirty");
        Replacements.put("40", "forty");
        Replacements.put("50", "fifty");
        Replacements.put("60", "sixty");
        Replacements.put("70", "seventy");
        Replacements.put("80", "eighty");
        Replacements.put("90", "ninety");

        Replacements.put("zeroth", "zeroth");
        Replacements.put("onest", "first");
        Replacements.put("twond", "second");
        Replacements.put("threerd", "third");
        Replacements.put("fourth", "fourth");
        Replacements.put("fiveth", "fifth");
        Replacements.put("sixth", "sixth");
        Replacements.put("seventh", "seventh");
        Replacements.put("eightth", "eigth");
        Replacements.put("nineth", "ninth");
        Replacements.put("tenth", "tenth");
        Replacements.put("eleventh", "eleventh");
        Replacements.put("twelveth", "twelfth");
        Replacements.put("thirteenth", "thirteenth");
        Replacements.put("fourteenth", "fourteenth");
        Replacements.put("fifteenth", "fifteenth");
        Replacements.put("sixteenth", "sixteenth");
        Replacements.put("seventeenth", "seventeenth");
        Replacements.put("eighteenth", "eighteenth");
        Replacements.put("nineteenth", "nineteenth");
        Replacements.put("twentyth", "twentieth");
        Replacements.put("thirtyth", "thirtieth");
        Replacements.put("fortyth", "fortieth");
        Replacements.put("fiftyth", "fiftieth");
        Replacements.put("sixtyth", "sixtieth");
        Replacements.put("seventyth", "seventieth");
        Replacements.put("eightyth", "eightieth");
        Replacements.put("ninetyth", "ninetieth");
        Replacements.put("hundredth", "hundreth");
        Replacements.put("thousandth", "thousandth");

        timeReplacements = new HashMap<>();
        timeReplacements.put("bc", "B.C");
        timeReplacements.put("ad", "A.D");
        timeReplacements.put("pm", "P.M");
        timeReplacements.put("am", "A.M");
      	
    }
		
    private int _countOfWord = 0;
    private int _countOfIllegal = 0;
    private int _countOfPunctuation = 0;
    private int _countOfWildCard = 0;
    private int _countOfNumber = 0;
    private int _countOfWhiteSpace = 0;
    private int _countOfPhoneNumber = 0;
    private int _countOfUnitary = 0;
    private int _countOfTime = 0;
    private int _countOfOrdinal;
    private int _countOfKm;
    private int _countOfCurrency;
    private int _countOfPronounce;
    private int _countOfReplacementText;

    private int _legalMatches = -1;

    private String _originalText;
    private String _normalizedText;
	private boolean _dropAllPunctuation;

    public ReplaceLogic(String originalText, boolean dropAllPunctuation)
    {
        _originalText = originalText;
        _dropAllPunctuation = dropAllPunctuation;
    }

    public int getCountOfWildCard()
    {
    	return _countOfWildCard;
    }

    public int getCountOfNumber()
    {
    	return _countOfNumber;
    }

    public int getCountOfReplacementText()
    {
        return _countOfReplacementText;
    }

    public int getCountOfPronounce()
    {
        return _countOfPronounce;
    }

    public int getCountOfCurrency()
    {
        return _countOfCurrency;
    }

    public int getCountOfOrdinal()
    {
        return _countOfOrdinal;
    }

    public int getCountOfWhiteSpace()
    {
        return _countOfWhiteSpace;
    }

    public int getCountOfPhoneNumber()
    {
        return _countOfPhoneNumber;
    }

    public int getCountOfUnitary()
    {
        return _countOfUnitary;
    }


    public int getCountOfTime()
    {
        return _countOfTime;
    }

    public int getCountOfWord()
    {
        return _countOfWord;
    }

    public int getCountOfIllegal()
    {
        return _countOfIllegal;
    }

    public int getCountOfPunctuation()
    {
        return _countOfPunctuation;
    }

    public int getCountOfKm()
    { 
        return _countOfKm;
    }        

    int timeGroupIndex = 1;
    int illegalGroupIndex = 7;
    int unitaryCharactersGroupIndex = 8;
    int phoneNumberGroupIndex = 9;
    int currencyGroupIndex = 13;
    int numberGroupIndex = 14;
    int numberPositionGroupIndex = 20;
    int wordGroupIndex = 21;
    int punctuationGroupIndex = 22;
    int wildCardGroupIndex = 23;
    int whiteSpaceGroupIndex = 24;      
    int replaceTextGroupIndex = 25;
    int pronounceGroupIndex = 26;

    public String apply(Matcher match)
    {  	
        String rv = "";     
        String illegal = match.group(illegalGroupIndex);
        String replaceText = match.group(replaceTextGroupIndex);
        String pronounce = match.group(pronounceGroupIndex);
        
        if (replaceText != null)
        {     
            _legalMatches++;    
            
            StringBuilder builder = new StringBuilder();

            if (_legalMatches > 0)
            {
                builder.append(" ");
            }

            String splitValue = replaceText;
            splitValue = splitValue.substring(1, replaceText.length() - 1);
            String parts[] = splitValue.split("=", 2);

            String display = parts[0].trim();
            String text = parts[1].trim();

            builder.append(" ");
            builder.append(text);

            rv = builder.toString();

            _countOfReplacementText++;
        }
        else if (pronounce != null)
        {
            _legalMatches++;    
            
            StringBuilder builder = new StringBuilder();

            if (_legalMatches > 0)
            {
                builder.append(" ");
            }

            String splitValue = pronounce;
            splitValue = splitValue.substring(1, pronounce.length() - 1);
            String parts[] = splitValue.split("=", 2);

            String display = parts[0].trim();
            String text = parts[1].trim();

            builder.append(" ");
            builder.append(display);

            rv = builder.toString();

            _countOfPronounce++;
        }
        else if (illegal != null == false)
        {
            StringBuilder builder = new StringBuilder();

            String wildCard = match.group(wildCardGroupIndex);
            String number = match.group(numberGroupIndex);
            String word = match.group(wordGroupIndex);
            String punctuation = match.group(punctuationGroupIndex);
            String whiteSpace = match.group(whiteSpaceGroupIndex);
            String phoneNumber = match.group(phoneNumberGroupIndex);
            String unitaryCharacters = match.group(unitaryCharactersGroupIndex);
            String time = match.group(timeGroupIndex);

            if (word != null || number != null || phoneNumber != null || unitaryCharacters != null || time != null)
            {
                _legalMatches++;

                if (_legalMatches > 0)
                {
                    builder.append(" ");
                }

                if (word != null)
                {
                    _countOfWord++;

                    String wordKey = word.toLowerCase();

                    if (Replacements.containsKey(wordKey))
                    {
                        builder.append(Replacements.get(wordKey));
                    }
                    else
                    {
                        builder.append(word);
                    }
                }
                else if (unitaryCharacters != null)
                {
                    _countOfUnitary++;

                    if ("%".equals(unitaryCharacters))
                    {
                            builder.append("percent");
                    }
                    else if("+".equals(unitaryCharacters))
                    {
                            builder.append("plus");
                    }
                }
                else if (time != null)
                {
                    _countOfTime++;

                    String lowerTimeValue = time.toLowerCase();

                    String meridian = null;
                    String timePortion = null;

                    int indexOfP = lowerTimeValue.indexOf('p');
                    int indexOfA = lowerTimeValue.indexOf('a');

                    if (indexOfP > -1)
                    {
                        meridian = lowerTimeValue.substring(indexOfP).replace(".", "");
                        timePortion = lowerTimeValue.substring(0, indexOfP).replace(".", ":");
                    }
                    else if (indexOfA > -1)
                    {
                        meridian = lowerTimeValue.substring(indexOfA).replace(".", "");
                        timePortion = lowerTimeValue.substring(0, indexOfA).replace(".", ":");
                    }
                    else
                    {
                        timePortion = lowerTimeValue.replace(".", ":");
                    }

                    if (meridian != null)
                    {
                        if (timeReplacements.containsKey(meridian))
                        {
                            meridian = timeReplacements.get(meridian);
                        }

                        meridian = meridian.toUpperCase();
                    }

                    String timePortionParts[] = timePortion.split(":");

                    Double hourPart = Double.parseDouble(timePortionParts[0]);
                    builder.append(StringUtil.join(" ", NumberSplitter.GetNumberSpeechKeys(hourPart, false)));
                    Double minutePart = Double.parseDouble(timePortionParts[1]);

                    if (minutePart > 0)
                    {
                        builder.append(" ");

                        if (minutePart < 10)
                        {
                            builder.append("O");
                            builder.append(" ");
                        }

                        builder.append(StringUtil.join(" ", NumberSplitter.GetNumberSpeechKeys(minutePart, false)));
                    }

                    if (meridian != null)
                    {
                        builder.append(" ");
                        builder.append(meridian);
                    }
                }
                else if (phoneNumber != null)
                {
                    _countOfPhoneNumber++;

                    String numbers[] = phoneNumber.split("[ -]");

                    for (int i = 0; i < numbers.length - 1; i++)
                    {
                        String numberPart = numbers[i];

                        for (int j = 0; j < numberPart.length(); j++)
                        {
                        	char digit = numberPart.charAt(j);                        	
                            builder.append(Replacements.get(String.valueOf(digit)));
                            builder.append(" ");
                        }
                    }

                    String lastNumber = numbers[numbers.length - 1];

                    for (int j = 0; j < lastNumber.length() - 1; j++)
                    {
                    	char digit = lastNumber.charAt(j);                        	
                        builder.append(Replacements.get(String.valueOf(digit)));
                        builder.append(" ");
                    }

                    builder.append(Replacements.get(String.valueOf(lastNumber.charAt(lastNumber.length() - 1))));
                }
                else if (number != null)
                {
                    _countOfNumber++;

                    String numberPosition = match.group(numberPositionGroupIndex);
                    String currencySymbol = match.group(currencyGroupIndex);

                    if (currencySymbol != null)
                    {
                        _countOfCurrency++;
                    }

                    String numberValue = number;
                    String ordinalValue = null;

                    if (numberPosition != null)
                    {
                        _countOfOrdinal++;

                        ordinalValue = numberPosition.substring(numberPosition.length() - 2);
                    }
                    
                    Double amount = Double.parseDouble(numberValue.replace(",",""));                    
                    String numberWords[] = NumberSplitter.GetNumberSpeechKeys(amount, true).toArray(new String[0]);

                    String numberText = null;

                    if (ordinalValue != null)
                    {
                        ordinalValue = numberWords[numberWords.length - 1] + ordinalValue;
                        String newLastNumberWord = Replacements.get(ordinalValue);
                        numberWords[numberWords.length - 1] = newLastNumberWord;
                    }

                    numberText = StringUtil.join(" ", numberWords);

                    // this would be an odd item such as $103rd - so i'd make it read "dollar one hundred and third"
                    if (currencySymbol != null && ordinalValue != null)
                    {
                        builder.append("dollar ");
                    }

                    builder.append(numberText);

                    if (currencySymbol != null && ordinalValue == null)
                    {
                        if("$".equals(currencySymbol) || true)
                        {
                        	builder.append(amount == 1 ? " dollar" : " dollars");
                        }
                    }
                }
            }
            else if (punctuation != null)
            {
                _countOfPunctuation++;
                
                if(_dropAllPunctuation == false)
                {
                	builder.append(punctuation);
                }
            }
            else if (wildCard != null)
            {
                _legalMatches++;

                if (_legalMatches > 0)
                {
                    builder.append(" ");
                }
                
                _countOfWildCard++;

                builder.append("_");
            }

            rv = builder.toString();
        }

        else
        {
            _countOfIllegal++;
        }

        return rv;
    }

    public String getOriginalText()
    {
    	return _originalText;
    }

    public String getNormalizedText()
    {
    	return _normalizedText;
    }
    
    void setNormalizedText(String value)
    {
    	_normalizedText = value;   
    }
}