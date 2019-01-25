package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

/**
 * A linear combination of criterion values that we aim to maximize in codex selection
 */
public class Objective {
    private RealVector weights;

    /**
     *
     * @param weights the weight assigned to each criterion in the linear combination
     */
    public Objective(double[] weights) {
        this.weights = MatrixUtils.createRealVector(weights);
    }

    /**
     * Computes the objective value
     * @param criteriaOutput doubles representing the value of each criterion
     * @return the correct linear combination of criteriaOutput
     */
    public double compute(double[] criteriaOutput) {
        RealVector criteriaVector = MatrixUtils.createRealVector(criteriaOutput);
        assert criteriaVector.getDimension() == weights.getDimension();
        return criteriaVector.dotProduct(weights);
    }

    /**
     * Computes the objective value
     * @param x a sample
     * @param f a function we apply to the sample
     * @param criteria a list of criteria functions applied to the output of the function
     * @return the correct linear combination of the criteria's output
     */
    public double compute(Sample x, Function f, List<Criterion> criteria) {
        assert criteria.size() == weights.getDimension();
        FunctionOutput fx = f.apply(x);
        double[] criteriaOutput = new double[criteria.size()];
        for (int i = 0; i < criteria.size(); i++) {
            criteriaOutput[i] = criteria.get(i).apply(fx);
        }
        fx.delete();
        return compute(criteriaOutput);
    }

    /**
     * Find the maximum objective value in the criteria-space bounded by confidence intervals.
     * Because the objective is convex, the maximum point must lie on one of the vertices.
     * @param confidenceIntervalsC a list of confidence intervals for the each criteria for a given function
     * @return the maximum objective
     */
    public double maxRectangle(ConfidenceInterval[] confidenceIntervalsC) {
        int numCriteria = weights.getDimension();
        double[] greatestObjectiveVertex = new double[numCriteria];
        // For each criterion, chooses the lower bound or upper bound depending on which point will have a greater
        //      value for this given criterion -- because of the linearity of the objective, that vertex must have the
        //      maximum value
        for (int j = 0; j < numCriteria; j++) {
            if (weights.getEntry(j) > 0) {
                greatestObjectiveVertex[j] = confidenceIntervalsC[j].getUpperBound();
            } else {
                greatestObjectiveVertex[j] = confidenceIntervalsC[j].getLowerBound();
            }
        }
        return compute(greatestObjectiveVertex);

        /* Deprecated code with old method
        Double maxObjective = null;

        // Iterates through every combination of upper/lower bounds to check all vertices
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            double objective = compute(vertex);
            if (maxObjective == null || objective > maxObjective) {
                maxObjective = objective;
            }
        }

        return maxObjective;*/
    }

    /**
     * Find the minimum objective value in the criteria-space bounded by confidence intervals.
     * Because the objective is convex, the minimum point must lie on one of the vertices.
     * @param confidenceIntervalsC a list of confidence intervals for the each criteria for a given function
     * @return the minimum objective
     */
    public double minRectangle(ConfidenceInterval[] confidenceIntervalsC) {
        int numCriteria = weights.getDimension();
        double[] leastObjectiveVertex = new double[numCriteria];
        // For each criterion, chooses the lower bound or upper bound depending on which point will have a lower
        //      value for this given criterion -- because of the linearity of the objective, that vertex must have the
        //      minimum value
        for (int c = 0; c < numCriteria; c++) {
            if (weights.getEntry(c) > 0) {
                leastObjectiveVertex[c] = confidenceIntervalsC[c].getLowerBound();
            } else {
                leastObjectiveVertex[c] = confidenceIntervalsC[c].getUpperBound();
            }
        }
        return compute(leastObjectiveVertex);

        /* Deprecated code with old method

        Double minObjective = null;

        // Iterates through every combination of upper/lower bounds to check all vertices
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            double objective = compute(vertex);
            if (minObjective == null || objective < minObjective) {
                minObjective = objective;
            }
        }

        return minObjective;*/
    }

}
