package tests.gossipLearning.utils;

import gossipLearning.evaluators.MAError;
import gossipLearning.evaluators.MSError;
import gossipLearning.evaluators.RMSError;
import gossipLearning.evaluators.ValueBasedEvaluator;
import gossipLearning.evaluators.ZeroOneError;
import gossipLearning.utils.Utils;

import java.io.Serializable;

import junit.framework.TestCase;

public class ValueBasedEvaluatorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -1015020021282237724L;
  
  private ValueBasedEvaluator evaluator;
  
  public void testAggregation() {
    evaluator = new MAError();
    double[] result;
    ValueBasedEvaluator[] evals = new ValueBasedEvaluator[10];
    for (int i = 0; i < evals.length; i++) {
      evals[i] = new MAError();
      for (int j = 0; j < 10; j++) {
        evals[i].evaluate(0.0, i);
      }
      evaluator.merge(evals[i]);
    }
    //System.out.println(evaluator);
    result = evaluator.getResults();
    //System.out.println(Arrays.toString(result));
    assertEquals(result[0], 4.5, Utils.EPS);
    assertEquals(result[1], 2.8722813232690143082, Utils.EPS);
    assertEquals(result[2], 0.0, Utils.EPS);
    assertEquals(result[3], 9.0, Utils.EPS);
  }
  
  public void testMAError() {
    evaluator = new MAError();
    double[] result;
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 0.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 0.0);
    assertEquals(result[3], 0.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 1.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 1.0);
    assertEquals(result[3], 1.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, i);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 4.5, Utils.EPS);
    assertEquals(result[1], 2.8722813232690143082, Utils.EPS);
    assertEquals(result[2], 0.0, Utils.EPS);
    assertEquals(result[3], 9.0, Utils.EPS);
    
    MAError me = (MAError)evaluator.clone();
    assertEquals(evaluator, me);
  }
  
  public void testMSError() {
    evaluator = new MSError();
    double[] result;
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 0.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 0.0);
    assertEquals(result[3], 0.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 1.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 1.0);
    assertEquals(result[3], 1.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, i);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 28.5, Utils.EPS);
    assertEquals(result[1], 2.6852374196707447851e+01, Utils.EPS);
    assertEquals(result[2], 0.0, Utils.EPS);
    assertEquals(result[3], 81.0, Utils.EPS);
    
    MSError me = (MSError)evaluator.clone();
    assertEquals(evaluator, me);
  }
  
  public void testRMSError() {
    evaluator = new RMSError();
    double[] result;
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 0.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 0.0);
    assertEquals(result[3], 0.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 1.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 1.0);
    assertEquals(result[3], 1.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, i);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 5.3385391260156556115, Utils.EPS);
    assertEquals(result[1], 2.6852374196707447851e+01, Utils.EPS);
    assertEquals(result[2], 0.0, Utils.EPS);
    assertEquals(result[3], 81.0, Utils.EPS);
    
    RMSError me = (RMSError)evaluator.clone();
    assertEquals(evaluator, me);
  }
  
  public void testZeroOneError() {
    evaluator = new ZeroOneError();
    double[] result;
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 0.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 0.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 0.0);
    assertEquals(result[3], 0.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, 1.0);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 1.0);
    assertEquals(result[1], 0.0);
    assertEquals(result[2], 1.0);
    assertEquals(result[3], 1.0);
    evaluator.clear();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(0.0, i);
    }
    result = evaluator.getResults();
    assertEquals(result[0], 0.9, Utils.EPS);
    assertEquals(result[1], 0.3, Utils.EPS);
    assertEquals(result[2], 0.0, Utils.EPS);
    assertEquals(result[3], 1.0, Utils.EPS);
    
    ZeroOneError me = (ZeroOneError)evaluator.clone();
    assertEquals(evaluator, me);
  }

}
