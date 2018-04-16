package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the computation and analysis of the one-shot Rademacher complexity
 */
public class EMDComplexity implements EmpiricalComplexity {

    private RealVector getSigmaVector(int numSamples) {
        Random rand = new Random();
        assert numSamples % 2 == 0;
        List<Double> sigma = new ArrayList<Double>();
        for (int i = 0; i < numSamples; i++) {
            if (i % 2 == 0) sigma.add(1.);
            else sigma.add(-1.);
        }
        Collections.shuffle(sigma);
        return MatrixUtils.createRealVector(
                sigma.stream().mapToDouble(Double::doubleValue).toArray());
    }

    @Override
    public double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples) {
        RealVector sigmaVector = getSigmaVector(samples.size());

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

        RealVector sigmaVector = getSigmaVector(numSamples);

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
