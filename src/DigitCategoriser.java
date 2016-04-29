
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.System.in;
import static java.lang.System.nanoTime;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/*
 This is Digit Categoriser program that takes inputs from the Optical Recognition of Handwritten Digits Data Set.
 It categorises digits using K-Nearest-Neighbor(s) Algorithm. This outputs the validation results.
 */
public class DigitCategoriser {

    //Class model for Data Vector
    static class DataSet {

        int[] inputs;
        int output;

        public DataSet(int[] inputs, int output) {
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public String toString() {
            return "Category:" + output + " Data:" + Arrays.toString(inputs) + "\n";
        }

    }

    //Class model to compare two results for sorting the distances
    private static class DistanceComparator implements Comparator<Result> {

        @Override
        public int compare(Result o1, Result o2) {
            if (o1.distance < o2.distance) {
                return -1;
            } else if (o1.distance == o2.distance) {
                return 0;
            } else {
                return 1;
            }
        }

    }

    //Class model for result
    static class Result {

        double distance;
        int targetOutput;

        //Constructor that takes two parameters, one is double variable for distance and integer variable for the label
        public Result(double distance, int targetOutput) {
            this.distance = distance;
            this.targetOutput = targetOutput;
        }

        @Override
        public String toString() {
            return "Result{" + "distance=" + distance + ", label=" + targetOutput + '}';
        }

    }

