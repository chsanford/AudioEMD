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
	
	/**
	 * field to store the convertedFileExtension
	 */
    private String convertedFileExtension;
    
    /**
     * field to store the arguments needed to convert from wav
     */
    private String convertArguments;
    
    /**
     * field to store the command line to run the denoiser
     */
    private String runCommandLine;
    
    /**
     * field to store the arguments needed to convert back to wav
     */
    private String convertBackArguments;

    /**
     *
     * @param convertedFileExtension the file extension for the audio that is fed into the denoiser: e.g. "wav", "raw"
     * @param convertArguments the arguments needed with sox to convert to the file format desired by the denoiser
     * 		  e.g. " -b 16 -c 1 -r 48k -e unsigned -t raw "
     * @param runCommandLine the command line to run the denoiser
     * 		  e.g. "./denoising_algs/rnnoise/examples/rnnoise_demo"
     * @param convertBackArguments the arguments needed with sox to convert back from to the file format
     * 		  e.g. "sox -r 48k -b 16 -c 1 -e unsigned "
     */
    CommandLineDenoisingAlgorithm(String convertedFileExtension,
    			String convertArguments, String runCommandLine, String convertBackArguments) {
        this.convertedFileExtension = convertedFileExtension;
        this.convertArguments = convertArguments;
        this.runCommandLine = runCommandLine;
        this.convertBackArguments = convertBackArguments;
    }

    /**
     * Converts a WAV file into the needed format for the denoiser
     * Should call the tryConversion method
     * @param inputFile the input WAV file; obtained from the input AudioSequence
     * @param convertedLocation the (currently empty) file for the input to the denoising algorithm
     *                          (of type convertedFileExtension)
     */
    public void convertForDenoising(File inputFile, File convertedLocation) {
        try {
            Process proc = run.exec("sox " + inputFile.getAbsolutePath()
                    + convertArguments + convertedLocation.getAbsolutePath());
            // For each command, it's necessary to wait until the procedure completes.
            // Otherwise, the next command may operate on an empty file.
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Denoises a properly-formatted file
     * Should call the tryDenoise method
     * @param fileToDenoise the file (of type convertedFileExtension) to input to the denoising algorithm
     * @param denoisedLocation the (currently empty) file for the output of the denoising algorithm
     *                          (of type convertedFileExtension)
     */
    public void denoise(File fileToDenoise, File denoisedLocation) {
        try {
            Process proc = run.exec(runCommandLine
                    + " " + fileToDenoise.getAbsolutePath()
                    + " " + denoisedLocation.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the output of the denoising algorithm back into a WAV file
     * @param denoisedOutput the file (of type convertedFileExtension) output by the denoising algorithm
     * @param finalLocation the final WAV file representing the denoised audio
     */
    public void convertBack(File denoisedOutput, File finalLocation) {
        try {
            Process proc = run.exec(convertBackArguments
                    + denoisedOutput.getAbsolutePath() + " "
                    + finalLocation.getAbsolutePath());
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

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
