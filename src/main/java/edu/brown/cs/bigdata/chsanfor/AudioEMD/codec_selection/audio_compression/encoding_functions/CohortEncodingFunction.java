package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.encoding_functions;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.EncodingFunctionOutput;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Clayton on 1/22/19.
 */
public class CohortEncodingFunction extends EncodingFunction {
    private List<Criterion> criteria;
    private List<EncodingFunction> cohort;
    Objective objective;
    Map<String, Integer> optimalSchemeIndices = new HashMap<>();

    public CohortEncodingFunction(List<EncodingFunction> cohort, List<Criterion> criteria, Objective objective) {
        super("", criteria);
        this.cohort = cohort;
        this.criteria = criteria;
        this.objective = objective;
    }

    @Override
    public FunctionOutput apply(Sample sample) {
        double optimalObjective = -1;
        EncodingFunction optimalFunction = cohort.get(0);
        for (int f = 0; f < cohort.size(); f++) {
            EncodingFunctionOutput sampleOutput = (EncodingFunctionOutput) cohort.get(0).apply(sample);
            double[] criteriaOutput = new double[criteria.size()];
            for (int c = 0; c < criteria.size(); c++) criteriaOutput[c] = sampleOutput.getCriterion(criteria.get(c));
            double newObjective = objective.compute(criteriaOutput);
            if (f == 0 || newObjective < optimalObjective) {
                optimalObjective = newObjective;
                optimalFunction = cohort.get(f);
            }
        }
        return optimalFunction.apply(sample);
    }

    @Override
    public String toString() {
        String output = "{";
        for (int f = 0; f < cohort.size(); f++) {
            if (f > 0) output += ", ";
            output += cohort.get(f).toString();
        }
        output += "}";
        return output;
    }

    @Override
    public void compress(AudioSequence input, File output) {
//        double optimalObjective = objective.compute(input, cohort.get(0), criteria);
//        optimalSchemeIndices.put(output.getPath(), 0);
//        for (int f = 1; f < cohort.size(); f++) {
//            double newObjective = objective.compute(input, cohort.get(f), criteria);
//            if (newObjective < optimalObjective) {
//                optimalObjective = newObjective;
//                optimalSchemeIndices.put(output.getPath(), f);
//            }
//        }
//        cohort.get(optimalSchemeIndices.get(output.getPath())).compress(input, output);
        int optimalEncodingFunctionIndex = getOptimalEncodingFunctionIndex(input);
        optimalSchemeIndices.put(output.getPath(), optimalEncodingFunctionIndex);
        cohort.get(optimalEncodingFunctionIndex).compress(input, output);
    }

    public int getOptimalEncodingFunctionIndex(AudioSequence input) {
        double optimalObjective = objective.compute(input, cohort.get(0), criteria);
        int optimalEncodingFunctionIndex = 0;
        for (int f = 1; f < cohort.size(); f++) {
            double newObjective = objective.compute(input, cohort.get(f), criteria);
            if (newObjective < optimalObjective) {
                optimalObjective = newObjective;
                optimalEncodingFunctionIndex = f;
            }
        }
        return optimalEncodingFunctionIndex;
    }

    public int getOptimalEncodingFunctionIndex(Double[][][] criterionValuesCFS, int s, int[] indicesF) {
        double optimalObjective = objective.compute(getFirstDimRow(criterionValuesCFS, 0, s));
        int optimalEncodingFunctionIndex = indicesF[0];
        for (int f = 1; f < cohort.size(); f++) {
            double newObjective = objective.compute(getFirstDimRow(criterionValuesCFS, indicesF[f], s));
            if (newObjective < optimalObjective) {
                optimalObjective = newObjective;
                optimalEncodingFunctionIndex = indicesF[f];
            }
        }
        return optimalEncodingFunctionIndex;
    }

    private double[] getFirstDimRow(Double[][][] tensor, int y, int z) {
        double[] output = new double[tensor.length];
        for (int x = 0; x < tensor.length; x++) output[x] = tensor[x][y][z];
        return output;
    }

    @Override
    public void decompress(File input, File output) {
        cohort.get(optimalSchemeIndices.get(input.getPath())).decompress(input, output);
    }

    @Override
    public String getScheme() {
        return null;
    }
}
