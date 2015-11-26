package com.englishtown.android.asr.task;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackInterface;
import com.englishtown.android.asr.utils.Logger;
import com.englishtown.android.asr.utils.SimpleLame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.pocketsphinx;

/**
 * Speech recognition task, which runs in a worker thread.
 *
 * This class implements speech recognition for this demo application. It takes
 * the form of a long-running task which accepts requests to start and stop
 * listening, and emits recognition results to a listener.
 *
 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
 */
public class RecognizerTask implements Runnable {

    static {
        System.loadLibrary("mp3lame");
    }

	public static final int RECORDER_SAMPLERATE = 8000 * 2;
    public static final float RECORDER_SAMPLERATE_FLOAT = RECORDER_SAMPLERATE;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
    private static final String TAG = RecognizerTask.class.getSimpleName();
    private static final String POCKETSPHINX_UTTID = "efektaudio";
    private String mFilePath;

	ASRConfig asrConfig;

    private int amplitude;

    /**
	 * Audio recording task.
	 * 
	 * This class implements a task which pulls blocks of audio from the system
	 * audio input and places them on a queue.
	 * 
	 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
	 */
	class AudioTask implements Runnable {
		
		/**
		 * Queue on which audio blocks are placed.
		 */
		LinkedBlockingQueue<short[]> q;
		AudioRecord rec;
		int block_size;
		volatile boolean done;

		static final int DEFAULT_BLOCK_SIZE = 512;

        short[] buffer;
        byte[] mp3buffer;
        FileOutputStream output;


        AudioTask() {
			this.init(new LinkedBlockingQueue<short[]>(), DEFAULT_BLOCK_SIZE);
		}

		AudioTask(LinkedBlockingQueue<short[]> q) {
			this.init(q, DEFAULT_BLOCK_SIZE);
		}

