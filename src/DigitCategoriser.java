
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import java.util.*;

public class DigitCategoriser {

    static class DataVector {

        int[] inputs;
        int output;

        public DataVector(int[] inputs, int output) {
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public String toString() {
            return "Category:" + output + " Data:" + Arrays.toString(inputs) + "\n";
        }

    }

    //Class model to compare two results for sorting
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

    //Object model for Result
    static class Result {

        double distance;
        int label;

        public Result(double distance, int label) {
            this.distance = distance;
            this.label = label;
        }

        @Override
        public String toString() {
            return "Result{" + "distance=" + distance + ", label=" + label + '}';
        }

    }

    //creates the data set from file
    private ArrayList<DataVector> createDataSetFromFile(String csvFile) {
        ArrayList<DataVector> dataList = new ArrayList<>();
        int[][] dataSetArray = new int[2810][65];
        try {
            int instances = 0;
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(csvFile)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] dataFromCSV = line.split(",");

                //takes the input attributes
                int[] input = new int[63];
                for (int i = 0; i < dataFromCSV.length - 1; i++) {
                    if (i < 63) {
                        dataSetArray[instances][i] = parseInt(dataFromCSV[i]); //parse data    
                        input[i] = dataSetArray[instances][i];
                    }
                }

                //takes the class label
                int label = parseInt(dataFromCSV[64]);
                //dataList.add(new DataSet(dataSetArray[instances], dataSetArray[instances][64]));
                dataList.add(new DataVector(input, label));
                instances++;
            }
        } catch (IOException | NumberFormatException ex) {
        }
        return dataList;
    }

    private int randomKNumber() {
        return (int) (Math.random() * 11 + 1);
    }

    //It performs 2-fold cross validation returning a accuracy result
    private void crossValidate(ArrayList<DataVector> dataSet, int kFold) {
        ArrayList<DataVector>[] subSets = new ArrayList[kFold];

        ArrayList<DataVector> trainingSet = new ArrayList<>(dataSet.size() / kFold);
        ArrayList<DataVector> testingSet = new ArrayList<>(dataSet.size() / kFold);

        for (int i = 0; i < dataSet.size(); i++) {
            int randomIndex = new Random().nextInt(dataSet.size());
            trainingSet.add(dataSet.get(randomIndex));
        }

        for (int i = 0; i < dataSet.size(); i++) {
            int randomIndex = new Random().nextInt(dataSet.size());
            testingSet.add(dataSet.get(randomIndex));
        }

        subSets[0] = trainingSet;
        subSets[1] = testingSet;

        double accuracy = 0;
        double meanSquaredError = 0;
        int counter = 0;
        for (ArrayList<DataVector> subSet : subSets) {
            System.out.println("Subset " + counter + 1);
            for (int k = 1; k < 11; k++) {
                int[] predictedOutput = new int[subSet.size()];
                for (int testSetIndex = 0; testSetIndex < subSet.size(); testSetIndex++) {
                    int predicts = findNearestNeigbour(dataSet, subSet.get(testSetIndex), k);
                    predictedOutput[testSetIndex] = predicts;
                    //System.out.println("Target Output: " + subSet.get(testSetIndex).output + " Actual Output: " + predicts);
                }
                System.out.println("K = " + k);
                accuracy = accuracyRate(subSet, predictedOutput);
                System.out.println("Accuracy: " + accuracy);
                meanSquaredError = meanSquareError(subSet, predictedOutput);
                System.out.println("Mean Squared Error: " + meanSquaredError);
            }
            counter++;
        }

    }

    //calculates and returns the accuracy rate between actual and expected outputs
    private double accuracyRate(ArrayList<DataVector> testSet, int[] predictions) {
        int correct = 0;
        double size = testSet.size();
        for (int i = 0; i < size; i++) {
            if (testSet.get(i).output == predictions[i]) {
                correct += 1;
            }
        }

        double average = (correct / size) * 100;

        return average;
    }

    //calculates and returns the mean squared error
    private double meanSquareError(ArrayList<DataVector> testSet, int[] predictions) {
        int count = 0;
        double totalError = 0;
        double size = testSet.size();
        for (int i = 0; i < size; i++) {
            int difference = testSet.get(i).output - predictions[i];
            totalError += difference * difference;
            count++;
        }

        return totalError / (2 * count);
    }

    //calculates and returns the distance between two inputs
    private double distance(DataVector vectorA, int[] vectorB) {
        double distance = 0;
        for (int d = 0; d < vectorA.inputs.length; d++) {
            distance += Math.pow(vectorA.inputs[d] - vectorB[d], 2);
        }
        return Math.sqrt(distance);
    }

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

    //Finds the nearest Neighbours
    private int findNearestNeigbour(ArrayList<DataVector> dataSet, DataVector testData, int k) {
        ArrayList<Result> resultList = new ArrayList<>(k);
        for (DataVector dataVector : dataSet) {
            double distance = distance(dataVector, testData.inputs);
            resultList.add(new Result(distance, dataVector.output));
        }

        //Sorts the results by distance
        Collections.sort(resultList, new DistanceComparator());

        //Getting the nearest class found
        int[] neighbours = new int[k];
        for (int i = 0; i < k; i++) {
            neighbours[i] = resultList.get(i).label;
        }

        //result of the voting from the neighbours found earlier
        int closestCategory = voteClosestCategory(neighbours);

        return closestCategory;
    }

    public static void main(String[] args) {

        DigitCategoriser digitCategoriser = new DigitCategoriser();
        String dataSet1 = "cw2DataSet1.csv";
        String dataSet2 = "cw2DataSet2.csv";
        String dataset3 = "digits.csv";
        String dataset4 = "digits_1.csv";
        out.println("Reading data...");

        long startNanoTime = System.nanoTime();
        ArrayList<DataVector> trainSetList = digitCategoriser.createDataSetFromFile(dataSet1);
        ArrayList<DataVector> testSetList = digitCategoriser.createDataSetFromFile(dataSet2);

        //Testing the data and gets the initial result
        int[] predictions = new int[testSetList.size()];
        int index = 0;
        int k = digitCategoriser.randomKNumber();
        for (DataVector inputs : testSetList) {
            int output = digitCategoriser.findNearestNeigbour(trainSetList, inputs, k);
            System.out.println("Predicted: " + output + " Actual: " + inputs.output);
            predictions[index] = output;
            index++;
        }
        double accuracy = digitCategoriser.accuracyRate(testSetList, predictions);
        System.out.println("K = " + k + " Accuracy: " + accuracy);

        long estimatedTime = System.nanoTime() - startNanoTime;
        out.println("\nEstimated nanotime: " + estimatedTime + " nanoseconds");
        Scanner scan = new Scanner(System.in);
        System.out.println("Continue for cross validation? Press Y for Yes and N for No.");
        switch (scan.nextLine()) {
            case "y":
                //2 Fold Cross Validation here
                System.out.println("Performing 2-Fold Cross Validation...");
                digitCategoriser.crossValidate(trainSetList, 2);
                break;
        }

    }

}
