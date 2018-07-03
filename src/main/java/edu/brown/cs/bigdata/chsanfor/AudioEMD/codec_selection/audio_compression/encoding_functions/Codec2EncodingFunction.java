package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Clayton on 7/2/18.
 */
public class Codec2EncodingFunction extends EncodingFunction {
    private static String EXTENSION = "bit";
    private static int MAX_BITRATE = 3200;
    private static int MIN_BITRATE = 700;

    private int bitrate;

    private Runtime run = Runtime.getRuntime();

    public Codec2EncodingFunction(int bitrate, List<Criterion> criteria) {
        super(EXTENSION, criteria);
        assert bitrate <= MAX_BITRATE && bitrate >= MIN_BITRATE;
        this.bitrate = bitrate;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void compress(AudioSequence input, File output) {
        try {
            Process proc = run.exec("codecs/codec2/build_linux/src/c2enc "
                    + bitrate + " "
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
        long thread_id = java.lang.Thread.currentThread().getId();
        File tempRaw = new File("data/temp/temp_compressed_" + input.getName() + "_" + thread_id + ".raw");


        try {
            Process procDecompress = run.exec("codecs/codec2/build_linux/src/c2enc "
                    + bitrate + " "
                    + input.getAbsolutePath() + " "
                    + tempRaw.getAbsolutePath());
            procDecompress.waitFor();
            //TODO fix constants here
            Process procConvert = run.exec("sox -r 44100 -e unsigned -b 8 -c 1 "
                    + tempRaw.getAbsolutePath() + " "
                    + output.getAbsolutePath());
            procConvert.waitFor();
            tempRaw.delete();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getScheme() {
        return null;
    }
}
