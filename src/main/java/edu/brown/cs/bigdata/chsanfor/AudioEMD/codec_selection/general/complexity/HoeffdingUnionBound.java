package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.ConfidenceInterval;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;

import java.util.List;

/**
 * Created by Clayton on 11/13/18.
 */
public class HoeffdingUnionBound implements EmpiricalComplexity {
    @Override
    public double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples) {
        return 0;
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS) {
        return 0;
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS, int samplesToUse) {
        return 0;
    }

    @Override
    public ConfidenceInterval getConfidenceInterval(
            double empiricalMean,
            double complexity,
            double delta,
            double numSamples,
            double numCriteria,
            double numFunctions) {
        double radius = Math.sqrt(Math.log(2. * numFunctions * numCriteria / delta) / 2. / numSamples);
        return new ConfidenceInterval(
                delta,
                empiricalMean + radius,
                empiricalMean - radius);
    }
}
