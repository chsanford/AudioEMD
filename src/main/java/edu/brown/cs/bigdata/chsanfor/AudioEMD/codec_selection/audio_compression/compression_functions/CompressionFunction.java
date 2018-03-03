package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.CompressionFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionRatioCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.CompressionTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.DecompressionTimeCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.ErrorCriterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import javax.print.attribute.standard.Compression;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a function that compresses and decompresses audio files and obtains metadata about the transformation.
 * Neither the compressed nor the decompressed file are stored after applying the function.
 */
public abstract class CompressionFunction extends Function {
    private String extension;

    public CompressionFunction(String extension) {
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
        criteria.put(ErrorCriterion.getName(),
                decompressed.error(audioSample) / (audioSample.getSequenceLength()));
        criteria.put(CompressionRatioCriterion.getName(),
                1.0 * tempCompressed.length() / audioSample.getAudioFile().length());
        criteria.put(CompressionTimeCriterion.getName(),
                1.0 * (endCompressionTime - startCompressionTime) / (10e9));
        criteria.put(DecompressionTimeCriterion.getName(),
                1.0 * (endDecompressionTime - endCompressionTime) / (10e9));

        tempCompressed.delete();
        tempDecompressed.delete();

        return new CompressionFunctionOutput(criteria);
    }

    public abstract void compress(AudioSequence input, File output);

    public abstract void decompress(File input, File output);

    public abstract String getScheme();

}
