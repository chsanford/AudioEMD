package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.CompressionFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.CompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;

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
        return ((CompressionFunctionOutput) fx).getCriterion(NAME);
    }
}

