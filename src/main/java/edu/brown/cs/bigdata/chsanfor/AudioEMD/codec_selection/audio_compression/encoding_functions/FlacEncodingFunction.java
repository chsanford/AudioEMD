package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;

import java.util.List;

/**
 * Created by Clayton on 2/9/18.
 */
public class FlacEncodingFunction extends SoxEncodingFunction {
    public FlacEncodingFunction(List<Criterion> criteria) {
        super("flac", criteria);
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
