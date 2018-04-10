package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;

/**
 * Provides a framework for audio denoising algorithms of all kinds.
 */
public interface DenoisingAlgorithm {

    /**
     * Applies the denoising algorithm to some audio sequence
     * @param inputSequence the audio sequence to apply the algorithm to
     * @param outputLocation the file location for the denoised sequence
     * @return an AudioSequence corresponding to outputLocation
     */
    public AudioSequence apply(AudioSequence inputSequence, File outputLocation);
}
