import jcuda.Pointer;

/*
 * Main layer class
 */
public class Layer {
	public int inputSize, layerSize, weightSize;
	public float[] weights, biases;
	
	/*
	 * given 3x2 weight matrix
	 * weights are stored in the format
	 * w[0] w[3]
	 * w[1] w[4]
	 * w[2] w[5]
	 * when doing matrix calculations
	 */
	
	public Layer(int layerSize, int inputSize) {
		this.layerSize = layerSize;
		this.inputSize = inputSize;
		this.weightSize = inputSize * layerSize;
		weights = Utilities.randFloatArray(weightSize);
		biases = Utilities.randFloatArray(layerSize);
	}
	
	//constructor that creates a deep copy of the given layer
	public Layer(Layer parent) {
		this.inputSize = parent.inputSize;
		this.layerSize = parent.layerSize;
		this.weightSize = parent.weightSize;
		this.weights = parent.weights.clone();
		this.biases = parent.biases.clone();
	}

	/*
	 * takes in a float array and processes the data into an output for the next layer
	 * used for input and hidden layers
	 */
	public Pointer run(Pointer p_D) {
		Utilities.dotProduct(1, layerSize, inputSize, p_D, weights);
		Utilities.addBiasAndActivation(p_D, biases);
		return p_D;
	}
	
	public void mutate(double prob) {
		for(int i = 0; i < weightSize; i ++) {
			if(Math.random() < prob) {
				weights[i] = (float) Math.random() * 2 - 1;
			}
		}
		for(int i = 0; i < layerSize; i ++) {
			if(Math.random() < prob) {
				biases[i] = (float) Math.random() * 2 - 1;
			}
		}
	}
	
	public Layer clone() {
		return new Layer(this);
	}
}
