package edu.brown.cs.bigdata.chsanfor.AudioEMD.matching;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.LearningAudioDenoising;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;

/**
 * Represents a pair of a noise file and a clean file.
 *
 * While this maintains a location for the corresponding mixed noisy file, the file is only actually there if
 * hasNoisyFile is true. This is done for storage reasons -- the cluster format creates prohibitively many noisy files
 * to store at once.
 */
public class NoiseCleanPair {

    private AudioSequence cleanSeq;
    private AudioSequence noiseSeq;
    private File noisyFile;

    private boolean hasNoisyFile;

    /**
     * @param cleanSeq AudioSequence representing the clean audio
     * @param noiseSeq AudioSequence representing the noise audio
     * @param noisyFile (currently empty) file expressing the location of the noisy file
     */
    public NoiseCleanPair(AudioSequence cleanSeq, AudioSequence noiseSeq, File noisyFile) {
        this.cleanSeq = cleanSeq;
        this.noiseSeq = noiseSeq;
        this.noisyFile = noisyFile;
        hasNoisyFile = false;
    }

    /**
     * @return true iff the noisy file has been created from its two component files
     */
    public boolean hasNoisyFile() {
        return hasNoisyFile;
    }

    /**
     * @return AudioSequence representing the clean audio
     */
    public AudioSequence getCleanSeq() {
        return cleanSeq;
    }

    /**
     * @return AudioSequence representing the noise audio
     */
    public AudioSequence getNoiseSeq() {
        return noiseSeq;
    }

    /**
     * Creates the noisy file from the two components and outputs an AudioSequence representing the file
     * @return AudioSequence representing the noisy audio
     */
    public AudioSequence getNoisySeq() {
        if (hasNoisyFile) {
            return new AudioSequence(noisyFile);
        } else {
            hasNoisyFile = true;
            return LearningAudioDenoising.mergeAudioSequences(cleanSeq, noiseSeq, noisyFile);
        }
    }

    /**
     * Deletes the noisy files for data storage reasons
     */
    public void removeNoisyFile() {
        if (hasNoisyFile) {
            noisyFile.delete();
            hasNoisyFile = false;
        }
    }

}
