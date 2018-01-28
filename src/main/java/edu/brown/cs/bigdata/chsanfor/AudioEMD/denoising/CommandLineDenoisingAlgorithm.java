package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;


import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;

/**
 * Represents all denoising algorithms that are run on the command line. In general, these follow the format:
 *  1) Convert an imported audio sequence into a different format
 *  2) Denoise the properly-formatted sequence with a command
 *  3) Convert the denoised file back into WAV format
 */
public abstract class CommandLineDenoisingAlgorithm implements DenoisingAlgorithm {
    private String convertedFileExtension;

    /**
     *
     * @param convertedFileExtension the file extension for the audio that is fed into the denoiser: e.g. "wav", "raw"
     */
    CommandLineDenoisingAlgorithm(String convertedFileExtension) {
        this.convertedFileExtension = convertedFileExtension;
    }

    /**
     * Converts a WAV file into the needed format for the denoiser
     * @param inputFile the input WAV file; obtained from the input AudioSequence
     * @param convertedLocation the (currently empty) file for the input to the denoising algorithm
     *                          (of type convertedFileExtension)
     */
    public abstract void convertForDenoising(File inputFile, File convertedLocation);

    /**
     * Denoises a properly-formatted file
     * @param fileToDenoise the file (of type convertedFileExtension) to input to the denoising algorithm
     * @param denoisedLocation the (currently empty) file for the output of the denoising algorithm
     *                          (of type convertedFileExtension)
     */
    public abstract void denoise(File fileToDenoise, File denoisedLocation);

    /**
     * Converts the output of the denoising algorithm back into a WAV file
     * @param denoisedOutput the file (of type convertedFileExtension) output by the denoising algorithm
     * @param finalLocation the final WAV file representing the denoised audio
     */
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
