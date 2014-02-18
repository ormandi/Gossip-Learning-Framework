package tests.gossipLearning.utils;

import gossipLearning.utils.Matrix;
import gossipLearning.utils.Utils;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

public class UtilsTest extends TestCase {
  private static final double EPS = 1E-5;
  
  public void testRegression() {
    double[] array = new double[]{1, 2, 3, 4, 5};
    double[] exp = new double[]{5.0, 0.0};
    double[] res = Utils.regression(array);
    assertEquals(exp[0], res[0]);
    assertEquals(exp[1], res[1]);
    array = new double[]{0.1, 0.2, 0.3, 0.4, 0.5};
    exp = new double[]{0.5, 0.0};
    res = Utils.regression(array);
    assertEquals(exp[0], res[0]);
    assertEquals(exp[1], res[1]);
    array = new double[]{1, 1, 1, 1, 1};
    exp = new double[]{0.0, 1.0};
    res = Utils.regression(array);
    assertEquals(exp[0], res[0]);
    assertEquals(exp[1], res[1]);
    array = new double[]{0, 0, 0, 0, 0};
    exp = new double[]{0.0, 0.0};
    res = Utils.regression(array);
    assertEquals(exp[0], res[0]);
    assertEquals(exp[1], res[1]);
  }
  
  public void testIsPower2() {
    assertTrue(Utils.isPower2(1.0));
    assertTrue(Utils.isPower2(2.0));
    assertTrue(Utils.isPower2(1024.0));
    assertTrue(Utils.isPower2(0.5));
    assertFalse(Utils.isPower2(3.0));
  }
  
  public void testCDF() {
    assertEquals(0.5, Utils.cdf(0, 0, 1.0), EPS);
    assertEquals(0.64531, Utils.cdf(0.42019, 0.23694, 0.49170), EPS);
    assertEquals(Double.NaN, Utils.cdf(0, 0, 0));
    assertEquals(1.0 - Utils.cdf(1, 0, 1), Utils.cdf(-1, 0, 1));
    
  }
  
  public void testERF() {
    assertEquals(0.0, Utils.erf(0), EPS);
    assertEquals(0.84270, Utils.erf(1), EPS);
    assertEquals(-0.84270, Utils.erf(-1), EPS);
    assertEquals(1, Utils.erf(10), EPS);
  }
  
  public void testNormalize() {
    double[] vector = new double[]{0, 0, 0, 0, 0};
    double[] exp = new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
    assertTrue(Arrays.equals(exp, Utils.normalize(vector)));
    vector = new double[]{0, 0, 0, 0, 1};
    assertTrue(Arrays.equals(vector, Utils.normalize(vector)));
  }
  
  public void testDFFT() {
    double[] vector = new double[]{1, 0, 1, 0, 1, 0, 1, 0};
    double[] exp = new double[]{4, 0, 0, 0, 0, 0, 0, 0};
    assertTrue(Arrays.equals(exp, Utils.dfft(vector)));
    
    vector = new double[]{1, 0, 0, 0, 1, 0, 0, 0};
    exp = new double[]{2, 0, 0, 0, 2, 0, 0, 0};
    assertTrue(Arrays.equals(exp, Utils.dfft(vector)));
    
    vector = new double[]{0, 0, 1, 0, 0, 0, 1, 0};
    exp = new double[]{2, 0, 0, 0, -2, 0, 0, 0};
    assertTrue(Arrays.equals(exp, Utils.dfft(vector)));
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 1, 0};
    exp = new double[]{2, 0, 1, 1, 0, 0, 1, -1};
    double[] result = Utils.dfft(vector);
    for (int i = 0; i < exp.length; i++) {
      assertEquals(exp[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 0, 0};
    exp = new double[]{1, 0, 1, 0, 1, 0, 1, 0};
    assertTrue(Arrays.equals(exp, Utils.dfft(vector)));
    
    vector = new double[]{0, 0, 0, 0, 0, 0, 1, 0};
    exp = new double[]{1, 0, 0, 1, -1, 0, 0, -1};
    result = Utils.dfft(vector);
    for (int i = 0; i < exp.length; i++) {
      assertEquals(exp[i], result[i], EPS);
    }
  }
  
  public void testIDFFT() {
    double[] vector = new double[]{1, 0, 1, 0, 1, 0, 1, 0};
    double[] result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 1, 0, 0, 0};
    result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{0, 0, 1, 0, 0, 0, 1, 0};
    result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 1, 0};
    result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 0, 0};
    result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{0, 0, 0, 0, 0, 0, 1, 0};
    result = Utils.idfft(Utils.dfft(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
  }
  
  public void testWavelet() {
    double[] vector = new double[]{1, 0, 1, 0, 1, 0, 1, 0};
    double[] result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 1, 0, 0, 0};
    result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{0, 0, 1, 0, 0, 0, 1, 0};
    result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 1, 0};
    result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{1, 0, 0, 0, 0, 0, 0, 0};
    result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
    
    vector = new double[]{0, 0, 0, 0, 0, 0, 1, 0};
    result = Utils.idhwt(Utils.dhwt(vector));
    for (int i = 0; i < vector.length; i++) {
      assertEquals(vector[i], result[i], EPS);
    }
  }
  
  public void testButterflyRandomOrthogonalMatrix() {
    int n = 1;
    int rank = 1;
    Random r = new Random(1234567890);
    Matrix M = Utils.butterflyRandomOrthogonalMatrix(n, rank, r);
    Matrix MT = new Matrix(M).transpose();
    Matrix MTM = MT.mul(M);
    double sum = 0.0;
    for (int i = 0; i < rank; i++) {
      for (int j = 0; j < rank; j++) {
        sum += MTM.get(i, j);
      }
    }
    assertEquals(rank, sum, EPS);
    n = 32;
    rank = 32;
    M = Utils.butterflyRandomOrthogonalMatrix(n, rank, r);
    MT = new Matrix(M).transpose();
    MTM = MT.mul(M);
    sum = 0.0;
    for (int i = 0; i < rank; i++) {
      for (int j = 0; j < rank; j++) {
        sum += MTM.get(i, j);
      }
    }
    assertEquals(rank, sum, EPS);
    n = 32;
    rank = 10;
    M = Utils.butterflyRandomOrthogonalMatrix(n, rank, r);
    MT = new Matrix(M).transpose();
    MTM = MT.mul(M);
    sum = 0.0;
    for (int i = 0; i < rank; i++) {
      for (int j = 0; j < rank; j++) {
        sum += MTM.get(i, j);
      }
    }
    assertEquals(rank, sum, EPS);
  }
  @Test (expected=RuntimeException.class) public void butterflyMatrixPower() {
    Utils.butterflyRandomOrthogonalMatrix(10, 1, null);
  }
  @Test (expected=RuntimeException.class) public void butterflyMatrixGreater() {
    Utils.butterflyRandomOrthogonalMatrix(1, 10, null);
  }
}
