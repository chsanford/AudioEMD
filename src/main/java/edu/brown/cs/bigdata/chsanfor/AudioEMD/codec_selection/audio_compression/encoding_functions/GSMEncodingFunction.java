package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

/**
 * Created by Clayton on 2/12/18.
 */
public class GSMEncodingFunction extends SoxEncodingFunction {
    public GSMEncodingFunction() {
        super("gsm");
    }

    @Override
    public String getScheme() {
        return "gsm";
    }

    @Override
    public String toString() {
        return "GSM Compression";
    }
}
