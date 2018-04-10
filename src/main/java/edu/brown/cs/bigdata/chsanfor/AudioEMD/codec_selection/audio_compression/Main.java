package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.LameMP3EncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.MP3EncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.OggEncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

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
        assert args.length >= 3;
        String option = args[1];

        System.out.println(option);

        int initialSampleSize = 100;
        double epsilon = 0.05;
        double delta = 0.05;

        List<Criterion> criteria = Arrays.asList(
                new PEAQObjectiveDifferenceCriterion(),
                new RootMeanSquaredErrorCriterion(),
                new CompressionRatioCriterion(),
                new EncodingTimeCriterion(),
                new DecodingTimeCriterion()
        );

        List<Function> functionClass = Arrays.asList(
                (Function) new LameMP3EncodingFunction(1, criteria),
                (Function) new LameMP3EncodingFunction(3, criteria),
                (Function) new LameMP3EncodingFunction(5, criteria),
                (Function) new LameMP3EncodingFunction(7, criteria),
                (Function) new LameMP3EncodingFunction(9, criteria)
        );

        Objective objective = new Objective(new double[]{-0.5, 0, -0.5, 0, 0});

        Constraint constraint = new Constraint(
                new double[][]{{0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}},
                new double[]{0.5, 0.5}
        );

        BruteForce bf = new BruteForce(new OneShotRademacherComplexity());
        ProgressiveSampling ps = new ProgressiveSampling(new OneShotRademacherComplexity());

        if (Objects.equals(option, "ps-directory")) {
            String dirPath = args[2];

            File originalDir = new File(dirPath);
            File[] files = originalDir.listFiles();
            List<Sample> audioSamples = new ArrayList<>();
            for (File file : files) audioSamples.add(new AudioSequence(file));

            try {
                AlgorithmSelectionOutput out = ps.runAlgorithm(
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
                    EmptyConfidenceIntervalException e) {
                e.printStackTrace();
            }

        } else if (Objects.equals(option, "fill-csv")) {
            String dirPath = args[2];
            String sampleCSV = args[3];

            File originalDir = new File(dirPath);
            File[] files = originalDir.listFiles();
            List<Sample> audioSamples = new ArrayList<>();
            for (File file : files) audioSamples.add(new AudioSequence(file));

            ps.fillCSV(
                    audioSamples,
                    functionClass,
                    criteria,
                    new File(sampleCSV)
            );

        } else if (Objects.equals(option, "ps-csv")) {
            String sampleCSV = args[2];
            String outCSV = args[3];
            try {
                AlgorithmSelectionOutput out = ps.runAlgorithm(
                        new File(sampleCSV),
                        new File(outCSV),
                        initialSampleSize,
                        functionClass,
                        criteria,
                        objective,
                        constraint,
                        epsilon,
                        delta);
            } catch (InsufficientSampleSizeException |
                    NoSatisfactoryFunctionsException |
                    EmptyConfidenceIntervalException e) {
                e.printStackTrace();
            }
        }
    }
}
