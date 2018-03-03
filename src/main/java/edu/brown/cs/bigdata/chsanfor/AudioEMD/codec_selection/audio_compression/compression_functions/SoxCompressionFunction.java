package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.io.IOException;

/**
 * Created by Clayton on 2/9/18.
 */
public abstract class SoxCompressionFunction extends CompressionFunction {
    private Runtime run = Runtime.getRuntime();

    public SoxCompressionFunction(String extension) {
        super(extension);
    }

    @Override
    public void compress(AudioSequence input, File output) {
        try {
            Process proc = run.exec("sox "
                    + input.getAudioFile().getAbsolutePath() + " "
                    + output.getAbsolutePath());
            // For each command, it's necessary to wait until the procedure completes.
            // Otherwise, the next command may operate on an empty file.
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decompress(File input, File output) {
        try {
            Process proc = run.exec("sox "
                    + input.getAbsolutePath() + " "
                    + output.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
