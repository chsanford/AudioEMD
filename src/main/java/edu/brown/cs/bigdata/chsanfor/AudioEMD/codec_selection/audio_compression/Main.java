package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.MP3EncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions.OggEncodingFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionRatioCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.EncodingTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.DecodingTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.MeanSquaredErrorCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Clayton on 2/27/18.
 */
public class Main {
    public static void main(String[] args) {
        File originalDir = new File("data/edinburgh/noisy_testset_wav/");
        File[] files = originalDir.listFiles();
        List<Sample> audioSamples = new ArrayList<>();
        for (File file : files) audioSamples.add(new AudioSequence(file));

        List<Function> functionClass = Arrays.asList(
                (Function) new MP3EncodingFunction(),
                (Function) new OggEncodingFunction()
        );

        List<Criterion> criteria = Arrays.asList(
                new MeanSquaredErrorCriterion(),
                new CompressionRatioCriterion(),
                new EncodingTimeCriterion(),
                new DecodingTimeCriterion()
        );

        Objective objective = new Objective(new double[]{-0.5, -0.5, 0, 0});

        //Constraint constraint = new Constraint();
        Constraint constraint = new Constraint(
                new double[][]{{0, 0, 1, 0}, {0, 0, 0, 1}},
                new double[]{0.5, 0.5}
        );

        BruteForce bf = new BruteForce(new OneShotRademacherComplexity());
        ProgressiveSampling ps = new ProgressiveSampling(new OneShotRademacherComplexity());

        try {
            /*AlgorithmSelectionOutput out = bf.runAlgorithm(
                    audioSamples,
                    functionClass,
                    criteria,
                    objective,
                    constraint,
                    0.05);*/
            AlgorithmSelectionOutput out = ps.runAlgorithm(
                    audioSamples,
                    50,
                    functionClass,
                    criteria,
                    objective,
                    constraint,
                    0.05,
                    0.5);


            System.out.println("Best algorithm: " + out.getOptimalFunction().toString());
            System.out.println("Objective upper bound: " + out.getUpperBound());
            for (int c = 0; c < criteria.size(); c++) {
                System.out.println("Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                        out.getOptimalCriteriaMeansC()[c] + " in " +
                        out.getOptimalCriteriaConfidenceIntervalsC()[c].toString());
            }

        } catch (InsufficientSampleSizeException | NoSatisfactoryFunctionsException | EmptyConfidenceIntervalException e) {
            e.printStackTrace();
        }
    }

}
