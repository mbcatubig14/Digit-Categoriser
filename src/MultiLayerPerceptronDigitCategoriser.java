
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Muhammad
 */
public class MultiLayerPerceptronDigitCategoriser {

    private final double SLOPE = 1;
    double[] output;
    ArrayList<ArrayList<Neuron>> layers;
    ArrayList<Neuron> inputNeurons;
    ArrayList<Neuron> outputNeurons;

    //Object model for Data sets
    static class DataSet {

        int[] inputs;
        int output;

        public DataSet(int[] inputs, int output) {
            this.inputs = inputs;
            this.output = output;
        }
        int[] outputs;

        public DataSet(int[] inputs, int[] outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }
    }

    //Class model of a neuron
    static class Neuron {

        double inputFunction;//Input function for this neuron
        double transferFunction; //input function for this neuron
        ArrayList<Connection> inputConnections; //Collection of neuron's input connections
        ArrayList<Connection> outputConnections;//Collection of neuron's output connections

        private double input = 0;
        private double output = 0;
        private int categoryLabel;

        public Neuron(double inputFunction, double transferFunction) {
            this.inputFunction = inputFunction;
            this.transferFunction = transferFunction;
            inputConnections = new ArrayList();
            outputConnections = new ArrayList();
        }

    }

    //Class model for Connection of two neurons with weights
    static class Connection {

        Neuron fromNeuron;
        Neuron toNeuron;
        double weight;

        public Connection(Neuron fromNeuron, Neuron toNeuron, double weight) {
            this.fromNeuron = fromNeuron;
            this.toNeuron = toNeuron;
            this.weight = weight;
        }

        public Connection(Neuron fromNeuron, Neuron toNeuron) {
            this.fromNeuron = fromNeuron;
            this.toNeuron = toNeuron;
        }

        public double getWeightedInput() {
            return fromNeuron.output * weight;
        }

    }

    private double[] weightedSum(int[] inputs, double[] weights) {
        double[] weightedSum = new double[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            weightedSum[i] += inputs[i] * weights[i];
        }

        return weightedSum;
    }

    public double sigmoidFunction(double weightedSum) {
        return 1 + Math.exp(-SLOPE * weightedSum);
    }

