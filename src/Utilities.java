import jcuda.jcublas.*;
import jcuda.*;
import jcuda.vec.VecFloat;
public class Utilities {
	
	public static float[] randFloatArray(int n) {
		float[] f = new float[n];
		for(int i = 0; i < n; i ++) {
			f[i] = (float) Math.random() * 2 - 1;
		}
		return f;
	}
	
	public static Pointer dotProduct(int m, int n, int k, Pointer p_D, float[] b) {
		int bl = b.length;
		Pointer p_B = new Pointer();
		//allocate memory
		JCublas.cublasAlloc(bl, Sizeof.FLOAT, p_B);
		//give device host data
		JCublas.cublasSetVector(bl, Sizeof.FLOAT, Pointer.to(b), 1, p_B, 1);
		//perform dot product
		JCublas.cublasSgemm('n', 'n', m, n, k, 1, p_D, m, p_B, k, 0, p_D, m);
		//retrieve data from device
		//free memory
		JCublas.cublasFree(p_B);
		return p_D;
	}
	
	public static Pointer addBiasAndActivation(Pointer p_D, float[] b) {
		int bl = b.length;
		Pointer p_B = new Pointer();
		VecFloat.init();
		JCublas.cublasAlloc(bl, Sizeof.FLOAT, p_B);
		JCublas.cublasSetVector(bl, Sizeof.FLOAT, Pointer.to(b), 1, p_B, 1);
		//add bias from b
		VecFloat.add(bl, p_D, p_D, p_B);
		//use atan as activation function
		VecFloat.atan(bl, p_D, p_D);
		JCublas.cublasFree(p_B);
		VecFloat.shutdown();
		return p_D;
	}
	
	public static float[] softmaxNormalization(float[] a) {
		int al = a.length;
		float max = a[0];
		for(int i = 1; i < a.length; i ++) {
			max = Math.max(max, a[i]);
		}
		Pointer p_A = new Pointer();
		VecFloat.init();
		JCublas.cublasAlloc(al, Sizeof.FLOAT, p_A);
		JCublas.cublasSetVector(al, Sizeof.FLOAT, Pointer.to(a), 1, p_A, 1);
		//subtract max value from vector to make values <0
		VecFloat.subScalar(al, p_A, p_A, max);
		//exponentiate values
		VecFloat.exp(al, p_A, p_A);
		//normalize
		float sum = JCublas.cublasSasum(al, p_A, 1);
		VecFloat.divScalar(al, p_A, p_A, sum);
		JCublas.cublasGetVector(al, Sizeof.FLOAT, p_A, 1, Pointer.to(a), 1);
		JCublas.cublasFree(p_A);
		VecFloat.shutdown();
		return a;
	}
	/**
	 * using two networks A and B to create a new network C with weights and biases of either one
	 * where prob is the chance that the network will take on attributes of network A for each weight and bias
	 * @param a The first network
	 * @param b The second network
	 * @param prob The change that the method will choose the a over b for any given attribute (must be from 0-1)
	 */
	public static Network crossover(Network a, Network b, double prob) {
		Network c = a.clone();
		for(int l = 0; l < c.numLayers; l ++) {
			for(int i = 0; i < c.layers[l].weightSize; i ++) {
				if(Math.random() > prob) {
					c.layers[l].weights[i] = b.layers[l].weights[i];
				}
			}
			for(int i = 0; i < c.layers[l].layerSize; i ++) {
				if(Math.random() > prob) {
					c.layers[l].biases[i] = b.layers[l].biases[i];
				}
			}
		}
		return c;
	}
}
