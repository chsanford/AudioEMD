package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.MP3CompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.OggCompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionRatioCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.DecompressionTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.ErrorCriterion;
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
                (Function) new MP3CompressionFunction(),
                (Function) new OggCompressionFunction()
        );

        List<Criterion> criteria = Arrays.asList(
                new ErrorCriterion(),
                new CompressionRatioCriterion(),
                new CompressionTimeCriterion(),
                new DecompressionTimeCriterion()
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
            /*
            AlgorithmSelectionOutput out = bf.runAlgorithm(
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

            System.out.println("Best algorithm: " + out.getOptimalFunction().getClass().getName());
            System.out.println("Criteria 0 mean: " + out.getOptimalCriteriaMeansC()[0]);
            System.out.println("Criteria 1 mean: " + out.getOptimalCriteriaMeansC()[1]);
            System.out.println("Criteria 2 mean: " + out.getOptimalCriteriaMeansC()[2]);
            System.out.println("Criteria 3 mean: " + out.getOptimalCriteriaMeansC()[3]);
            System.out.println("Objective upper bound: " + out.getUpperBound());
            System.out.println("Criteria 0 lower bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[0].getLowerBound());
            System.out.println("Criteria 0 upper bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[0].getUpperBound());
            System.out.println("Criteria 1 lower bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[1].getLowerBound());
            System.out.println("Criteria 1 upper bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[1].getUpperBound());
            System.out.println("Criteria 2 lower bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[2].getLowerBound());
            System.out.println("Criteria 2 upper bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[2].getUpperBound());
            System.out.println("Criteria 3 lower bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[3].getLowerBound());
            System.out.println("Criteria 3 upper bound: " + out.getOptimalCriteriaConfidenceIntervalsC()[3].getUpperBound());

        } catch (InsufficientSampleSizeException e) {
            e.printStackTrace();
        } catch (NoSatisfactoryFunctionsException e) {
            e.printStackTrace();
        } catch (EmptyConfidenceIntervalException e) {
            e.printStackTrace();
        }
    }

}
