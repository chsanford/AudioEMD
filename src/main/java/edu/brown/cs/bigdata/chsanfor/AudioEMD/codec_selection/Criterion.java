package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

import java.util.List;

/**
 * Some quantity that we wish to measure about the output of the function applied to a sample
 */
public abstract class Criterion {
    /**
     * Note: despite the redundancy, we include all three values to avoid having to recompute f if it is
     *              time-inefficient
     * @param x sample point
     * @param f function to apply to sample
     * @param fx f(x), output of function applied to sample
     * @return value of the criterion
     */
    public abstract double apply(Sample x, Function f, FunctionOutput fx);

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
            criterionOutput[i] = apply(samples.get(i), f, functionOutput.get(i));
        }
        return criterionOutput;
    }
}
