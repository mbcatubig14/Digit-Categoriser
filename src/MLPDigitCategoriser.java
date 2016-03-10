
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class MLPDigitCategoriser {

    //Class model of Data set
    static class DataSet {

        int[] inputs;
        int output;

        public DataSet(int[] inputs, int output) {
            this.inputs = inputs;
            this.output = output;
        }
    }

    //Class model of Neuron
    static class Neuron {

        double inputFunction;
        double transferFunction;
        int bias = 1;//Use when bias is needed
        int label;//Label for this neuron
        double error = 0;//Local error for this neuron

        ArrayList<Connection> inputConnections;
        ArrayList<Connection> outputConnections;

        public Neuron() {
            inputConnections = new ArrayList<>();
            outputConnections = new ArrayList<>();
        }

        public Neuron(double inputFunction, double transferFunction) {
            this.inputFunction = inputFunction;
            this.transferFunction = transferFunction;
            inputConnections = new ArrayList<>();
            outputConnections = new ArrayList<>();
        }

    }

    //Class model for Layer
    static class Layer {

        int numberOfNeurons;
        ArrayList<Neuron> neuronsInLayers;

        public Layer(int numberOfNeurons, Neuron neuron) {
            this.numberOfNeurons = numberOfNeurons;
            neuronsInLayers = new ArrayList<>();

            for (int neuronCounter = 0; neuronCounter < numberOfNeurons; neuronCounter++) {
                neuronsInLayers.add(neuron);
            }
        }
    }

    //Class model for connection
    static class Connection {

        Neuron fromNeuron;
        Neuron toNeuron;
        double weight;

        public Connection(Neuron fromNeuron, Neuron toNeuron) {
            this.fromNeuron = fromNeuron;
            this.toNeuron = toNeuron;
        }

    }

    final double learningRate = 0.1;
    final double SLOPE = 1.0;
    ArrayList<Layer> layersInNetwork = new ArrayList<>();
    ArrayList<Neuron> inputNeurons = new ArrayList<>();
    ArrayList<Neuron> outputNeurons = new ArrayList<>();
    double totalError = 0;
    double patternCount = 0;

    //calculates and returns the mean squared error
    private double[] meanSquaredError(ArrayList<Neuron> targetOutput, double[] predictedOutput) {

        double[] patternErrors = new double[targetOutput.size()];

        for (int i = 0; i < predictedOutput.length; i++) {
            //System.out.println("targetOutput: " + targetOutput.get(i).transferFunction + " predictedOutput: " + predictedOutput[i]);
            patternErrors[i] = targetOutput.get(i).transferFunction - predictedOutput[i];

            totalError += patternErrors[i] * patternErrors[i];
        }
        patternCount++;

        return patternErrors;
    }

    private double totalError() {
        return totalError / (2 * patternCount);
    }

    //creates the data set from file
    private ArrayList<DataSet> createFromFile(String csvFile) {
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
                        dataSetArray[instances][i] = Integer.parseInt(dataFromCSV[i]); //parse data    
                        input[i] = dataSetArray[instances][i];
                    }
                }
                //takes the class label
                int label = Integer.parseInt(dataFromCSV[64]);
                dataList.add(new DataSet(input, label));
                instances++;
            }
        } catch (IOException | NumberFormatException ex) {
        }
        return dataList;
    }

    private double[] weightedSum(int[] inputs, double[] weights) {
        double[] weightedSum = new double[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            weightedSum[i] += inputs[i] * weights[i];
        }

        return weightedSum;
    }

    private double sigmoidFunction(double input) {
        double expressResult = 1 + Math.exp(-SLOPE * input);
        return 1 / expressResult;
    }

    //Array of randomise weights with the length of the inputs 
    public double[] randomiseInputWeights(int inputLength) {
        double[] randomWeights = new double[inputLength];
        for (int index = 0; index < randomWeights.length; index++) {
            randomWeights[index] = Math.random() * 1;
        }
        return randomWeights;
    }

    //Updates weights of a neuron
    private void updateNeuronWeights(Neuron neuron) {
        double momentum = 0.25;
        for (Connection connection : neuron.inputConnections) {
            double input = connection.fromNeuron.transferFunction;
            double neuronError = neuron.error;

            double previousWeightValue = connection.weight;
            double weightChange = learningRate * neuronError * input + momentum * (connection.weight - previousWeightValue);

            connection.weight += weightChange;
        }
    }

    private double randomWeight() {
        return Math.random() * 1;
    }

    private double randomiseWeightRanges(double min, double max) {
        return min + new Random().nextDouble() * (max - min);
    }

    private void connect(Neuron fromNeuron, Neuron toNeuron, double weight) {
        Connection connection = new Connection(fromNeuron, toNeuron);
        toNeuron.inputConnections.add(connection);
        connection.fromNeuron.outputConnections.add(connection);
    }

    private void connectLayers(Layer fromLayer, Layer toLayer) {
        for (Neuron fromNeuron : fromLayer.neuronsInLayers) {
            for (Neuron toNeuron : toLayer.neuronsInLayers) {
                connect(fromNeuron, toNeuron, randomWeight());
            }
        }

    }

    private void createNetwork(int[] neuronsInLayers, Neuron neuron) {
        Neuron inputNeuron = new Neuron();
        Layer layer = new Layer(neuronsInLayers[0], inputNeuron);
        boolean useBias = true;
        if (useBias) {
            Neuron biasNeuron = new Neuron();
            layer.neuronsInLayers.add(biasNeuron);
        }

        layersInNetwork.add(layer);

        Layer previousLayer = layer;

        for (int layerIndex = 1; layerIndex < neuronsInLayers.length; layerIndex++) {
            int numOfNeurons = neuronsInLayers[layerIndex];

            layer = new Layer(numOfNeurons, neuron);

            if (useBias && (layerIndex < (neuronsInLayers.length - 1))) {
                Neuron newBiasNeuron = new Neuron(0.0, 0.0);
                layer.neuronsInLayers.add(newBiasNeuron);
            }
            layersInNetwork.add(layer);

            connectLayers(previousLayer, layer);

            previousLayer = layer;
        }
        setInputAndOutput();
        randomiseWeightRanges(-0.7, 0.7);
    }

    private void setInputAndOutput() {
        ArrayList<Neuron> inputNeuronsList = new ArrayList<>();
        Layer firstLayer = layersInNetwork.get(0);
        for (Neuron neuron : firstLayer.neuronsInLayers) {
            inputNeuronsList.add(neuron);
        }

        Neuron[] inputNeuronsArray = new Neuron[inputNeuronsList.size()];
        inputNeuronsArray = inputNeuronsList.toArray(inputNeuronsArray);

        inputNeurons.addAll(Arrays.asList(inputNeuronsArray));
        outputNeurons.addAll(layersInNetwork.get(layersInNetwork.size() - 1).neuronsInLayers);

    }

    //It performs 2-fold cross validation returning a accuracy result
    private void crossValidate(ArrayList<DataSet> trainingSet, ArrayList<DataSet> testingSet) {

        train(trainingSet);
        System.out.println("Reversing DataSets...");
        train(testingSet);

    }

    private void backPropagate(double[] outputError) {
        //Calculates error and update output neurons
        int i = 0;
        for (Neuron neuron : outputNeurons) {
            if (outputError[i] == 0) {
                neuron.error = 0;
                i++;
                continue;
            }

            double derivative = SLOPE * neuron.transferFunction * (1 - neuron.transferFunction) + 0.1;
            double delta = outputError[i] * derivative;
            neuron.error = delta;

            updateNeuronWeights(neuron);
            i++;
        }

        //Calculates error and update hidden neurons
        for (int layerIndex = layersInNetwork.size() - 2; layerIndex > 0; layerIndex--) {
            for (Neuron neuron : layersInNetwork.get(layerIndex).neuronsInLayers) {

                //Calculates the neuron's error (delta)
                double deltaSum = 0.0;
                for (Connection connection : neuron.outputConnections) {
                    double delta = connection.toNeuron.error * connection.weight;
                    deltaSum += delta;
                }
                double derivative = SLOPE * neuron.transferFunction * (1 - neuron.transferFunction) + 0.1;
                double neuronError = derivative * deltaSum;
                neuron.error = neuronError;
                updateNeuronWeights(neuron);
            }
        }
    }

    private void train(ArrayList<DataSet> dataSetsFromFile) {
        int counter = 1;
        for (DataSet dataSet : dataSetsFromFile) {
            int[] counts = new int[]{dataSet.inputs.length, 50, 10};

            double[] weightedSum = weightedSum(dataSet.inputs, randomiseInputWeights(dataSet.inputs.length));

            for (int i = 0; i < weightedSum.length; i++) {
                double sigmoid = sigmoidFunction(weightedSum[i]);
                Neuron neuron = new Neuron(weightedSum[i], sigmoid);
                neuron.label = dataSet.output;
                createNetwork(counts, neuron);

                for (Layer layer : layersInNetwork) {
                    for (Neuron inputNeuron : layer.neuronsInLayers) {
                        if (inputNeuron.inputConnections.size() > 0) {
                            inputNeuron.inputFunction = weightedSum[i];
                        }
                        inputNeuron.transferFunction = sigmoid;
                        inputNeuron.label = dataSet.output;
                    }
                }
            }

            double[] networkOutputs = new double[outputNeurons.size()];

            for (int j = 0; j < outputNeurons.size(); j++) {
                networkOutputs[j] = outputNeurons.get(j).transferFunction;
            }
            double accuracyRate = accuracyRate(outputNeurons, networkOutputs);
            double[] patternError = meanSquaredError(outputNeurons, networkOutputs);
            backPropagate(patternError);

            System.out.println("Instance: " + counter + " Total Error: " + totalError() + " Accuracy: " + accuracyRate);
            counter++;

        }
    }

    //calculates and returns the accuracy rate between actual and expected outputs
    private double accuracyRate(ArrayList<Neuron> testSet, double[] predictions) {
        int correct = 0;
        double size = testSet.size();
        for (int i = 0; i < size; i++) {
            if (testSet.get(i).transferFunction == predictions[i]) {
                correct += 1;
            }
        }
        System.out.println(correct + " out of " + predictions.length + " correct class.");
        double average = (correct / size) * 100;

        return average;
    }

    public static void main(String[] args) {
        MLPDigitCategoriser mLPDC = new MLPDigitCategoriser();
        ArrayList<DataSet> dataSetsFromFile1 = mLPDC.createFromFile("cw2DataSet1.csv");
        ArrayList<DataSet> dataSetsFromFile2 = mLPDC.createFromFile("cw2DataSet2.csv");
        //ArrayList<DataSet> dataSetsFromFile3 = mLPDC.createFromFile("digits.csv");
        mLPDC.train(dataSetsFromFile1);
        //mLPDC.crossValidate(dataSetsFromFile1, dataSetsFromFile2);
    }
}
