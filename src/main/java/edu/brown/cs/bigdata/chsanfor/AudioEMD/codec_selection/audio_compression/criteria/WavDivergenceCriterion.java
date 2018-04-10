package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.util.List;

/**
 * Created by Clayton on 4/5/18.
 */
public abstract class WavDivergenceCriterion extends Criterion {

    public abstract double computeCriterion(AudioSequence originalSeq, AudioSequence decompressedSeq);
}
