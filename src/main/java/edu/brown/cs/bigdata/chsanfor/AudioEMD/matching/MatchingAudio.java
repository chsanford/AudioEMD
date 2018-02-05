package edu.brown.cs.bigdata.chsanfor.AudioEMD.matching;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.*;

/**
 * Mixes clean files and noise files to create lots of audio data to denoise.
 */
public abstract class MatchingAudio {
    /**
     * Combines clusters of audio sequences, making pairs of clean and noise files
     * @param cleanAudio clean audio sequences to be paired up in clusters
     * @param noiseAudio noise audio sequences to be paired up in clusters
     * @param destPath location to output the files representing the pairs of noise and clean files
     * @return A list of NoiseCleanPair, pairs of noisy and clean data
     */
    public List<NoiseCleanPair> matchAudio(
            AudioSequence[] cleanAudio,
            AudioSequence[] noiseAudio,
            File destPath) {

        // permutes files to make clusters random
        List<AudioSequence> cleanList = Arrays.asList(cleanAudio);
        List<AudioSequence> noiseList = Arrays.asList(noiseAudio);
        Collections.shuffle(cleanList);
        Collections.shuffle(noiseList);

        return createPairs(cleanList, noiseList, destPath);
    }

    /**
     * To be implemented by subclasses. Dictates how pairs are created.
     * @param cleanList clean audio sequences to be paired up in clusters
     * @param noiseList noise audio sequences to be paired up in clusters
     * @param destPath location to output the files representing the pairs of noise and clean files
     * @return A list of NoiseCleanPair, pairs of noisy and clean data
     */
    protected abstract List<NoiseCleanPair> createPairs(
            List<AudioSequence> cleanList,
            List<AudioSequence> noiseList,
            File destPath);
}
