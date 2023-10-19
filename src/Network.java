import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;

public class Network {
	public Layer[] layers;
	public int inputSize, outputSize, numLayers, layerSize;
	public Network(int inputSize, int outputSize, int numLayers, int layerSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.numLayers = numLayers;
		this.layerSize = layerSize;
		layers = new Layer[numLayers];
		layers[0] = new Layer(layerSize, inputSize);
		layers[numLayers - 1] = new Layer(outputSize, layerSize);
		for(int i = 1; i < numLayers - 1; i ++) {
			layers[i] = new Layer(layerSize, layerSize);
		}
	}
	
	// constructor that creates a deep copy of the given network
	public Network(Network parent) {
		this.inputSize = parent.inputSize;
		this.outputSize = parent.outputSize;
		this.numLayers = parent.numLayers;
		this.layerSize = parent.layerSize;
		this.layers = new Layer[numLayers];
		for(int i = 0; i < numLayers; i ++) {
			this.layers[i] = parent.layers[i].clone();
		}
	}

	public float[] run(float[] inputs) {
		float[] data = inputs.clone();
		Pointer p_D = new Pointer();
		JCublas.cublasInit();
		JCublas.cublasAlloc(Math.max(layerSize, Math.max(inputSize, outputSize)), Sizeof.FLOAT, p_D);
		JCublas.cublasSetVector(data.length, Sizeof.FLOAT, Pointer.to(data), 1, p_D, 1);
		for(int i = 0; i < numLayers; i ++) {
			p_D = layers[i].run(p_D);
		}
		data = new float[outputSize];
		JCublas.cublasGetVector(outputSize, Sizeof.FLOAT, p_D, 1, Pointer.to(data), 1);
		data = Utilities.softmaxNormalization(data);
		JCublas.cublasFree(p_D);
		JCublas.cublasShutdown();
		return data;
	}
	
	public Network mutate(double prob) {
		for(Layer l : layers) {
			l.mutate(prob);
		}
		return this;
	}
	
	public Network clone() {
		return new Network(this);
	}
}
