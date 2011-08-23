package tests.gossipLearning.utils;

import gossipLearning.utils.PoissonRandom;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class PoissonRandomTest extends TestCase {
  /**
   * It initializes the test variable
   */
  private static final Map<Integer, Integer> f1 = new TreeMap<Integer, Integer>();
  private static final double n = 1000.0;
  private static final double eps = 1.0E-10;
  static {
    PoissonRandom r = new PoissonRandom(1.0, 123456789);
    for (int i = 0; i < n; i ++) {
      int k = (int) r.nextPoisson(); 
      if (!f1.containsKey(k)) {
        f1.put(k, 1);
      } else {
        f1.put(k, f1.get(k) + 1);
      }
    }
    for (int k : f1.keySet()) {
      System.out.println(k + "\t" + f1.get(k) / n);
    }
  }
  
  /**
   * It tests the generation
   */
  public void testGeneration0() {
    assertEquals(0.367, f1.get(0) / n, eps);
  }
  public void testGeneration1() {
    assertEquals(0.382, f1.get(1) / n, eps);
  }
  public void testGeneration2() {
    assertEquals(0.182, f1.get(2) / n, eps);
  }
  public void testGeneration3() {
    assertEquals(0.058, f1.get(3) / n, eps);
  }
  public void testGeneration4() {
    assertEquals(0.008, f1.get(4) / n, eps);
  }
  public void testGeneration5() {
    assertEquals(0.003, f1.get(5) / n, eps);
  }

}
