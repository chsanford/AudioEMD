package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.*;
import java.util.List;

/**
 * Created by Clayton on 3/14/18.
 */
public class LameMP3EncodingFunction extends EncodingFunction {
    private static String EXTENSION = "mp3";
    private static int MAX_PORTABILITY = 9;
    private static int MIN_PORTABILITY = 0;

    private int portability;

    private Runtime run = Runtime.getRuntime();

    /**
     *
     * @param portability integer in {0, ..., 9} where 0 is highest quality and 9 is most portable
     * @param criteria
     */
    public LameMP3EncodingFunction(int portability, List<Criterion> criteria) {
        super(EXTENSION, criteria);
        assert portability <= MAX_PORTABILITY && portability >= MIN_PORTABILITY;
        this.portability = portability;
    }

    @Override
    public void compress(AudioSequence input, File output) {
        try {
            Process proc = run.exec("lame "
                    + input.getAudioFile().getAbsolutePath() + " "
                    + output.getAbsolutePath() + " -V" + portability);
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
                    + output.getAbsolutePath() + " -V" + portability + " --decode");
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
        return "Lame MP3 Compression V" + portability;
    }
}
