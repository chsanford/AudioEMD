package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;

import java.util.List;

/**
 * Created by Clayton on 2/9/18.
 */
public class OggEncodingFunction extends SoxEncodingFunction {
    public OggEncodingFunction(List<Criterion> criteria) {
        super("ogg", criteria);
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
