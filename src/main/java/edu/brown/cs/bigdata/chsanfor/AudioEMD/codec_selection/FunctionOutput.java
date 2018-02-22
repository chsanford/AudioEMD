package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

/**
 * Output of a function... more may be listed here later.
 * e.g. decompressed audio
 */
public interface FunctionOutput {

    /**
     * Deletes the output. In the case of file outputs, this may be needed to reduce the amount of space.
     */
    void delete();
}
