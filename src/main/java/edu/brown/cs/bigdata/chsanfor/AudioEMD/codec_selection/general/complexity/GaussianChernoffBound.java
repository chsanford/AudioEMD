package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.ConfidenceInterval;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;

import java.util.List;

/**
 * Created by Clayton on 11/25/18.
 */
public class GaussianChernoffBound implements EmpiricalComplexity {
    @Override
    public double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples) {
        return 0;
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS) {
        return getComplexity(criterionValuesFS, criterionValuesFS[0].length);
    }

    @Override
    /** This is not a true complexity measure!!!! Rather, this just computes the plugin variance; we use this format so
     * that it mixes most easily with the code
     */
    public double getComplexity(Double[][] criterionValuesFS, int samplesToUse) {
        double pluginVariance = 0;
        for (int f = 0; f < criterionValuesFS.length; f++) {
            double meanCriterionValue = 0;
            double meanSquaredCriterionValue = 0;
            for (int s = 0; s < samplesToUse; s++) {
                meanCriterionValue += (criterionValuesFS[f][s] / samplesToUse);
                meanSquaredCriterionValue += (Math.pow(criterionValuesFS[f][s], 2) / samplesToUse);
            }
            pluginVariance = Math.max(pluginVariance, meanSquaredCriterionValue - Math.pow(meanCriterionValue, 2));
        }
        return pluginVariance;
    }

    @Override
    public ConfidenceInterval getConfidenceInterval(
            double empiricalMean,
            double complexity,
            double delta,
            double numSamples,
            double numCriteria,
            double numFunctions) {
        double radius = Math.sqrt(2 * complexity * Math.log(2. * numFunctions / delta) / numSamples);
        return new ConfidenceInterval(
                delta,
                Math.min(empiricalMean + radius, 1),
                Math.max(empiricalMean - radius, 0));
    }
}
