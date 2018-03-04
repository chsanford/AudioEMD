package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import java.util.List;

/**
 * Some quantity that we wish to measure about the output of the function applied to a sample
 */
public abstract class Criterion {

    /**
     * @param fx f(x), output of function applied to sample
     * @return value of the criterion
     */
    public abstract double apply(FunctionOutput fx);

    /**
     * Applies the criterion to a list of samples which have the same function applied
     * @param samples list of sample points
     * @param f function to apply to samples
     * @return values of criterion for each sample
     */
    public double[] applyAll(List<Sample> samples, Function f) {
        List<FunctionOutput> functionOutput = f.applyAll(samples);
        double[] criterionOutput = new double[functionOutput.size()];
        for (int i = 0; i < criterionOutput.length; i++) {
            criterionOutput[i] = apply(functionOutput.get(i));
        }
        return criterionOutput;
    }

    /**
     *
     * @return a strings representing the type of criterion
     */
    public abstract String toString();
}
