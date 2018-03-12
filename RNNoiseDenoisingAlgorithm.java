package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;

import java.io.File;
import java.io.IOException;

/**
 * Represents the RNNoise neural net (https://github.com/xiph/rnnoise/) approach to denoising audio.
 * Takes RAW files as input.
 */
public class RNNoiseDenoisingAlgorithm extends CommandLineDenoisingAlgorithm {

    private Runtime run = Runtime.getRuntime();


    public RNNoiseDenoisingAlgorithm() {
        super("raw", " -b 16 -c 1 -r 48k -e unsigned -t raw ", "./denoising_algs/rnnoise/examples/rnnoise_demo", 
        		"sox -r 48k -b 16 -c 1 -e unsigned ");
    }

}
