package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The brute force algorithm for bounding performances of functions on different criteria and checking with confidence
 * whether constraints are satisfied.
 */
public class BruteForce {
    private EmpiricalComplexity complexity;

    /**
     *
     * @param complexity a complexity measure to measure the complexity of the function class on each criterion
     *                   (e.g. Rademacher complexity)
     */
    public BruteForce(EmpiricalComplexity complexity) {
        this.complexity = complexity;
    }

    /**
     *
     * @param samples a list of sample data points that functions operate on
     * @param functionClass functions that map sample points to outputs that are analyzed by criteria
     * @param criteria measurements of various quantities of function outputs
     * @param objective a linear combination of criteria that functions aim to maximize
     * @param constraint a set of linear inequalities that must be satisfied by the optimal function
     * @param delta a probability of error
     * @return a BruteForceOutput object with the optimal function, empirical estimates for its value for each
     *                  critierion, confidence bounds on those values, and an upper bound on the objective
     * @throws InsufficientSampleSizeException if not enough data is present to ensure that at least one function will
     *                  certainly satisfy the constraints
     */
    public BruteForceOutput runAlgorithm(
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta) throws InsufficientSampleSizeException {

        double[][] empiricalMeansFC = new double[functionClass.size()][criteria.size()];
        ConfidenceInterval[][] confidenceIntervalsFC = new ConfidenceInterval[functionClass.size()][criteria.size()];

        for (int i = 0; i < criteria.size(); i++) {
            // Computes complexity for each criterion
            Criterion c = criteria.get(i);
            double complexityC = complexity.getComplexity(c, functionClass, samples);
            for (int j = 0; j < functionClass.size(); j++) {
                Function f = functionClass.get(j);

                // Estimates the value of each criterion for each function
                empiricalMeansFC[i][j] = 0;
                double[] valuesCF = c.applyAll(samples, f);
                for (double v : valuesCF) {
                    empiricalMeansFC[i][j] += v;
                }

                // Bounds those estimates
                confidenceIntervalsFC[i][j] = complexity.getConfidenceInterval(
                        empiricalMeansFC[i][j],
                        complexityC,
                        delta,
                        samples.size(),
                        criteria.size());
            }
        }

        Integer optimalFIndex = null;
        Double optimalLowerBoundF = null;
        Double maxUpperBound = null;
        for (int i = 0; i < functionClass.size(); i++) {
            // For functions that we are confident are valid, we find the one with the greatest lower-bound objective
            if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC[i])) {
                double lowerBound = objective.minRectangle(confidenceIntervalsFC[i]);
                if (optimalFIndex == null || lowerBound < optimalLowerBoundF) {
                    optimalLowerBoundF = lowerBound;
                    optimalFIndex = i;
                }
            // For functions that we are confident are invalid, then we find an upper bound on the objective function
            } else if (constraint.isNeverValidRectangle(confidenceIntervalsFC[i])) {
                double upperBound = objective.maxRectangle(confidenceIntervalsFC[i]);
                if (maxUpperBound == null || upperBound > maxUpperBound) {
                    maxUpperBound = upperBound;
                }
            }
        }

        // If no valid function is found, throw exception
        if (optimalFIndex == null) {
            throw new InsufficientSampleSizeException();
        }

        return new BruteForceOutput(
                functionClass.get(optimalFIndex),
                empiricalMeansFC[optimalFIndex],
                confidenceIntervalsFC[optimalFIndex],
                maxUpperBound);
    }
}
