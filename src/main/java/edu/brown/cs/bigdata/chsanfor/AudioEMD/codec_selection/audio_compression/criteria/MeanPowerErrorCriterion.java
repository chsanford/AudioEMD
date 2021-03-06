package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

/**
 * Obtains the squared error between the original and decompressed files
 */
public class MeanPowerErrorCriterion extends WavDivergenceCriterion {
    private double power;

    public MeanPowerErrorCriterion(double power) {
        this.power = power;
    }

    private static String NAME = "MEAN_POWER_ERROR";

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

    public double getPower() {
        return power;
    }

    @Override
    public double computeCriterion(AudioSequence originalSeq, AudioSequence decompressedSeq) {
        return decompressedSeq.error(originalSeq, power) / (originalSeq.getSequenceLength());
    }
}
