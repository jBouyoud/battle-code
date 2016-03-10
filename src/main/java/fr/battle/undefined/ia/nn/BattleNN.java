package fr.battle.undefined.ia.nn;

import lombok.extern.slf4j.Slf4j;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.exceptions.VectorSizeMismatchException;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.core.transfer.Linear;
import org.neuroph.core.transfer.RectifiedLinear;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.comp.neuron.InputNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.random.RangeRandomizer;

/**
 * http://www.nervanasys.com/demystifying-deep-reinforcement-learning/
 * https://sites.google.com/a/deepmind.com/dqn/
 * http://www.computervisiontalks.com/deep-learning-lecture-16-reinforcement-
 * learning-and-neuro-dynamic-programming-nando-de-freitas/
 *
 *
 *
 * https://github.com/torch/nn/blob/master/Linear.lua
 * https://github.com/torch/nn/blob/master/doc/simple.md#nn.Reshape
 * https://github.com/torch/nn/blob/master/doc/simple.md#nn.Linear
 *
 * http://neuroph.sourceforge.net/documentation.html
 * http://neuroph.sourceforge.net/javadoc/index.html
 * http://neuroph.sourceforge.net/images/uml/BasicClassDiagram.jpg
 * http://neuroph.sourceforge.net/Getting%20Started%20with%20Neuroph%202.7.pdf
 * http://technobium.com/stock-market-prediction-using-neuroph-neural-networks/
 * https://github.com/technobium/neuroph-neural-network/blob/master/src/main/
 * java/com/technobium/NeuralNetworkStockPredictor.java#L134
 */
@Slf4j
public class BattleNN extends NeuralNetwork<BackPropagation> {

	/** Serial ID */
	private static final long serialVersionUID = -8772491149779658792L;

	private final int inputNeurons;
	private final int outputNeurons;

	/**
	 * @param inputNeurons
	 * @param outputNeurons
	 */
	public BattleNN(final int inputNeurons, final int outputNeurons) {

		this.inputNeurons = inputNeurons;
		this.outputNeurons = outputNeurons;

		final int hiddenNnLayerCount = (int) Math.sqrt(Math.pow((double) inputNeurons + outputNeurons, 2d));

		// set network type
		setNetworkType(NeuralNetworkType.MULTI_LAYER_PERCEPTRON);
		// set input and output cells for network
		NeuralNetworkFactory.setDefaultIO(this);

		final SupervisedLearning learningRule = getLearningRule();
		learningRule.setMaxError(1e-5);
		learningRule.setLearningRate(.99);
		learningRule.setMaxIterations(1000);
		learningRule.addListener(learningEvent -> {
			final SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
			LOGGER.info("Network error for interation " + rule.getCurrentIteration() + ": "
					+ rule.getTotalNetworkError());
		});

		final NeuronProperties linearReductionLayerNnProp = new NeuronProperties();
		linearReductionLayerNnProp.setProperty("useBias", true);
		linearReductionLayerNnProp.setProperty("transferFunction", TransferFunctionType.LINEAR);

		final NeuronProperties maxLayerNnProp = new NeuronProperties();
		linearReductionLayerNnProp.setProperty("transferFunction", RectifiedLinear.class);

		// local n_hid = 128
		// local mlp = nn.Sequential()
		// mlp:add(nn.Reshape(self.hist_len*self.ncols*self.state_dim))
		// mlp:add(nn.Linear(self.hist_len*self.ncols*self.state_dim, n_hid))
		// mlp:add(nn.Rectifier())
		// mlp:add(nn.Linear(n_hid, n_hid))
		// mlp:add(nn.Rectifier())
		// mlp:add(nn.Linear(n_hid, self.n_actions))

		// set learnng rule
		setLearningRule(new BackPropagation());
		// this.setLearningRule(new MomentumBackpropagation());
		// this.setLearningRule(new DynamicBackPropagation());
		randomizeWeights(new RangeRandomizer(-0.7, 0.7));

		// create input layer
		final Layer inputLayer = LayerFactory.createLayer(inputNeurons, new NeuronProperties(InputNeuron.class,
				Linear.class));
		this.addLayer(inputLayer);

		// 1nd layer linear reduction
		final Layer firstHidden = LayerFactory.createLayer(hiddenNnLayerCount, linearReductionLayerNnProp);
		firstHidden.addNeuron(new BiasNeuron());
		this.addLayer(firstHidden);
		ConnectionFactory.fullConnect(inputLayer, firstHidden);

		// 2nd layer max(0,x)
		final Layer secondHidden = LayerFactory.createLayer(hiddenNnLayerCount, maxLayerNnProp);
		this.addLayer(secondHidden);
		ConnectionFactory.fullConnect(firstHidden, secondHidden);

		// 3rd layer linear reduction
		final Layer thirdHidden = LayerFactory.createLayer(hiddenNnLayerCount, linearReductionLayerNnProp);
		thirdHidden.addNeuron(new BiasNeuron());
		this.addLayer(thirdHidden);
		ConnectionFactory.fullConnect(secondHidden, thirdHidden);

		// 4th layer max(0,x)
		final Layer fourthHidden = LayerFactory.createLayer(hiddenNnLayerCount, maxLayerNnProp);
		this.addLayer(fourthHidden);
		ConnectionFactory.fullConnect(thirdHidden, fourthHidden);

		// OutputLayer
		linearReductionLayerNnProp.setProperty("useBias", false);
		final Layer outputLayer = LayerFactory.createLayer(outputNeurons, linearReductionLayerNnProp);
		this.addLayer(outputLayer);
		ConnectionFactory.fullConnect(fourthHidden, outputLayer);
	}

	public void setInput(final int inputVector[]) throws VectorSizeMismatchException {
		if (inputVector.length != getInputsCount()) {
			throw new VectorSizeMismatchException("Input vector size does not match network input dimension!");
		}
		int i = 0;
		for (final Neuron neuron : getInputNeurons()) {
			neuron.setInput(inputVector[i++]);
		}
	}

	public int[] getOutputAsInt() {
		int i = 0;
		final int[] output = new int[getOutputsCount()];
		for (final Neuron ouputNeuron : getOutputNeurons()) {
			final double out = ouputNeuron.getOutput();
			this.output[i] = out;
			output[i++] = (int) out;
		}
		return output;
	}

	/**
	 * Learn from replayed Memory
	 *
	 * @param replayedMemory
	 */
	public void learn(final double[] futureState, final double reward) {
		final DataSet trainingSet = new DataSet(inputNeurons, 1);
		trainingSet.addRow(new DataSetRow(futureState, new double[] { reward }));
		learn(trainingSet);
	}
}