    //creates the data set from file that takes a String parameter and returns an Arraylist of DataSet
    private ArrayList<DataSet> createDataSetFromFile(String csvFile) {
        ArrayList<DataSet> dataList = new ArrayList<>();
        int[][] dataSetArray = new int[2810][65];
        try {
            int instances = 0;
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(csvFile)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] dataFromCSV = line.split(",");

                //takes the input attributes
                int[] input = new int[64];

                for (int i = 0; i < dataFromCSV.length - 1; i++) {
                    if (i < input.length) {
                        dataSetArray[instances][i] = parseInt(dataFromCSV[i]); //parse data    
                        input[i] = dataSetArray[instances][i];
                    }
                }

                //takes the class label
                int label = parseInt(dataFromCSV[64]);
                //dataList.add(new DataSet(dataSetArray[instances], dataSetArray[instances][64]));
                dataList.add(new DataSet(input, label));
                instances++;
            }
        } catch (IOException | NumberFormatException ex) {
        }
        return dataList;
    }

    //It performs 2-fold cross validation returning a accuracy result
    private void crossValidate(ArrayList<DataSet> trainingSet, ArrayList<DataSet> testingSet, int kFold) {

        int counter = 0;
        for (int k = 1; k <= 10; k++) {
            int[] predictedOutput = new int[trainingSet.size()];
            for (int testSetIndex = 0; testSetIndex < trainingSet.size(); testSetIndex++) {
                int predicts = findNearestNeigbour(trainingSet, testingSet.get(testSetIndex), k);
                predictedOutput[testSetIndex] = predicts;
            }

            out.println("K = " + k);
            double accuracy = accuracyRate(testingSet, predictedOutput);

            out.println("Switching Folds...");
            int[] predictedOutput1 = new int[testingSet.size()];
            for (int trainSetIndex = 0; trainSetIndex < testingSet.size(); trainSetIndex++) {
                int predicts = findNearestNeigbour(testingSet, trainingSet.get(trainSetIndex), k);
                predictedOutput1[trainSetIndex] = predicts;
                //System.out.println("Target Output: " + subSet.get(testSetIndex).output + " Actual Output: " + predicts);
            }

            accuracy += accuracyRate(trainingSet, predictedOutput1);

            double average = accuracy / kFold;
            out.println("Average Accuracy: " + average);
        }
        counter++;
    }

    //calculates and returns the accuracy rate between actual output and expected output
    private double accuracyRate(ArrayList<DataSet> testSet, int[] predictions) {
        int correct = 0;
        double size = testSet.size();
        for (int i = 0; i < size; i++) {
            if (testSet.get(i).output == predictions[i]) {
                correct += 1;
            }
        }
        out.println(correct + " out of " + predictions.length + " correct classes.");
        double average = (correct / size) * 100;

        return average;
    }

    //calculates and returns the distance between two inputs of vectorA and vectorB
    private double distance(DataSet vectorA, int[] vectorB) {
        double distance = 0;
        for (int d = 0; d < vectorA.inputs.length; d++) {
            distance += pow(vectorA.inputs[d] - vectorB[d], 2);
        }
        return sqrt(distance);
    }

    // vote for the closest category
    private int voteClosestCategory(int[] neighbours) {
        int[] classLabels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] counts = new int[classLabels.length];

        //Gets the number of counts that matches one of the class labels
        for (int i = 0; i < classLabels.length; i++) {
            for (int j = 0; j < neighbours.length; j++) {
                if (neighbours[j] == classLabels[i]) {
                    counts[i]++;
                }
            }
        }

        //Finds the maximum number of occurences
        int max = counts[0];
        for (int counter = 1; counter < counts.length; counter++) {
            if (counts[counter] > max) {
                max = counts[counter];
            }
        }

        //Increments the frequency if the counts[counter] is equal to the maximum number of occurences
        int freq = 0;
        for (int counter = 0; counter < counts.length; counter++) {
            if (counts[counter] == max) {
                freq++;
            }
        }

        int index = -1;
        if (freq == 1) { // if the frequency is once then we found one major category
            for (int counter = 0; counter < counts.length; counter++) {
                if (counts[counter] == max) {
                    index = counter;
                    break;
                }
            }
            return classLabels[index];
        } else { //otherwise if more than one then we break it at a random index
            int[] frequencyIndexes = new int[freq];
            int freqIndex = 0;
            for (int counter = 0; counter < counts.length; counter++) {
                if (counts[counter] == max) {
                    frequencyIndexes[freqIndex] = counter;
                    freqIndex++;
                }
            }

            int rIndex = new Random().nextInt(frequencyIndexes.length);
            int nIndex = frequencyIndexes[rIndex];
            //return unique value at that index 
            return classLabels[nIndex];
        }

    }

    //Finds the nearest Neighbours with the parameter of training set and the testing data
    private int findNearestNeigbour(List<DataSet> dataSet, DataSet testData, int k) {
        ArrayList<Result> resultList = new ArrayList<>(k);
        for (DataSet dataVector : dataSet) {
            double distance = distance(dataVector, testData.inputs);
            resultList.add(new Result(distance, dataVector.output));
        }

        //Sorts the results by distance
        sort(resultList, new DistanceComparator());

        //Gets the nearest classes found
        int[] neighbours = new int[k];
        for (int i = 0; i < k; i++) {
            neighbours[i] = resultList.get(i).targetOutput;
        }

        //result of the voting from the neighbours found earlier
        int closestCategory = voteClosestCategory(neighbours);

        return closestCategory;
    }

    public static void main(String[] args) {

        DigitCategoriser digitCategoriser = new DigitCategoriser();
        String dataSet1 = "cw2DataSet1.csv";
        String dataSet2 = "cw2DataSet2.csv";
        out.println("Reading data...");

        ArrayList<DataSet> trainSetList = digitCategoriser.createDataSetFromFile(dataSet1);
        ArrayList<DataSet> testSetList = digitCategoriser.createDataSetFromFile(dataSet2);

        //Testing is done here it can be opt-out
        //long startNanoTime = nanoTime();//Starting time
//        for (int k = 1; k <= 10; k++) { //from k = 1 to k = 10
//            int index = 0;
//            int[] predictions = new int[testSetList.size()];
//            out.println("K = " + k + " mode");
//            for (DataSet inputs : testSetList) {
//                int output = digitCategoriser.findNearestNeigbour(trainSetList, inputs, k);
//                predictions[index] = output;
//                out.println("Instance " + index + " Predicted: " + output + " Actual: " + inputs.output);
//                index++;
//            }
//            double accuracy = digitCategoriser.accuracyRate(testSetList, predictions);
//            out.println("Accuracy: " + accuracy);
//        }
//        long estimatedTime = nanoTime() - startNanoTime;
//        out.println("\nEstimated nanotime: " + estimatedTime + " nanoseconds");
        Scanner scan = new Scanner(in);
        out.println("Continue for cross validation? Press Y for Yes and N for No.");
        switch (scan.nextLine()) {
            case "y":
                //2 Fold Cross Validation here
                out.println("Performing 2-Fold Cross Validation...");
                long startValidationTime = nanoTime();//Starting time
                digitCategoriser.crossValidate(trainSetList, testSetList, 2);
                long estimatedValidationTime = nanoTime() - startValidationTime;//Ending time
                out.println("Estimated validation Time: " + estimatedValidationTime + " nanoseconds");
                break;
        }
    }
}
