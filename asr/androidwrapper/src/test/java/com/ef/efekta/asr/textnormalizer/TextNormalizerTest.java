package com.ef.efekta.asr.textnormalizer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class TextNormalizerTest {
    Logger logger = LoggerFactory.getLogger("WordFinderTest");

    private TextNormalizer normalizer;

    @Before
    public void setUp() throws Exception {
        logger.info("Locale.setDefault:FRANCE");
        Locale.setDefault(Locale.FRANCE);

        normalizer = new TextNormalizer(WordFinder.getPattern());
        assertNotNull("Should not be null", normalizer);
    }

    @Test
    public void testNormalizeString() {

        List<Pairs> ordinals = new ArrayList<>();
        ordinals.add(new Pairs("Hello23", "Hello twenty three"));
        ordinals.add(new Pairs("594,444,134", "five hundred and ninety four thousand four hundred and forty four thousand one hundred and thirty four"));
        ordinals.add(new Pairs("7:00", "seven"));
        ordinals.add(new Pairs("3,000", "three thousand"));
        ordinals.add(new Pairs("1111 23", "one one one one two three"));
        ordinals.add(new Pairs("1111-23", "one one one one two three"));
        ordinals.add(new Pairs("1111 23 56", "one one one one two three five six"));
        ordinals.add(new Pairs("1111-23-56", "one one one one two three five six"));
        ordinals.add(new Pairs("1111-23 56", "one one one one two three five six"));
        ordinals.add(new Pairs("did", "did"));
        ordinals.add(new Pairs("54,444", "fifty four thousand four hundred and forty four"));
        ordinals.add(new Pairs("542.12322", "five hundred and forty two point one two three two two"));
        ordinals.add(new Pairs("1,234.0", "one thousand two hundred and thirty four"));
        ordinals.add(new Pairs("56789", "fifty six thousand seven hundred and eighty nine"));
        ordinals.add(new Pairs("1", "one"));
        ordinals.add(new Pairs("12", "twelve"));
        ordinals.add(new Pairs("134", "one hundred and thirty four"));
        ordinals.add(new Pairs("1234", "one thousand two hundred and thirty four"));
        ordinals.add(new Pairs("12345", "twelve thousand three hundred and forty five"));
        ordinals.add(new Pairs("123456", "one hundred and twenty three thousand four hundred and fifty six"));
        ordinals.add(new Pairs("1234567", "one thousand two hundred and thirty four thousand five hundred and sixty seven"));
        ordinals.add(new Pairs("12345678", "twelve thousand three hundred and forty five thousand six hundred and seventy eight"));
        ordinals.add(new Pairs("123456789", "one hundred and twenty three thousand four hundred and fifty six thousand seven hundred and eighty nine"));
        ordinals.add(new Pairs("7.00pm", "seven P.M"));
        ordinals.add(new Pairs("8:00", "eight"));
        ordinals.add(new Pairs("8:01", "eight O one"));
        ordinals.add(new Pairs("9:00pm", "nine P.M"));
        ordinals.add(new Pairs("9:00 pm", "nine P.M"));
        ordinals.add(new Pairs("9:00  pm", "nine P.M"));
        ordinals.add(new Pairs("10:00a.m", "ten A.M"));
        ordinals.add(new Pairs("11:12", "eleven twelve"));
        ordinals.add(new Pairs("11:12pm", "eleven twelve P.M"));
        ordinals.add(new Pairs("1019 bc", "one thousand and nineteen bc")); //?
        ordinals.add(new Pairs("1019BC", "one thousand and nineteen BC"));	//?

        //ordinals.add(new Pairs("1019b.c", "one thousand and nineteen BC"));

        //ordinals.add(new Pairs("2013ad", "2013 A.D"));
        ordinals.add(new Pairs("[jason=what time is it mr wolf]", " what time is it mr wolf"));
        ordinals.add(new Pairs("<hello=H S1 S EH L O + UH _^ . _! _& _, _. _? _s>", " hello"));

        for(Pairs ordinal : ordinals) {
            String normalizedText = normalizer.normalize(ordinal.text);
            assertEquals("should be equal", ordinal.target, normalizedText);
        }

        for(Pairs ordinal : ordinals) {
            String normalizedText = normalizer.normalize(ordinal.text, true);
            assertEquals("should be equal", ordinal.target, normalizedText);
        }

    }

    @Test
    public void testNormalizeDropAllPunctuation() {

        List<Pairs> ordinals = new ArrayList<>();
        ordinals.add(new Pairs("", ""));

        ordinals.add(new Pairs("S411", ""));
        ordinals.add(new Pairs("S3.0", ""));

        ordinals.add(new Pairs("My order number is KL72X.", ""));  // 135672 ?
        ordinals.add(new Pairs("No, I didn't.", "")); 				// 126189
        ordinals.add(new Pairs("No, I didn�t. On Monday.", ""));
        ordinals.add(new Pairs("Of course. The British English equivalent of 'trash can' is ' dustbin.'", "")); 	 //129468
        ordinals.add(new Pairs("Really? You're supposed to say 'pants' instead of that.", ""));
        ordinals.add(new Pairs("Can I just add my two cents' worth?", ""));						// 125794
        ordinals.add(new Pairs("Linux version is 3.0", ""));
        ordinals.add(new Pairs("Linux version is v3.0", ""));
        ordinals.add(new Pairs("Linux version is v2.6.27", ""));
        ordinals.add(new Pairs("I get 1.0,at 5.0pm", ""));
        ordinals.add(new Pairs("I'm Kicool.", ""));
        ordinals.add(new Pairs("My homepage is www.ef.com.", ""));
        ordinals.add(new Pairs("My eMail is kicool.zhang@ef.com.", ""));
        ordinals.add(new Pairs("I don't known C#, but C++.", ""));
        ordinals.add(new Pairs("1+1=2", ""));
        ordinals.add(new Pairs("I don't know.", ""));
        ordinals.add(new Pairs("I'll be thre.", ""));
        ordinals.add(new Pairs("Can I just add my two cents' worth?", "")); //125794
        ordinals.add(new Pairs("I don�t think he�s a hard worker.", "")); //125092
        ordinals.add(new Pairs("How about 2Big2Catch?", "")); 				//66810
        ordinals.add(new Pairs("You should think about Let'sSocialize.", ""));
        ordinals.add(new Pairs("I'd recommend ManyThings4U.", ""));
        ordinals.add(new Pairs("I'm sorry, I haven't got time", ""));
        ordinals.add(new Pairs("I describe the dramatic rise of 0.5 percent year on year.", ""));
        ordinals.add(new Pairs("That's 'Let's do it'.", ""));
        ordinals.add(new Pairs("My No. is 1234 5678.Am I right?", ""));
        ordinals.add(new Pairs("1019 b.c", "one thousand and nineteen b. c")); //?
        ordinals.add(new Pairs("am.", "A.M."));
        ordinals.add(new Pairs("10.00am.", "ten A.M."));
        ordinals.add(new Pairs("10.00 am.", "ten A.M."));
        ordinals.add(new Pairs("10.00  am.", "ten A.M."));
        ordinals.add(new Pairs("a.m.", "A.M."));
        ordinals.add(new Pairs("im.", "I.M."));
        ordinals.add(new Pairs("pm.", "P.M."));
        ordinals.add(new Pairs("My name is ___, hi!", "My name is _, hi!"));
        ordinals.add(new Pairs("Sam's pen!", "Sam's pen!"));
        ordinals.add(new Pairs("Hi. I need some information about the film 'Believe It.'", "Hi. I need some information about the film Believe It."));
        ordinals.add(new Pairs("Are you showing the film 'Believe It'?", "Are you showing the film 'Believe It'?"));
        ordinals.add(new Pairs("banana /j/ ice cream", "banana j ice cream")); //113733
        ordinals.add(new Pairs("int-er-est", "int- er- est"));
        for(Pairs ordinal : ordinals) {
            try {
                String normalizedDropFalse = normalizer.normalize(ordinal.text, false);
                String normalizedDropTrue = normalizer.normalize(ordinal.text, true);
                logger.info("{} | {} | {}", ordinal.text, normalizedDropFalse, normalizedDropTrue);
            } catch (Exception e) {
                logger.error("S: {} Exception:", ordinal.text, e);
                e.printStackTrace();
            }
        }
    }

    public class Pairs {
        public String text;
        public String target;
        public Pairs(String _text, String _target) {
            text = _text;
            target = _target;
        }

    }

    @Test
    public void testPhoneNumbers() {
        String text = "1111 23";
        String normalizedText = normalizer.normalize(text);
        String assertText = "one one one one two three";
        assertEquals("should be equal", assertText, normalizedText);
    }

    @Test
    public void testNormalizerNumbers() {
        List<Pairs> ordinals = new ArrayList<>();
        ordinals.add(new Pairs("0th", "zeroth"));
        ordinals.add(new Pairs("1st", "first"));
        ordinals.add(new Pairs("2nd", "second"));
        ordinals.add(new Pairs("3rd", "third"));
        ordinals.add(new Pairs("4th", "fourth"));
        ordinals.add(new Pairs("5th", "fifth"));
        ordinals.add(new Pairs("6th", "sixth"));
        ordinals.add(new Pairs("7th", "seventh"));
        ordinals.add(new Pairs("8th", "eigth"));
        ordinals.add(new Pairs("9th", "ninth"));
        ordinals.add(new Pairs("10th", "tenth"));
        ordinals.add(new Pairs("11th", "eleventh"));
        ordinals.add(new Pairs("12th", "twelfth"));
        ordinals.add(new Pairs("13th", "thirteenth"));
        ordinals.add(new Pairs("14th", "fourteenth"));
        ordinals.add(new Pairs("15th", "fifteenth"));
        ordinals.add(new Pairs("16th", "sixteenth"));
        ordinals.add(new Pairs("17th", "seventeenth"));
        ordinals.add(new Pairs("18th", "eighteenth"));
        ordinals.add(new Pairs("19th", "nineteenth"));
        ordinals.add(new Pairs("20th", "twentieth"));
        ordinals.add(new Pairs("30th", "thirtieth"));
        ordinals.add(new Pairs("40th", "fortieth"));
        ordinals.add(new Pairs("50th", "fiftieth"));
        ordinals.add(new Pairs("60th", "sixtieth"));
        ordinals.add(new Pairs("70th", "seventieth"));
        ordinals.add(new Pairs("80th", "eightieth"));
        ordinals.add(new Pairs("90th", "ninetieth"));
        ordinals.add(new Pairs("100th", "one hundreth"));
        ordinals.add(new Pairs("1000th", "one thousandth"));
        ordinals.add(new Pairs("10000th", "ten thousandth"));
        ordinals.add(new Pairs("122nd", "one hundred and twenty second"));

        for(Pairs ordinal : ordinals) {
            assertEquals(ordinal.target, normalizer.normalize(ordinal.text));
        }

        List<Pairs> numbers = new ArrayList<>();
        numbers.add(new Pairs("0", "zero"));
        numbers.add(new Pairs("1", "one"));
        numbers.add(new Pairs("2", "two"));
        numbers.add(new Pairs("3", "three"));
        numbers.add(new Pairs("4", "four"));
        numbers.add(new Pairs("5", "five"));
        numbers.add(new Pairs("6", "six"));
        numbers.add(new Pairs("7", "seven"));
        numbers.add(new Pairs("8", "eight"));
        numbers.add(new Pairs("9", "nine"));
        numbers.add(new Pairs("10", "ten"));
        numbers.add(new Pairs("11", "eleven"));
        numbers.add(new Pairs("12", "twelve"));
        numbers.add(new Pairs("13", "thirteen"));
        numbers.add(new Pairs("14", "fourteen"));
        numbers.add(new Pairs("15", "fifteen"));
        numbers.add(new Pairs("16", "sixteen"));
        numbers.add(new Pairs("17", "seventeen"));
        numbers.add(new Pairs("18", "eighteen"));
        numbers.add(new Pairs("19", "nineteen"));
        numbers.add(new Pairs("20", "twenty"));
        numbers.add(new Pairs("30", "thirty"));
        numbers.add(new Pairs("40", "forty"));
        numbers.add(new Pairs("50", "fifty"));
        numbers.add(new Pairs("60", "sixty"));
        numbers.add(new Pairs("70", "seventy"));
        numbers.add(new Pairs("80", "eighty"));
        numbers.add(new Pairs("90", "ninety"));
        numbers.add(new Pairs("100", "one hundred"));
        numbers.add(new Pairs("1000", "one thousand"));
        numbers.add(new Pairs("10000", "ten thousand"));
        numbers.add(new Pairs("122", "one hundred and twenty two"));

        for(Pairs number : numbers) {
            assertEquals(number.target, normalizer.normalize(number.text));
        }

        String text;
        String normalizedText;

        text = "I'm looking at coming 1st, 101st, even 3rd or 33rd, but once i came 2nd then 202322nd";
        normalizedText = normalizer.normalize(text);
        assertEquals(
                "I'm looking at coming first, one hundred and first, even third or thirty third, but once i came second then two hundred and two thousand three hundred and twenty second",
                normalizedText);
    }
}