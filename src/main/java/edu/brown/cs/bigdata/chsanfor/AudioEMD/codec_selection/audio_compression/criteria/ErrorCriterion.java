package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.CompressionFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

/**
 * Obtains the squared error between the original and decompressed files
 */
public class ErrorCriterion extends Criterion {
    private static String NAME = "ERROR";

    public static String getName() {
        return NAME;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return ((CompressionFunctionOutput) fx).getCriterion(NAME);
    }
}
