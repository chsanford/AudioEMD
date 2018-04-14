package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Clayton on 3/14/18.
 */
public class LameConstantMP3EncodingFunction extends EncodingFunction {
    private static String EXTENSION = "mp3";
    private static List<Integer> VALID_BITRATES =
            Arrays.asList(8, 16, 24, 32, 40, 48, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320);

    private int bitrate;

    private Runtime run = Runtime.getRuntime();

    /**
     *
     * @param bitrate
     * @param criteria
     */
    public LameConstantMP3EncodingFunction(int bitrate, List<Criterion> criteria) {
        super(EXTENSION, criteria);
        assert VALID_BITRATES.contains(bitrate);
        this.bitrate = bitrate;
    }

    @Override
    public void compress(AudioSequence input, File output) {
        try {
            Process proc = run.exec("lame "
                    + input.getAudioFile().getAbsolutePath() + " "
                    + output.getAbsolutePath() + " -b " + bitrate);
            // For each command, it's necessary to wait until the procedure completes.
            // Otherwise, the next command may operate on an empty file.
            proc.waitFor();


            /*InputStream outputText = proc.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(outputText));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }*/


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decompress(File input, File output) {
        try {
            //Process proc = run.exec("sox "
            //        + input.getAbsolutePath() + " "
            //        + output.getAbsolutePath());
            Process proc = run.exec("lame "
                    + input.getAbsolutePath() + " "
                    + output.getAbsolutePath() + " -b " + bitrate + " --decode");
            proc.waitFor();

            /*InputStream outputText = proc.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(outputText));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }*/

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
        return "Lame Constant MP3 Compression Bitrate " + bitrate;
    }
}
