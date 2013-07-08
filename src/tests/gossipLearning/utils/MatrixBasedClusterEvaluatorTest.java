package tests.gossipLearning.utils;

import gossipLearning.evaluators.MatrixBasedClusterEvaluator;
import gossipLearning.evaluators.MatrixBasedEvaluator;
import gossipLearning.utils.Utils;

import java.io.Serializable;

import junit.framework.TestCase;

public class MatrixBasedClusterEvaluatorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -8206775892528055090L;
  
  private MatrixBasedClusterEvaluator evaluator;
  
  public void testClone() {
    evaluator = new MatrixBasedClusterEvaluator();
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
    assertEquals(evaluator, (MatrixBasedClusterEvaluator)evaluator.clone());
  }
  
  public void testMatrix() {
    evaluator = new MatrixBasedClusterEvaluator();
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
    String expected = "e\\p\t0\t1\n------------------------\n0:\t10\t5\n1:\t2\t20\n";
    assertEquals(expected, evaluator.toString());
  }
  
  public void testAggregation() {
    evaluator = new MatrixBasedClusterEvaluator();
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
    MatrixBasedEvaluator me = (MatrixBasedClusterEvaluator)evaluator.clone();
    String expected = "e\\p\t0\t1\n------------------------\n0:\t20\t10\n1:\t4\t40\n";
    evaluator.merge(me);
    assertEquals(expected, evaluator.toString());
  }
  
  public void testResults() {
    evaluator = new MatrixBasedClusterEvaluator();
    for (int i = 0; i < 5; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    for (int i = 0; i < 1; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(0.0, 2.0);
    }
    for (int i = 0; i < 1; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 4; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    for (int i = 0; i < 1; i++) {
      evaluator.evaluate(2.0, 1.0);
    }
    for (int i = 0; i < 3; i++) {
      evaluator.evaluate(2.0, 2.0);
    }
    MatrixBasedClusterEvaluator ev = new MatrixBasedClusterEvaluator();
    ev.merge(evaluator);
    double[] result = ev.getResults();
    assertEquals(result[0], 0.7058823529411765, Utils.EPS);
    assertEquals(result[1], 0.3645617718571898, Utils.EPS);
    assertEquals(result[2], 0.7058823529411765, Utils.EPS);
  }
  
  public void testZeros() {
    evaluator = new MatrixBasedClusterEvaluator();
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(1.0, 0.0);
    }
    for (int i = 0; i < 15; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    MatrixBasedClusterEvaluator ev = new MatrixBasedClusterEvaluator();
    ev.merge(evaluator);
    double[] result = ev.getResults();
    assertEquals(result[0], 0.8823529411764706, Utils.EPS);
    assertEquals(result[1], 0.0, Utils.EPS);
    assertEquals(result[2], 0.8823529411764706, Utils.EPS);
    evaluator = new MatrixBasedClusterEvaluator();
    for (int i = 0; i < 2; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    for (int i = 0; i < 15; i++) {
      evaluator.evaluate(1.0, 1.0);
    }
    ev = new MatrixBasedClusterEvaluator();
    ev.merge(evaluator);
    result = ev.getResults();
    assertEquals(result[0], 1.0, Utils.EPS);
    assertEquals(result[1], 0.0, Utils.EPS);
    assertEquals(result[2], 0.8823529411764706, Utils.EPS);
  }
  
}
