package edu.cmu.pocketsphinx;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class DecoderTest {
    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    @Test
    public void testDecoder() {
        Decoder d = new Decoder();
        assertNotNull(d);
    }

    @Test
    public void testGetConfig() {
        Decoder d = new Decoder();
        Config c = d.getConfig();
        assertNotNull(c);
        assertEquals(c.getString("-lmname"), "default");
    }

    @Test
    public void testDecodeRaw() {
        Config c = new Config();
        c.setFloat("-samprate", 8000);
        Decoder d = new Decoder(c);
        AudioInputStream ais = null;
        try {
            URL testwav = new URL("file:src/test/resources/data/wsj/n800_440c0207.wav");
            AudioInputStream tmp = AudioSystem.getAudioInputStream(testwav);
            /* Convert it to the desired audio format for PocketSphinx. */
            AudioFormat targetAudioFormat =
                    new AudioFormat((float)c.getFloat("-samprate"),
                            16, 1, true, true);
            ais = AudioSystem.getAudioInputStream(targetAudioFormat, tmp);
        } catch (IOException e) {
            fail("Failed to open " + e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            fail("Unsupported file type of " + e.getMessage());
        }

        d.startUtt("");
        byte[] b = new byte[4096];
        try {
            int nbytes;
            while ((nbytes = ais.read(b)) >= 0) {
                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);
                short[] s = new short[nbytes/2];
                bb.asShortBuffer().get(s);
                d.processRaw(s, s.length, false, false);
                System.out.println(d.getHyp().getHypstr());
            }
        } catch (IOException e) {
            fail("Error when reading goforward.wav" + e.getMessage());
        }
        d.endUtt();
    }
}
