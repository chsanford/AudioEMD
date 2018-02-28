package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;

/**
 * Created by Clayton on 2/9/18.
 */
public class OggCompressionFunction extends SoxCompressionFunction {
    public OggCompressionFunction() {
        super("ogg");
    }

    @Override
    public String getScheme() {
        return "ogg";
    }
}
