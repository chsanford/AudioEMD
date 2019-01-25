package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;
import org.apache.commons.io.FilenameUtils;

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

        String filename = FilenameUtils.getBaseName(audioSample.getFileName());

        long thread_id = java.lang.Thread.currentThread().getId();
        File tempCompressed = new File("data/temp/temp_compressed_" + filename + "_" + thread_id + "." + extension);
        File tempDecompressed = new File("data/temp/temp_decompressed_" + filename + "_" + thread_id + ".wav");



        long startCompressionTime = System.nanoTime();
        compress(audioSample, tempCompressed);
        long endCompressionTime = System.nanoTime();
        decompress(tempCompressed, tempDecompressed);
        long endDecompressionTime = System.nanoTime();

        AudioSequence decompressed = new AudioSequence(tempDecompressed);

        Map<Criterion, Double> criteriaMap2 = new HashMap<>();

        for (Criterion c : criteria) {
            if (c instanceof RawMomentCriterion) {
                c = ((RawMomentCriterion) c).getBaseCriterion();
            }
            if (c instanceof WavDivergenceCriterion) {
                criteriaMap2.put(c, ((WavDivergenceCriterion) c).computeCriterion(audioSample, decompressed));
            }  else if (c instanceof CompressionRatioCriterion) {
                //criteriaMap2.put(c, 1.0 * tempCompressed.length() / audioSample.getAudioFile().length());
                criteriaMap2.put(c,  Math.min(1,
                        1.0 * tempCompressed.length() / audioSample.getAudioFile().length() * 75. / 32.));
            } else if (c instanceof EncodingTimeCriterion) {
                criteriaMap2.put(c, (endCompressionTime - startCompressionTime) / (10e9));
            } else if (c instanceof DecodingTimeCriterion) {
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
