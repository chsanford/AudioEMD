package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

/**
 * Represents a confience interval with upper and lower bounds and a probability of being in the interval
 */
public class ConfidenceInterval {
    private double delta;
    private double upperBound;
    private double lowerBound;


    /**
     *
     * @param delta probability of being in the interval
     * @param upperBound upper bound on the interval
     * @param lowerBound lower bound on the interval
     */
    public ConfidenceInterval(double delta, double upperBound, double lowerBound) {
        this.delta = delta;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    public double getDelta() {
        return delta;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public String toString() {
        return "[" + lowerBound + ", " + upperBound + "]";
    }
}
