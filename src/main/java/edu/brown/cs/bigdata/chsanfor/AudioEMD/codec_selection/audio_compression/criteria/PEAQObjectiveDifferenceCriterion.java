package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Clayton on 4/5/18.
 */
public class PEAQObjectiveDifferenceCriterion extends WavDivergenceCriterion {
    private static String NAME = "PEAQ_OBJECTIVE_DIFFERENCE";
    private Runtime runtime = Runtime.getRuntime();

    public static String getName() {
        return NAME;
    }

    @Override
    public double apply(FunctionOutput fx) {
        return ((EncodingFunctionOutput) fx).getCriterion(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public double computeCriterion(AudioSequence originalSeq, AudioSequence decompressedSeq) {
        try {
            Process proc = runtime.exec(
                    "./comparison_algs/gstpeaq/src/peaq --basic" +
                            " --gst-plugin-path=./comparison_algs/gstpeaq/src/.libs " +
                            decompressedSeq.getAudioFile().getAbsolutePath() + " " +
                            originalSeq.getAudioFile().getAbsolutePath());
            proc.waitFor();
            InputStream outputText = proc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(outputText));
            String firstLine = br.readLine();
            String objectiveDifferenceGradeStr = firstLine.split("\\s+")[3];
            double objectiveDifferenceGrade = Double.valueOf(objectiveDifferenceGradeStr);
            //System.out.println(Math.min(Math.max(objectiveDifferenceGrade / -4, 0), 1));
            return Math.min(Math.max(objectiveDifferenceGrade / -4, 0), 1);
        } catch (InterruptedException | IOException | NumberFormatException e) {
            e.printStackTrace();
            return 1;
        }

    }
}
