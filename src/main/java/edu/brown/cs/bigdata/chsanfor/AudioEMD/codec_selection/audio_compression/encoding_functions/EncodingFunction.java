package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a function that compresses and decompresses audio files and obtains metadata about the transformation.
 * Neither the compressed nor the decompressed file are stored after applying the function.
 */
public abstract class EncodingFunction extends Function {
    private String extension;
    private List<Criterion> criteria;

    public EncodingFunction(String extension, List<Criterion> criteria) {
        this.extension = extension;
        this.criteria = criteria;
    }

    @Override
    public FunctionOutput apply(Sample sample) {
        AudioSequence audioSample = (AudioSequence) sample;

        File tempCompressed = new File("data/temp/temp_compressed." + extension);
        File tempDecompressed = new File("data/temp/temp_decompressed.wav");


        long startCompressionTime = System.nanoTime();
        compress(audioSample, tempCompressed);
        long endCompressionTime = System.nanoTime();
        decompress(tempCompressed, tempDecompressed);
        long endDecompressionTime = System.nanoTime();

        AudioSequence decompressed = new AudioSequence(tempDecompressed);

        Map<Criterion, Double> criteriaMap2 = new HashMap<>();

        /*Map<String, Double> criteriaMap = new HashMap<>();

        criteriaMap.put(RootMeanSquaredErrorCriterion.getName(),
                Math.sqrt(decompressed.error(audioSample) / (audioSample.getSequenceLength())));
        criteriaMap.put(CompressionRatioCriterion.getName(),
                1.0 * tempCompressed.length() / audioSample.getAudioFile().length());
        criteriaMap.put(EncodingTimeCriterion.getName(),
                1.0 * (endCompressionTime - startCompressionTime) / (10e9));
        criteriaMap.put(DecodingTimeCriterion.getName(),
                1.0 * (endDecompressionTime - endCompressionTime) / (10e9));*/

        for (Criterion c : criteria) {
            if (c.toString() == RootMeanSquaredErrorCriterion.getName()) {
                criteriaMap2.put(c, decompressed.error(audioSample) / (audioSample.getSequenceLength()));
            } else if (c.toString() == MeanPowerErrorCriterion.getName()) {
                double power = ((MeanPowerErrorCriterion) c).getPower();
                criteriaMap2.put(c, decompressed.error(audioSample, power) / (audioSample.getSequenceLength()));
            } else if (c.toString() == CompressionRatioCriterion.getName()) {
                criteriaMap2.put(c, 1.0 * tempCompressed.length() / audioSample.getAudioFile().length());
            } else if (c.toString() == EncodingTimeCriterion.getName()) {
                criteriaMap2.put(c, (endCompressionTime - startCompressionTime) / (10e9));
            } else if (c.toString() == DecodingTimeCriterion.getName()) {
                criteriaMap2.put(c, (endDecompressionTime - endCompressionTime) / (10e9));
            }
        }



        tempCompressed.delete();
        tempDecompressed.delete();

        //return new EncodingFunctionOutput(criteriaMap);
        return new EncodingFunctionOutput(criteriaMap2);
    }

    public abstract void compress(AudioSequence input, File output);

    public abstract void decompress(File input, File output);

    public abstract String getScheme();

}
