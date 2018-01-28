package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.DenoisingAlgorithm;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.RNNoiseDenoisingAlgorithm;

import java.io.File;

/**
 * Created by Clayton on 1/28/18.
 */
public class Main {
    public static void main(String[] args) {
        AudioSequence input = new AudioSequence(new File("data/scp/p257_424.wav"));
        DenoisingAlgorithm rnnoise = new RNNoiseDenoisingAlgorithm();
        AudioSequence output = rnnoise.apply(input, new File("temp/denoised_output.wav"));
    }
}
