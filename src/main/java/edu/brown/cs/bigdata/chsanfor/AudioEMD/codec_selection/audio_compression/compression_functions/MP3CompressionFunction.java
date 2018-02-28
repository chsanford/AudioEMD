package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;


/**
 * Created by Clayton on 2/9/18.
 */
public class MP3CompressionFunction extends SoxCompressionFunction {
    private Runtime run = Runtime.getRuntime();

    public MP3CompressionFunction() {
        super("mp3");
    }

    @Override
    public String getScheme() {
        return "mp3";
    }
}
