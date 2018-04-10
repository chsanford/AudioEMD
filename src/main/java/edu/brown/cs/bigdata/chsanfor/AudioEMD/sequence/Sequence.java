package edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence;

/**
 * Represents something that can be expressed as a sequence of doubles.
 *
 * For example, this could be implemented by a class of audio files.
 */
public interface Sequence {

    /**
     * @return An array of doubles representing the contents of the sequence.
     */
    public double[] getSequence();

    /**
     * @return The length of the sequence specified.
     */
    public int getSequenceLength();

    /**
     * @param seq2 Another sequence of the same type and size.
     * @return An error representing the difference between the two sequences.
     */
    public double error(Sequence seq2);

}
