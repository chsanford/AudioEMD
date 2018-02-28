package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;

/**
 * Created by Clayton on 2/12/18.
 */
public class GSMCompressionFunction extends SoxCompressionFunction {
    public GSMCompressionFunction() {
        super("gsm");
    }

    @Override
    public String getScheme() {
        return "gsm";
    }
}
