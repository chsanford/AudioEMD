package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
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
                        criteria.size(),
                        functionClass.size());

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

    public AlgorithmSelectionOutput runAlgorithm(
            File outputCSV,
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta,
            boolean isApproximation) throws InsufficientSampleSizeException {


        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        List<String> headerRecord = new ArrayList<>();
        headerRecord.add("ITERATION");
        headerRecord.add("NUMBER_SAMPLES");
        headerRecord.add("FUNCTION_INDEX");
        headerRecord.add("OBJECTIVE_MEAN");
        headerRecord.add("OBJECTIVE_MIN");
        headerRecord.add("OBJECTIVE_MAX");
        headerRecord.add("CRITERION_INDEX");
        headerRecord.add("CRITERION_MEAN");
        headerRecord.add("CRITERION_MIN");
        headerRecord.add("CRITERION_MAX");
        csvWriter.writeNext(headerRecord.toArray(new String[headerRecord.size()]));

        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][samples.size()];

        for (int f = 0; f < functionClass.size(); f++) {
            for (int s = 0; s < samples.size(); s++) {
                FunctionOutput fOut = functionClass.get(f).apply(samples.get(s));
                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = criteria.get(c).apply(fOut);
                }
            }
        }

        double[][][] empiricalMeansFSC = new double[functionClass.size()][samples.size()][criteria.size()];
        ConfidenceInterval[][][] confidenceIntervalsFSC =
                new ConfidenceInterval[functionClass.size()][samples.size()][criteria.size()];

        double[][] empiricalMeansFC = new double[functionClass.size()][criteria.size()];
        ConfidenceInterval[][] confidenceIntervalsFC = new ConfidenceInterval[functionClass.size()][criteria.size()];

        for (int c = 0; c < criteria.size(); c++) {
            for (int f = 0; f < functionClass.size(); f++) {

                // Estimates the value of each criterion for each function
               // empiricalMeansFC[f][c] = 0.;

                for (int s = 0; s < samples.size(); s++) {
                    if (s == 0) {
                        empiricalMeansFSC[f][s][c] = criterionValuesCFS[c][f][s];
                    } else {
                        empiricalMeansFSC[f][s][c] =
                                empiricalMeansFSC[f][s - 1][c] * (s - 1) / s + criterionValuesCFS[c][f][s] / s;
                    }
                    if (isApproximation) {
                        confidenceIntervalsFSC[f][s][c] = ((OneShotRademacherComplexity) complexity).getApproximateConfidenceInterval(
                                empiricalMeansFSC[f][s][c],
                                criterionValuesCFS[c],
                                delta,
                                s + 1,
                                100
                        );
                    } else {
                        confidenceIntervalsFSC[f][s][c] = complexity.getConfidenceInterval(
                                empiricalMeansFSC[f][s][c],
                                complexity.getComplexity(criterionValuesCFS[c], s + 1),
                                delta,
                                s + 1,
                                criteria.size(),
                                functionClass.size());
                    }


                }
                empiricalMeansFC[f][c] = empiricalMeansFSC[f][samples.size() - 1][c];
                System.out.println(empiricalMeansFC[f][c]);

                // Bounds those estimates
                confidenceIntervalsFC[f][c] = confidenceIntervalsFSC[f][samples.size() - 1][c];

            }
        }

        for (int s = 0; s < samples.size(); s++) {
            for (int f = 0; f < functionClass.size(); f++) {
                for (int c = 0; c < criteria.size(); c++) {
                    List<String> critRecord = new ArrayList<>();
                    critRecord.add(String.valueOf(s));
                    critRecord.add(String.valueOf(s + 1));
                    critRecord.add(String.valueOf(f));
                    critRecord.add(String.valueOf(objective.compute(empiricalMeansFSC[f][s])));
                    critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsFSC[f][s])));
                    critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsFSC[f][s])));
                    critRecord.add(String.valueOf(c));
                    critRecord.add(String.valueOf(empiricalMeansFSC[f][s][c]));
                    critRecord.add(String.valueOf(confidenceIntervalsFSC[f][s][c].getLowerBound()));
                    critRecord.add(String.valueOf(confidenceIntervalsFSC[f][s][c].getUpperBound()));
                    csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
                }
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
                ArrayUtils.toObject(empiricalMeansFC[optimalFIndex]),
                confidenceIntervalsFC[optimalFIndex],
                maxUpperBound);
    }

    public AlgorithmSelectionOutput runAlgorithmComplete(
            File inputCSV,
            File outputCSV,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta,
            boolean isApproximation) throws InsufficientSampleSizeException {

        int numSamples = 0;
        List<Integer> sampleIndices = new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvCounter = new CSVReader(reader);

            csvCounter.readNext();

            int totalLines = 0;
            String[] nextRecord;
            while ((nextRecord = csvCounter.readNext()) != null) {
                totalLines++;
                int s = Integer.valueOf(nextRecord[0]);
                if (!sampleIndices.contains(s)) {
                    sampleIndices.add(s);
                }
            }
            numSamples = totalLines / functionClass.size();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][numSamples];

        // Generates a permutation to ensure randomness of samples
        List<Integer> samplePermutation = new ArrayList<>();
        for (int s = 0; s < numSamples; s++) {
            samplePermutation.add(s);
        }
        java.util.Collections.shuffle(samplePermutation);

        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvReader = new CSVReader(reader);

            csvReader.readNext();

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                int s = samplePermutation.get(sampleIndices.indexOf(Integer.valueOf(nextRecord[0])));
                int f = Integer.valueOf(nextRecord[2]);

                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = Double.valueOf(nextRecord[3 + c]);
                }

            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }


        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        List<String> headerRecord = new ArrayList<>();
        headerRecord.add("ITERATION");
        headerRecord.add("NUMBER_SAMPLES");
        headerRecord.add("FUNCTION_INDEX");
        headerRecord.add("OBJECTIVE_MEAN");
        headerRecord.add("OBJECTIVE_MIN");
        headerRecord.add("OBJECTIVE_MAX");
        headerRecord.add("CRITERION_INDEX");
        headerRecord.add("CRITERION_MEAN");
        headerRecord.add("CRITERION_MIN");
        headerRecord.add("CRITERION_MAX");
        csvWriter.writeNext(headerRecord.toArray(new String[headerRecord.size()]));


        double[][][] empiricalMeansFSC = new double[functionClass.size()][numSamples][criteria.size()];
        ConfidenceInterval[][][] confidenceIntervalsFSC =
                new ConfidenceInterval[functionClass.size()][numSamples][criteria.size()];

        double[][] empiricalMeansFC = new double[functionClass.size()][criteria.size()];
        ConfidenceInterval[][] confidenceIntervalsFC = new ConfidenceInterval[functionClass.size()][criteria.size()];

        for (int c = 0; c < criteria.size(); c++) {
            for (int f = 0; f < functionClass.size(); f++) {

                // Estimates the value of each criterion for each function
                // empiricalMeansFC[f][c] = 0.;

                for (int s = 0; s < numSamples; s++) {
                    if (s == 0) {
                        empiricalMeansFSC[f][s][c] = criterionValuesCFS[c][f][s];
                    } else {
                        empiricalMeansFSC[f][s][c] =
                                empiricalMeansFSC[f][s - 1][c] * (s - 1) / s + criterionValuesCFS[c][f][s] / s;
                    }
                    if (isApproximation) {
                        confidenceIntervalsFSC[f][s][c] = ((OneShotRademacherComplexity) complexity).getApproximateConfidenceInterval(
                                empiricalMeansFSC[f][s][c],
                                criterionValuesCFS[c],
                                delta,
                                s + 1,
                                100
                        );
                    } else {
                        confidenceIntervalsFSC[f][s][c] = complexity.getConfidenceInterval(
                                empiricalMeansFSC[f][s][c],
                                complexity.getComplexity(criterionValuesCFS[c], s + 1),
                                delta,
                                s + 1,
                                criteria.size(),
                                functionClass.size());
                    }


                }
                empiricalMeansFC[f][c] = empiricalMeansFSC[f][numSamples - 1][c];
                System.out.println(empiricalMeansFC[f][c]);

                // Bounds those estimates
                confidenceIntervalsFC[f][c] = confidenceIntervalsFSC[f][numSamples - 1][c];

            }
        }

        for (int s = 0; s < numSamples; s++) {
            for (int f = 0; f < functionClass.size(); f++) {
                for (int c = 0; c < criteria.size(); c++) {
                    List<String> critRecord = new ArrayList<>();
                    critRecord.add(String.valueOf(s));
                    critRecord.add(String.valueOf(s + 1));
                    critRecord.add(String.valueOf(functionClass.get(f).toString()));
                    critRecord.add(String.valueOf(objective.compute(empiricalMeansFSC[f][s])));
                    critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsFSC[f][s])));
                    critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsFSC[f][s])));
                    critRecord.add(String.valueOf(c));
                    critRecord.add(String.valueOf(empiricalMeansFSC[f][s][c]));
                    critRecord.add(String.valueOf(confidenceIntervalsFSC[f][s][c].getLowerBound()));
                    critRecord.add(String.valueOf(confidenceIntervalsFSC[f][s][c].getUpperBound()));
                    csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
                }
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
                ArrayUtils.toObject(empiricalMeansFC[optimalFIndex]),
                confidenceIntervalsFC[optimalFIndex],
                maxUpperBound);
    }

    public AlgorithmSelectionOutput runAlgorithm(
            File inputCSV,
            File outputCSV,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double delta,
            boolean isApproximation) throws InsufficientSampleSizeException {

        int numSamples = 0;
        List<Integer> sampleIndices = new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvCounter = new CSVReader(reader);

            csvCounter.readNext();

            int totalLines = 0;
            String[] nextRecord;
            while ((nextRecord = csvCounter.readNext()) != null) {
                totalLines++;
                int s = Integer.valueOf(nextRecord[0]);
                if (!sampleIndices.contains(s)) {
                    sampleIndices.add(s);
                }
            }
            numSamples = totalLines / functionClass.size();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][numSamples];

        // Generates a permutation to ensure randomness of samples
        List<Integer> samplePermutation = new ArrayList<>();
        for (int s = 0; s < numSamples; s++) {
            samplePermutation.add(s);
        }
        java.util.Collections.shuffle(samplePermutation);

        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvReader = new CSVReader(reader);

            csvReader.readNext();

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                int s = samplePermutation.get(sampleIndices.indexOf(Integer.valueOf(nextRecord[0])));
                int f = Integer.valueOf(nextRecord[2]);

                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = Double.valueOf(nextRecord[3 + c]);
                }

            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }


        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        List<String> headerRecord = new ArrayList<>();
        headerRecord.add("ITERATION");
        headerRecord.add("NUMBER_SAMPLES");
        headerRecord.add("FUNCTION_INDEX");
        headerRecord.add("OBJECTIVE_MEAN");
        headerRecord.add("OBJECTIVE_MIN");
        headerRecord.add("OBJECTIVE_MAX");
        headerRecord.add("CRITERION_INDEX");
        headerRecord.add("CRITERION_MEAN");
        headerRecord.add("CRITERION_MIN");
        headerRecord.add("CRITERION_MAX");
        csvWriter.writeNext(headerRecord.toArray(new String[headerRecord.size()]));

        double[][] empiricalMeansFC = new double[functionClass.size()][criteria.size()];
        ConfidenceInterval[][] confidenceIntervalsFC = new ConfidenceInterval[functionClass.size()][criteria.size()];

        for (int c = 0; c < criteria.size(); c++) {
            for (int f = 0; f < functionClass.size(); f++) {

                // Estimates the value of each criterion for each function
                // empiricalMeansFC[f][c] = 0.;

                empiricalMeansFC[f][c] = 0.;

                for (double v : criterionValuesCFS[c][f]) {
                    empiricalMeansFC[f][c] += (v / numSamples);
                }

                if (isApproximation && complexity instanceof OneShotRademacherComplexity) {
                    confidenceIntervalsFC[f][c] = ((OneShotRademacherComplexity) complexity).getApproximateConfidenceInterval(
                            empiricalMeansFC[f][c],
                            criterionValuesCFS[c],
                            delta,
                            numSamples,
                            100
                    );
                } else if (isApproximation && complexity instanceof EMDComplexity) {
                    confidenceIntervalsFC[f][c] = ((EMDComplexity) complexity).getApproximateConfidenceInterval(
                            empiricalMeansFC[f][c],
                            criterionValuesCFS[c],
                            delta,
                            numSamples,
                            100
                    );
                } else {
                    confidenceIntervalsFC[f][c] = complexity.getConfidenceInterval(
                            empiricalMeansFC[f][c],
                            complexity.getComplexity(criterionValuesCFS[c], numSamples),
                            delta,
                            numSamples,
                            criteria.size(),
                            functionClass.size());
                }
            }
        }

        for (int f = 0; f < functionClass.size(); f++) {
            for (int c = 0; c < criteria.size(); c++) {
                System.out.println(f + ", " + c);
                List<String> critRecord = new ArrayList<>();
                critRecord.add(String.valueOf(numSamples - 1));
                critRecord.add(String.valueOf(numSamples));
                critRecord.add(String.valueOf(functionClass.get(f).toString()));
                critRecord.add(String.valueOf(objective.compute(empiricalMeansFC[f])));
                critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsFC[f])));
                critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsFC[f])));
                critRecord.add(String.valueOf(c));
                critRecord.add(String.valueOf(empiricalMeansFC[f][c]));
                critRecord.add(String.valueOf(confidenceIntervalsFC[f][c].getLowerBound()));
                critRecord.add(String.valueOf(confidenceIntervalsFC[f][c].getUpperBound()));
                csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
            }
        }

        try {
            writer.close();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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

        System.out.println("Optimal Function: " + functionClass.get(optimalFIndex).toString());

        System.out.println("Smallest Interval: " +
                (objective.maxRectangle(confidenceIntervalsFC[optimalFIndex]) - objective.minRectangle(confidenceIntervalsFC[optimalFIndex])));



        int numContendingFunctions = 0;
        for (int f = 0; f < functionClass.size(); f++) {
            if (objective.minRectangle(confidenceIntervalsFC[f]) < objective.maxRectangle(confidenceIntervalsFC[optimalFIndex])) {
                numContendingFunctions++;
            }
        }

        System.out.println("Functions Remaining: " + numContendingFunctions);

        // If no valid function is found, throw exception
        if (optimalFIndex == null) {
            throw new InsufficientSampleSizeException();
        }

        return new AlgorithmSelectionOutput(
                functionClass.get(optimalFIndex),
                ArrayUtils.toObject(empiricalMeansFC[optimalFIndex]),
                confidenceIntervalsFC[optimalFIndex],
                maxUpperBound);
    }
}
