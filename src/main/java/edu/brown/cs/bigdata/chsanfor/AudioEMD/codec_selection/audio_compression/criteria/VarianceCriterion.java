package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Represents a crierion that places bounds on variance; in order for this to work, both the first and second moment
 * must be bounded
 */
public class VarianceCriterion extends Criterion {
    private Criterion baseCriterion;
    private Criterion squaredCriterion;
    private String name;

    public VarianceCriterion(Criterion baseCriterion) {
        this.baseCriterion = baseCriterion;
        this.squaredCriterion = new RawMomentCriterion(baseCriterion, 2);
        this.name = baseCriterion.toString() + " VARIANCE";
    }

    @Override
    /**
     * TODO: make this a more meaningful value?
     */
    public double apply(FunctionOutput fx) {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public Criterion getBaseCriterion() {
        return baseCriterion;
    }

    public Criterion getSquaredCriterion() {
        return squaredCriterion;
    }
}
