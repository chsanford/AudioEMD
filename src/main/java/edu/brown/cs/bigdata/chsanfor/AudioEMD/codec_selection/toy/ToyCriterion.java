package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.toy;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;

/**
 * Created by Clayton on 3/15/18.
 */
public class ToyCriterion extends Criterion {
    private double param;

    public ToyCriterion(double param) {
        this.param = param;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }
}
