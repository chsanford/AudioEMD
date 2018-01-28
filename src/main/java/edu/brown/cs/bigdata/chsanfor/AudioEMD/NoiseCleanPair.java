package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import java.io.File;

public class NoiseCleanPair {

    private AudioSequence cleanSeq;
    private AudioSequence noiseSeq;
    private File noisyFile;

    private boolean hasNoisyFile;

    public NoiseCleanPair(AudioSequence cleanSeq, AudioSequence noiseSeq, File noisyFile) {
        this.cleanSeq = cleanSeq;
        this.noiseSeq = noiseSeq;
        this.noisyFile = noisyFile;
        hasNoisyFile = false;
    }

    public boolean hasNoisyFile() {
        return hasNoisyFile;
    }

    public AudioSequence getCleanSeq() {
        return cleanSeq;
    }

    public AudioSequence getNoiseSeq() {
        return noiseSeq;
    }

    public AudioSequence getNoisySeq() {
        if (hasNoisyFile) {
            return new AudioSequence(noisyFile);
        } else {
            hasNoisyFile = true;
            return LearningAudioDenoising.mergeAudioSequences(cleanSeq, noiseSeq, noisyFile);
        }
    }

    public void removeNoisyFile() {
        if (hasNoisyFile) {
            noisyFile.delete();
            hasNoisyFile = false;
        }
    }

}
