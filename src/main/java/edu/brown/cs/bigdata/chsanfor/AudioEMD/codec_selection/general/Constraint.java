package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

/**
 * A set of linear inequality constraints that must be satisfied with high probability for a function to be selected.
 */
public class Constraint {
    private boolean hasConstraints;
    private RealMatrix coefficientMatrix;
    private RealVector boundVector;

    /**
     *
     * @param coefficients a matrix A such that A[i][j] represents the weight for criterion j in inequality i
     * @param bound a vector b such that b[i] represents the maximum value for the weighted sum in inequality i
     */
    public Constraint(double[][] coefficients, double[] bound) {
        hasConstraints = true;
        coefficientMatrix = MatrixUtils.createRealMatrix(coefficients);
        boundVector = MatrixUtils.createRealVector(bound);
        assert boundVector.getDimension() == coefficientMatrix.getRowDimension();
    }

    /**
     * Creates a constraint model with no constraints.
     */
    public Constraint() {
        hasConstraints = false;
    }

    /**
     *
     * @param criteriaOutput a point in criteria space
     * @return true if the point satisfies the constraint
     */
    public boolean isValid(double[] criteriaOutput) {
        if (!hasConstraints) return true;
        RealVector criteriaVector = MatrixUtils.createRealVector(criteriaOutput);
        assert criteriaVector.getDimension() == coefficientMatrix.getColumnDimension();
        double[] output = coefficientMatrix.operate(criteriaOutput);

        boolean valid = true;
        for (int i = 0; i < output.length; i++) {
            valid = valid && (output[i] <= boundVector.getEntry(i));
        }
        return valid;
    }

    /**
     *
     * @param x a sample point
     * @param f a function applied to the sample
     * @param criteria a set of criteria we measure the output with
     * @return true if the point in criteria space satisfies the constraint
     */
    public boolean isValid(Sample x, Function f, List<Criterion> criteria) {
        if (!hasConstraints) return true;
        assert criteria.size() == coefficientMatrix.getColumnDimension();
        FunctionOutput fx = f.apply(x);
        double[] criteriaOutput = new double[criteria.size()];
        for (int i = 0; i < criteria.size(); i++) {
            criteriaOutput[i] = criteria.get(i).apply(fx);
        }
        fx.delete();
        return isValid(criteriaOutput);
    }

    /**
     * Determines whether the constraints are always satisfied in the criteria space enclosed by confidence intervals.
     * Because the objective is convex, if all vertices are contained, then all points are contained.
     * @param confidenceIntervalsC a list of confidence intervals for the each criteria for a given function
     * @return true if all points in the rectangular criteria space are valid
     */
    public boolean isAlwaysValidRectangle(ConfidenceInterval[] confidenceIntervalsC) {
        if (!hasConstraints) return true;
        int numConstraints = boundVector.getDimension();
        int numCriteria = coefficientMatrix.getColumnDimension();
        for (int i = 0; i < numConstraints; i++) {
            double[] greatestConstraintVertex = new double[numCriteria];
            // For each criterion, chooses the lower bound or upper bound depending on which point will have a greater
            //      value for this given criterion -- that way, if that vertex satisfies the constraint, the whole
            //      rectangle will
            for (int j = 0; j < numCriteria; j++) {
                if (coefficientMatrix.getEntry(i, j) > 0) {
                    greatestConstraintVertex[j] = confidenceIntervalsC[j].getUpperBound();
                } else {
                    greatestConstraintVertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }
            if (!isValid(greatestConstraintVertex)) return false;
        }
        return true;
        /* Deprecated code from past method that tested the validity of each vertex
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            if (!isValid(vertex)) return false;
        }

        return true;*/
    }

    /**
     * Determines whether the constraints are never satisfied in the criteria space enclosed by confidence intervals.
     * Because the objective is convex, if all vertices are not contained, then all points are not contained.
     * @param confidenceIntervalsC a list of confidence intervals for the each criteria for a given function
     * @return true if no points in the rectangular criteria space are valid
     */
    public boolean isNeverValidRectangle(ConfidenceInterval[] confidenceIntervalsC) {
        if (!hasConstraints) return false;
        int numConstraints = boundVector.getDimension();
        int numCriteria = coefficientMatrix.getColumnDimension();
        for (int i = 0; i < numConstraints; i++) {
            double[] leastConstraintVertex = new double[numCriteria];
            // For each criterion, chooses the lower bound or upper bound depending on which point will have a lesser
            //      value for this given criterion -- that way, if that vertex does not satisfy the constraint, the
            //      whole rectangle will not
            for (int j = 0; j < numCriteria; j++) {
                if (coefficientMatrix.getEntry(i, j) > 0) {
                    leastConstraintVertex[i] = confidenceIntervalsC[i].getLowerBound();
                } else {
                    leastConstraintVertex[i] = confidenceIntervalsC[i].getUpperBound();
                }
            }
            if (isValid(leastConstraintVertex)) return false;
        }
        return true;
        /* Deprecated code from past method that tested the validity of each vertex
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            if (isValid(vertex)) return false;
        }

        return true;
        */
    }
}
