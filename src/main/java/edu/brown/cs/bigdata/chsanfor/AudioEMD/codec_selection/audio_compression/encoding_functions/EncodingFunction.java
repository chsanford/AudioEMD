package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionRatioCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.EncodingTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.DecodingTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.MeanSquaredErrorCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a function that compresses and decompresses audio files and obtains metadata about the transformation.
 * Neither the compressed nor the decompressed file are stored after applying the function.
 */
public abstract class EncodingFunction extends Function {
    private String extension;

    public EncodingFunction(String extension) {
        this.extension = extension;
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

        Map<String, Double> criteria = new HashMap<>();
        criteria.put(MeanSquaredErrorCriterion.getName(),
                decompressed.error(audioSample) / (audioSample.getSequenceLength()));
        criteria.put(CompressionRatioCriterion.getName(),
                1.0 * tempCompressed.length() / audioSample.getAudioFile().length());
        criteria.put(EncodingTimeCriterion.getName(),
                1.0 * (endCompressionTime - startCompressionTime) / (10e9));
        criteria.put(DecodingTimeCriterion.getName(),
                1.0 * (endDecompressionTime - endCompressionTime) / (10e9));

        tempCompressed.delete();
        tempDecompressed.delete();

        return new EncodingFunctionOutput(criteria);
    }

    public abstract void compress(AudioSequence input, File output);

    public abstract void decompress(File input, File output);

    public abstract String getScheme();

}
