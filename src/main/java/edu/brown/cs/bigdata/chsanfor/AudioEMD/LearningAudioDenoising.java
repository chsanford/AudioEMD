package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import edu.brown.cs.bigdata.chsanfor.AudioEMD.denoising.DenoisingAlgorithm;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.matching.MatchingAudio;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.matching.NoiseCleanPair;
import edu.brown.cs.bigdata.chsanfor.AudioEMD.sequence.AudioSequence;
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
    private List<NoiseCleanPair> trainingNoiseCleanPair;

    /**
     * @param trainingCleanDir directory containing the clean audio files for training
     * @param trainingNoiseDir directory containing the noise files for training
     * @param cleanClusterSize number of clean files to include in each cluster
     * @param noiseClusterSize number of noise files to include in each cluster
     * @param denoisingAlgorithms an array of denoising algorithms to apply to training samples and combine
     */
    public LearningAudioDenoising(
            File trainingCleanDir,
            File trainingNoiseDir,
            int cleanClusterSize,
            int noiseClusterSize,
            DenoisingAlgorithm[] denoisingAlgorithms) {
        this.denoisingAlgorithms = denoisingAlgorithms;
        File[] cleanFiles = trainingCleanDir.listFiles();
        File[] noiseFiles = trainingNoiseDir.listFiles();

        // Converts files to AudioSequence format
        AudioSequence[] trainingClean = new AudioSequence[cleanFiles.length];
        for (int i = 0; i <= cleanFiles.length; i++) {
            trainingClean[i] = new AudioSequence(cleanFiles[i]);
        }
        AudioSequence[] trainingNoise = new AudioSequence[noiseFiles.length];
        for (int i = 0; i <= noiseFiles.length; i++) {
            trainingNoise[i] = new AudioSequence(noiseFiles[i]);
        }

        // Creates pairs of noise and clean files in cluster to be used as training data
        trainingNoiseCleanPair = MatchingAudio.MatchAudio(
                trainingClean, trainingNoise, cleanClusterSize, noiseClusterSize,
                new File("temp/temp_file.wav"));

        optimizeWeights();

    }

    /**
     * Finds the best set of weights of denoising algorithms such that a weighted combination of the algorithms
     * minimizes L2 error
     * @return an array representing non-negative weights for each algorithm; sums to 1
     */
    private double[] optimizeWeights() {
        // matrix and vector to be used in quadratic program: min_w: w^T * Q * w + W^T * L
        // (explained in info/denoising_error_formulation.pdf)
        RealMatrix QMatrix = MatrixUtils.createRealMatrix(denoisingAlgorithms.length, denoisingAlgorithms.length);
        RealVector LVector = MatrixUtils.createRealVector(new double[denoisingAlgorithms.length]);

        for (NoiseCleanPair trainingNoisyCleanPair : trainingNoiseCleanPair) {
            AudioSequence trainingClean = trainingNoisyCleanPair.getCleanSeq();
            AudioSequence trainingNoisy = trainingNoisyCleanPair.getNoisySeq();

            double[] cleanSeq = trainingClean.getSequence();
            int sequenceLength = trainingClean.getSequenceLength();
            AudioSequence[] denoised = new AudioSequence[denoisingAlgorithms.length];
            double[][] denoisedSeq = new double[denoisingAlgorithms.length][];

            // For each training combo, denoises the noisy file with each denoising algorithm
            for (int j = 0; j < denoised.length; j++) {
                denoised[j] = denoisingAlgorithms[j].apply(
                        trainingNoisy, new File("temp/temp_denoised" + j + ".wav"));
                denoisedSeq[j] = denoised[j].getSequence();
            }

            // Fills in Q and L based on the denoised sequences
            for (int l = 0; l < sequenceLength; l++) {
                double[] denoisedElements = new double[denoisingAlgorithms.length];
                for (int j = 0; j < denoised.length; j++) {
                    denoisedElements[j] = denoisedSeq[j][l];
                }
                RealVector denoisedValues = MatrixUtils.createRealVector(denoisedElements);
                QMatrix = QMatrix.add(denoisedValues.outerProduct(denoisedValues).scalarMultiply(2.0 / sequenceLength));
                LVector = LVector.add(denoisedValues.mapMultiply(-2 * cleanSeq[l] / sequenceLength));
            }

            // Deletes all denoised files, to conserve memory
            for (AudioSequence seq : denoised) {
                seq.getAudioFile().delete();
            }

            trainingNoisyCleanPair.removeNoisyFile();
        }

        try {
            // uses CPLEX to solve a quadratic program to minimize the error of a weighted combination of
            // denoising algorithms
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

    /**
     * Merges two audio sequences to create a file that overlays them
     * @param seq1 the first audio sequence to overlay
     * @param seq2 the second audio sequence to overlay
     * @param outFile the location of merged file (empty as of now)
     * @return an AudioSequence corresponding to the merged file in outFile
     * TODO: Figure out a way to merge more than 2 audio sequences proportionally
     */
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
