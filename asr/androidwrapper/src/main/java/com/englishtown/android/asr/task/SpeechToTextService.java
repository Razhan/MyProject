package com.englishtown.android.asr.task;

public interface SpeechToTextService {
	
	public interface Callback {
		void onSttResult(String normalizedResult);
	}
	
	void setModelPath(String dictionaryPath, String languageModelPath);

	/**
	 * Start listening, callback will be called with result as soon as any exists until stopListening is called.
	 * @param callback
	 */
	void startListening(Callback callback);
	void stopListening();
	boolean isListening();

}
