package edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising;

import java.io.File;
import java.io.IOException;

/**
 * Represents the NNog Denoising algorithm (https://github.com/nnog/denoise) approach to denoising audio.
 * Takes wav files as input.
 */
public class NoNoiseDenoisingAlgorithm extends CommandLineDenoisingAlgorithm {

    private Runtime run = Runtime.getRuntime();

    /**
     * arguments - in:
     * -b 16 : BITS, the number of bits in each encoded sample; for output file, should be set to input encoding size
     *  - REMOVED
     * -c 1 : CHANNELS, the number of audio channels in the audio file; only supported with some audio types
     * -r 48k  : gives sample rate in Hz (or kHz if appended with 'k') of the file. usually used for headerless raw files
     * 			 sometimes is 44.1k for wav files. Is there a way to find this?
     *  - REMOVED
     * -e unsigned : ENCODING, sometimes needed with file-types that support more than one encoding type. yes, needed
     * 	- REMOVED
     * t wav : gives filetype, usually used to inform sox of headerless filetypes
     * 
     * arguments - out: 
     * -r 48k  : gives sample rate in Hz (or kHz if appended with 'k') of the file. usually used for headerless raw files
     * -b 16 : BITS, the number of bits in each encoded sample; for output file, should be set to input encoding size
     * -c 1 : CHANNELS, the number of audio channels in the audio file
     * -e unsigned (keep) : ENCODING, sometimes needed with file-types that support more than one encoding type. yes, needed
     * 
     * Questions:
     * what file types are we taking in?
     * - -b 16, -r 48k seem to depend on file types coming in - e.g. wavs can have 44.1 kHz
     * need to install LAME
     */
    public NoNoiseDenoisingAlgorithm() {
        super("mp3", " -c 1 -t mp3 ", "./denoising_algs/noNoise/noNoise.sh", 
        		"sox -r 48k -b 16 -c 1 ");
    }

}
