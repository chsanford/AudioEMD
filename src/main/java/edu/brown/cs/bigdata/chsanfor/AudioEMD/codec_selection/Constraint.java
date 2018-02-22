package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

/**
 * A set of linear inequality constraints that must be satisfied with high probability for a function to be selected.
 */
public class Constraint {
    private RealMatrix coefficientMatrix;
    private RealVector boundVector;

    /**
     *
     * @param coefficients a matrix A such that A[i][j] represents the weight for criterion j in inequality i
     * @param bound a vector b such that b[i] represents the maximum value for the weighted sum in inequality i
     */
    public Constraint(double[][] coefficients, double[] bound) {
        coefficientMatrix = MatrixUtils.createRealMatrix(coefficients);
        boundVector = MatrixUtils.createRealVector(bound);
        assert boundVector.getDimension() == coefficientMatrix.getRowDimension();
    }

    /**
     *
     * @param criteriaOutput a point in criteria space
     * @return true if the point satisfies the constraint
     */
    public boolean isValid(double[] criteriaOutput) {
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
        assert criteria.size() == coefficientMatrix.getColumnDimension();
        FunctionOutput fx = f.apply(x);
        double[] criteriaOutput = new double[criteria.size()];
        for (int i = 0; i < criteria.size(); i++) {
            criteriaOutput[i] = criteria.get(i).apply(x, f, fx);
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
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getUpperBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            if (!isValid(vertex)) return false;
        }

        return true;
    }

    /**
     * Determines whether the constraints are never satisfied in the criteria space enclosed by confidence intervals.
     * Because the objective is convex, if all vertices are not contained, then all points are not contained.
     * @param confidenceIntervalsC a list of confidence intervals for the each criteria for a given function
     * @return true if no points in the rectangular criteria space are valid
     */
    public boolean isNeverValidRectangle(ConfidenceInterval[] confidenceIntervalsC) {
        for (int i = 0; i < Math.pow(2, confidenceIntervalsC.length); i++) {
            double[] vertex = new double[confidenceIntervalsC.length];
            for (int j = 0; i < confidenceIntervalsC.length; j++) {
                if ((i / Math.pow(2, j) % 2 == 0)) {
                    vertex[j] = confidenceIntervalsC[j].getUpperBound();
                }  else {
                    vertex[j] = confidenceIntervalsC[j].getLowerBound();
                }
            }

            if (isValid(vertex)) return false;
        }

        return true;
    }
}
