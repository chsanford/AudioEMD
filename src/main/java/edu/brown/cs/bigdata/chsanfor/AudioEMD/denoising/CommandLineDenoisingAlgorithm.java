package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.AudioSequence;

import java.io.File;

/**
 * Created by Clayton on 1/27/18.
 */
public abstract class CommandLineDenoisingAlgorithm implements DenoisingAlgorithm {
    private String convertedFileExtension;

    public CommandLineDenoisingAlgorithm(String convertedFileExtension) {
        this.convertedFileExtension = convertedFileExtension;
    }

    public abstract void convertForDenoising(File inputFile, File convertedLocation);

    public abstract void denoise(File fileToDenoise, File denoisedLocation);

    public abstract void convertBack(File denoisedOutput, File finalLocation);

    public AudioSequence apply(AudioSequence inputSequence, File outputLocation) {
        File tempInputFile = new File("temp/temp_input." + convertedFileExtension);
        File tempOutputFile = new File("temp/temp_output." + convertedFileExtension);

        convertForDenoising(inputSequence.getAudioFile(), tempInputFile);
        denoise(tempInputFile, tempOutputFile);
        convertBack(tempOutputFile, outputLocation);

        tempInputFile.delete();
        tempOutputFile.delete();

        return new AudioSequence(outputLocation);
    }

}
