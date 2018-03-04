package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.Map;

/**
 * Represents the metadata output of a compression algorithm
 */
public class EncodingFunctionOutput implements FunctionOutput  {
    private Map<String, Double> criteria;

    public EncodingFunctionOutput(Map<String, Double> criteria) {
        this.criteria = criteria;
    }

    public double getCriterion(String criterionName) {
        return criteria.get(criterionName);
    }

    @Override
    public void delete() {
        ;
    }
}
