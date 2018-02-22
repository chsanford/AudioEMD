package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

import java.util.ArrayList;
import java.util.List;

/**
 * A function that can be applied to a sample
 */
public abstract class Function {

    public abstract FunctionOutput apply(Sample x);

    /**
     * Applies the function to a list of samples
     * @param samples list of sample points
     * @return values of the function at each sample
     */
    public List<FunctionOutput> applyAll(List<Sample> samples) {
        List<FunctionOutput> output = new ArrayList<>();
        for (Sample x : samples) {
            output.add(apply(x));
        }
        return output;
    }
}
