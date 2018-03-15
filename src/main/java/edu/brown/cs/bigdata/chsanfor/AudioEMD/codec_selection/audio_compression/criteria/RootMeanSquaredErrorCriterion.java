package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

/**
 * Created by Clayton on 3/13/18.
 */
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Obtains the root mean squared error between the original and decompressed files
 */
public class RootMeanSquaredErrorCriterion extends Criterion {
    private static String NAME = "ROOT_MEAN_SQUARED_ERROR";

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