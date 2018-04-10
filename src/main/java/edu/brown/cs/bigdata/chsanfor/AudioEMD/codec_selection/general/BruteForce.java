package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

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
     * @return a AlgorithmSelectionOutput object with the optimal function, empirical estimates for its value for each
     *                  critierion, confidence bounds on those values, and an upper bound on the objective
     * @throws InsufficientSampleSizeException if not enough data is present to ensure that at least one function will
     *                  certainly satisfy the constraints
     */
    public AlgorithmSelectionOutput runAlgorithm(
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta) throws InsufficientSampleSizeException {

        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][samples.size()];

        for (int f = 0; f < functionClass.size(); f++) {
            for (int s = 0; s < samples.size(); s++) {
                FunctionOutput fOut = functionClass.get(f).apply(samples.get(s));
                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = criteria.get(c).apply(fOut);
                }
            }
        }

        Double[][] empiricalMeansFC = new Double[functionClass.size()][criteria.size()];
        ConfidenceInterval[][] confidenceIntervalsFC = new ConfidenceInterval[functionClass.size()][criteria.size()];

        for (int c = 0; c < criteria.size(); c++) {
            // Computes complexity for each criterion
            double complexityC = complexity.getComplexity(criterionValuesCFS[c]);

            for (int f = 0; f < functionClass.size(); f++) {

                // Estimates the value of each criterion for each function
                empiricalMeansFC[f][c] = 0.;
                for (double v : criterionValuesCFS[c][f]) {
                    empiricalMeansFC[f][c] += (v / samples.size());
                }
                System.out.println(empiricalMeansFC[f][c]);

                // Bounds those estimates
                confidenceIntervalsFC[f][c] = complexity.getConfidenceInterval(
                        empiricalMeansFC[f][c],
                        complexityC,
                        delta,
                        samples.size(),
                        criteria.size());

            }
        }

        Integer optimalFIndex = null;
        Double optimalLowerBoundF = null;
        Double maxUpperBound = null;
        for (int f = 0; f < functionClass.size(); f++) {
            // For functions that we are confident are valid, we find the one with the greatest lower-bound objective
            if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC[f])) {
                double lowerBound = objective.minRectangle(confidenceIntervalsFC[f]);
                if (optimalFIndex == null || lowerBound < optimalLowerBoundF) {
                    optimalLowerBoundF = lowerBound;
                    optimalFIndex = f;
                }
            }
            // For functions that may be valid, then we find an upper bound on the objective function
            if (!constraint.isNeverValidRectangle(confidenceIntervalsFC[f])) {
                double upperBound = objective.maxRectangle(confidenceIntervalsFC[f]);
                if (maxUpperBound == null || upperBound > maxUpperBound) {
                    maxUpperBound = upperBound;
                }
            }
        }

        // If no valid function is found, throw exception
        if (optimalFIndex == null) {
            throw new InsufficientSampleSizeException();
        }

        return new AlgorithmSelectionOutput(
                functionClass.get(optimalFIndex),
                empiricalMeansFC[optimalFIndex],
                confidenceIntervalsFC[optimalFIndex],
                maxUpperBound);
    }
}
