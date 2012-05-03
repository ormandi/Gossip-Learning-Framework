package tests.gossipLearning.utils;

import gossipLearning.utils.SparseVector;

import java.io.Serializable;

import junit.framework.TestCase;

public class ArraySparseVectorTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -3392393660556646822L;
  
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
    assertEquals(v1, v2);
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
  
  public void testGet() {
    SparseVector v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.get(-1), 0.0);
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
    SparseVector v4 = new SparseVector(new double[]{1.2,6,0,6,0,2.2});
    SparseVector v5 = new SparseVector(new double[]{0,0,0,2});
    SparseVector v6 = new SparseVector(new double[]{-0.5});
    assertEquals(v1.add(v2), v3);
    v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v2, 2.0), v4);
    v1 = new SparseVector(new double[]{1,0,0,2});
    assertEquals(v1.add(v6, 2.0), v5);
  }

}
