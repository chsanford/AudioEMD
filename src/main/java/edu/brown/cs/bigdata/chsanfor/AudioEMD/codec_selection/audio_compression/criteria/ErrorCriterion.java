package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

/**
 * Created by Clayton on 2/27/18.
 */
public class ErrorCriterion extends Criterion {
    @Override
    public double apply(Sample x, Function f, FunctionOutput fx) {
        AudioSequence xCast = (AudioSequence) x;
        AudioSequence fxCast = (AudioSequence) fx;
        return xCast.error(fxCast) / xCast.getSequenceLength();
    }
}
