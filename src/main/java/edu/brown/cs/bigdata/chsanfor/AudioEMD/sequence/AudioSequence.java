package edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Sample;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.wavfile.WavFile;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.wavfile.WavFileException;

import java.io.File;
import java.io.IOException;

/**
 * Represents an audio file and the sequence of numbers in [0,1] that can be obtained from it.
 */
public class AudioSequence implements Sequence, Sample, FunctionOutput {
    private File audioFile;

    /**
     * Creates an audio sequence where the audio file already exists.
     *
     * @param audioFile Location of file
     */
    public AudioSequence(File audioFile) {
        this.audioFile = audioFile;
    }

    public double[] getSequence() {
        try {
            WavFile wavFile = WavFile.openWavFile(audioFile);
            long nf = wavFile.getNumFrames();
            double[] sequence = new double[(int)nf];
            wavFile.readFrames(sequence, (int)nf);
            wavFile.close();
            return sequence;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        return new double[0];
    }

    public int getSequenceLength() {
        int len = 0;
        try {
            WavFile wavFile = WavFile.openWavFile(audioFile);
            len = (int) wavFile.getNumFrames();
            wavFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        return len;
    }

    /**
     * Obtains the L2 error between two audio time sequence arrays.
     */
    public double error(Sequence seq2) {
        return error(seq2, 2);
    }

    /**
     * Obtains the mean power error between two sequences given a power
     */
    public double error(Sequence seq2, double power) {
        double error = 0;
        double[] arr1 = getSequence();
        double[] arr2 = seq2.getSequence();
        int len1 = getSequenceLength();
        int len2 = seq2.getSequenceLength();
        for (int i = 0; i < len1 && i < len2; i++) {
            error += Math.pow(arr1[i] - arr2[i], power);
        }
        return Math.pow(error, 1. / power);
    }
    /**
     * Obtains the audio file corresponding to the sequence
     * @return the File corresponding to the sequence
     */
    public File getAudioFile() {
        return audioFile;
    }

    /**
     * Gets the name of the audio file for the sequence
     * @return a String representing the name of the audio file
     */
    public String getFileName() {
        return audioFile.getName();
    }

    @Override
    public void delete() {
        audioFile.delete();
    }
}


