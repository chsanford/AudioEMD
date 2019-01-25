package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.CohortEncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.EncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EMDComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.EmpiricalComplexity;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.OneShotRademacherComplexity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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

            int numSamples1 = 0;
            String[] nextRecord;

            while ((nextRecord = csvReader1.readNext()) != null) {
                int s = Integer.valueOf(nextRecord[0]);
                numSamples1 = Math.max(numSamples1, s + 1);
                csvWriter.writeNext(nextRecord);
            }

            while ((nextRecord = csvReader2.readNext()) != null) {
                nextRecord[0] = String.valueOf(Integer.valueOf(nextRecord[0]) + numSamples1);
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

    public void mergeCSVFunctions(File inputCSV1, File inputCSV2, File outputCSV) {
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

            int numFunctions1 = 0;
            String[] nextRecord;

            while ((nextRecord = csvReader1.readNext()) != null) {
                int f = Integer.valueOf(nextRecord[2]);
                numFunctions1 = Math.max(numFunctions1, f + 1);
                csvWriter.writeNext(nextRecord);
            }

            while ((nextRecord = csvReader2.readNext()) != null) {
                nextRecord[2] = String.valueOf(Integer.valueOf(nextRecord[2]) + numFunctions1);
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
            double delta
    ) throws InsufficientSampleSizeException, NoSatisfactoryFunctionsException, EmptyConfidenceIntervalException, IncorrectlyClassifiedCriterionException {

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

            for (int c = 0; c < criteria.size(); c++) {
                if (criteria.get(c) instanceof MeanCriterion) {
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
                                criteria.size(),
                                functionClass.size());
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

                    for (int f = 0; f < functionClass.size(); f++) {
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

            Integer optimalFIndex = null;
            Double optimalUpperBoundF = null;
            Double minLowerBound = null;

            List<Integer> toRemove = new ArrayList<>();

            for (int f = 0; f < functionClass.size(); f++) {
                // For functions that we are confident are valid, we find the one with the lowest upper-bound objective
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                    double upperBound = objective.maxRectangle(confidenceIntervalsFC.get(f));
                    if (optimalFIndex == null || upperBound < optimalUpperBoundF) {
                        optimalUpperBoundF = upperBound;
                        optimalFIndex = f;
                    }
                }
                // For functions that may be valid, then we find a lower bound on the objective function
                if (!constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                    double lowerBound = objective.maxRectangle(confidenceIntervalsFC.get(f));
                    if (minLowerBound == null || lowerBound > minLowerBound) {
                        minLowerBound = lowerBound;
                    }
                } else {
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

            List<Function> prunedFunctionClass = new ArrayList<>();
            List<Double[]> prunedEmpiricalMeansFC = new ArrayList<>();
            List<ConfidenceInterval[]> prunedConfidenceIntervalsFC = new ArrayList<>();
            for (int f = 0; f < functionClass.size(); f++) {
                if (! toRemove.contains(f)) {
                    prunedFunctionClass.add(functionClass.get(f));
                    prunedEmpiricalMeansFC.add(empiricalMeansFC.get(f));
                    prunedConfidenceIntervalsFC.add(confidenceIntervalsFC.get(f));
                }
            }

            if (prunedFunctionClass.size() == 0) {
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

            functionClass = prunedFunctionClass;
            empiricalMeansFC = prunedEmpiricalMeansFC;
            confidenceIntervalsFC = prunedConfidenceIntervalsFC;
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

        // counts the number of samples
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

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = (int) Math.floor(Math.log(1. * numSamples / initialSampleSize + 1) / Math.log(2));

        Double[][][] allCriterionValuesCFS = new Double[criteria.size()][functionClass.size()][numSamples];

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
                    allCriterionValuesCFS[c][f][s] = Double.valueOf(nextRecord[3 + c]);
//                    if (criteria.get(c) instanceof CompressionRatioCriterion) {
//                        allCriterionValuesCFS[c][f][s] = Math.min(1, allCriterionValuesCFS[c][f][s] * 75. / 32.);
//                    } else if (criteria.get(c) instanceof RawMomentCriterion &&
//                            ((RawMomentCriterion) criteria.get(c)).getBaseCriterion() instanceof CompressionRatioCriterion) {
//                        allCriterionValuesCFS[c][f][s] = Math.min(1, allCriterionValuesCFS[c][f][s] *
//                                Math.pow(75. / 32., ((RawMomentCriterion) criteria.get(c)).getPower()));
//                    }
                }

            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }


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
        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        List<String> headerRecord = new ArrayList<>();
        headerRecord.add("ITERATION");
        headerRecord.add("NUMBER_SAMPLES");
        headerRecord.add("FUNCTION_INDEX");
        //headerRecord.add("FUNCTION_NAME");
        //headerRecord.add("FUNCTION_STATUS");
        headerRecord.add("OBJECTIVE_MEAN");
        headerRecord.add("OBJECTIVE_MIN");
        headerRecord.add("OBJECTIVE_MAX");
        headerRecord.add("CRITERION_INDEX");
        //headerRecord.add("CRITERION_NAME");
        headerRecord.add("CRITERION_MEAN");
        headerRecord.add("CRITERION_MIN");
        headerRecord.add("CRITERION_MAX");
        csvWriter.writeNext(headerRecord.toArray(new String[headerRecord.size()]));

        int[] numZeros = new int[functionClass.size()];

        for (int f = 0; f < functionClass.size(); f++) {
            for (int s = 0; s < numSamples; s++) {
                if (allCriterionValuesCFS[0][f][s] == 0) {
                    numZeros[f]++;
                }
            }

        }

        for (int i = 0; i < maxIterations; i++) {

            Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][sampleSize];

            for (int f = 0; f < functionClass.size(); f++) {
                for (int s = 0; s < sampleSize; s++) {
                    for (int c = 0; c < criteria.size(); c++) {
                        criterionValuesCFS[c][f][s] = allCriterionValuesCFS[c][f][s + firstSample];
                    }
                }
            }

            for (int c = 0; c < criteria.size(); c++) {
                if (criteria.get(c) instanceof MeanCriterion) {
                    // Computes complexity for each criterion
                    double complexityC = complexity.getComplexity(criterionValuesCFS[c]);

                    for (int f = 0; f < functionClass.size(); f++) {

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
                                    functionClass.size());
                        }
                        if (confidenceIntervalsFC.get(f)[c] == null) {
                            confidenceIntervalsFC.get(f)[c] = newInterval;
                        } else {
                            confidenceIntervalsFC.get(f)[c] = newInterval;
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

                    for (int f = 0; f < functionClass.size(); f++) {
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


            System.out.println("Iteration " + (i + 1) + " of " + maxIterations + " (" + sampleSize + " samples)");
            for (int c = 0; c < criteria.size(); c++) {
                // Computes complexity for each criterion
                double complexityC = complexity.getComplexity(criterionValuesCFS[c]);
                System.out.println("Criteria " + c + " (" + criteria.get(c).toString() + ") Complexity: " + complexityC);
            }

            for (int f = 0; f < functionClass.size(); f++) {
                String functionStatus = "undetermined";
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                    functionStatus = "valid";
                } else if (constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                    functionStatus = "invalid";
                }
                System.out.println("Function " + f + ": " + functionClass.get(f).toString() +
                        "(" + functionStatus + ")");
                double[] currentMeans = new double[criteria.size()];
                for (int c = 0; c < criteria.size(); c++) currentMeans[c] = empiricalMeansFC.get(f)[c];
                System.out.println("- Objective " + objective.compute(currentMeans) + " in [" + objective.minRectangle(confidenceIntervalsFC.get(f))
                        + ", " + objective.maxRectangle(confidenceIntervalsFC.get(f)) + "]");
                for (int c = 0; c < criteria.size(); c++) {
                    System.out.println("- Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                            empiricalMeansFC.get(f)[c] + " in " +
                            confidenceIntervalsFC.get(f)[c].toString());
                    List<String> critRecord = new ArrayList<>();
                    critRecord.add(String.valueOf(i));
                    critRecord.add(String.valueOf(sampleSize));
                    //critRecord.add(String.valueOf(f));
                    critRecord.add(functionClass.get(f).toString());
                    //critRecord.add(functionStatus);
                    critRecord.add(String.valueOf(objective.compute(currentMeans)));
                    critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsFC.get(f))));
                    critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsFC.get(f))));
                    critRecord.add(String.valueOf(c));
                    //critRecord.add(criteria.get(c).toString());
                    critRecord.add(String.valueOf(empiricalMeansFC.get(f)[c]));
                    critRecord.add(String.valueOf(confidenceIntervalsFC.get(f)[c].getLowerBound()));
                    critRecord.add(String.valueOf(confidenceIntervalsFC.get(f)[c].getUpperBound()));
                    csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
                }
            }
            System.out.println();

            Integer optimalFIndex = null;
            Double optimalUpperBoundF = null;
            Double minLowerBound = null;

            List<Integer> toRemove = new ArrayList<>();

            for (int f = 0; f < functionClass.size(); f++) {
                // For functions that we are confident are valid, we find the one with the lowest upper-bound objective
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsFC.get(f))) {
                    double upperBound = objective.maxRectangle(confidenceIntervalsFC.get(f));
                    if (optimalFIndex == null || upperBound < optimalUpperBoundF) {
                        optimalUpperBoundF = upperBound;
                        optimalFIndex = f;
                    }
                }
                // For functions that may be valid, then we find a lower bound on the objective function
                if (!constraint.isNeverValidRectangle(confidenceIntervalsFC.get(f))) {
                    double lowerBound = objective.minRectangle(confidenceIntervalsFC.get(f));
                    if (minLowerBound == null || lowerBound > minLowerBound) {
                        minLowerBound = lowerBound;
                    }
                } else {
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

            List<Function> prunedFunctionClass = new ArrayList<>();
            List<Double[]> prunedEmpiricalMeansFC = new ArrayList<>();
            List<ConfidenceInterval[]> prunedConfidenceIntervalsFC = new ArrayList<>();

            for (int f = 0; f < functionClass.size(); f++) {
                if (! toRemove.contains(f)) {
                    prunedFunctionClass.add(functionClass.get(f));
                    prunedEmpiricalMeansFC.add(empiricalMeansFC.get(f));
                    prunedConfidenceIntervalsFC.add(confidenceIntervalsFC.get(f));
                }
            }

            Double[][][] prunedAllCriterionValuesCFS = new Double[criteria.size()][prunedFunctionClass.size()][numSamples];
            int fIndex = 0;
            for (int f = 0; f < functionClass.size(); f++) {
                if (! toRemove.contains(f)) {
                    for (int c = 0; c < criteria.size(); c++) {
                        prunedAllCriterionValuesCFS[c][fIndex] = allCriterionValuesCFS[c][f];
                    }
                    fIndex++;
                }
            }


            System.out.println("Optimal Function: " + functionClass.get(optimalFIndex).toString());

            System.out.println("Remaining Functions: " + prunedFunctionClass.size());

            System.out.println("Smallest Interval: " +
                    (objective.maxRectangle(confidenceIntervalsFC.get(optimalFIndex))
                            - objective.minRectangle(confidenceIntervalsFC.get(optimalFIndex))));

            System.out.println("Iterations: " + (i + 1));

            if (prunedFunctionClass.size() == 0) {
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

            functionClass = prunedFunctionClass;
            empiricalMeansFC = prunedEmpiricalMeansFC;
            confidenceIntervalsFC = prunedConfidenceIntervalsFC;
            allCriterionValuesCFS = prunedAllCriterionValuesCFS;
            firstSample += sampleSize;
            sampleSize *= 2;

        }

        try {
            csvWriter.close();
            //writer.close();
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

        // counts the number of samples
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

        int firstSample = 0;
        int sampleSize = initialSampleSize;
        int maxIterations = (int) Math.floor(Math.log(1. * numSamples / initialSampleSize + 1) / Math.log(2));

        Double[][][] allCriterionValuesCFS = new Double[criteria.size()][functionClass.size()][numSamples];

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
                    allCriterionValuesCFS[c][f][s] = Double.valueOf(nextRecord[3 + c]);
                }

            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }


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

        int[] numZeros = new int[functionClass.size()];

        for (int f = 0; f < functionClass.size(); f++) {
            for (int s = 0; s < numSamples; s++) {
                if (allCriterionValuesCFS[0][f][s] == 0) {
                    numZeros[f]++;
                }
            }

        }

        for (int i = 0; i < maxIterations; i++) {

            Double[][][] criterionValuesCFS = new Double[criteria.size()][functionClass.size()][sampleSize];

            for (int f = 0; f < functionClass.size(); f++) {
                for (int s = 0; s < sampleSize; s++) {
                    for (int c = 0; c < criteria.size(); c++) {
                        criterionValuesCFS[c][f][s] = allCriterionValuesCFS[c][f][s + firstSample];
                    }
                }
            }

            // Criterion, Cohort, Sample
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

            for (int c = 0; c < criteria.size(); c++) {
                if (criteria.get(c) instanceof MeanCriterion) {
                    // Computes complexity for each criterion
                    double complexityC = complexity.getComplexity(criterionValuesCCoS[c]);

                    for (int co = 0; co < cohorts.size(); co++) {

                        // Estimates the value of each criterion for each function
                        empiricalMeansCoC.get(co)[c] = 0.;

                        for (double v : criterionValuesCCoS[c][co]) {
                            empiricalMeansCoC.get(co)[c] += (v / sampleSize);
                        }

                        // Bounds those estimates
                        ConfidenceInterval newInterval;
                        if (isApproximation && complexity instanceof OneShotRademacherComplexity) {
                            newInterval = ((OneShotRademacherComplexity) complexity).getApproximateConfidenceInterval(
                                    empiricalMeansCoC.get(co)[c],
                                    criterionValuesCFS[c],
                                    delta,
                                    sampleSize,
                                    100
                            );
                        } else if (isApproximation && complexity instanceof EMDComplexity) {
                            newInterval = ((EMDComplexity) complexity).getApproximateConfidenceInterval(
                                    empiricalMeansCoC.get(co)[c],
                                    criterionValuesCFS[c],
                                    delta,
                                    sampleSize,
                                    100
                            );
                        } else {
                            newInterval = complexity.getConfidenceInterval(
                                    empiricalMeansCoC.get(co)[c],
                                    complexityC,
                                    delta / maxIterations,
                                    sampleSize,
                                    criteria.size(),
                                    functionClass.size());
                        }
                        if (confidenceIntervalsCoC.get(co)[c] == null) {
                            confidenceIntervalsCoC.get(co)[c] = newInterval;
                        } else {
                            confidenceIntervalsCoC.get(co)[c] = newInterval;
                        }
                        if (confidenceIntervalsCoC.get(co)[c].getLowerBound() >
                                confidenceIntervalsCoC.get(co)[c].getUpperBound()) {
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

                    for (int co = 0; co < cohorts.size(); co++) {
                        double baseCriterionMean = empiricalMeansCoC.get(co)[baseCriterionIndex];
                        ConfidenceInterval baseCriterionInterval = confidenceIntervalsCoC.get(co)[baseCriterionIndex];
                        double squaredCriterionMean = empiricalMeansCoC.get(co)[squaredCriterionIndex];
                        ConfidenceInterval squaredCriterionInterval = confidenceIntervalsCoC.get(co)[squaredCriterionIndex];

                        empiricalMeansCoC.get(co)[c] = squaredCriterionMean - Math.pow(baseCriterionMean, 2);
                        confidenceIntervalsCoC.get(co)[c] = new ConfidenceInterval(
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


            System.out.println("Iteration " + (i + 1) + " of " + maxIterations + " (" + sampleSize + " samples)");
            for (int c = 0; c < criteria.size(); c++) {
                // Computes complexity for each criterion
                double complexityC = complexity.getComplexity(criterionValuesCFS[c]);
                System.out.println("Criteria " + c + " (" + criteria.get(c).toString() + ") Complexity: " + complexityC);
            }

            for (int co = 0; co < cohorts.size(); co++) {
                String functionStatus = "undetermined";
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsCoC.get(co))) {
                    functionStatus = "valid";
                } else if (constraint.isNeverValidRectangle(confidenceIntervalsCoC.get(co))) {
                    functionStatus = "invalid";
                }
                System.out.println("Function " + co + ": " + cohorts.get(co).toString() +
                        "(" + functionStatus + ")");
                double[] currentMeans = new double[criteria.size()];
                for (int c = 0; c < criteria.size(); c++) currentMeans[c] = empiricalMeansCoC.get(co)[c];
                System.out.println("- Objective " + objective.compute(currentMeans)
                        + " in [" + objective.minRectangle(confidenceIntervalsCoC.get(co))
                        + ", " + objective.maxRectangle(confidenceIntervalsCoC.get(co)) + "]");
                for (int c = 0; c < criteria.size(); c++) {
                    System.out.println("- Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                            empiricalMeansCoC.get(co)[c] + " in " +
                            confidenceIntervalsCoC.get(co)[c].toString());
                    List<String> critRecord = new ArrayList<>();
                    critRecord.add(String.valueOf(i));
                    critRecord.add(String.valueOf(sampleSize));
                    critRecord.add(cohorts.get(co).toString());
                    critRecord.add(String.valueOf(objective.compute(currentMeans)));
                    critRecord.add(String.valueOf(objective.minRectangle(confidenceIntervalsCoC.get(co))));
                    critRecord.add(String.valueOf(objective.maxRectangle(confidenceIntervalsCoC.get(co))));
                    critRecord.add(String.valueOf(c));
                    critRecord.add(String.valueOf(empiricalMeansCoC.get(co)[c]));
                    critRecord.add(String.valueOf(confidenceIntervalsCoC.get(co)[c].getLowerBound()));
                    critRecord.add(String.valueOf(confidenceIntervalsCoC.get(co)[c].getUpperBound()));
                    csvWriter.writeNext(critRecord.toArray(new String[critRecord.size()]));
                }
            }
            System.out.println();

            Integer optimalCoIndex = null;
            Double optimalUpperBoundCo = null;
            Double minLowerBound = null;

            List<Integer> toRemove = new ArrayList<>();

            for (int co = 0; co < cohorts.size(); co++) {
                // For functions that we are confident are valid, we find the one with the lowest upper-bound objective
                if (constraint.isAlwaysValidRectangle(confidenceIntervalsCoC.get(co))) {
                    double upperBound = objective.maxRectangle(confidenceIntervalsCoC.get(co));
                    if (optimalCoIndex == null || upperBound < optimalUpperBoundCo) {
                        optimalUpperBoundCo = upperBound;
                        optimalCoIndex = co;
                    }
                }
                // For functions that may be valid, then we find a lower bound on the objective function
                if (!constraint.isNeverValidRectangle(confidenceIntervalsCoC.get(co))) {
                    double lowerBound = objective.minRectangle(confidenceIntervalsCoC.get(co));
                    if (minLowerBound == null || lowerBound > minLowerBound) {
                        minLowerBound = lowerBound;
                    }
                } else {
                    // Remove as a valid function if no intersection with viable region
                    toRemove.add(co);
                }
            }

            if (optimalCoIndex != null) {
                for (int co = 0; co < cohorts.size(); co++) {
                    if ((objective.minRectangle(confidenceIntervalsCoC.get(co)) >
                            objective.maxRectangle(confidenceIntervalsCoC.get(optimalCoIndex)))
                            && !toRemove.contains(co)) {
                        toRemove.add(co);
                    }
                }
            }

            List<CohortEncodingFunction> prunedCohortClass = new ArrayList<>();
            List<int[]> prunedCohortCombinations = new ArrayList<>();
            List<Double[]> prunedEmpiricalMeansCoC = new ArrayList<>();
            List<ConfidenceInterval[]> prunedConfidenceIntervalsCoC = new ArrayList<>();

            for (int co = 0; co < cohorts.size(); co++) {
                if (! toRemove.contains(co)) {
                    prunedCohortClass.add(cohorts.get(co));
                    prunedCohortCombinations.add(cohortCombinations.get(co));
                    prunedEmpiricalMeansCoC.add(empiricalMeansCoC.get(co));
                    prunedConfidenceIntervalsCoC.add(confidenceIntervalsCoC.get(co));
                }
            }

//            Double[][][] prunedAllCriterionValuesCFS = new Double[criteria.size()][prunedCohortClass.size()][numSamples];
//            int fIndex = 0;
//            for (int co = 0; co < cohorts.size(); co++) {
//                if (! toRemove.contains(co)) {
//                    for (int c = 0; c < criteria.size(); c++) {
//                        prunedAllCriterionValuesCFS[c][fIndex] = allCriterionValuesCFS[c][f];
//                    }
//                    fIndex++;
//                }
//            }


            System.out.println("Optimal Function: " + cohorts.get(optimalCoIndex).toString());

            System.out.println("Remaining Functions: " + prunedCohortClass.size());

            System.out.println("Smallest Interval: " +
                    (objective.maxRectangle(confidenceIntervalsCoC.get(optimalCoIndex))
                            - objective.minRectangle(confidenceIntervalsCoC.get(optimalCoIndex))));

            System.out.println("Iterations: " + (i + 1));

            if (prunedCohortClass.size() == 0) {
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

            cohorts = prunedCohortClass;
            cohortCombinations = prunedCohortCombinations;
            empiricalMeansCoC = prunedEmpiricalMeansCoC;
            confidenceIntervalsCoC = prunedConfidenceIntervalsCoC;
//            allCriterionValuesCFS = prunedAllCriterionValuesCFS;
            firstSample += sampleSize;
            sampleSize *= 2;

        }

        try {
            csvWriter.close();
            //writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If no valid function is found, throw exception

        throw new InsufficientSampleSizeException();


    }

//    public int[] getNextCombination(int n, int k, int[] combination) {
//        assert combination.length == k;
//        int[] newCombination = combination.clone();
//        if (combination[0] == n - k) {
//            return null;
//        } else {
//            for (int i = 1; i <= k; i++) {
//                if (newCombination[k - i] != n - i) {
//                    newCombination[k - i]++;
//                    for (int j = 0; j < i - 1; j++) {
//                        newCombination[k - i + j + 1] = newCombination[k - i + j] + 1;
//                    }
//                    break;
//                }
//            }
//        }
//        return newCombination;
//    }

    public int[] getCombination(long index, int n, int k) {
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
}