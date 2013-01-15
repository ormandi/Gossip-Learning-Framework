package tests.gossipLearning.controls.observers.errorComputation;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import gossipLearning.controls.observers.errorComputation.Purity;
import gossipLearning.utils.SparseVector;

import org.junit.Before;
import org.junit.Test;
/***
 * This class is an extensive test case of our Purity implementation.
 * 
 * @author arphead
 *
 */
public class PuritiyTest {

	@Before
	public void setUp() {
	}

	/***
	 * Test the postProcess function with 0 diagonal Confusion Matrix
	 */
	@Test
	public void testPostProcessWithZeroDiag() {
		Purity puri;
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

		puri = new Purity();
		puri.setConfusionMatrix(confusionMatrix);

		double actual = puri.postProcess(0.0);
		double expected = 1.0;
		assertEquals(expected, actual, 0.01);
	}

	/***
	 * Test the postProcess function with a 0-column in Confusion Matrix
	 */
	@Test
	public void testPostProcessWithAlmostAllToOne() {
		Purity puri;
		Map<Integer, SparseVector> confusionMatrix;

		confusionMatrix = new TreeMap<Integer, SparseVector>();
		double[] sp0e = {100,1,0};
		SparseVector sp0 = new SparseVector(sp0e);

		double[] sp1e = {100,1,0};
		SparseVector sp1 = new SparseVector(sp1e);

		double[] sp2e = {100,1,0};
		SparseVector sp2 = new SparseVector(sp2e);

		confusionMatrix.put(0, sp0);
		confusionMatrix.put(1, sp1);
		confusionMatrix.put(2, sp2);

		puri = new Purity();
		puri.setConfusionMatrix(confusionMatrix);

		double actual = puri.postProcess(0.0);
		double expected = 1.0;
		assertEquals(expected, actual, 0.01);
	}

	/***
	 * Test the postProcess function with a referenced example
	 * @see <a href="http://www-nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">example</a>
	 */
	@Test
	public void testPostProcessWithSample() {
		Purity puri;
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

		puri = new Purity();
		puri.setConfusionMatrix(confusionMatrix);

		double actual = puri.postProcess(0.0);
		double expected = 0.706;
		assertEquals(expected, actual, 0.01);
	}



}
