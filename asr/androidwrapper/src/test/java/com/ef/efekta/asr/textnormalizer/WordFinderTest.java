package com.ef.efekta.asr.textnormalizer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class WordFinderTest {

    Logger logger = LoggerFactory.getLogger("WordFinderTest");

    @Test
    public void testGetPattern() throws IOException {
        final String file = "src/test/resources/JavaWordFinder.txt";

        FileInputStream fis = new FileInputStream(file);
        logger.info("Open:{}", file);

        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);

        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            builder.append(line);
        }

        String src = WordFinder.getPattern();
        String target = builder.toString();
        logger.info("String src    :{}", src);
        logger.info("String target :{}", target);
        assertEquals(src, target);
    }

}
