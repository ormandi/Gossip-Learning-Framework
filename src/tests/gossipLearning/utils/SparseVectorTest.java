package tests.gossipLearning.utils;

import gossipLearning.utils.SparseVector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class SparseVectorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -3392393660556646822L;
  
  public void testConstruct() {
    SparseVector v0 = new SparseVector();
    v0.put(1, 2.0);
    v0.put(3, 0.3);
    SparseVector v1 = new SparseVector(new double[]{0, 2, 0, 0.3});
    assertEquals(v0, v1);
    Map<Integer, Double> map = new TreeMap<Integer, Double>();
    map.put(3, 0.3);
    map.put(1, 2.0);
    SparseVector v2 = new SparseVector(map);
    assertEquals(v0, v2);
    map = new HashMap<Integer, Double>();
    map.put(1, 2.0);
    map.put(3, 0.3);
    v2 = new SparseVector(map);
    assertEquals(v0, v2);
    SparseVector v3 = new SparseVector(v0);
    assertEquals(v0, v3);
    int[] indices = new int[]{2, 5, 6, 8};
    double[] values = new double[]{0.5, 2.3, 0, 1.2};
    SparseVector v4 = new SparseVector(indices, values);
    assertEquals(v4, new SparseVector(new double[]{0, 0, 0.5, 0, 0, 2.3, 0, 0, 1.2}));
  }
  
  public void testClone() {
    SparseVector vector = new SparseVector();
    vector.put(1, 1.0);
    vector.put(2, 1.2);
    SparseVector vectorClone = (SparseVector)vector.clone();
    assertEquals(vector.toString(), vectorClone.toString());
    vector.remove(10);
    assertEquals(vector.toString(), vectorClone.toString());
    vector.remove(2);
    assertFalse(vector.toString().equals(vectorClone.toString()));
  }
  
  public void testEquals() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v3 = new SparseVector(new double[]{1,0,0,2,0,0,0,9});
    assertEquals(v1, v2);
    assertFalse(v1.equals(v3));
  }
  
  public void testGet() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.get(3), 2.0);
    assertEquals(v1.get(Integer.MAX_VALUE), 0.0);
  }
  
  public void testPut() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1,0,0,2,0,0,8});
    SparseVector v3 = new SparseVector(new double[]{1,0.2,0,2,0,0,8});
    SparseVector v4 = new SparseVector(new double[]{10,0.2,0,2,0,0,8});
    SparseVector v5 = new SparseVector(new double[]{0,0.2,0,2,0,0,8});
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
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v3 = new SparseVector(new double[]{0,0,0,2});
    v1.remove(1);
    assertEquals(v1, v2);
    v1.remove(0);
    assertEquals(v1, v3);
  }
  
  public void testAdd() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{0.1,3,0,2,0,1.1});
    SparseVector v3 = new SparseVector(new double[]{1.1,3,0,4,0,1.1});
    assertEquals(v1.add(v2), v3);
    v1 = new SparseVector(new double[]{1.0,0,0,1.0});
    v2 = new SparseVector(new double[]{0,1.0,1.0,0});
    v3 = new SparseVector(new double[]{1.0,1.0,1.0,1.0,0,});
    assertEquals(v1.add(v2), v3);
  }
  
  public void testAdd2() {
    SparseVector v2 = new SparseVector(new double[]{0.1,3,0,2,0,1.1});
    SparseVector v4 = new SparseVector(new double[]{1.2,6,0,6,0,2.2});
    SparseVector v5 = new SparseVector(new double[]{0,0,0,2});
    SparseVector v6 = new SparseVector(new double[]{-0.5});
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v2, 2.0), v4);
    v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v6, 2.0), v5);
  }
  
  public void testAdd3() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1,0,1.5,2});
    assertEquals(v1.add(2, 1.5), v2);
    SparseVector v3 = new SparseVector(new double[]{1,0,1.5});
    assertEquals(v1.add(3, -2), v3);
    assertEquals(v1.add(8, 0), v3);
  }
  
  public void testMul() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1,2,3,4});
    assertEquals(v1.mul(v2), 9.0);
  }
  
  public void testMul2() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    SparseVector v2 = new SparseVector(new double[]{1.8,0,0,3.6});
    SparseVector v3 = new SparseVector(new double[]{});
    assertEquals(v1.mul(1.8), v2);
    assertEquals(v1.mul(0.0), v3);
  }
  
  public void testPointMul() {
    SparseVector v1 = new SparseVector(new double[]{1,0,2,2});
    SparseVector v2 = new SparseVector(new double[]{2,0,0,5,4});
    SparseVector v3 = new SparseVector(new double[]{2,0,0,10,0});
    assertEquals(v1.pointMul(v2), v3);
  }
  
  public void testDiv() {
    SparseVector v1 = new SparseVector(new double[]{1,0,2,2});
    SparseVector v2 = new SparseVector(new double[]{2,0,0,5,4});
    SparseVector v3 = new SparseVector(new double[]{0.5,0,0,0.4,0});
    assertEquals(v1.div(v2), v3);
    v1 = new SparseVector(new double[]{1,0,0,2});
    v2 = new SparseVector(new double[]{1,0,0,2,0});
    v3 = new SparseVector(new double[]{1,0,0.2,2,9.3});
    SparseVector vr = new SparseVector(new double[]{1,0,0,1});
    assertEquals(v1.div(v1), vr);
    v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.div(v2), vr);
    v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.div(v3), vr);
  }
  
  public void testIndexAt() {
    SparseVector v1 = new SparseVector(new double[]{1,0,3.56,2});
    assertEquals(v1.indexAt(0), 0);
    assertEquals(v1.indexAt(2), 3);
  }
  
  public void testValueAt() {
    SparseVector v1 = new SparseVector(new double[]{1,0,3.56,2});
    assertEquals(v1.valueAt(0), 1.0);
    assertEquals(v1.valueAt(1), 3.56);
  }
  
  public void testSize() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.size(), 2);
    assertEquals(v1.add(0, -1.0).size(), 1);
  }
  
  public void testClear() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.size(), 2);
    assertEquals(v1.clear().size(), 0);
    assertEquals(v1, new SparseVector());
  }
  
  public void testNorm() {
    SparseVector v1 = new SparseVector(new double[]{3,0,0,4});
    assertEquals(v1.norm(), 5.0);
  }
  
  public void testMaxIndex() {
    SparseVector v1 = new SparseVector(new double[]{3,0,0,4,0});
    assertEquals(v1.maxIndex(), 3);
  }
  
  public void testSqrt() {
    SparseVector v1 = new SparseVector(new double[]{9,0,0,4,0});
    SparseVector vr = new SparseVector(new double[]{3,0,0,2,0});
    assertEquals(v1.sqrt(), vr);
  }
  
  public void testInv() {
    SparseVector v1 = new SparseVector(new double[]{2,0,0,4,0});
    SparseVector vr = new SparseVector(new double[]{0.5,0,0,0.25,0});
    assertEquals(v1.inv(), vr);
  }
  
  public void testSum() {
    SparseVector v = new SparseVector(new double[]{0.5,0,0,0.25,0});
    assertEquals(v.sum(), 0.75);
  }
  
  public void testCosSim() {
    SparseVector v = new SparseVector(new double[]{1.0,0,0,1.0});
    SparseVector v2 = new SparseVector(new double[]{1.0,0,0,1.0});
    assertEquals(1.0, v.cosSim(v2), 1E-5);
    v2 = new SparseVector(new double[]{0.0,1.0,1.0,0.0});
    assertEquals(0.0, v.cosSim(v2), 1E-5);
    v2 = new SparseVector(new double[]{-1.0,0,0,-1.0});
    assertEquals(-1.0, v.cosSim(v2), 1E-5);
  }
  
  public void testEuclideanDistance() {
    SparseVector v = new SparseVector(new double[]{1.0,0,0,1.0});
    SparseVector v2 = new SparseVector(new double[]{1.0,0,0,1.0});
    assertEquals(0.0, v.euclideanDistance(v2), 1E-5);
    v2 = new SparseVector(new double[]{0.0,1.0,1.0,0.0});
    assertEquals(2.0, v.euclideanDistance(v2), 1E-5);
    v2 = new SparseVector(new double[]{-1.0,0,0,-1.0});
    assertEquals(Math.sqrt(8), v.euclideanDistance(v2), 1E-5);
  }

}
