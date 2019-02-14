package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.IncorrectlyClassifiedCriterionException;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EMDComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EmpiricalComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.OneShotRademacherComplexity;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The brute force algorithm for bounding performances of functions on different criteria and checking with confidence
 * whether constraints are satisfied.
 */
public class GlobalSampling {
    private EmpiricalComplexity complexity;

    /**
     *
     * @param complexity a complexity measure to measure the complexity of the function class on each criterion
     *                   (e.g. Rademacher complexity)
     */
    public GlobalSampling(EmpiricalComplexity complexity) {
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
            double delta,
            boolean isApproximation
    ) throws InsufficientSampleSizeException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        Double[][][] criterionValuesCFS = sampleCriterionComputations(
                samples,
                functionClass,
                criteria);

        List<Double[]> empiricalMeansFC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsFC = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            empiricalMeansFC.add(new Double[criteria.size()]);
            confidenceIntervalsFC.add(new ConfidenceInterval[criteria.size()]);
        }

        ProgressiveSampling.computeMeansAndConfidenceIntervals(
                criterionValuesCFS,
                empiricalMeansFC,
                confidenceIntervalsFC,
                criteria,
                functionClass.size(),
                samples.size(),
                1,
                delta,
                isApproximation,
                complexity);

        Integer optimalFIndex = ProgressiveSampling.getOptimalFunctionIndex(
                functionClass,
                confidenceIntervalsFC,
                objective,
                constraint);

        Double minLowerBound = ProgressiveSampling.getMinLowerBound(
                functionClass,
                confidenceIntervalsFC,
                objective,
                constraint);


        return new AlgorithmSelectionOutput(
                functionClass.get(optimalFIndex),
                empiricalMeansFC.get(optimalFIndex),
                confidenceIntervalsFC.get(optimalFIndex),
                minLowerBound);
    }

    public AlgorithmSelectionOutput runAlgorithm(
            File outputCSV,
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta,
            boolean isApproximation) throws InsufficientSampleSizeException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        Double[][][] criterionValuesCFS = sampleCriterionComputations(
                samples,
                functionClass,
                criteria);

        return runAlgorithmCSVOutput(
                criterionValuesCFS,
                functionClass,
                criteria,
                objective,
                constraint,
                samples.size(),
                outputCSV,
                delta,
                isApproximation);
    }

    public AlgorithmSelectionOutput runAlgorithm(
            File inputCSV,
            File outputCSV,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta,
            boolean isApproximation) throws InsufficientSampleSizeException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        int numSamples = ProgressiveSampling.getNumSamplesCSV(inputCSV, functionClass.size());
        List<Integer> sampleIndices = ProgressiveSampling.getSampleIndicesCSV(inputCSV, functionClass.size());

        Double[][][] criterionValuesCFS = ProgressiveSampling.readCriterionValuesFromCSV(
                criteria.size(),
                functionClass.size(),
                numSamples,
                sampleIndices,
                inputCSV);

        return runAlgorithmCSVOutput(
                criterionValuesCFS,
                functionClass,
                criteria,
                objective,
                constraint,
                numSamples,
                outputCSV,
                delta,
                isApproximation);
    }

    private AlgorithmSelectionOutput runAlgorithmCSVOutput(
            Double[][][] criterionValuesCFS,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            int numSamples,
            File outputCSV,
            double delta,
            boolean isApproximation
    ) throws EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {
        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = ProgressiveSampling.writeHeaderOutputCSV(writer);

        List<Double[]> empiricalMeansFC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsFC = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            empiricalMeansFC.add(new Double[criteria.size()]);
            confidenceIntervalsFC.add(new ConfidenceInterval[criteria.size()]);
        }
        ProgressiveSampling.computeMeansAndConfidenceIntervals(
                criterionValuesCFS,
                empiricalMeansFC,
                confidenceIntervalsFC,
                criteria,
                functionClass.size(),
                numSamples,
                1,
                delta,
                isApproximation,
                complexity);

        ProgressiveSampling.writeIterationResultsCSV(
                1,
                1,
                numSamples,
                criteria,
                functionClass,
                criterionValuesCFS,
                empiricalMeansFC,
                confidenceIntervalsFC,
                constraint,
                objective,
                csvWriter,
                complexity,
                false);

        try {
            writer.close();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Integer optimalFIndex = ProgressiveSampling.getOptimalFunctionIndex(
                functionClass,
                confidenceIntervalsFC,
                objective,
                constraint);

        Double minLowerBound = ProgressiveSampling.getMinLowerBound(
                functionClass,
                confidenceIntervalsFC,
                objective,
                constraint);


        System.out.println("Optimal Function: " + functionClass.get(optimalFIndex).toString());

        System.out.println("Smallest Interval: " +
                (objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex))
                        - objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex))));


        int numContendingFunctions = 0;
        for (int f = 0; f < functionClass.size(); f++) {
            if (objective.minRectangle(confidenceIntervalsFC.get(f))
                    < objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex))) {
                numContendingFunctions++;
            }
        }

        System.out.println("Functions Remaining: " + numContendingFunctions);


        return new AlgorithmSelectionOutput(
                functionClass.get(optimalFIndex),
                empiricalMeansFC.get(optimalFIndex),
                confidenceIntervalsFC.get(optimalFIndex),
                minLowerBound);

    }

    private Double[][][] sampleCriterionComputations(
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria) {
        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][samples.size()];
        for (int f = 0; f < functionClass.size(); f++) {
            for (int s = 0; s < samples.size(); s++) {
                FunctionOutput fOut = functionClass.get(f).apply(samples.get(s));
                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = criteria.get(c).apply(fOut);
                }
            }
        }
        return criterionValuesCFS;
    }
}
