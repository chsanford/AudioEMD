package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.CompressionFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Obtains the amount of time needed to compress an audio file
 */
public class CompressionTimeCriterion extends Criterion {
    private static String NAME = "COMPRESSION_TIME";

    public static String getName() {
        return NAME;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return ((CompressionFunctionOutput) fx).getCriterion(NAME);
    }
}
