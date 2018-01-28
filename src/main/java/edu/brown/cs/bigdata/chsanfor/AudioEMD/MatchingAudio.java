package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Mixes clusters of clean files and noise files to create lots of audio data to denoise
 */
public class MatchingAudio {
    /**
     * Combines clusters of audio sequences, making pairs of clean and noise files
     * @param cleanAudio
     * @param noiseAudio
     * @param cleanClusterSize
     * @param noiseClusterSize
     * @param destPath
     * @return A map mapping noise audio sequences to clean sequences
     */
    public static List<NoiseCleanPair> MatchAudio(
            AudioSequence[] cleanAudio,
            AudioSequence[] noiseAudio,
            int cleanClusterSize,
            int noiseClusterSize,
            File destPath) {
        List<AudioSequence> cleanList = Arrays.asList(cleanAudio);
        List<AudioSequence> noiseList = Arrays.asList(noiseAudio);
        Collections.shuffle(cleanList);
        Collections.shuffle(noiseList);
        int cleanIndex = 0;
        int noiseIndex = 0;
        List<NoiseCleanPair> outPairs = new ArrayList<>();
        while (cleanIndex < cleanList.size() && noiseIndex < noiseList.size()) {
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

    private static List<NoiseCleanPair> mixCleanNoise(
            List<AudioSequence> cleanSublist, List<AudioSequence> noiseSublist, File destPath) {
        List<NoiseCleanPair> output = new ArrayList<>();
        for (AudioSequence cleanSeq : cleanSublist) {
            for (AudioSequence noiseSeq : noiseSublist) {
                output.add(new NoiseCleanPair(
                        noiseSeq,
                        cleanSeq,
                        new File(destPath.getPath() + noiseSeq.getFileName() + "_" + cleanSeq.getFileName())));
            }
        }
        return output;
    }
}
