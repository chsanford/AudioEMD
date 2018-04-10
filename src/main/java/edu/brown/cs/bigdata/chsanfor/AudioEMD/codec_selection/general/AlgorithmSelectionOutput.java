package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

public class AlgorithmSelectionOutput {
    private Function optimalFunction;
    private Double[] optimalCriteriaMeansC;
    private ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC;
    private double upperBound;

    public AlgorithmSelectionOutput(Function optimalFunction, Double[] optimalCriteriaMeansC, ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC, double upperBound) {
        this.optimalFunction = optimalFunction;
        this.optimalCriteriaMeansC = optimalCriteriaMeansC;
        this.optimalCriteriaConfidenceIntervalsC = optimalCriteriaConfidenceIntervalsC;
        this.upperBound = upperBound;
    }

    public Function getOptimalFunction() {
        return optimalFunction;
    }

    public Double[] getOptimalCriteriaMeansC() {
        return optimalCriteriaMeansC;
    }

    public ConfidenceInterval[] getOptimalCriteriaConfidenceIntervalsC() {
        return optimalCriteriaConfidenceIntervalsC;
    }

    public double getUpperBound() {
        return upperBound;
    }
}