    private ArrayList<DataSet> createFromFile(String csvFile) {
        ArrayList<DataSet> dataList = new ArrayList<>();
        int[][] dataSetArray = new int[20][65];
        try {
            int instances = 0;
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(csvFile)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] dataFromCSV = line.split(",");

                //takes the input attributes
                for (int i = 0; i < dataFromCSV.length - 1; i++) {
                    dataSetArray[instances][i] = parseInt(dataFromCSV[i]); //parse data    
                }
                //takes the class label
                int label = parseInt(dataFromCSV[64]);
                //dataList.add(new DataSet(dataSetArray[instances], dataSetArray[instances][64]));
                dataList.add(new DataSet(dataSetArray[instances], label));
                instances++;
            }
        } catch (IOException | NumberFormatException ex) {
        }
        return dataList;
    }

    private ArrayList<Neuron> createNeuronsInLayers(int neuronCounts, Neuron neuron) {
        ArrayList<Neuron> neuronsInLayers = new ArrayList<>();
        for (int i = 0; i < neuronCounts; i++) {
            neuronsInLayers.add(neuron);
        }

        return neuronsInLayers;
    }

    //creates a connection between two neurons
    private void createConnections(Neuron fromNeuron, Neuron toNeuron) {
        Connection connection = new Connection(fromNeuron, toNeuron);
        toNeuron.inputConnections.add(connection);
        connection.fromNeuron.outputConnections.add(connection);

    }

    //creates a connection between two neurons with weights
    private void createConnections(Neuron fromNeuron, Neuron toNeuron, double weights) {
        Connection connection = new Connection(fromNeuron, toNeuron, weights);
        toNeuron.inputConnections.add(connection);
        connection.fromNeuron.outputConnections.add(connection);
    }

    private void connectLayers(ArrayList<Neuron> fromLayer, ArrayList<Neuron> toLayer) {
        Neuron[] fromNeurons = (Neuron[]) fromLayer.toArray();
        Neuron[] toNeurons = (Neuron[]) toLayer.toArray();
        for (Neuron fromNeuron : fromNeurons) {
            for (Neuron toNeuron : toNeurons) {
                createConnections(fromNeuron, toNeuron);
            }
        }
    }

    private void createNetwork(List<Integer> neuronsInLayers, Neuron neuron) {

        //creates input layer
        ArrayList<Neuron> layer = createNeuronsInLayers(neuronsInLayers.get(0), neuron);
        Neuron biasNeuron = new Neuron(0.0, 0.0);
        layer.add(biasNeuron);
        layers.add(layer);

        ArrayList<Neuron> prevLayer = layer;

        for (int layerIndex = 1; layerIndex < neuronsInLayers.size(); layerIndex++) {
            int numOfNeurons = neuronsInLayers.get(layerIndex);
            layer = createNeuronsInLayers(numOfNeurons, neuron);
            if (layerIndex < (neuronsInLayers.size() - 1)) {
                Neuron newBiasNeuron = new Neuron(0.0, 0.0);
                layer.add(newBiasNeuron);
            }
            //add create layer to network
            layers.add(layer);
            if (prevLayer != null) {
                connectLayers(prevLayer, layer);
            }

            prevLayer = layer;
        }
    }

    private void initialise(List<Integer> neuronsInLayers, Neuron neuron) {
        ArrayList<Neuron> inputNeuronsList = inputNeurons;
        ArrayList<ArrayList<Neuron>> layer = layers;
        for (ArrayList<Neuron> neuronsLayers : layer) {
            for (Neuron eachNeuron : neuronsLayers) {
                inputNeuronsList.add(eachNeuron);
            }
        }

        Neuron[] inputNeuronsArray = new Neuron[inputNeuronsList.size()];
        inputNeuronsArray = inputNeuronsList.toArray(inputNeuronsArray);

        Neuron[] outputNeuronsArray = (Neuron[]) layer.get(layers.size() - 1).toArray();

        inputNeurons.addAll(Arrays.asList(inputNeuronsArray));
        outputNeurons.addAll(Arrays.asList(outputNeuronsArray));

        createNetwork(neuronsInLayers, neuron);
    }

    private void doBackPropagation() {

    }

    //Array of randomise weights with the length of the inputs 
    public double[] randomiseInputWeights(int inputLength) {
        double[] randomWeights = new double[inputLength];
        for (int index = 0; index < randomWeights.length; index++) {
            randomWeights[index] = Math.random() * 1;
        }
        return randomWeights;
    }

    public MultiLayerPerceptronDigitCategoriser(String csvFile) {
        layers = new ArrayList<>();
        inputNeurons = new ArrayList<>();
        outputNeurons = new ArrayList<>();

        List<Integer> neuronNumCollections = new ArrayList<>();

        for (DataSet dataSet : createFromFile(csvFile)) {
            neuronNumCollections.add(dataSet.inputs.length);
//set input for all of them
            int neuronCounts = 0;
            for (Neuron neuron : inputNeurons) {
                neuron.input = dataSet.inputs[neuronCounts];
                neuronNumCollections.add(50);
                initialise(neuronNumCollections, neuron);
                neuronCounts++;
            }

            //calculate network
            double[] inputFunction = weightedSum(dataSet.inputs, randomiseInputWeights(dataSet.inputs.length));
            for (int inputFunctionCounts = 0; inputFunctionCounts < inputFunction.length; inputFunctionCounts++) {
                for (ArrayList<Neuron> layer : layers) {
                    for (Neuron neuron : layer) {
                        if (neuron.inputConnections.size() > 0) {
                            neuron.input = inputFunction[inputFunctionCounts];
                        }
                        neuron.output = sigmoidFunction(neuron.input);

                    }
                }
            }

            //get output
            for (int outputCount = 0; outputCount < outputNeurons.size(); outputCount++) {
                output[outputCount] = outputNeurons.get(outputCount).output;
                neuronNumCollections.add(output.length);
            }

            System.out.print("Input: " + Arrays.toString(dataSet.inputs));
            System.out.println(" Output: " + Arrays.toString(output));
        }

    }

    public static void main(String[] args) {
        MultiLayerPerceptronDigitCategoriser multiLayerPerceptronDigitCategoriser = new MultiLayerPerceptronDigitCategoriser("digits.csv");

    }

}
