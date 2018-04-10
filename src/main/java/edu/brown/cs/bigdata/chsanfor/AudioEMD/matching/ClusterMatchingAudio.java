package edu.brown.cs.bigdata.chsanfor.AudioEMD.matching;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mixes clusters of clean files and noise files to create lots of audio data to denoise.
 *
 * Each cluster consists of a clean files and b noise files. Pairs are made of all combinations within each cluster.
 * The clusters themselves are chosen at random.
 */
public class ClusterMatchingAudio extends MatchingAudio {
    private int cleanClusterSize;
    private int noiseClusterSize;

    public ClusterMatchingAudio(int cleanClusterSize, int noiseClusterSize) {
        this.cleanClusterSize = cleanClusterSize;
        this.noiseClusterSize = noiseClusterSize;
    }

    public List<NoiseCleanPair> createPairs(
            List<AudioSequence> cleanList,
            List<AudioSequence> noiseList,
            File destPath) {
        // sizes of clusters must be smaller than the number of each sample
        assert cleanClusterSize <= cleanList.size();
        assert noiseClusterSize <= noiseList.size();

        int cleanIndex = 0;
        int noiseIndex = 0;
        List<NoiseCleanPair> outPairs = new ArrayList<>();
        while (cleanIndex < cleanList.size() && noiseIndex < noiseList.size()) {
            // Creates a cluster by adding a set number of clean and noise samples together and making all combinations
            List<AudioSequence> cleanSublist = new ArrayList<>();
            for (int i = 0; i < cleanClusterSize; i++) {
                if (cleanIndex < cleanList.size()) cleanSublist.add(cleanList.get(cleanIndex));
                cleanIndex++;
            }
            List<AudioSequence> noiseSublist = new ArrayList<>();
            for (int j = 0; j < noiseClusterSize; j++) {
                if (noiseIndex < noiseList.size()) noiseSublist.add(noiseList.get(noiseIndex));
                noiseIndex++;
            }
            outPairs.addAll(mixCleanNoise(cleanSublist, noiseSublist, destPath));
        }
        return outPairs;
    }

    private List<NoiseCleanPair> mixCleanNoise(
            List<AudioSequence> cleanSublist, List<AudioSequence> noiseSublist, File destPath) {
        List<NoiseCleanPair> output = new ArrayList<>();
        for (AudioSequence cleanSeq : cleanSublist) {
            for (AudioSequence noiseSeq : noiseSublist) {
                output.add(new NoiseCleanPair(
                        noiseSeq,
                        cleanSeq,
                        new File(destPath.getPath() + noiseSeq.getFileName() +
                                "_" + cleanSeq.getFileName() + "wav")));
            }
        }
        return output;
    }
}
