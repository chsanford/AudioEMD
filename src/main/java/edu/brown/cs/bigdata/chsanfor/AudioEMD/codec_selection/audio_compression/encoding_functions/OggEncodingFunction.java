package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

/**
 * Created by Clayton on 2/9/18.
 */
public class OggEncodingFunction extends SoxEncodingFunction {
    public OggEncodingFunction() {
        super("ogg");
    }

    @Override
    public String getScheme() {
        return "ogg";
    }

    @Override
    public String toString() {
        return "Ogg Compression";
    }
}
