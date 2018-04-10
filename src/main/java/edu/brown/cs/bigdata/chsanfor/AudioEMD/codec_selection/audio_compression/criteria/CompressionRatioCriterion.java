package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Obtains the ratio of the size of the compressed file to that of the uncompressed file
 */
public class CompressionRatioCriterion extends Criterion {

    public static String NAME = "COMPRESSION_RATIO";

    public static String getName() {
        return NAME;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return ((EncodingFunctionOutput) fx).getCriterion(this);
    }

    @Override
    public String toString() {
        return getName();
    }
}

