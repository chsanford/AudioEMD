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

/**
 * Created by Clayton on 2/27/18.
 */
public class Main {
    public static void main(String[] args) {
        String dirPath = "data/edinburgh/noisy_testset_wav/";
        if (args.length >= 2) {
            dirPath = args[1];
        }
        File originalDir = new File(dirPath);
        File[] files = originalDir.listFiles();
        List<Sample> audioSamples = new ArrayList<>();
        for (File file : files) audioSamples.add(new AudioSequence(file));



        List<Criterion> criteria = Arrays.asList(
                new RootMeanSquaredErrorCriterion(),
                new CompressionRatioCriterion(),
                new EncodingTimeCriterion(),
                new DecodingTimeCriterion()
        );

        List<Function> functionClass = Arrays.asList(
                //(Function) new MP3EncodingFunction(criteria),
                //(Function) new OggEncodingFunction(criteria)
                (Function) new LameMP3EncodingFunction(0, criteria),
                (Function) new LameMP3EncodingFunction(1, criteria),
                (Function) new LameMP3EncodingFunction(2, criteria),
                (Function) new LameMP3EncodingFunction(3, criteria),
                (Function) new LameMP3EncodingFunction(4, criteria),
                (Function) new LameMP3EncodingFunction(5, criteria),
                (Function) new LameMP3EncodingFunction(6, criteria)
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
                    0.05);


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
        List<String> l = new ArrayList<String>();
        l.add(directory);
        return loadDataset(l);
    }
}
