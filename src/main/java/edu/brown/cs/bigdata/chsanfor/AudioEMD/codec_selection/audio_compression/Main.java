package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.GSMCompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.MP3CompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions.OggCompressionFunction;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionRatioCriterion;
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
                new CompressionRatioCriterion()
        );

        Objective objective = new Objective(new double[]{-0.5, -0.5});

        Constraint constraint = new Constraint();

        BruteForce bf = new BruteForce(new OneShotRademacherComplexity());

        try {
            BruteForceOutput out = bf.runAlgorithm(
                    audioSamples,
                    functionClass,
                    criteria,
                    objective,
                    constraint,
                    0.05);

        } catch (InsufficientSampleSizeException e) {
            e.printStackTrace();
        }
    }

}
