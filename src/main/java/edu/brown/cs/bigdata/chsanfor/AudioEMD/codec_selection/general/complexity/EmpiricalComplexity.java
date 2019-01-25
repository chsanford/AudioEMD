package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.ConfidenceInterval;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;

import java.util.List;

/**
 * Represents an empirical sample complexity that can be computed to create confidence intervals
 */
public interface EmpiricalComplexity {

    /**
     * Computes the sample complexity for a criterion applied to a function class
     * @param c a criterion applied to a given function class
     * @param functionClass a set of functions that can be applied to samples
     * @param samples randomly chosen data points
     * @return a measure of complexity, which can be used to make bounds
     */
    double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples);

    /**
     * Computes the sample complexity for a criterion applied to a function class
     * @param criterionValuesFS gives the value of a criterion evaluated for each certain function on each sample
     * @return a measure of complexity, which can be used to make bounds
     */
    double getComplexity(Double[][] criterionValuesFS);

    /**
     * Computes the sample complexity for a criterion applied to a function class, where only the first samplesToUse
     * samples are used
     * @param criterionValuesFS gives the value of a criterion evaluated for each certain function on each sample
     * @param samplesToUse gives number of samples in criterionValuesFS to use in complexity calculation
     * @return a measure of complexity, which can be used to make bounds
     */
    double getComplexity(Double[][] criterionValuesFS, int samplesToUse);

    /**
     * Obtains a confidence interval based on the complexity obtained
     * @param empiricalMean the empirically-estimated mean for the criterion applied to the function class
     * @param complexity the complexity value for the corresponding criterion and function class
     * @param delta the maximum probability of failure
     * @param numSamples number of samples used to compute empirical mean and complexity
     * @param numCriteria number of criteria to consider
     * @return ConfidenceInterval object
     */
    ConfidenceInterval getConfidenceInterval(
            double empiricalMean,
            double complexity,
            double delta,
            double numSamples,
            double numCriteria,
            double numFunctions);
}
