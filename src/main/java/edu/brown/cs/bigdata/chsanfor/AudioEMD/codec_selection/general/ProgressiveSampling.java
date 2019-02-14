package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.CohortEncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.EncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EMDComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EmpiricalComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.OneShotRademacherComplexity;

import org.apache.commons.math3.util.CombinatoricsUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.*;


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
     * merges two properly-formatted CSVs and adjusts the numbers so that the numbering of samples does not conflict
     */
    public void mergeCSVSamples(File inputCSV1, File inputCSV2, File outputCSV) {
        mergeCSVByColumn(inputCSV1, inputCSV2, outputCSV, 0);
    }

    public void mergeCSVFunctions(File inputCSV1, File inputCSV2, File outputCSV) {
        mergeCSVByColumn(inputCSV1, inputCSV2, outputCSV, 2);
    }

    /**
     * Merges two CSVs with different overlapping numeric values for the specified column
     * @param inputCSV1
     * @param inputCSV2
     * @param outputCSV
     * @param columnIndex
     */
    private void mergeCSVByColumn(File inputCSV1, File inputCSV2, File outputCSV, int columnIndex) {
        try {
            Reader reader1 = Files.newBufferedReader(inputCSV1.toPath());
            CSVReader csvReader1 = new CSVReader(reader1);

            Reader reader2 = Files.newBufferedReader(inputCSV2.toPath());
            CSVReader csvReader2 = new CSVReader(reader2);

            Writer writer = Files.newBufferedWriter(outputCSV.toPath());
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            csvWriter.writeNext(csvReader1.readNext());
            csvReader2.readNext();

            int numInColumn1 = 0;
            String[] nextRecord;

            while ((nextRecord = csvReader1.readNext()) != null) {
                int i = Integer.valueOf(nextRecord[columnIndex]);
                numInColumn1 = Math.max(numInColumn1, i + 1);
                csvWriter.writeNext(nextRecord);
            }

            while ((nextRecord = csvReader2.readNext()) != null) {
                nextRecord[columnIndex] = String.valueOf(Integer.valueOf(nextRecord[columnIndex]) + numInColumn1);
                csvWriter.writeNext(nextRecord);
            }

            csvReader1.close();
            csvReader2.close();
            csvWriter.close();

            reader1.close();
            reader2.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillCSV(
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            File outputCSV) {
        Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][samples.size()];

        for (int f = 0; f < functionClass.size(); f++) {
            System.out.println("Processing function " + f + " / " + functionClass.size());
            Function function = functionClass.get(f);
            double[][] criteriaOutput = samples
                    .parallelStream()
                    .map(function::apply)
                    .map(fOut -> Criterion.applyCriteria(criteria, fOut))
                    .toArray(double[][]::new);
            for (int s = 0; s < samples.size(); s++) {
                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = criteriaOutput[s][c];
                }
            }
        }

        try {
            Writer writer = Files.newBufferedWriter(outputCSV.toPath());
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String> headerRecord = new ArrayList<>();
            headerRecord.add("SAMPLE_INDEX");
            headerRecord.add("FUNCTION_NAME");
            headerRecord.add("FUNCTION_INDEX");
            for (Criterion criterion : criteria) {
                headerRecord.add(criterion.toString());
            }
            csvWriter.writeNext(headerRecord.toArray(new String[headerRecord.size()]));

            for (int s = 0; s < samples.size(); s++) {
                // If one of the criteria is invalid (value of -1), filter the line
                boolean validSample = true;
                for (int f = 0; f < functionClass.size(); f++) {
                    for (int c = 0; c < criteria.size(); c++) {
                        validSample = validSample && (criterionValuesCFS[c][f][s] != -1);
                    }
                }
                if (validSample) {
                    for (int f = 0; f < functionClass.size(); f++) {
                        List<String> lineRecord = new ArrayList<>();
                        lineRecord.add(String.valueOf(s));
                        lineRecord.add(functionClass.get(f).toString());
                        lineRecord.add(String.valueOf(f));
                        for (int c = 0; c < criteria.size(); c++) {
                            lineRecord.add(String.valueOf(criterionValuesCFS[c][f][s]));
                        }
                        csvWriter.writeNext(lineRecord.toArray(new String[lineRecord.size()]));
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            double delta,
            boolean isApproximation
    ) throws InsufficientSampleSizeException, NoSatisfactoryFunctionsException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = 31 - Integer.numberOfLeadingZeros(samples.size() / initialSampleSize + 1);
//        int maxIterations = (int) Math.floor(Math.log(1. * numSamples / initialSampleSize + 1) / Math.log(2));
        List<Double[]> empiricalMeansFC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsFC = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            empiricalMeansFC.add(new Double[criteria.size()]);
            confidenceIntervalsFC.add(new ConfidenceInterval[criteria.size()]);
        }


        for (int i = 0; i < maxIterations; i++) {

            System.out.println("Iteration " + i + " of " + maxIterations + " (" + sampleSize + " samples)");

            Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][sampleSize];

            firstSample = obtainCriteriaFromSamples(
                    samples,
                    functionClass,
                    criteria,
                    criterionValuesCFS,
                    sampleSize,
                    firstSample);

            computeMeansAndConfidenceIntervals(
                    criterionValuesCFS,
                    empiricalMeansFC,
                    confidenceIntervalsFC,
                    criteria,
                    functionClass.size(),
                    sampleSize,
                    maxIterations,
                    delta,
                    isApproximation,
                    complexity);

            // print criterion details
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

            Integer optimalFIndex = getOptimalFunctionIndex(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint);

            Double minLowerBound = getMinLowerBound(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint);

            List<Integer> toRemove = getFunctionIndicesToPrune(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint,
                    optimalFIndex);

            toRemove.stream()
                    .sorted(Comparator.reverseOrder())
                    .forEach(f -> {
                        functionClass.remove(f);
                        empiricalMeansFC.remove(f);
                        confidenceIntervalsFC.remove(f);
                    });


            if (functionClass.size() == 0) {
                throw new NoSatisfactoryFunctionsException();
            }

            if (optimalFIndex != null &&
                    (objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex)) <=
                    objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex)) + epsilon)) {
                return new AlgorithmSelectionOutput(
                        functionClass.get(optimalFIndex),
                        empiricalMeansFC.get(optimalFIndex),
                        confidenceIntervalsFC.get(optimalFIndex),
                        minLowerBound);
            }

            sampleSize *= 2;

        }

        // If no valid function is found, throw exception

        throw new InsufficientSampleSizeException();


    }


    public AlgorithmSelectionOutput runAlgorithm(
            File inputCSV,
            File outputCSV,
            int initialSampleSize,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double epsilon,
            double delta,
            boolean isApproximation
    ) throws InsufficientSampleSizeException, NoSatisfactoryFunctionsException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        int numSamples = getNumSamplesCSV(inputCSV, functionClass.size());
        List<Integer> sampleIndices = getSampleIndicesCSV(inputCSV, functionClass.size());

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = 31 - Integer.numberOfLeadingZeros(numSamples / initialSampleSize + 1);
//        int maxIterations = (int) Math.floor(Math.log(1. * numSamples / initialSampleSize + 1) / Math.log(2));
        System.out.println(maxIterations);

        Double[][][] allCriterionValuesCFS = readCriterionValuesFromCSV(
                criteria.size(),
                functionClass.size(),
                numSamples,
                sampleIndices,
                inputCSV);


        List<Double[]> empiricalMeansFC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsFC = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            empiricalMeansFC.add(new Double[criteria.size()]);
            confidenceIntervalsFC.add(new ConfidenceInterval[criteria.size()]);
        }

        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = writeHeaderOutputCSV(writer);

        for (int i = 0; i < maxIterations; i++) {

            Double[][][] criterionValuesCFS = getSub3DTensorThirdAxis(allCriterionValuesCFS, firstSample, sampleSize);

            computeMeansAndConfidenceIntervals(
                    criterionValuesCFS,
                    empiricalMeansFC,
                    confidenceIntervalsFC,
                    criteria,
                    functionClass.size(),
                    sampleSize,
                    maxIterations,
                    delta,
                    isApproximation,
                    complexity);

            writeIterationResultsCSV(
                    i,
                    maxIterations,
                    sampleSize,
                    criteria,
                    functionClass,
                    criterionValuesCFS,
                    empiricalMeansFC,
                    confidenceIntervalsFC,
                    constraint,
                    objective,
                    csvWriter,
                    complexity,
                    true);

            Integer optimalFIndex = getOptimalFunctionIndex(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint);

            System.out.println("Optimal Function: " + functionClass.get(optimalFIndex).toString());
            System.out.println("Smallest Interval: " +
                    (objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex))
                            - objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex))));

            Double minLowerBound = getMinLowerBound(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint);

            List<Integer> toRemove = getFunctionIndicesToPrune(
                    functionClass,
                    confidenceIntervalsFC,
                    objective,
                    constraint,
                    optimalFIndex);

            if (toRemove.size() == functionClass.size()) {
                throw new NoSatisfactoryFunctionsException();
            }

            if (optimalFIndex != null &&
                    (objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex)) <=
                            objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex)) + epsilon)) {
                try {
                    writer.close();
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new AlgorithmSelectionOutput(
                        functionClass.get(optimalFIndex),
                        empiricalMeansFC.get(optimalFIndex),
                        confidenceIntervalsFC.get(optimalFIndex),
                        minLowerBound);
            }

            Double[][][] prunedAllCriterionValuesCFS =
                    new Double[criteria.size()][functionClass.size() - toRemove.size()][numSamples];
            int fIndex = 0;
            for (int f = 0; f < functionClass.size(); f++) {
                if (! toRemove.contains(f)) {
                    for (int c = 0; c < criteria.size(); c++) {
                        prunedAllCriterionValuesCFS[c][fIndex] = allCriterionValuesCFS[c][f];
                    }
                    fIndex++;
                }
            }
            allCriterionValuesCFS = prunedAllCriterionValuesCFS;

            toRemove.stream()
                    .sorted(Comparator.reverseOrder())
                    .mapToInt(Integer::intValue)
                    .forEach(f -> {
                        functionClass.remove(f);
                        empiricalMeansFC.remove(f);
                        confidenceIntervalsFC.remove(f);
                    });

            System.out.println("Remaining Functions: " + functionClass.size());
            System.out.println("Iterations: " + (i + 1));

            firstSample += sampleSize;
            sampleSize *= 2;

        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If no valid function is found, throw exception
        throw new InsufficientSampleSizeException();
    }


    public AlgorithmSelectionOutput runMultiAlgorithm(
            File inputCSV,
            File outputCSV,
            int initialSampleSize,
            List<Function> functionClass,
            List<Criterion> criteria,
            Objective objective,
            Constraint constraint,
            double epsilon,
            double delta,
            boolean isApproximation,
            int cohortSize
    ) throws InsufficientSampleSizeException, NoSatisfactoryFunctionsException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

        long numFunctionCohorts = CombinatoricsUtils.binomialCoefficient(functionClass.size(), cohortSize);

        List<CohortEncodingFunction> cohorts = new ArrayList<>();
        List<int[]> cohortCombinations = new ArrayList<>();
        for (int co = 0; co < numFunctionCohorts; co++) {
            int[] cohortCombination = getCombination(co, functionClass.size(), cohortSize);
            cohortCombinations.add(cohortCombination);
            List<EncodingFunction> cohortFunctions = new ArrayList<>();
            for (int f: cohortCombination) cohortFunctions.add((EncodingFunction) functionClass.get(f));
            cohorts.add(new CohortEncodingFunction(cohortFunctions, criteria, objective));
        }

        int numSamples = getNumSamplesCSV(inputCSV, functionClass.size());
        List<Integer> sampleIndices = getSampleIndicesCSV(inputCSV, functionClass.size());

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = 31 - Integer.numberOfLeadingZeros(numSamples / initialSampleSize + 1);
//        int maxIterations = (int) Math.floor(Math.log(1. * numSamples / initialSampleSize + 1) / Math.log(2));

        Double[][][] allCriterionValuesCFS = readCriterionValuesFromCSV(
                criteria.size(),
                functionClass.size(),
                numSamples,
                sampleIndices,
                inputCSV);


        List<Double[]> empiricalMeansCoC = new ArrayList<>();
        List<ConfidenceInterval[]> confidenceIntervalsCoC = new ArrayList<>();

        for (int co = 0; co < numFunctionCohorts; co++) {
            empiricalMeansCoC.add(new Double[criteria.size()]);
            confidenceIntervalsCoC.add(new ConfidenceInterval[criteria.size()]);
        }

        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(outputCSV.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = writeHeaderOutputCSV(writer);

        for (int i = 0; i < maxIterations; i++) {

            Double[][][] criterionValuesCFS = getSub3DTensorThirdAxis(allCriterionValuesCFS, firstSample, sampleSize);

            // Obtain cohort criterion values from function values
            Double[][][] criterionValuesCCoS = new Double[criteria.size()][cohorts.size()][sampleSize];
            for (int co = 0; co < cohorts.size(); co++) {
                int[] functionIndices = cohortCombinations.get(co);
                for (int s = 0; s < sampleSize; s++) {
                    int optimalEncodingFunctionIndex =
                            cohorts.get(co).getOptimalEncodingFunctionIndex(criterionValuesCFS, s, functionIndices);
                    for (int c = 0; c < criteria.size(); c++) {
                        criterionValuesCCoS[c][co][s] = criterionValuesCFS[c][optimalEncodingFunctionIndex][s];
                    }
                }
            }

            computeMeansAndConfidenceIntervals(
                    criterionValuesCFS,
                    empiricalMeansCoC,
                    confidenceIntervalsCoC,
                    criteria,
                    cohorts.size(),
                    sampleSize,
                    maxIterations,
                    delta,
                    isApproximation,
                    complexity);

            List<Function> castedCohorts = (List<Function>) (List<? extends Function>) cohorts;

            writeIterationResultsCSV(
                    i,
                    maxIterations,
                    sampleSize,
                    criteria,
                    castedCohorts,
                    criterionValuesCFS,
                    empiricalMeansCoC,
                    confidenceIntervalsCoC,
                    constraint,
                    objective,
                    csvWriter,
                    complexity,
                    true);

            Integer optimalCoIndex = getOptimalFunctionIndex(
                    castedCohorts,
                    confidenceIntervalsCoC,
                    objective,
                    constraint);

            System.out.println("Optimal Function: " + cohorts.get(optimalCoIndex).toString());
            System.out.println("Smallest Interval: " +
                    (objective.maxRectangle(confidenceIntervalsCoC.get(optimalCoIndex))
                            - objective.minRectangle(confidenceIntervalsCoC.get(optimalCoIndex))));

            Double minLowerBound = getMinLowerBound(
                    castedCohorts,
                    confidenceIntervalsCoC,
                    objective,
                    constraint);

            List<Integer> toRemove = getFunctionIndicesToPrune(
                    castedCohorts,
                    confidenceIntervalsCoC,
                    objective,
                    constraint,
                    optimalCoIndex);

            if (toRemove.size() == cohorts.size()) {
                throw new NoSatisfactoryFunctionsException();
            }

            if (optimalCoIndex != null &&
                    (objective.maxRectangle(confidenceIntervalsCoC.get(optimalCoIndex)) <=
                            objective.minRectangle(confidenceIntervalsCoC.get(optimalCoIndex)) + epsilon)) {
                try {
                    writer.close();
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new AlgorithmSelectionOutput(
                        cohorts.get(optimalCoIndex),
                        empiricalMeansCoC.get(optimalCoIndex),
                        confidenceIntervalsCoC.get(optimalCoIndex),
                        minLowerBound);
            }

            toRemove.stream()
                    .sorted(Comparator.reverseOrder())
                    .forEach(co -> {
                        cohorts.remove(co);
                        cohortCombinations.remove(co);
                        empiricalMeansCoC.remove(co);
                        confidenceIntervalsCoC.remove(co);
                    });

            System.out.println("Remaining Functions: " + cohorts.size());
            System.out.println("Iterations: " + (i + 1));

            firstSample += sampleSize;
            sampleSize *= 2;

        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If no valid function is found, throw exception

        throw new InsufficientSampleSizeException();


    }


    public static int[] getCombination(long index, int n, int k) {
        assert k < n;
        int[] combination = new int[k];
        if (k != 0) {
            if (k == n) {
                for (int j = 0; j < k; j++) combination[j] = j;
            } else if (index < CombinatoricsUtils.binomialCoefficient(n - 1, k - 1)) {
                int[] subCombination = getCombination(index, n - 1, k - 1);
                combination[0] = 0;
                for (int j = 1; j < k; j++) combination[j] = subCombination[j - 1] + 1;
            } else {
                int[] subCombination = getCombination(
                        index - CombinatoricsUtils.binomialCoefficient(n - 1, k - 1),n - 1, k);
                for (int j = 0; j < k; j++) combination[j] = subCombination[j] + 1;
            }
        }
        return combination;
    }

    /**
     * Counts the number of samples in the CSV whose output is formatted according to  the fillCSV function
     * @param inputCSV
     * @param numFunctions
     * @return
     */
    public static int getNumSamplesCSV(File inputCSV, int numFunctions) {
        int numSamples = 0;
        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvCounter = new CSVReader(reader);
            csvCounter.readNext();
            int totalLines = 0;
            while (csvCounter.readNext() != null) {
                totalLines++;
            }
            numSamples = totalLines / numFunctions;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numSamples;

    }

    /**
     * Obtains a list of indices for each sample, according to their order in the CSV
     * @param inputCSV
     * @param numFunctions
     * @return
     */
    public static List<Integer> getSampleIndicesCSV(File inputCSV, int numFunctions) {
        List<Integer> sampleIndices = new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(inputCSV.toPath());
            CSVReader csvCounter = new CSVReader(reader);

            csvCounter.readNext();

            String[] nextRecord;
            while ((nextRecord = csvCounter.readNext()) != null) {
                int s = Integer.valueOf(nextRecord[0]);
                if (!sampleIndices.contains(s)) {
                    sampleIndices.add(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sampleIndices;
    }

    public static Double[][][] readCriterionValuesFromCSV(
            int numCriteria,
            int numFunctions,
            int numSamples,
            List<Integer> sampleIndices,
            File inputCSV) {
        Double[][][] allCriterionValuesCFS = new Double[numCriteria][numFunctions][numSamples];

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

                for (int c = 0; c < numCriteria; c++) {
                    allCriterionValuesCFS[c][f][s] = Double.valueOf(nextRecord[3 + c]);
                }

            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        return allCriterionValuesCFS;
    }

    /**
     * Obtains a sub-tensor of a given tensor along the third axis
     * @param tensor 3-dimensional tensor
     * @param subThirdOffset starting index for the sub-tensor along the 3rd axis
     * @param subThirdLength width of the sub-tensor along the 3rd axis
     * @return
     */
    private static Double[][][] getSub3DTensorThirdAxis(
            Double[][][] tensor,
            int subThirdOffset,
            int subThirdLength) {
        Double[][][] subTensor = new Double[tensor.length][tensor[0].length][subThirdLength];

        for (int x1 = 0; x1 < tensor.length; x1++) {
            for (int x2 = 0; x2 < tensor[0].length; x2++) {
                for (int x3 = 0; x3 < subThirdLength; x3++) {

                    subTensor[x1][x2][x3] = tensor[x1][x2][x3 + subThirdOffset];
                }
            }
        }
        return subTensor;
    }

    /**
     * Fills in empiricalMeansFC and confidenceIntervalsFC with the corresponding values for each functions and
     * criterion
     * @param criterionValuesCFS
     * @param empiricalMeansFC
     * @param confidenceIntervalsFC
     * @param criteria
     * @param numFunctions
     * @param sampleSize
     * @param maxIterations
     * @param delta
     * @param isApproximation
     * @throws EmptyConfidenceIntervalException
     * @throws IncorrectlyClassifiedCriterionException
     */
    public static void computeMeansAndConfidenceIntervals(
            Double[][][] criterionValuesCFS,
            List<Double[]> empiricalMeansFC,
            List<ConfidenceInterval[]> confidenceIntervalsFC,
            List<Criterion> criteria,
            int numFunctions,
            int sampleSize,
            int maxIterations,
            double delta,
            boolean isApproximation,
            EmpiricalComplexity complexity)
    throws EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {
        for (int c = 0; c < criteria.size(); c++) {
            if (criteria.get(c) instanceof MeanCriterion) {
                // Computes complexity for each criterion
                double complexityC = complexity.getComplexity(criterionValuesCFS[c]);

                for (int f = 0; f < numFunctions; f++) {

                    // Estimates the value of each criterion for each function
                    empiricalMeansFC.get(f)[c] = 0.;

                    for (double v : criterionValuesCFS[c][f]) {
                        empiricalMeansFC.get(f)[c] += (v / sampleSize);
                    }

                    // Bounds those estimates
                    ConfidenceInterval newInterval;
                    if (isApproximation && complexity instanceof OneShotRademacherComplexity) {
                        newInterval = ((OneShotRademacherComplexity) complexity).getApproximateConfidenceInterval(
                                empiricalMeansFC.get(f)[c],
                                criterionValuesCFS[c],
                                delta,
                                sampleSize,
                                100
                        );
                    } else if (isApproximation && complexity instanceof EMDComplexity) {
                        newInterval = ((EMDComplexity) complexity).getApproximateConfidenceInterval(
                                empiricalMeansFC.get(f)[c],
                                criterionValuesCFS[c],
                                delta,
                                sampleSize,
                                100
                        );
                    } else {
                        newInterval = complexity.getConfidenceInterval(
                                empiricalMeansFC.get(f)[c],
                                complexityC,
                                delta / maxIterations,
                                sampleSize,
                                criteria.size(),
                                numFunctions);
                    }
                    if (confidenceIntervalsFC.get(f)[c] == null) {
                        confidenceIntervalsFC.get(f)[c] = newInterval;
                    } else {
                        ConfidenceInterval oldInterval = confidenceIntervalsFC.get(f)[c];
                        confidenceIntervalsFC.get(f)[c] = new ConfidenceInterval(
                                delta,
                                Math.min(oldInterval.getUpperBound(), newInterval.getUpperBound()),
                                Math.max(oldInterval.getLowerBound(), newInterval.getLowerBound()));
                    }
                    if (confidenceIntervalsFC.get(f)[c].getLowerBound() >
                            confidenceIntervalsFC.get(f)[c].getUpperBound()) {
                        throw new EmptyConfidenceIntervalException();
                    }

                }
            } else if (criteria.get(c) instanceof VarianceCriterion) {
                // We require that base criterion and squared criterion have already been computed
                Integer baseCriterionIndex = null;
                Integer squaredCriterionIndex = null;
                for (int c2 = 0; c2 < c; c2++) {
                    if (Criterion.isMatch(
                            ((VarianceCriterion) criteria.get(c)).getBaseCriterion(),
                            criteria.get(c2))) {
                        baseCriterionIndex = c2;
                    } else if (Criterion.isMatch(
                            ((VarianceCriterion) criteria.get(c)).getSquaredCriterion(),
                            criteria.get(c2))) {
                        squaredCriterionIndex = c2;
                    }
                }

                for (int f = 0; f < numFunctions; f++) {
                    double baseCriterionMean = empiricalMeansFC.get(f)[baseCriterionIndex];
                    ConfidenceInterval baseCriterionInterval = confidenceIntervalsFC.get(f)[baseCriterionIndex];
                    double squaredCriterionMean = empiricalMeansFC.get(f)[squaredCriterionIndex];
                    ConfidenceInterval squaredCriterionInterval = confidenceIntervalsFC.get(f)[squaredCriterionIndex];

                    empiricalMeansFC.get(f)[c] = squaredCriterionMean - Math.pow(baseCriterionMean, 2);
                    confidenceIntervalsFC.get(f)[c] = new ConfidenceInterval(
                            delta,
                            Math.min(1, squaredCriterionInterval.getUpperBound() -
                                    Math.pow(baseCriterionInterval.getLowerBound(), 2)),
                            Math.max(0, squaredCriterionInterval.getLowerBound() -
                                    Math.pow(baseCriterionInterval.getUpperBound(), 2)));
                }

            } else {
                throw new IncorrectlyClassifiedCriterionException();
            }
        }
    }


    /**
     * Creates a CSV writer and uses it to write the first line of the output CSV
     * @param writer
     * @return
     */
    public static CSVWriter writeHeaderOutputCSV(Writer writer) {
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

        return csvWriter;

    }

    /**
     * Writes relevant data to the CSV for the end of the iteration and prints other results to the commandline
     * @param iteration
     * @param maxIterations
     * @param sampleSize
     * @param criteria
     * @param functionClass
     * @param criterionValuesCFS
     * @param empiricalMeansFC
     * @param confidenceIntervalsFC
     * @param constraint
     * @param objective
     * @param csvWriter
     */
    public static void writeIterationResultsCSV(
            int iteration,
            int maxIterations,
            int sampleSize,
            List<Criterion> criteria,
            List<Function> functionClass,
            Double[][][] criterionValuesCFS,
            List<Double[]> empiricalMeansFC,
            List<ConfidenceInterval[]> confidenceIntervalsFC,
            Constraint constraint,
            Objective objective,
            CSVWriter csvWriter,
            EmpiricalComplexity complexity,
            boolean includePrint
            ) {
        if (includePrint) {
            System.out.println("Iteration " + (iteration + 1) + " of " + maxIterations + " (" + sampleSize + " samples)");
            for (int c = 0; c < criteria.size(); c++) {
                // Computes complexity for each criterion
                double complexityC = complexity.getComplexity(criterionValuesCFS[c]);
                System.out.println("Criteria " + c + " (" + criteria.get(c).toString() + ") Complexity: " + complexityC);
            }
        }

        for (int f = 0; f < functionClass.size(); f++) {
            String functionStatus = "undetermined";
            if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                functionStatus = "valid";
            } else if (constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                functionStatus = "invalid";
            }

            double[] currentMeans = new double[criteria.size()];
            for (int c = 0; c < criteria.size(); c++) currentMeans[c] = empiricalMeansFC.get(f)[c];
            if (includePrint) {
                System.out.println("Function " + f + ": " + functionClass.get(f).toString() +
                        "(" + functionStatus + ")");
                System.out.println("- Objective " + objective.compute(currentMeans)
                        + " in [" + objective.minRectangle(confidenceIntervalsFC.get(f))
                        + ", " + objective.maxRectangle(confidenceIntervalsFC.get(f)) + "]");
            }
            for (int c = 0; c < criteria.size(); c++) {
                if (includePrint) {
                    System.out.println("- Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                            empiricalMeansFC.get(f)[c] + " in " +
                            confidenceIntervalsFC.get(f)[c].toString());
                }
                List<String> critRecord = new ArrayList<>();
                critRecord.add(String.valueOf(iteration));
                critRecord.add(String.valueOf(sampleSize));
                critRecord.add(functionClass.get(f).toString());
                critRecord.add(String.valueOf(objective.compute(currentMeans)));
                critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsFC.get(f))));
                critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsFC.get(f))));
                critRecord.add(String.valueOf(c));
                critRecord.add(String.valueOf(empiricalMeansFC.get(f)[c]));
                critRecord.add(String.valueOf(confidenceIntervalsFC.get(f)[c].getLowerBound()));
                critRecord.add(String.valueOf(confidenceIntervalsFC.get(f)[c].getUpperBound()));
                csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
            }
        }
        if (includePrint) {
            System.out.println();
        }
    }


    public static Integer getOptimalFunctionIndex(
            List<Function> functionClass,
            List<ConfidenceInterval[]> confidenceIntervalsFC,
            Objective objective,
            Constraint constraint) {
        Integer optimalFIndex = null;
        Double optimalUpperBoundF = null;

        for (int f = 0; f < functionClass.size(); f++) {
            // For functions that we are confident are valid, we find the one with the lowest upper-bound objective
            if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                double upperBound = objective.maxRectangle(confidenceIntervalsFC.get(f));
                if (optimalFIndex == null || upperBound < optimalUpperBoundF) {
                    optimalUpperBoundF = upperBound;
                    optimalFIndex = f;
                }
            }
        }
        return optimalFIndex;
    }

    public static Double getMinLowerBound(
            List<Function> functionClass,
            List<ConfidenceInterval[]> confidenceIntervalsFC,
            Objective objective,
            Constraint constraint) {
        Double minLowerBound = null;

        for (int f = 0; f < functionClass.size(); f++) {
            // For functions that may be valid, then we find a lower bound on the objective function
            if (!constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                double lowerBound = objective.minRectangle(confidenceIntervalsFC.get(f));
                if (minLowerBound == null || lowerBound > minLowerBound) {
                    minLowerBound = lowerBound;
                }
            }
        }
        return minLowerBound;
    }

    /**
     * Based on the past iteration's confidence intervals, determines which function indices can be removed
     * @param functionClass
     * @param confidenceIntervalsFC
     * @param objective
     * @param constraint
     * @return
     */
    private static List<Integer> getFunctionIndicesToPrune(
            List<Function> functionClass,
            List<ConfidenceInterval[]> confidenceIntervalsFC,
            Objective objective,
            Constraint constraint,
            Integer optimalFIndex) {
        List<Integer> toRemove = new ArrayList<>();

        for (int f = 0; f < functionClass.size(); f++) {
            if (constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                // Remove as a valid function if no intersection with viable region
                toRemove.add(f);
            }
        }

        if (optimalFIndex != null) {
            for (int f = 0; f < functionClass.size(); f++) {
                if ((objective.minRectangle(confidenceIntervalsFC.get(f)) >
                        objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex)))
                        && !toRemove.contains(f)) {
                    toRemove.add(f);
                }
            }
        }
        Collections.sort(toRemove);
        return toRemove;
    }


    /**
     * Returns the increased offset for firstSample after reading the sample criterion values into criterionValuesCFS
     * @param samples
     * @param functionClass
     * @param criteria
     * @param criterionValuesCFS
     * @param sampleSize
     * @param firstSample
     * @return
     * @throws InsufficientSampleSizeException
     */
    private static int obtainCriteriaFromSamples(
            List<Sample> samples,
            List<Function> functionClass,
            List<Criterion> criteria,
            Double[][][] criterionValuesCFS,
            int sampleSize,
            int firstSample
    ) throws InsufficientSampleSizeException {
        List<Sample> currentSamples = samples.subList(firstSample, firstSample + sampleSize);
        firstSample += sampleSize;

        for (int f = 0; f < functionClass.size(); f++) {
            Function function = functionClass.get(f);
            double[][] criteriaOutput = currentSamples
                    .parallelStream()
                    .map(function::apply)
                    .map(fOut -> Criterion.applyCriteria(criteria, fOut))
                    .toArray(double[][]::new);
            for (int s = 0; s < currentSamples.size(); s++) {
                for (int c = 0; c < criteria.size(); c++) {
                    criterionValuesCFS[c][f][s] = criteriaOutput[s][c];
                }
            }
        }

        for (int s = 0; s < currentSamples.size(); s++) {

            boolean validSample = true;
            for (int f = 0; f < functionClass.size(); f++) {
                for (int c = 0; c < criteria.size(); c++) {
                    validSample = validSample && (criterionValuesCFS[c][f][s] != -1);
                }
            }

            // If one of the criteria is invalid (value of -1), replace the sample until one works
            while (!validSample) {
                // Replace with subsequent sample at end of current sample range
                if (firstSample >= sampleSize) {
                    throw new InsufficientSampleSizeException();
                } else {
                    currentSamples.set(s, samples.get(firstSample));
                    firstSample++;
                    for (int f = 0; f < functionClass.size(); f++) {
                        for (int c = 0; c < criteria.size(); c++) {
                            criterionValuesCFS[c][f][s] = criteria.get(c).apply(
                                    functionClass.get(f).apply(
                                            currentSamples.get(s)));
                        }
                    }
                    validSample = true;
                    for (int f = 0; f < functionClass.size(); f++) {
                        for (int c = 0; c < criteria.size(); c++) {
                            validSample = validSample && (criterionValuesCFS[c][f][s] != -1);
                        }
                    }
                }
            }
        }
        return firstSample;
    }


}