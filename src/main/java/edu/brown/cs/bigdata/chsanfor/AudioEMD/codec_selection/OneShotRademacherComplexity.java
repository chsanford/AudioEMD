package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;
import java.util.Random;

/**
 * Represents the computation and analysis of the one-shot Rademacher complexity
 */
public class OneShotRademacherComplexity implements EmpiricalComplexity {
    @Override
    public double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples) {
        Random rand = new Random();
        double[] sigma = new double[samples.size()];
        for (int i = 0; i < sigma.length; i++) {
            if (rand.nextBoolean()) sigma[i] = 1;
            else sigma[i] = -1;
        }
        RealVector sigmaVector = MatrixUtils.createRealVector(sigma);

        Double maxComplexity = null;
        Function maxFunction = null;
        for (Function f : functionClass) {
            RealVector cfVector = MatrixUtils.createRealVector(c.applyAll(samples, f));
            cfVector.mapSubtract(0.5);
            double functionComplexity = cfVector.dotProduct(sigmaVector) / samples.size();
            if (maxFunction == null || functionComplexity > maxComplexity) {
                maxComplexity = functionComplexity;
                maxFunction = f;
            }
        }
        return Math.max(0, maxComplexity);
    }

    @Override
    public ConfidenceInterval getConfidenceInterval(
            double empiricalMean,
            double complexity,
            double delta,
            double numSamples,
            double numCriteria) {
        double radius = 2 * complexity + 3 * Math.sqrt(Math.log(2 * numCriteria / delta) / (2 * numSamples));
        return new ConfidenceInterval(delta, empiricalMean + radius, empiricalMean - radius);
    }


}