		AudioTask(LinkedBlockingQueue<short[]> q, int block_size, String mp3_path) {
			this.init(q, block_size);

            buffer = new short[this.block_size];
            mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

//            String mFilePath = Environment.getExternalStorageDirectory() + "/aaab.mp3";

            try {
                output = new FileOutputStream(new File(mp3_path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }

        private RecorderAndPlaybackInterface recorderAndPlaybackInterface;

        public void setRecorderSaveInterface(int recorderMode,
                RecorderAndPlaybackInterface recorderAndPlaybackInterface) {
            this.recorderAndPlaybackInterface = recorderAndPlaybackInterface;
        }

		void init(LinkedBlockingQueue<short[]> q, int block_size) {
			this.done = false;
			this.q = q;
			this.block_size = block_size;

			int bufferSize = AudioRecord.getMinBufferSize(
					RecognizerTask.RECORDER_SAMPLERATE,
					RecognizerTask.RECORDER_CHANNELS,
					RecognizerTask.RECORDER_AUDIO_ENCODING);
			if (bufferSize < 8192) {
				bufferSize = 8192;
			}
			this.rec = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
					RECORDER_SAMPLERATE, RECORDER_CHANNELS,
	        RECORDER_AUDIO_ENCODING, bufferSize);
		}

		public int getBlockSize() {
			return block_size;
		}

		public void setBlockSize(int block_size) {
			this.block_size = block_size;
		}

		public LinkedBlockingQueue<short[]> getQueue() {
			return q;
		}

		public void stop() {
			this.done = true;
		}

		public void run() {
		    if(null != recorderAndPlaybackInterface){
		        recorderAndPlaybackInterface.startRecording();
		    }

            SimpleLame.init(RECORDER_SAMPLERATE, 1, RECORDER_SAMPLERATE, 32);

            this.rec.startRecording();
			while (!this.done) {
				int nshorts = this.readBlock();
				if (nshorts <= 0)
					break;
			}

            int flushResult = SimpleLame.flush(mp3buffer);

            if (flushResult != 0) {
                try {
                    output.write(mp3buffer, 0, flushResult);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.rec.stop();
			this.rec.release();
			
			if(null != recorderAndPlaybackInterface){
                recorderAndPlaybackInterface.stopRecording();
            }

		}

		int readBlock() {
			int nshorts = this.rec.read(buffer, 0, buffer.length);
			
			if (nshorts > 0) {
				this.q.add(buffer);

                int encResult = SimpleLame.encode(buffer,
                        buffer, nshorts, mp3buffer);

                if (encResult != 0) {
                    try {
                        output.write(mp3buffer, 0, encResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
			}

			if(null != recorderAndPlaybackInterface){
				recorderAndPlaybackInterface.saveAudioRecorderFile(buffer, nshorts);
			}

            double sum = 0;

            for (int i = 0; i < nshorts; i++) {
                sum += buffer [i] * buffer [i];
            }
            if (nshorts > 0) {
                amplitude = (int)Math.sqrt(sum / nshorts);
            }
			return nshorts;
		}
	}

	/**
	 * PocketSphinx native decoder object.
	 */
	Decoder ps;
	/**
	 * Audio recording task.
	 */
	AudioTask audio;
	/**
	 * Thread associated with recording task.
	 */
	Thread audio_thread;
	/**
	 * Queue of audio buffers.
	 */
	LinkedBlockingQueue<short[]> audioq;
	/**
	 * Listener for recognition results.
	 */
	RecognitionListener rl;
	/**
	 * Whether to report partial results.
	 */
	boolean use_partials;

	/**
	 * State of the main loop.
	 */
	enum State {
		IDLE, LISTENING
	};
	/**
	 * Events for main loop.
	 */
	enum Event {
		NONE, START, STOP, SHUTDOWN
	};

	/**
	 * Current event.
	 */
	Event mailbox;
	private boolean done;
	private boolean uttStarted;

    public int getAmplitude() {
        return amplitude;
    }

	public RecognitionListener getRecognitionListener() {
		return rl;
	}

	public void setRecognitionListener(RecognitionListener rl) {
		this.rl = rl;
	}

	public void setUsePartials(boolean use_partials) {
		this.use_partials = use_partials;
	}

	public boolean getUsePartials() {
		return this.use_partials;
	}

	public RecognizerTask(ASRConfig asrConfig, String mp3path) {
        this.asrConfig = asrConfig;
		Config c = new Config();
		setCommonCfg(c);
		setDefaultDic(c);
		setDefaultLm(c);

		this.ps = new Decoder(c);
		this.audio = null;
		this.audioq = new LinkedBlockingQueue<short[]>();
		this.use_partials = false;
		this.mailbox = Event.NONE;
        this.mFilePath = mp3path;
	}

	public RecognizerTask(ASRConfig asrConfig, String dictionaryPath, String languageModelPath) {
        this.asrConfig = asrConfig;
		Config c = new Config();
		setCommonCfg(c);
		
		if (isFileExits(dictionaryPath)) {
			c.setString("-dict", dictionaryPath);
		} else {
			Logger.e(TAG, "File not exits:" + dictionaryPath + ", set to default!");
			
			setDefaultDic(c);
		}
		
		if (isFileExits(languageModelPath)) {
			if (isJSGF(languageModelPath)) {
				c.setString("-jsgf", languageModelPath);
			} else {
				c.setString("-lm", languageModelPath);
			}
		} else {
			Logger.i(TAG, "File not exits:" + languageModelPath + ", set to default!");
			
			setDefaultLm(c);
		}

		this.ps = new Decoder(c);
		this.audio = null;
		this.audioq = new LinkedBlockingQueue<short[]>();
		this.use_partials = false;
		this.mailbox = Event.NONE;
	}
	
	private int recorderMode;
	private RecorderAndPlaybackInterface recorderAndPlaybackInterface;
	public void setRecorderSaveInterface(int recorderMode, RecorderAndPlaybackInterface recorderAndPlaybackInterface){
	    this.recorderMode = recorderMode;
	    this.recorderAndPlaybackInterface = recorderAndPlaybackInterface;
	}

	public void run() {
		dumpCfg();
		
		done = false;
		/* State of the main loop. */
		State state = State.IDLE;
		/* Previous partial hypothesis. */
		String partial_hyp = null;
		
		while (!done) {
			/* Read the mail. */
			Event todo = Event.NONE;
			synchronized (this.mailbox) {
				todo = this.mailbox;
				/* If we're idle then wait for something to happen. */
				if (state == State.IDLE && todo == Event.NONE) {
					try {
						Logger.v(TAG, "waiting");
						this.mailbox.wait();
						todo = this.mailbox;
						Logger.v(TAG, "got" + todo);
					} catch (InterruptedException e) {
						/* Quit main loop. */
						Logger.e(TAG, "Interrupted waiting for mailbox, shutting down");
						todo = Event.SHUTDOWN;
					}
				}
				/* Reset the mailbox before releasing, to avoid race condition. */
				this.mailbox = Event.NONE;
			}
			/* Do whatever the mail says to do. */
			switch (todo) {
			case NONE:
				if (state == State.IDLE)
					Logger.e(TAG, "Received NONE in mailbox when IDLE, threading error?");
				break;
			case START:
				if (state == State.IDLE) { 
					Logger.v(TAG, "START");
					this.audio = new AudioTask(this.audioq, 1024, mFilePath);
					this.audio_thread = new Thread(this.audio, "AudioTaskThread");
					this.ps.startUtt(POCKETSPHINX_UTTID);
					this.uttStarted = true;
					this.audio_thread.start();
					state = State.LISTENING;

					this.audio.setRecorderSaveInterface(recorderMode, recorderAndPlaybackInterface);
				}
				else
					Logger.e(TAG, "Received START in mailbox when LISTENING");
				break;
			case STOP:
				if (state == State.IDLE)
					Logger.e(TAG, "Received STOP in mailbox when IDLE");
				else {
					Logger.v(TAG, "STOP");
					
					assert this.audio != null;
					this.audio.stop();
					try {
						this.audio_thread.join();
					}
					catch (InterruptedException e) {
						Logger.e(TAG, "Interrupted waiting for audio thread, shutting down");
						done = true;
					}
					/* Drain the audio queue. */
					short[] buf;
					while ((buf = this.audioq.poll()) != null) {
						//Logger.d(TAG, "Reading " + buf.length + " samples from queue");
						this.ps.processRaw(buf, buf.length, false, false);
					}
					this.ps.endUtt();
					this.uttStarted = false;
					
					notifyHyp(this.ps.getHyp());
					state = State.IDLE;
				}
				break;
			case SHUTDOWN:
				Logger.v(TAG, "SHUTDOWN");
				if (this.audio != null) {
					this.audio.stop();
					assert this.audio_thread != null;
					try {
						this.audio_thread.join();
					}
					catch (InterruptedException e) {
						Logger.e(TAG, "InterruptedException occurs as shutting down");
					}
					if (this.uttStarted) this.ps.endUtt();
				}
				
				this.audio = null;
				this.audio_thread = null;
				state = State.IDLE;
				done = true;
				break;
			}
			/* Do whatever's appropriate for the current state.  Actually this just means processing audio if possible. */
			if (state == State.LISTENING) {
				assert this.audio != null;
				try {
					short[] buf = this.audioq.take();
					//Logger.d(TAG, "Reading " + buf.length + " samples from queue");
					this.ps.processRaw(buf, buf.length, false, false);
					Hypothesis hyp = this.ps.getHyp();
					if (hyp != null) {
						String hypstr = hyp.getHypstr();
						if (hypstr != null && !hypstr.equals(partial_hyp)) {
							if (this.rl != null) {
                                Logger.d(TAG, "Hypothesis: " + hyp.getHypstr() + " score:" + hyp.getBest_score());

								Bundle b = new Bundle();
								b.putString("hyp", hyp.getHypstr());
								this.rl.onPartialResults(b);
							}
						}
						partial_hyp = hypstr;
					}
				} catch (InterruptedException e) {
					Logger.d(TAG, "Interrupted in audioq.take");
				}
			}
		}
	}

	public void start() {
		Logger.v(TAG, "signalling START");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Logger.v(TAG, "signalled START");
			this.mailbox = Event.START;
		}
	}

	public void stop() {
		Logger.v(TAG, "signalling STOP");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Logger.v(TAG, "signalled STOP");
			this.mailbox = Event.STOP;
		}
	}

	public void shutdown() {
		Logger.v(TAG, "signalling SHUTDOWN");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Logger.v(TAG, "signalled SHUTDOWN");
			this.mailbox = Event.SHUTDOWN;
		}
	}

	public boolean isDone() {
		return done;
	}
	
	public void setModelPath(String dictionaryPath, String languageModelPath) {
		Config c = ps.getConfig();
		
		if (isFileExits(dictionaryPath)) {
			c.setString("-dict", dictionaryPath);
		} else {
			Logger.e(TAG, "File not exits:" + dictionaryPath + ", set to default!");
			
			setDefaultDic(c);
		}
		
		if (isFileExits(languageModelPath)) {
			if (isJSGF(languageModelPath)) {
				c.setString("-jsgf", languageModelPath);
				c.setString("-lm", null);
			} else {
				c.setString("-jsgf", null);
				c.setString("-lm", languageModelPath);
			}
		} else {
			Logger.e(TAG, "File not exits:" + languageModelPath + ", set to default!");
			
			setDefaultLm(c);
		}

		int result = ps.setConfig(c);
		if (result < 0) {
            Logger.e(TAG, "setConfig failed!!! " + languageModelPath);

            setDefaultLm(c);
            result = ps.setConfig(c);
            if (result < 0) {
                Logger.e(TAG, "setDefaultLm failed, too!!!!!!");
            }
        }

		dumpCfg();
	}
	
	public boolean isJSGF(String languageModelPath) {
		return languageModelPath.toLowerCase().endsWith("jsgf");
	}

	private boolean isFileExits(String filePath) {
    	return new File(filePath).exists();
	}
	
	private void dumpCfg() {
		Logger.i(TAG, "run on cfg:");
		Logger.i(TAG, "-hmm " + ps.getConfig().getString("-hmm"));
		Logger.i(TAG, "-dict " + ps.getConfig().getString("-dict"));
		Logger.i(TAG, "-lm " + ps.getConfig().getString("-lm"));
		Logger.i(TAG, "-jsgf " + ps.getConfig().getString("-jsgf"));
        Logger.i(TAG, "-backtrace " + ps.getConfig().getBoolean("-backtrace"));
        Logger.i(TAG, "-bestpath " + ps.getConfig().getBoolean("-bestpath"));
        Logger.i(TAG, "-verbose " + ps.getConfig().getBoolean("-verbose"));
        Logger.i(TAG, "-samprate " + ps.getConfig().getFloat("-samprate"));
	}
	
	private void setCommonCfg(Config c) {
		if (asrConfig.isDebug())
			setDebugCfg(c);

		c.setString("-hmm", asrConfig.POCKETSPHINX_CFG_HMMDIR_HUB4);
		c.setFloat("-samprate", RECORDER_SAMPLERATE_FLOAT);
		c.setInt("-maxhmmpf", 2000);
		c.setInt("-maxwpf", 10);
		c.setInt("-pl_window", 2);
		c.setBoolean("-backtrace", true);
		c.setBoolean("-bestpath", false);
	}
	
	private void setDebugCfg(Config c) {
		final String debugDir = asrConfig.getBaseCacheDir() + "/edu.cmu.pocketsphinx";
		
		File dir = new File(debugDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		pocketsphinx.setLogfile(debugDir + File.separator + "pocketsphinx.log");
		c.setBoolean("-verbose", true);
		c.setString("-rawlogdir", asrConfig.getAsrHMMDir());
	}
	
	private void setDefaultDic(Config c) {
		if (isFileExits(asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT)) {
			c.setString("-dict", asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT);
		} else {
			Logger.e(TAG, "!!Default file not exits:" + asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT);
		}
	}
	
	private void setDefaultLm(Config c) {
		if (isFileExits(asrConfig.POCKETSPHINX_CFG_DEFAULT_LM)) {
			c.setString("-jsgf", asrConfig.POCKETSPHINX_CFG_DEFAULT_LM);
		} else {
			Logger.e(TAG, "!!Default file not exits:" + asrConfig.POCKETSPHINX_CFG_DEFAULT_LM);
		}
	}
	
//	public static String getDefaultDicPath() {
//		return asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT;
//	}
	

	private void notifyHyp(Hypothesis hyp) {
		if (this.rl != null) {
			if (hyp == null) {
				Logger.d(TAG, "Recognition failure");
				this.rl.onError(-1);
			}
			else {
				Bundle b = new Bundle();
				Logger.d(TAG, "Final hypothesis: " + hyp.getHypstr());
				Logger.d(TAG, "Final uttid     : " + hyp.getUttid());
				Logger.d(TAG, "Final score     : " + hyp.getBest_score());

				b.putString("hyp", hyp.getHypstr());
				this.rl.onResults(b);
			}
		}
	}

}
