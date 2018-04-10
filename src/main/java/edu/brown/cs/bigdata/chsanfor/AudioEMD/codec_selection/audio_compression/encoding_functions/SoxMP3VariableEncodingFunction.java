package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Clayton on 3/14/18.
 */
public class SoxMP3VariableEncodingFunction extends EncodingFunction {
    private static String EXTENSION = "mp3";

    private int bitrate;

    private Runtime run = Runtime.getRuntime();

    /**
     * @param bitrate
     * @param criteria
     */
    public SoxMP3VariableEncodingFunction(int bitrate, List<Criterion> criteria) {
        super(EXTENSION, criteria);
        this.bitrate = bitrate;
    }

    @Override
    public void compress(AudioSequence input, File output) {
        try {
            Process proc = run.exec("sox "
                    + input.getAudioFile().getAbsolutePath()
                    + " -C " + bitrate + " "
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

    @Override
    public String getScheme() {
        return EXTENSION;
    }

    @Override
    public String toString() {
        return "MP3 Compression: Bitrate " + bitrate;
    }
}