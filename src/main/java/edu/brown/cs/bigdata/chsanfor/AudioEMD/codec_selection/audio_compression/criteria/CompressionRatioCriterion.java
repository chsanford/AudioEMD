package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.CompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;

/**
 * Created by Clayton on 2/27/18.
 */
public class CompressionRatioCriterion extends Criterion {
    @Override
    public double apply(Sample x, Function f, FunctionOutput fx) {
        AudioSequence xCast = (AudioSequence) x;
        CompressionFunction fCast = (CompressionFunction) f;

        File tempCompressed = new File("data/temp/temp_compressed." + fCast.getScheme());
        fCast.compress(xCast, tempCompressed);

        double compressionRatio = 1.0 * tempCompressed.length() / xCast.getAudioFile().length();

        tempCompressed.delete();

        return compressionRatio;
    }
}
