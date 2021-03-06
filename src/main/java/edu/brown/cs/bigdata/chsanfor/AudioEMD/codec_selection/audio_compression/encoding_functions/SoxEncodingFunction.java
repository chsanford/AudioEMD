package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Clayton on 2/9/18.
 */
public abstract class SoxEncodingFunction extends EncodingFunction {
    private Runtime run = Runtime.getRuntime();

    public SoxEncodingFunction(String extension, List<Criterion> criteria) {
        super(extension, criteria);
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
