package edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.toy;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.audio_compression.criteria.IncorrectlyClassifiedCriterionException;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.*;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.codec_selection.general.complexity.OneShotRademacherComplexity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Clayton on 3/15/18.
 */
public class Main {

    public static void main(String[] args) {

        int numSamples = 1000;
        List<Sample> samples = new ArrayList<Sample>();


        List<Criterion> criteria = Arrays.asList(
                (Criterion) new ToyCriterion(1)
        );

        List<Function> functionClass = Arrays.asList(
                (Function) new ToyFunction(1)
        );

        Objective objective = new Objective(new double[]{-0.5, -0.5, 0, 0});

        //Constraint constraint = new Constraint();
        Constraint constraint = new Constraint(
                new double[][]{{0, 0, 1, 0}, {0, 0, 0, 1}},
                new double[]{0.5, 0.5}
        );

        GlobalSampling bf = new GlobalSampling(new OneShotRademacherComplexity());
        ProgressiveSampling ps = new ProgressiveSampling(new OneShotRademacherComplexity());


        try {
            AlgorithmSelectionOutput out = ps.runAlgorithm(
                    samples,
                    50,
                    functionClass,
                    criteria,
                    objective,
                    constraint,
                    0.05,
                    0.05,
                    false);

            System.out.println("Best algorithm: " + out.getOptimalFunction().toString());
            System.out.println("Objective upper bound: " + out.getLowerBound());
            for (int c = 0; c < criteria.size(); c++) {
                System.out.println("Criteria " + c + " (" + criteria.get(c).toString() + ") " +
                        out.getOptimalCriteriaMeansC()[c] + " in " +
                        out.getOptimalCriteriaConfidenceIntervalsC()[c].toString());
            }

        } catch (InsufficientSampleSizeException |
                NoSatisfactoryFunctionsException |
                EmptyConfidenceIntervalException |
                IncorrectlyClassifiedCriterionException e) {
            e.printStackTrace();
        }
    }
}
