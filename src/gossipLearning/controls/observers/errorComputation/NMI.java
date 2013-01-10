package gossipLearning.controls.observers.errorComputation;

import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;

/**
 * This class can be used for computing the NMI (normalized mutual
 * information) measurement based on the confusion matrix.
 * @author István Hegedűs
 *
 */
public class NMI extends ConfusionMtxFunction {
	

	public static final double M_LOG2E = 1.4426950408889634074;
	
	/***
	 * Bugfixed by Arpad Berta
	 */
	@Override
	public double postProcess(double meanError) {
		double nmi = 0.0;
		SparseVector expSum = new SparseVector();
		SparseVector predSum = new SparseVector();
		double N = 0.0;
		double entropy = 0.0;

		int numClasses = 0;
		SparseVector v;
		for (int i : confusionMatrix.keySet()) {
			v = confusionMatrix.get(i);
			if (numClasses <= v.maxIndex()) {
				numClasses = v.maxIndex() + 1;
			}
			if (numClasses <= i) {
				numClasses = i + 1;
			}
			for (VectorEntry e : v) {
				N += e.value;
				expSum.put(i, expSum.get(i) + e.value);
				predSum.put(e.index, predSum.get(e.index) + e.value);
			}
		}

		for (int i = 0; i < numClasses; i ++) {
			if(N==0){
				System.out.println(N);
			}
			double pi = expSum.get(i) / N;
			double pj = predSum.get(i) / N;
			v = confusionMatrix.get(i);
			for (int j = 0; v != null && j < numClasses; j++) {
				double value = v.get(j);
				
				double ep = expSum.get(i) * predSum.get(j);
				double nvep = ep == 0.0 ? 0.0 : (N * value) / ep;
				double lpij = nvep == 0.0 ? 0.0 : Math.log(nvep);
				
				nmi += value == 0.0  ? 0.0 : (value / N) * lpij * M_LOG2E;

			}
			if (v != null) {
				v.clear();
			}
			double entropyi = pi == 0.0 ? 0.0 : pi * Math.log(pi) * M_LOG2E;
		    double entropyj = pj == 0.0 ? 0.0 :pj * Math.log(pj) * M_LOG2E;
			entropy -= 0.5 * (entropyi + entropyj);
		}
		return N == 0.0  ? 0.0 : nmi / entropy;
	}

}