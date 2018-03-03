package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

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
        for (Function f : functionClass) {
            RealVector cfVector = MatrixUtils.createRealVector(c.applyAll(samples, f));
            cfVector.mapSubtract(0.5);
            double functionComplexity = cfVector.dotProduct(sigmaVector) / samples.size();
            if (maxComplexity == null || functionComplexity > maxComplexity) {
                maxComplexity = functionComplexity;
            }
        }
        return Math.max(0, maxComplexity);
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS) {
        int numSamples = criterionValuesFS[0].length;
        int numFunctions = criterionValuesFS.length;
        Random rand = new Random();
        double[] sigma = new double[numSamples];
        for (int s = 0; s < sigma.length; s++) {
            if (rand.nextBoolean()) sigma[s] = 1;
            else sigma[s] = -1;
        }
        RealVector sigmaVector = MatrixUtils.createRealVector(sigma);

        Double maxComplexity = null;
        for (int f = 0; f < numFunctions; f++) {
            double[] vectorInput = new double[criterionValuesFS[f].length];
            for (int c = 0; c < vectorInput.length; c++) {
                vectorInput[c] = criterionValuesFS[f][c];
            }
            RealVector cfVector = MatrixUtils.createRealVector(vectorInput);
            cfVector.mapSubtract(0.5);
            double functionComplexity = cfVector.dotProduct(sigmaVector) / numSamples;
            if (maxComplexity == null || functionComplexity > maxComplexity) {
                maxComplexity = functionComplexity;
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
        return new ConfidenceInterval(
                delta,
                Math.min(1, empiricalMean + radius),
                Math.max(0, empiricalMean - radius));
    }


}
