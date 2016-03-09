
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Muhammad
 */
public class NewClass {
    public double meanSquaredError(ArrayList<Double> results) {
        double mserror = 0;
        double count = 0;
        for (double result : results) {
            mserror += result;
            count++;
        }
        count *= classLabels.length;
        mserror /= count;
        return mserror;
    }

    private double evaluateDataSet(ArrayList<DigitCategoriser.DataVector> subDataSets, int actualOutput) {
        int[] desiredOutputs = new int[subDataSets.size()];
        int[] actualOutputs = new int[subDataSets.size()];

        double meanSquaredError = 0;
        int i = 0;
        for (DigitCategoriser.DataVector trainingData : subDataSets) {
            desiredOutputs[i] = trainingData.output;
            actualOutputs[i] = actualOutput;
            meanSquaredError = calculatePatternError(actualOutputs, desiredOutputs);
            i++;
        }

        return meanSquaredError;
    }

    public double calculatePatternError(int[] predictedOutput, int[] targetOutput) {
        double totalError = 0;
        int patternCount = 0;
        double[] patternError = new double[targetOutput.length];
        for (int predictIndex = 0; predictIndex < predictedOutput.length; predictIndex++) {
            patternError[predictIndex] = targetOutput[predictIndex] - predictedOutput[predictIndex];
            totalError += Math.pow(patternError[predictIndex], 2);
        }

        patternCount++;
        double totalErrors = totalError / (2 * patternCount);
        return totalErrors;
    }
    
        //performs 2-fold test
//    private double crossValidate(ArrayList<DataVector> dataSets, int kFold) {
//        double error = 0;
//
//        //Subsampling part here
//        Collections.shuffle(dataSets);
//        List<ArrayList<DataVector>> subSets = new ArrayList<>();
//        ArrayList<Double> results = new ArrayList<>();
//        ArrayList<DataVector> newSubSet = new ArrayList<>();
//        Random randomGenerator = new Random();
//        int randomIndex;
//        for (int i = 0; i < kFold / dataSets.size(); i++) {
//            randomIndex = randomGenerator.nextInt(dataSets.size());
//            DataVector randomData = dataSets.get(randomIndex);
//            newSubSet.add(randomData);
//            subSets.add(newSubSet);
//        }
//
//        int i = 0;
//        for (ArrayList<DataVector> subSet : subSets) {
//            subSet.get(i).output = dataSets.get(i).output;
//            int outputTestResult = 0;
//            for (DataVector vectors : subSet) {
//                outputTestResult = findNearestNeigbours(subSet, vectors.inputs, randomKNumber());
//                //testing
//            }
//            for (ArrayList<DataVector> subSet1 : subSets) {
//                double result = evaluateDataSet(subSet1, outputTestResult);
//                results.add(result);
//            }
//            i++;
//        }
//        error = meanSquaredError(results);
//
//        return error;
//    }
}
