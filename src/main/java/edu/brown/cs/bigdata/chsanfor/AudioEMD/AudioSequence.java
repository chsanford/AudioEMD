package edu.brown.cs.bigdata.chsanfor.AudioEMD;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.wavfile.WavFile;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.wavfile.WavFileException;

import java.io.File;
import java.io.IOException;

/**
 * Represents an audio file and the sequence of numbers in [0,1] that can be obtained from it.
 */
public class AudioSequence implements Sequence {
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
        double[] sequence = new double[getSequenceLength()];
        try {
            WavFile wavFile = WavFile.openWavFile(audioFile);
            wavFile.readFrames(sequence, (int) wavFile.getNumFrames());
            wavFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        return sequence;
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
        assert getSequenceLength() == seq2.getSequenceLength();
        double error = 0;
        double[] arr1 = getSequence();
        double[] arr2 = seq2.getSequence();
        for (int i = 0; i < getSequenceLength(); i++) {
            error += Math.pow(arr1[i] - arr2[i], 2);
        }
        return Math.sqrt(error);
    }

    public File getAudioFile() {
        return audioFile;
    }

    public String getFileName() {
        return audioFile.getName();
    }
}


