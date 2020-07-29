package tests.gossipLearning.utils;

import java.io.Serializable;

import gossipLearning.utils.DenseVector;
import junit.framework.TestCase;

public class DenseVectorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -3392393660556646822L;
  
  public void testConstruct() {
    DenseVector v0 = new DenseVector();
    v0.put(1, 2.0);
    v0.put(3, 0.3);
    DenseVector v1 = new DenseVector(new double[]{0, 2, 0, 0.3});
    assertEquals(v0, v1);
    DenseVector v3 = new DenseVector(v0);
    assertEquals(v0, v3);
  }
  
  public void testClone() {
    DenseVector vector = new DenseVector();
    vector.put(1, 1.0);
    vector.put(2, 1.2);
    DenseVector vectorClone = (DenseVector)vector.clone();
    assertEquals(vector.toString(), vectorClone.toString());
    vector.remove(10);
    assertEquals(vector.toString(), vectorClone.toString());
    vector.remove(2);
    assertFalse(vector.toString().equals(vectorClone.toString()));
  }
  
  public void testEquals() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v3 = new DenseVector(new double[]{1,0,0,2,0,0,0,9});
    assertEquals(v1, v2);
    assertFalse(v1.equals(v3));
  }
  
  public void testSet() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1,0,0,2,0,0,0,9});
    assertFalse(v1.equals(v2));
    v1.set(v2);
    assertEquals(v1, v2);
  }
  
  public void testGet() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.get(3), 2.0);
    assertEquals(v1.get(Integer.MAX_VALUE), 0.0);
  }
  
  public void testPut() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1,0,0,2,0,0,8});
    DenseVector v3 = new DenseVector(new double[]{1,0.2,0,2,0,0,8});
    DenseVector v4 = new DenseVector(new double[]{10,0.2,0,2,0,0,8});
    DenseVector v5 = new DenseVector(new double[]{0,0.2,0,2,0,0,8});
    v1.put(6, 8.0);
    assertEquals(v1, v2);
    v1.put(1, 0.2);
    assertEquals(v1, v3);
    v1.put(0, 10.0);
    assertEquals(v1, v4);
    v1.put(0, 0.0);
    assertEquals(v1, v5);
  }
  
  public void testRemove() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v3 = new DenseVector(new double[]{0,0,0,2});
    v1.remove(1);
    assertEquals(v1, v2);
    v1.remove(0);
    assertEquals(v1, v3);
  }
  
  public void testAdd() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{0.1,3,0,2,0,1.1});
    DenseVector v3 = new DenseVector(new double[]{1.1,3,0,4,0,1.1});
    assertEquals(v1.add(v2), v3);
    v1 = new DenseVector(new double[]{1.0,0,0,1.0});
    v2 = new DenseVector(new double[]{0,1.0,1.0,0});
    v3 = new DenseVector(new double[]{1.0,1.0,1.0,1.0,0,});
    assertEquals(v1.add(v2), v3);
  }
  
  public void testAdd2() {
    DenseVector v2 = new DenseVector(new double[]{0.1,3,0,2,0,1.1});
    DenseVector v4 = new DenseVector(new double[]{1.2,6,0,6,0,2.2});
    DenseVector v5 = new DenseVector(new double[]{0,0,0,2});
    DenseVector v6 = new DenseVector(new double[]{-0.5});
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v2, 2.0), v4);
    v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v6, 2.0), v5);
  }
  
  public void testAdd3() {
    DenseVector v = new DenseVector(1);
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v.add(0, 1).add(3, 2), v1);
    DenseVector v2 = new DenseVector(new double[]{1,0,1.5,2});
    assertEquals(v1.add(2, 1.5), v2);
    DenseVector v3 = new DenseVector(new double[]{1,0,1.5});
    assertEquals(v1.add(3, -2), v3);
    assertEquals(v1.add(8, 0), v3);
  }
  
  public void testAdd4() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    double[] v2 = new double[]{0.1,3,0,2,0,1.1};
    DenseVector v3 = new DenseVector(new double[]{1.1,3,0,4,0,1.1});
    assertEquals(v1.add(v2), v3);
    v1 = new DenseVector(new double[]{1.0,0,0,1.0});
    v2 = new double[]{0,1.0,1.0,0};
    v3 = new DenseVector(new double[]{1.0,1.0,1.0,1.0,0,});
    assertEquals(v1.add(v2), v3);
  }
  
  public void testAdd5() {
    double[] v2 = new double[]{0.1,3,0,2,0,1.1};
    DenseVector v4 = new DenseVector(new double[]{1.2,6,0,6,0,2.2});
    DenseVector v5 = new DenseVector(new double[]{0,0,0,2});
    double[] v6 = new double[]{-0.5};
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v2, 2.0), v4);
    v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v6, 2.0), v5);
  }
  
  public void testMul() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1,2,3,4});
    assertEquals(v1.mul(v2), 9.0);
  }
  
  public void testMul2() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    DenseVector v2 = new DenseVector(new double[]{1.8,0,0,3.6});
    DenseVector v3 = new DenseVector(new double[]{});
    assertEquals(v1.mul(1.8), v2);
    assertEquals(v1.mul(0.0), v3);
  }
  
  public void testPointMul() {
    DenseVector v1 = new DenseVector(new double[]{1,0,2,2,0,2});
    DenseVector v2 = new DenseVector(new double[]{2,0,0,5,4});
    DenseVector v3 = new DenseVector(new double[]{2,0,0,10,0});
    assertEquals(v1.pointMul(v2), v3);
  }
  
  public void testDiv() {
    DenseVector v1 = new DenseVector(new double[]{1,0,2,2,0,2});
    DenseVector v2 = new DenseVector(new double[]{2,0,0,5,4});
    DenseVector v3 = new DenseVector(new double[]{0.5,0,0,0.4,0});
    assertEquals(v1.div(v2), v3);
    v1 = new DenseVector(new double[]{1,0,0,2});
    v2 = new DenseVector(new double[]{1,0,0,2,0});
    v3 = new DenseVector(new double[]{1,0,0.2,2,9.3});
    DenseVector vr = new DenseVector(new double[]{1,0,0,1});
    assertEquals(v1.div(v1), vr);
    v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.div(v2), vr);
    v1 = new DenseVector(new double[]{1,0,0,2});
    assertEquals(v1.div(v3), vr);
  }
  
  public void testClear() {
    DenseVector v1 = new DenseVector(new double[]{1,0,0,2});
    v1.clear();
    assertEquals(v1, new DenseVector());
  }
  
  public void testNorm2() {
    DenseVector v1 = new DenseVector(new double[]{3,0,0,4});
    assertEquals(v1.norm2(), 5.0);
  }
  
  public void testLength() {
    DenseVector v1 = new DenseVector(new double[]{3,0,0,4,0});
    assertEquals(v1.length(), 5);
  }
  
  public void testSqrt() {
    DenseVector v1 = new DenseVector(new double[]{9,0,0,4,0});
    DenseVector vr = new DenseVector(new double[]{3,0,0,2,0});
    assertEquals(v1.powerTo(0.5), vr);
  }
  
  public void testInvert() {
    DenseVector v1 = new DenseVector(new double[]{2,0,0,4,0});
    DenseVector vr = new DenseVector(new double[]{0.5,0,0,0.25,0});
    assertEquals(v1.invert(), vr);
  }
  
  public void testSum() {
    DenseVector v = new DenseVector(new double[]{0.5,0,0,0.25,0});
    assertEquals(v.sum(), 0.75);
  }
  
  public void testCosSim() {
    DenseVector v = new DenseVector(new double[]{1.0,0,0,1.0});
    DenseVector v2 = new DenseVector(new double[]{1.0,0,0,1.0});
    assertEquals(1.0, v.cosineSimilarity(v2), 1E-5);
    v2 = new DenseVector(new double[]{0.0,1.0,1.0,0.0});
    assertEquals(0.0, v.cosineSimilarity(v2), 1E-5);
    v2 = new DenseVector(new double[]{-1.0,0,0,-1.0});
    assertEquals(-1.0, v.cosineSimilarity(v2), 1E-5);
  }
  
  public void testEuclideanDistance() {
    DenseVector v = new DenseVector(new double[]{1.0,0,0,1.0});
    DenseVector v2 = new DenseVector(new double[]{1.0,0,0,1.0});
    assertEquals(0.0, v.euclideanDistance(v2), 1E-5);
    v2 = new DenseVector(new double[]{0.0,1.0,1.0,0.0});
    assertEquals(2.0, v.euclideanDistance(v2), 1E-5);
    v2 = new DenseVector(new double[]{-1.0,0,0,-1.0});
    assertEquals(Math.sqrt(8), v.euclideanDistance(v2), 1E-5);
  }

}
