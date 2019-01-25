package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Clayton on 2/27/18.
 */
public class Main {
    public static void main(String[] args) {

        String option = args[1];

        System.out.println(option);

        int initialSampleSize = 1000;
        double epsilon = 0.01;
        double delta = 0.05;

        List<Criterion> criteria = Arrays.asList(
                new PEAQObjectiveDifferenceCriterion(),
                new CompressionRatioCriterion(),
                new RootMeanSquaredErrorCriterion(),
                new EncodingTimeCriterion(),
                new DecodingTimeCriterion()
        );

        List<Function> functionClass = Arrays.asList(
                (Function) new LameMP3EncodingFunction(1, criteria),
                (Function) new LameMP3EncodingFunction(2, criteria),
                (Function) new LameMP3EncodingFunction(3, criteria),
                (Function) new LameMP3EncodingFunction(4, criteria),
                (Function) new LameMP3EncodingFunction(5, criteria),
                (Function) new LameMP3EncodingFunction(6, criteria),
                (Function) new LameMP3EncodingFunction(7, criteria),
                (Function) new LameMP3EncodingFunction(8, criteria),
                (Function) new LameMP3EncodingFunction(9, criteria),
                (Function) new LameConstantMP3EncodingFunction(320, criteria),
                (Function) new LameConstantMP3EncodingFunction(256, criteria),
                (Function) new LameConstantMP3EncodingFunction(128, criteria),
                (Function) new LameConstantMP3EncodingFunction(64, criteria));

        Objective objective = new Objective(new double[]{1, 1, 0, 0, 0});

        Constraint constraint = new Constraint(
                new double[][]{{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}},
                new double[]{0.5, 0.5}
        );

        GlobalSampling bf = new GlobalSampling(new OneShotRademacherComplexity());

        if (Objects.equals(option, "ps-directory")) {
            ProgressiveSampling psERC = new ProgressiveSampling(new OneShotRademacherComplexity());

            List<Sample> audioSamples = loadDataset(args[2]);
            try {
                psERC.runAlgorithm(
                        audioSamples,
                        initialSampleSize,
                        functionClass,
                        criteria,
                        objective,
                        constraint,
                        epsilon,
                        delta);
            } catch (InsufficientSampleSizeException |
                    NoSatisfactoryFunctionsException |
                    EmptyConfidenceIntervalException |
                    IncorrectlyClassifiedCriterionException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(option, "fill-csv")) {
            ProgressiveSampling psERC = new ProgressiveSampling(new OneShotRademacherComplexity());


            List<Sample> audioSamples = loadDataset(args[2]);
            String sampleCSV = args[3];
            psERC.fillCSV(
                    audioSamples,
                    functionClass,
                    criteria,
                    new File(sampleCSV)
            );
        } else if (Objects.equals(option, "ps-csv") || (Objects.equals(option, "bf-csv"))) {
            String sampleCSV = args[2];
            String outCSV = args[3];

            String functionOption = args[4];
            String criteriaOption = args[5];
            String objectiveOption = args[6];
            String constraintsOption = args[7];
            String boundOption = args[8];
            String approximationOption = args[9];
            delta = Double.valueOf(args[10]);
            epsilon = Double.valueOf(args[11]);
            initialSampleSize = Integer.valueOf(args[12]);
            int cohortSize = Integer.valueOf(args[13]);

            switch (functionOption) {
                case "ALL":
                    functionClass = Arrays.asList(
                            (Function) new LameMP3EncodingFunction(1, criteria),
                            (Function) new LameMP3EncodingFunction(2, criteria),
                            (Function) new LameMP3EncodingFunction(3, criteria),
                            (Function) new LameMP3EncodingFunction(4, criteria),
                            (Function) new LameMP3EncodingFunction(5, criteria),
                            (Function) new LameMP3EncodingFunction(6, criteria),
                            (Function) new LameMP3EncodingFunction(7, criteria),
                            (Function) new LameMP3EncodingFunction(8, criteria),
                            (Function) new LameMP3EncodingFunction(9, criteria),
                            (Function) new LameConstantMP3EncodingFunction(320, criteria),
                            (Function) new LameConstantMP3EncodingFunction(256, criteria),
                            (Function) new LameConstantMP3EncodingFunction(128, criteria),
                            (Function) new LameConstantMP3EncodingFunction(64, criteria));
                    break;
                case "VBR":
                    functionClass = Arrays.asList(
                            (Function) new LameMP3EncodingFunction(1, criteria),
                            (Function) new LameMP3EncodingFunction(2, criteria),
                            (Function) new LameMP3EncodingFunction(3, criteria),
                            (Function) new LameMP3EncodingFunction(4, criteria),
                            (Function) new LameMP3EncodingFunction(5, criteria),
                            (Function) new LameMP3EncodingFunction(6, criteria),
                            (Function) new LameMP3EncodingFunction(7, criteria),
                            (Function) new LameMP3EncodingFunction(8, criteria),
                            (Function) new LameMP3EncodingFunction(9, criteria));
                    break;
                case "ODD-VBR-CBR":
                    functionClass = Arrays.asList(
                            (Function) new LameMP3EncodingFunction(1, criteria),
                            (Function) new LameMP3EncodingFunction(3, criteria),
                            (Function) new LameMP3EncodingFunction(5, criteria),
                            (Function) new LameMP3EncodingFunction(7, criteria),
                            (Function) new LameMP3EncodingFunction(9, criteria),
                            (Function) new LameConstantMP3EncodingFunction(320, criteria),
                            (Function) new LameConstantMP3EncodingFunction(256, criteria),
                            (Function) new LameConstantMP3EncodingFunction(128, criteria),
                            (Function) new LameConstantMP3EncodingFunction(64, criteria));
                    break;
                case "ODD-VBR":
                    functionClass = Arrays.asList(
                            (Function) new LameMP3EncodingFunction(1, criteria),
                            (Function) new LameMP3EncodingFunction(3, criteria),
                            (Function) new LameMP3EncodingFunction(5, criteria),
                            (Function) new LameMP3EncodingFunction(7, criteria),
                            (Function) new LameMP3EncodingFunction(9, criteria));
                    break;
            }

            switch (criteriaOption) {
                case "ALL-VAR":
                    criteria = Arrays.asList(
                            new PEAQObjectiveDifferenceCriterion(),
                            new RawMomentCriterion(new PEAQObjectiveDifferenceCriterion(), 2),
                            new VarianceCriterion(new PEAQObjectiveDifferenceCriterion()),
                            new CompressionRatioCriterion(),
                            new RawMomentCriterion(new CompressionRatioCriterion(), 2),
                            new VarianceCriterion(new CompressionRatioCriterion()),
                            new RootMeanSquaredErrorCriterion(),
                            new RawMomentCriterion(new RootMeanSquaredErrorCriterion(), 2),
                            new VarianceCriterion(new RootMeanSquaredErrorCriterion()),
                            new EncodingTimeCriterion(),
                            new DecodingTimeCriterion());
                    switch (objectiveOption) {
                        case "PEAQ":
                            objective = new Objective(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
                            break;
                        case "CR":
                            objective = new Objective(new double[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0});
                            break;
                        case "MSE":
                            objective = new Objective(new double[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0});
                            break;
                        case "PEAQ-CR":
                            objective = new Objective(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0});
                            break;
                        case "PEAQ-2CR":
                            objective = new Objective(new double[]{1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0});
                            break;
                    }
                    switch (constraintsOption) {
                        case "TIME":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}},
                                    new double[]{0.5, 0.5});
                            break;
                        case "PEAQ-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}},
                                    new double[]{0.5});
                            break;
                        case "CR-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}},
                                    new double[]{0.5});
                            break;
                        case "PEAQ-CR-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}},
                                    new double[]{0.5, 0.5});
                            break;
                        case "NONE":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}},
                                    new double[]{0});
                            break;
                    }
                    break;
                case "PEAQ-CR-VAR":
                    criteria = Arrays.asList(
                            new PEAQObjectiveDifferenceCriterion(),
                            new RawMomentCriterion(new PEAQObjectiveDifferenceCriterion(), 2),
                            new VarianceCriterion(new PEAQObjectiveDifferenceCriterion()),
                            new CompressionRatioCriterion(),
                            new RawMomentCriterion(new CompressionRatioCriterion(), 2),
                            new VarianceCriterion(new CompressionRatioCriterion()));
                    switch (objectiveOption) {
                        case "PEAQ":
                            objective = new Objective(new double[]{1, 0, 0, 0, 0, 0});
                            break;
                        case "CR":
                            objective = new Objective(new double[]{0, 0, 0, 1, 0, 0});
                            break;
                        case "PEAQ-CR":
                            objective = new Objective(new double[]{1, 0, 0, 1, 0, 0});
                            break;
                        case "PEAQ-2CR":
                            objective = new Objective(new double[]{1, 0, 0, 2, 0, 0});
                            break;
                    }
                    switch (constraintsOption) {
                        case "PEAQ-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 1, 0, 0, 0}},
                                    new double[]{0.5});
                            break;
                        case "CR-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0, 1}},
                                    new double[]{0.5});
                            break;
                        case "PEAQ-CR-VAR":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 1}},
                                    new double[]{0.5, 0.5});
                            break;
                        case "NONE":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0, 0}},
                                    new double[]{0});
                            break;
                    }
                    break;
                case "ALL":
                    criteria = Arrays.asList(
                            new PEAQObjectiveDifferenceCriterion(),
                            new CompressionRatioCriterion(),
                            new RootMeanSquaredErrorCriterion(),
                            new EncodingTimeCriterion(),
                            new DecodingTimeCriterion());
                    switch (objectiveOption) {
                        case "PEAQ":
                            objective = new Objective(new double[]{1, 0, 0, 0, 0});
                            break;
                        case "CR":
                            objective = new Objective(new double[]{0, 1, 0, 0, 0});
                            break;
                        case "MSE":
                            objective = new Objective(new double[]{0, 0, 1, 0, 0});
                            break;
                        case "PEAQ-CR":
                            objective = new Objective(new double[]{1, 1, 0, 0, 0});
                            break;
                        case "PEAQ-2CR":
                            objective = new Objective(new double[]{1, 2, 0, 0, 0});
                            break;
                    }
                    switch (constraintsOption) {
                        case "TIME":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}},
                                    new double[]{0.5, 0.5});
                            break;
                        case "NONE":
                            constraint = new Constraint(
                                    new double[][]{{0, 0, 0, 0, 0}},
                                    new double[]{0});
                            break;
                    }
                    break;
                case "PEAQ-CR":
                    criteria = Arrays.asList(
                            new PEAQObjectiveDifferenceCriterion(),
                            new CompressionRatioCriterion());
                    switch (objectiveOption) {
                        case "PEAQ":
                            objective = new Objective(new double[]{1, 0});
                            break;
                        case "CR":
                            objective = new Objective(new double[]{0, 1});
                            break;
                        case "PEAQ-CR":
                            objective = new Objective(new double[]{1, 1});
                            break;
                        case "PEAQ-2CR":
                            objective = new Objective(new double[]{1, 2});
                            break;
                    }
                    constraint = new Constraint(
                            new double[][]{{0, 0}},
                            new double[]{0});
                    break;
            }

            EmpiricalComplexity complexityMeasure = null;
            switch (boundOption) {
                case "ERC":
                    complexityMeasure = new OneShotRademacherComplexity();
                    break;
                case "EMD":
                    complexityMeasure = new EMDComplexity();
                    break;
                case "HOEF-U":
                    complexityMeasure = new HoeffdingUnionBound();
                    break;
                case "G-CH":
                    complexityMeasure = new GaussianChernoffBound();
                    break;
            }

            boolean isApproximation = (Objects.equals(approximationOption, "TRUE"));


            try {
                if (cohortSize == 1) {
                    if (Objects.equals(option, "ps-csv")) {
                        ProgressiveSampling psAlg = new ProgressiveSampling(complexityMeasure);
                        psAlg.runAlgorithm(
                                new File(sampleCSV),
                                new File(outCSV),
                                initialSampleSize,
                                functionClass,
                                criteria,
                                objective,
                                constraint,
                                epsilon,
                                delta,
                                isApproximation);
                    } else {
                        GlobalSampling gsAlg = new GlobalSampling(complexityMeasure);
                        gsAlg.runAlgorithm(
                                new File(sampleCSV),
                                new File(outCSV),
                                functionClass,
                                criteria,
                                objective,
                                constraint,
                                delta,
                                isApproximation);
                    }
                } else {
                    if (Objects.equals(option, "ps-csv")) {
                        ProgressiveSampling psAlg = new ProgressiveSampling(complexityMeasure);
                        psAlg.runMultiAlgorithm(
                                new File(sampleCSV),
                                new File(outCSV),
                                initialSampleSize,
                                functionClass,
                                criteria,
                                objective,
                                constraint,
                                epsilon,
                                delta,
                                isApproximation,
                                cohortSize);
                    }
//                    else {
//                        GlobalSampling gsAlg = new GlobalSampling(complexityMeasure);
//                        gsAlg.runAlgorithm(
//                                new File(sampleCSV),
//                                new File(outCSV),
//                                functionClass,
//                                criteria,
//                                objective,
//                                constraint,
//                                delta,
//                                isApproximation);
//                    }
                }

            } catch (InsufficientSampleSizeException |
                    NoSatisfactoryFunctionsException |
                    EmptyConfidenceIntervalException |
                    IncorrectlyClassifiedCriterionException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(option, "merge-csv-samples")) {
            ProgressiveSampling psERC = new ProgressiveSampling(new OneShotRademacherComplexity());
            File csv1 = new File(args[2]);
            File csv2 = new File(args[3]);
            File outCSV = new File(args[4]);
            psERC.mergeCSVSamples(csv1, csv2, outCSV);
        } else if (Objects.equals(option, "merge-csv-functions")) {
            ProgressiveSampling psERC = new ProgressiveSampling(new OneShotRademacherComplexity());
            File csv1 = new File(args[2]);
            File csv2 = new File(args[3]);
            File outCSV = new File(args[4]);
            psERC.mergeCSVFunctions(csv1, csv2, outCSV);
        } else if (Objects.equals(option, "combinatorics-test")) {
            ProgressiveSampling psERC = new ProgressiveSampling(new OneShotRademacherComplexity());
            for (long i = 0; i < CombinatoricsUtils.binomialCoefficient(10, 6); i++) {
                System.out.println(i + Arrays.toString(psERC.getCombination(i, 10, 6)));
            }

        }
    }

    //Loading Datasets:
    public static List<Sample> loadDataset(List<String> directories, String filter) {
        List<Sample> samples = new ArrayList<>();
        for(String d : directories) {
            for(File f : new File(d).listFiles()) {
                if(filter.equals("") || f.getName().matches(filter)) {
                    samples.add(new AudioSequence(f));
                }
            }
        }
        return samples;
    }
    public static List<Sample> loadDataset(List<String> directories) {
        return loadDataset(directories, "");
    }
    public static List<Sample> loadDataset(String directory) {
        List<String> l = new ArrayList<>();
        l.add(directory);
        return loadDataset(l);
    }
}
