package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;


/**
 * Created by Clayton on 2/9/18.
 */
public class MP3EncodingFunction extends SoxEncodingFunction {
    private Runtime run = Runtime.getRuntime();

    public MP3EncodingFunction() {
        super("mp3");
    }

    @Override
    public String getScheme() {
        return "mp3";
    }

    @Override
    public String toString() {
        return "MP3 Compression";
    }
}
