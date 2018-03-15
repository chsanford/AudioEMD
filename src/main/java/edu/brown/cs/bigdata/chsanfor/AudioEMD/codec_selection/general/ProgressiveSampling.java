package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import com.sun.tools.javac.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The progressive sampling for bounding performances of functions on different criteria and checking with confidence
 * whether constraints are satisfied.
 */
public class ProgressiveSampling {
    private EmpiricalComplexity complexity;

    /**
     *
     * @param complexity a complexity measure to measure the complexity of the function class on each criterion
     *                   (e.g. Rademacher complexity)
     */
    public ProgressiveSampling(EmpiricalComplexity complexity) {
        this.complexity = complexity;
    }

    /**
     *
     * @param samples a list of sample data points that functions operate on
     * @param functionClass functions that map sample points to outputs that are analyzed by criteria
     * @param criteria measurements of various quantities of function outputs
     * @param objective a linear combination of criteria that functions aim to maximize
     * @param constraint a set of linear inequalities that must be satisfied by the optimal function
     * @param delta an upper bound on the probability of error
     * @return a AlgorithmSelectionOutput object with the optimal function, empirical estimates for its value for each
     *                  critierion, confidence bounds on those values, and an upper bound on the objective
     * @throws InsufficientSampleSizeException if not enough data is present to ensure that at least one function will
     *                  certainly satisfy the constraints
     */
    public AlgorithmSelectionOutput runAlgorithm(
            List<Sample> samples,
            int initialSampleSize,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double epsilon,
            double delta)
            throws InsufficientSampleSizeException, NoSatisfactoryFunctionsException, EmptyConfidenceIntervalException {

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = (int) Math.floor(Math.log(1. * samples.size() / initialSampleSize + 1) / Math.log(2));

        List<Double[]> empiricalMeansFC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsFC = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            empiricalMeansFC.add(new Double[criteria.size()]);
            confidenceIntervalsFC.add(new ConfidenceInterval[criteria.size()]);
        }


        for (int i = 0; i < maxIterations; i++) {

            System.out.println("Iteration " + i + " of " + maxIterations + " (" + sampleSize + " samples)");


            List<Sample> currentSamples = samples.subList(firstSample, firstSample + sampleSize);

            Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][currentSamples.size()];

            for (int f = 0; f < functionClass.size(); f++) {
                for (int s = 0; s < currentSamples.size(); s++) {
                    FunctionOutput fOut = functionClass.get(f).apply(currentSamples.get(s));
                    for (int c = 0; c < criteria.size(); c++) {
                        criterionValuesCFS[c][f][s] = criteria.get(c).apply(fOut);
                    }
                }
            }

            for (int c = 0; c < criteria.size(); c++) {
                // Computes complexity for each criterion
                double complexityC = complexity.getComplexity(criterionValuesCFS[c]);

                for (int f = 0; f < functionClass.size(); f++) {

                    // Estimates the value of each criterion for each function
                    empiricalMeansFC.get(f)[c] = 0.;
                    for (double v : criterionValuesCFS[c][f]) {
                        empiricalMeansFC.get(f)[c] += (v / sampleSize);
                    }

                    // Bounds those estimates
                    ConfidenceInterval newInterval = complexity.getConfidenceInterval(
                            empiricalMeansFC.get(f)[c],
                            complexityC,
                            delta / maxIterations,
                            currentSamples.size(),
                            criteria.size());
                    if (confidenceIntervalsFC.get(f)[c] == null) {
                        confidenceIntervalsFC.get(f)[c] = newInterval;
                    } else {
                        confidenceIntervalsFC.get(f)[c] = new ConfidenceInterval(
                                newInterval.getDelta(),
                                Math.min(confidenceIntervalsFC.get(f)[c].getUpperBound(), newInterval.getUpperBound()),
                                Math.max(confidenceIntervalsFC.get(f)[c].getLowerBound(), newInterval.getLowerBound()));
                    }
                    if (confidenceIntervalsFC.get(f)[c].getLowerBound() >
                            confidenceIntervalsFC.get(f)[c].getUpperBound()) {
                        throw new EmptyConfidenceIntervalException();
                    }

                }
            }

            Integer optimalFIndex = null;
            Double optimalLowerBoundF = null;
            Double maxUpperBound = null;

            List<Integer> toRemove = new ArrayList<>();

            for (int f = 0; f < functionClass.size(); f++) {
                // For functions that we are confident are valid, we find the one with the greatest lower-bound objective
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                    double lowerBound = objective.minRectangle(confidenceIntervalsFC.get(f));
                    if (optimalFIndex == null || lowerBound < optimalLowerBoundF) {
                        optimalLowerBoundF = lowerBound;
                        optimalFIndex = f;
                    }
                }
                // For functions that may be valid, then we find an upper bound on the objective function
                if (!constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                    double upperBound = objective.maxRectangle(confidenceIntervalsFC.get(f));
                    if (maxUpperBound == null || upperBound > maxUpperBound) {
                        maxUpperBound = upperBound;
                    }
                } else {
                    // Remove as a valid function if no intersection with viable region
                    toRemove.add(f);
                }
            }

            if (optimalFIndex != null) {
                for (int f = 0; f < functionClass.size(); f++) {
                    if ((objective.maxRectangle(confidenceIntervalsFC.get(f)) <
                            objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex)))
                            && !toRemove.contains(f)) {
                        toRemove.add(f);
                    }
                }
            }

            Collections.sort(toRemove, Collections.reverseOrder());
            for (int f : toRemove) {
                functionClass.remove(f);
                empiricalMeansFC.remove(f);
                confidenceIntervalsFC.remove(f);
            }

            if (functionClass.size() == 0) {
                throw new NoSatisfactoryFunctionsException();
            }

            if (optimalFIndex != null &&
                    (objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex)) >=
                    objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex)) - epsilon)) {
                return new AlgorithmSelectionOutput(
                        functionClass.get(optimalFIndex),
                        empiricalMeansFC.get(optimalFIndex),
                        confidenceIntervalsFC.get(optimalFIndex),
                        maxUpperBound);
            }

            firstSample += sampleSize;
            sampleSize *= 2;

            for (int f = 0; f < functionClass.size(); f++) {
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                    System.out.println("Function " + f + ": " + functionClass.get(f).toString() + "(valid)");
                } else if (constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                    System.out.println("Function " + f + ": " + functionClass.get(f).toString() + "(invalid)");
                } else {
                    System.out.println("Function " + f + ": " + functionClass.get(f).toString() + "(undetermined)");
                }
                double[] currentMeans = new double[criteria.size()];
                for (int c = 0; c < criteria.size(); c++) currentMeans[c] = empiricalMeansFC.get(f)[c];
                System.out.println("- Objective " + objective.compute(currentMeans) + " in [" + objective.minRectangle(confidenceIntervalsFC.get(f))
                        + ", " + objective.maxRectangle(confidenceIntervalsFC.get(f)) + "]");
                for (int c = 0; c < criteria.size(); c++) {
                    System.out.println("- Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                            empiricalMeansFC.get(f)[c] + " in " +
                            confidenceIntervalsFC.get(f)[c].toString());
                }
            }
            System.out.println();
            

        }

        // If no valid function is found, throw exception

        throw new InsufficientSampleSizeException();


    }
}
