package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

public class AlgorithmSelectionOutput {
    private Function optimalFunction;
    private Double[] optimalCriteriaMeansC;
    private ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC;
    private double lowerBound;

    public AlgorithmSelectionOutput(
            Function optimalFunction,
            Double[] optimalCriteriaMeansC,
            ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC,
            double lowerBound) {
        this.optimalFunction = optimalFunction;
        this.optimalCriteriaMeansC = optimalCriteriaMeansC;
        this.optimalCriteriaConfidenceIntervalsC = optimalCriteriaConfidenceIntervalsC;
        this.lowerBound = lowerBound;
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

    public double getLowerBound() {
        return lowerBound;
    }
}
