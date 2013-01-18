package tests.gossipLearning.utils;

import gossipLearning.evaluators.MatrixBasedEvaluator;
import gossipLearning.utils.Utils;

import java.io.Serializable;

import junit.framework.TestCase;

public class MatrixBasedEvaluatorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = 3365708556641258286L;
  
  private MatrixBasedEvaluator evaluator;
  
  public void testClone() {
    evaluator = new MatrixBasedEvaluator();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    for (int i = 0; i < 5; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 20; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    assertEquals(evaluator, (MatrixBasedEvaluator)evaluator.clone());
  }
  
  public void testMatrix() {
    evaluator = new MatrixBasedEvaluator();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    for (int i = 0; i < 5; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 20; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    String expected = "e\\p\t0\t1\n0\t10\t5\n1\t2\t20\n";
    assertEquals(expected, evaluator.toString());
  }
  
  public void testAggregation() {
    evaluator = new MatrixBasedEvaluator();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    for (int i = 0; i < 5; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 20; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    MatrixBasedEvaluator me = (MatrixBasedEvaluator)evaluator.clone();
    String expected = "e\\p\t0\t1\n0\t20\t10\n1\t4\t40\n";
    evaluator.merge(me);
    assertEquals(expected, evaluator.toString());
  }
  
  public void testResults() {
    evaluator = new MatrixBasedEvaluator();
    for (int i = 0; i < 5; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    for (int i = 0; i < 4; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 15; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    double[] result = evaluator.getResults();
    assertEquals(result[0], 7.6923076923076927347e-01, Utils.EPS);
    assertEquals(result[1], 7.1895424836601307117e-01, Utils.EPS);
    assertEquals(result[2], 7.5187969924812025901e-01, Utils.EPS);
    assertEquals(result[3], 7.3504844637487476433e-01, Utils.EPS);
  }

}
