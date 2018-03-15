package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;

import java.util.List;

/**
 * Created by Clayton on 2/12/18.
 */
public class GSMEncodingFunction extends SoxEncodingFunction {
    public GSMEncodingFunction(List<Criterion> criteria) {
        super("gsm", criteria);
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
