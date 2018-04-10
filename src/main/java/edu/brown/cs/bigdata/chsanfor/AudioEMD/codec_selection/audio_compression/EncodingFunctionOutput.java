package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.Criterion;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.FunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;

import java.io.File;
import java.util.Map;

/**
 * Represents the metadata output of a compression algorithm
 */
public class EncodingFunctionOutput implements FunctionOutput  {
    private Map<String, Double> criteria;
    private Map<Criterion, Double> criteriaMap;

    /*public EncodingFunctionOutput(Map<String, Double> criteria) {
        this.criteria = criteria;
    }*/

    public EncodingFunctionOutput(Map<Criterion, Double> criteriaMap) {
        this.criteriaMap = criteriaMap;
    }

    public double getCriterion(String criterionName) {
        return criteria.get(criterionName);
    }

    public double getCriterion(Criterion criterion) {
        return criteriaMap.get(criterion);
    }

    @Override
    public void delete() {
        ;
    }
}
