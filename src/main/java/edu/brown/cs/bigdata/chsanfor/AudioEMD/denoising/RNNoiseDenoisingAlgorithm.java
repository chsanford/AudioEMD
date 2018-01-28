package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;

import java.io.File;
import java.io.IOException;

public class RNNoiseDenoisingAlgorithm extends CommandLineDenoisingAlgorithm {

    private String runCommandLine = "./denoising-algs/rnnoise/examples/rnnoise_demo";
    private Runtime run = Runtime.getRuntime();


    public RNNoiseDenoisingAlgorithm() {
        super("raw");
    }

    @Override
    public void convertForDenoising(File inputFile, File convertedLocation) {
        try {
            Process proc = run.exec("sox " + inputFile.getAbsolutePath()
                    + " -b 16 -c 1 -r 48k -e unsigned -t raw " + convertedLocation.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void denoise(File fileToDenoise, File denoisedLocation) {
        try {
            Process proc = run.exec(runCommandLine
                    + " " + fileToDenoise.getAbsolutePath()
                    + " " + denoisedLocation.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertBack(File denoisedOutput, File finalLocation) {
        try {
            Process proc = run.exec("sox -r 48k -b 16 -c 1 -e unsigned "
                    + denoisedOutput.getAbsolutePath() + " "
                    + finalLocation.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
