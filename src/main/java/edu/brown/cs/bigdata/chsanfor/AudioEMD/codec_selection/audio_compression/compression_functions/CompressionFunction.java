package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.compression_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Function;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import javax.sound.sampled.AudioSystem;
import java.io.File;

/**
 * Created by Clayton on 2/27/18.
 */
public abstract class CompressionFunction extends Function {
    private String extension;

    public CompressionFunction(String extension) {
        this.extension = extension;
    }

    @Override
    public FunctionOutput apply(Sample sample) {
        String fileName = ((AudioSequence) sample).getFileName();
        File tempCompressed = new File("data/temp/temp_compressed." + extension);
        File tempDecompressed = new File("data/temp/temp_decompressed_" + fileName + "_" + extension +  ".wav");

        compress((AudioSequence) sample, tempCompressed);
        decompress(tempCompressed, tempDecompressed);
        AudioSequence decompressed = new AudioSequence(tempDecompressed);

        tempCompressed.delete();
        //tempDecompressed.delete();

        return decompressed;
    }

    public abstract void compress(AudioSequence input, File output);

    public abstract void decompress(File input, File output);

    public abstract String getScheme();

}
