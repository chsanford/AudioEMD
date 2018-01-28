package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.AudioSequence;

import java.io.File;

/**
 * Must be implemented by all denoising algorithms to remove noise from a sequence.
 */
public interface DenoisingAlgorithm {

    public AudioSequence apply(AudioSequence inputSequence, File outputLocation);
}
