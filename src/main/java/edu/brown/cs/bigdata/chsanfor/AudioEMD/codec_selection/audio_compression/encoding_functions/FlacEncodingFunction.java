package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

/**
 * Created by Clayton on 2/9/18.
 */
public class FlacEncodingFunction extends SoxEncodingFunction {
    public FlacEncodingFunction() {
        super("flac");
    }

    @Override
    public String getScheme() {
        return "flac";
    }

    @Override
    public String toString() {
        return "Flac Compression";
    }
}
