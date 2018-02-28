package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

public class BruteForceOutput {
    private Function optimalFunction;
    private double[] optimalCriteriaMeansC;
    private ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC;
    private double upperBound;

    public BruteForceOutput(Function optimalFunction, double[] optimalCriteriaMeansC, ConfidenceInterval[] optimalCriteriaConfidenceIntervalsC, double upperBound) {
        this.optimalFunction = optimalFunction;
        this.optimalCriteriaMeansC = optimalCriteriaMeansC;
        this.optimalCriteriaConfidenceIntervalsC = optimalCriteriaConfidenceIntervalsC;
        this.upperBound = upperBound;
    }

    public Function getOptimalFunction() {
        return optimalFunction;
    }

    public double[] getOptimalCriteriaMeansC() {
        return optimalCriteriaMeansC;
    }

    public ConfidenceInterval[] getOptimalCriteriaConfidenceIntervalsC() {
        return optimalCriteriaConfidenceIntervalsC;
    }

    public double getUpperBound() {
        return upperBound;
    }
}
