package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Computes the raw moment for a different criterion raised to some power
 */
public class RawMomentCriterion extends MeanCriterion {

    private String name;
    private double power;
    private Criterion baseCriterion;

    public RawMomentCriterion(Criterion baseCriterion, double power) {
        name = baseCriterion.toString() + " ^ " + power;
        this.power = power;
        this.baseCriterion = baseCriterion;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return Math.pow(((EncodingFunctionOutput) fx).getCriterion(baseCriterion), power);
    }

    @Override
    public String toString() {
        return name;
    }

    public double getPower() {
        return power;
    }

    public Criterion getBaseCriterion() {
        return baseCriterion;
    }
}
