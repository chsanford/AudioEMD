package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.DenoisingAlgorithm;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.RNNoiseDenoisingAlgorithm;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.Objects;

/**
 * Intended to be used for testing/running various commands.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            denoiseExample();
        }
        else if (Objects.equals(args[0], "codec")) {
            edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.Main.main(args);
        }
    }

    /**
     * Uses the RNNoise algorithm to denoise a sound file and save its output
     */
    private static void denoiseExample() {
        AudioSequence input = new AudioSequence(new File("data/sample/p257_424.wav"));
        DenoisingAlgorithm rnnoise = new RNNoiseDenoisingAlgorithm();
        AudioSequence output = rnnoise.apply(input, new File("temp/denoised_output.wav"));
    }
}
