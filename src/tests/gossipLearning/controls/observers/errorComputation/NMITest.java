package tests.gossipLearning.controls.observers.errorComputation;

import static org.junit.Assert.*;
import gossipLearning.controls.observers.errorComputation.NMI;
import gossipLearning.utils.SparseVector;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
/***
 * This class is an extensive test case of our NMI implementation.
 * 
 * @author arphead
 *
 */
public class NMITest {


	/***
	 * Test the postProcess function with a referenced example
	 * @see <a href="http://www-nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">example</a>
	 */
	@Test
	public void testPostProcessWithSample() {
		NMI nmi;
		Map<Integer, SparseVector> confusionMatrix;

		confusionMatrix = new TreeMap<Integer, SparseVector>();
		double[] sp0e = {5,1,2};
		SparseVector sp0 = new SparseVector(sp0e);

		double[] sp1e = {1,4,0};
		SparseVector sp1 = new SparseVector(sp1e);

		double[] sp2e = {0,1,3};
		SparseVector sp2 = new SparseVector(sp2e);

		confusionMatrix.put(0, sp0);
		confusionMatrix.put(1, sp1);
		confusionMatrix.put(2, sp2);

		nmi = new NMI();
		nmi.setConfusionMatrix(confusionMatrix);

		double actual = nmi.postProcess(0.0);
		double expected = 0.36;
		assertEquals(expected, actual, 0.01);
	}

	/***
	 * Test the postProcess function with 0 diagonal Confusion Matrix
	 */
	@Test
	public void testPostProcessWithZeroDiag() {
		NMI nmi;
		Map<Integer, SparseVector> confusionMatrix;


		confusionMatrix = new TreeMap<Integer, SparseVector>();
		double[] sp0e = {0,1,100};
		SparseVector sp0 = new SparseVector(sp0e);

		double[] sp1e = {100,0,1};
		SparseVector sp1 = new SparseVector(sp1e);

		double[] sp2e = {1,100,0};
		SparseVector sp2 = new SparseVector(sp2e);

		confusionMatrix.put(0, sp0);
		confusionMatrix.put(1, sp1);
		confusionMatrix.put(2, sp2);

		nmi = new NMI();
		nmi.setConfusionMatrix(confusionMatrix);

		double actual = nmi.postProcess(0.0);
		double expected = 0.95;
		assertEquals(expected, actual, 0.01);
	}

	/***
	 * Test the postProcess function with a 0-column in Confusion Matrix
	 */
	@Test
	public void testPostProcessWithAlmostAllToOne() {
		NMI nmi;
		Map<Integer, SparseVector> confusionMatrix;


		confusionMatrix = new TreeMap<Integer, SparseVector>();
		double[] sp0e = {100,0,0};
		SparseVector sp0 = new SparseVector(sp0e);

		double[] sp1e = {0,100,0};
		SparseVector sp1 = new SparseVector(sp1e);

		double[] sp2e = {100,0,0};
		SparseVector sp2 = new SparseVector(sp2e);

		confusionMatrix.put(0, sp0);
		confusionMatrix.put(1, sp1);
		confusionMatrix.put(2, sp2);

		nmi = new NMI();
		nmi.setConfusionMatrix(confusionMatrix);

		double actual = nmi.postProcess(0.0);
		double expected = 0.75;
		assertEquals(expected, actual, 0.05);
	}

}
