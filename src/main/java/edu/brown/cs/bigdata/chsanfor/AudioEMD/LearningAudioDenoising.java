package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.DenoisingAlgorithm;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.MatrixUtils;

import ilog.cplex.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contains optimization algorithm to use quadratic programming to find optimal weighting of denoising algorithms.
 */
public class LearningAudioDenoising {
    private DenoisingAlgorithm[] denoisingAlgorithms;
    private List<NoiseCleanPair> trainingNoisyCleanPair;

    public LearningAudioDenoising(
            File trainingCleanDir,
            File trainingNoiseDir,
            File workspace,
            int cleanClusterSize,
            int noiseClusterSize,
            DenoisingAlgorithm[] denoisingAlgorithms) {
        this.denoisingAlgorithms = denoisingAlgorithms;
        File[] cleanFiles = trainingCleanDir.listFiles();
        File[] noiseFiles = trainingNoiseDir.listFiles();

        AudioSequence[] trainingClean = new AudioSequence[cleanFiles.length];
        for (int i = 0; i <= cleanFiles.length; i++) {
            trainingClean[i] = new AudioSequence(cleanFiles[i]);
        }
        AudioSequence[] trainingNoise = new AudioSequence[noiseFiles.length];
        for (int i = 0; i <= noiseFiles.length; i++) {
            trainingNoise[i] = new AudioSequence(noiseFiles[i]);
        }

        trainingNoisyCleanPair = MatchingAudio.MatchAudio(
                trainingClean, trainingNoise, cleanClusterSize, noiseClusterSize, workspace);

        optimizeWeights();

    }

    private double[] optimizeWeights() {
        RealMatrix QMatrix = MatrixUtils.createRealMatrix(denoisingAlgorithms.length, denoisingAlgorithms.length);
        RealVector LVector = MatrixUtils.createRealVector(new double[denoisingAlgorithms.length]);
        for (NoiseCleanPair trainingNoisyCleanPair : trainingNoisyCleanPair) {
            AudioSequence trainingClean = trainingNoisyCleanPair.getCleanSeq();
            AudioSequence trainingNoisy = trainingNoisyCleanPair.getNoisySeq();

            double[] cleanSeq = trainingClean.getSequence();
            int sequenceLength = trainingClean.getSequenceLength();
            AudioSequence[] denoised = new AudioSequence[denoisingAlgorithms.length];
            double[][] denoisedSeq = new double[denoisingAlgorithms.length][];
            for (int j = 0; j < denoised.length; j++) {
                denoised[j] = denoisingAlgorithms[j].apply(
                        trainingNoisy, new File("temp/temp_denoised" + j + ".wav"));
                denoisedSeq[j] = denoised[j].getSequence();
            }

            for (int l = 0; l < sequenceLength; l++) {
                double[] denoisedElements = new double[denoisingAlgorithms.length];
                for (int j = 0; j < denoised.length; j++) {
                    denoisedElements[j] = denoisedSeq[j][l];
                }
                RealVector denoisedValues = MatrixUtils.createRealVector(denoisedElements);
                QMatrix = QMatrix.add(denoisedValues.outerProduct(denoisedValues).scalarMultiply(2.0 / sequenceLength));
                LVector = LVector.add(denoisedValues.mapMultiply(-2 * cleanSeq[l] / sequenceLength));
            }

            for (AudioSequence seq : denoised) {
                seq.getAudioFile().delete();
            }

            trainingNoisyCleanPair.removeNoisyFile();
        }

        try {
            IloCplex cplex = new IloCplex();
            // Constraints: Weights for each denoising algorithm must be in [0,1] and sum to 1
            IloNumVar[] weights = cplex.numVarArray(denoisingAlgorithms.length, 0, 1);
            cplex.addEq(cplex.sum(weights), 1);

            // Objective:
            IloNumExpr[] quadraticTerms = new IloNumExpr[denoisingAlgorithms.length * denoisingAlgorithms.length];
            for (int j = 0; j < denoisingAlgorithms.length; j++) {
                for (int k = 0; k < denoisingAlgorithms.length; k++) {
                    quadraticTerms[j * denoisingAlgorithms.length + k] =
                            cplex.prod(weights[j], weights[k], QMatrix.getEntry(j, k));
                }
            }
            cplex.addMinimize(cplex.sum(
                    cplex.scalProd(weights, LVector.toArray()),
                    cplex.sum(quadraticTerms)));

            if (cplex.solve()) {
                return cplex.getValues(weights);
            } else {
                return null;
            }

        } catch (IloException e) {
            e.printStackTrace();
            return null;
        }


    }

    public static AudioSequence mergeAudioSequences(AudioSequence seq1, AudioSequence seq2, File outFile) {
        Runtime run = Runtime.getRuntime();
        try {
            run.exec("sox -m " +
                    seq1.getAudioFile().toString() + " " +
                    seq2.getAudioFile().toString() + " " +
                    outFile.getCanonicalPath());
            return new AudioSequence(outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
