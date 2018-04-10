package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.toy;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;

import java.util.Random;

/**
 * Created by Clayton on 3/15/18.
 */
public class ToyFunction extends Function {
    private double out;
    private Random rand = new Random();

    public ToyFunction(double out) {
        this.out = out;
    }

    @Override
    public FunctionOutput apply(Sample x) {
        return new ToyFunctionOutput(out + rand.nextGaussian());
    }

    @Override
    public String toString() {
        return null;
    }
}
