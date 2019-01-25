package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.ConfidenceInterval;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the computation and analysis of the one-shot Rademacher complexity
 */
public class OneShotRademacherComplexity implements EmpiricalComplexity {

    private double[] getSigma(int length) {
        Random rand = new Random();
        double[] sigma = new double[length];
        for (int i = 0; i < sigma.length; i++) {
            if (rand.nextBoolean()) sigma[i] = 1;
            else sigma[i] = -1;
        }
        return sigma;
    }

    private double[] negate(double[] sigma) {
        double[] negatedSigma = new double[sigma.length];
        for (int i = 0; i < sigma.length; i++) negatedSigma[i] = -1 * sigma[i];
        return negatedSigma;
    }

    @Override
    public double getComplexity(Criterion c, List<Function> functionClass, List<Sample> samples) {
        RealVector sigmaVector = MatrixUtils.createRealVector(getSigma(samples.size()));

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

    private double getComplexity(Double[][] criterionValuesFS, int samplesToUse, double[] sigma) {
        int numFunctions = criterionValuesFS.length;

        RealVector sigmaVector = MatrixUtils.createRealVector(sigma);

        Double maxComplexity = null;
        for (int f = 0; f < numFunctions; f++) {
            double[] vectorInput = new double[samplesToUse];
            for (int s = 0; s < vectorInput.length; s++) {
                vectorInput[s] = criterionValuesFS[f][s];
            }
            RealVector cfVector = MatrixUtils.createRealVector(vectorInput);
            cfVector.mapSubtract(0.5);
            double functionComplexity = cfVector.dotProduct(sigmaVector) / samplesToUse;
            if (maxComplexity == null || functionComplexity > maxComplexity) {
                maxComplexity = functionComplexity;
            }
        }
        return Math.max(0, maxComplexity);
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS) {
        return getComplexity(criterionValuesFS, criterionValuesFS[0].length);
    }

    @Override
    public double getComplexity(Double[][] criterionValuesFS, int samplesToUse) {
        return getComplexity(criterionValuesFS, samplesToUse, getSigma(samplesToUse));
    }

    /**
     * Computes the OSRC with one sample dropped
     * @param criterionValuesFS
     * @param samplesToUse total number of samples (including the dropped one)
     * @param sigma vector of iid Rademacher for all elements
     * @param dropIndex index of dropped sample
     * @return
     */
    public double getComplexityDrop(
            Double[][] criterionValuesFS,
            int samplesToUse,
            double[] sigma,
            int dropIndex) {
        return getComplexityDropSwap(
                criterionValuesFS,
                samplesToUse,
                sigma,
                dropIndex,
                dropIndex);
    }

    /**
     * Conmputes the OSRC where one sample's position is swapped with another, which is dropped
     * @param criterionValuesFS
     * @param samplesToUse total number of samples (including the dropped one)
     * @param sigma vector of iid Rademacher for all elements
     * @param dropIndex index of the dropped sample
     * @param swapIndex index of the sample that is moved to the position of the dropped sample
     * @return
     */
    public double getComplexityDropSwap(
            Double[][] criterionValuesFS,
            int samplesToUse,
            double[] sigma,
            int dropIndex,
            int swapIndex) {
        double[] sigmaCopy = sigma.clone();
        sigmaCopy[swapIndex] = sigmaCopy[dropIndex];
        sigmaCopy[dropIndex] = 0;
        return samplesToUse / (samplesToUse - 1) * getComplexity(
                criterionValuesFS,
                samplesToUse,
                sigmaCopy);
    }

    @Override
    public ConfidenceInterval getConfidenceInterval(
            double empiricalMean,
            double complexity,
            double delta,
            double samplesToUse,
            double numCriteria,
            double numFunctions) {
        double radius = 2 * complexity + 3 * Math.sqrt(Math.log(2 * numCriteria / delta) / (2 * samplesToUse));
        return new ConfidenceInterval(
                delta,
                Math.min(1, empiricalMean + radius),
                Math.max(0, empiricalMean - radius));
    }

    /**
     * Computes a high probability variance bound based on Cyrus's writeup on approximations
     * @param criterionValuesFS
     * @param samplesToUse
     * @param maxEstimateTerms number of samplings of terms taken and added up
     * @return
     */
    public double getVarianceBound(
            Double[][] criterionValuesFS,
            int samplesToUse,
            double[] sigma,
            int maxEstimateTerms
    ) {
        //double[] sigma = getSigma(samplesToUse);
        int totalOrderedPairs = samplesToUse * (samplesToUse - 1);

        // The total number of pair for the estimation cannot be greater than the total number of ordered pairs
        int numEstimateTerms = Math.min(maxEstimateTerms, totalOrderedPairs);

        List<Integer> termIndices;
        if (totalOrderedPairs <= 5000) {
            termIndices = IntStream.range(0, totalOrderedPairs).boxed().collect(Collectors.toList());
            // Determines the we select pairs of values to sum up
            Collections.shuffle(termIndices);
        } else {
            termIndices = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < numEstimateTerms; i++) {
                boolean indexIsNew = false;
                while (! indexIsNew) {
                    int index = random.nextInt(totalOrderedPairs);
                    if (! termIndices.contains(index)) {
                        indexIsNew = true;
                        termIndices.add(index);
                    }
                }
            }
        }



        double varianceBound = 0;
        for (int i = 0; i < numEstimateTerms; i++) {
            int dropIndex = termIndices.get(i) / (samplesToUse - 1);
            int swapIndex = termIndices.get(i) % (samplesToUse - 1);
            // Ensures that swap index and drop index have different values
            if (swapIndex >= dropIndex) swapIndex++;
            // Scales additional term in variance estimate to account for the terms we skip
            double complexityDrop = getComplexityDrop(
                    criterionValuesFS,
                    samplesToUse,
                    sigma,
                    dropIndex
            );
            double complexityDropSwap = getComplexityDropSwap(
                    criterionValuesFS,
                    samplesToUse,
                    sigma,
                    dropIndex,
                    swapIndex
            );
            varianceBound += totalOrderedPairs / numEstimateTerms / samplesToUse *
                    Math.pow(complexityDrop - complexityDropSwap, 2);
        }
        return varianceBound;
    }

    public ConfidenceInterval getApproximateConfidenceInterval(
            double empiricalMean,
            Double[][] criterionValuesFS,
            double delta,
            int samplesToUse,
            int maxEstimateTerms
    ) {
        double[] sigma = getSigma(samplesToUse);
        double complexity = getComplexity(criterionValuesFS, samplesToUse, sigma) / 2 +
                getComplexity(criterionValuesFS, samplesToUse, negate(sigma)) / 2;
        double varianceBound = getVarianceBound(criterionValuesFS, samplesToUse, sigma, maxEstimateTerms);
        double normalDistConfidence = new NormalDistribution().inverseCumulativeProbability(1 - delta);
        double radius = 2 * complexity + normalDistConfidence * Math.sqrt(varianceBound / samplesToUse);
        System.out.println("Complexity: " + complexity + ", Var Bound: " + varianceBound +
                ", Conf: " + normalDistConfidence + ", Radius: " + radius);
        return new ConfidenceInterval(
                delta,
                Math.min(empiricalMean + radius, 1),
                Math.max(empiricalMean - radius, 0));
    }

}
