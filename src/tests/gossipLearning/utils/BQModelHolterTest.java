package tests.gossipLearning.utils;

import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.BQModelHolder;

import java.io.Serializable;

import junit.framework.TestCase;

public class BQModelHolterTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -1166834820815174860L;
  
  public void testConstructCloneSize() {
    BQModelHolder holder = new BQModelHolder(3);
    assertEquals(holder.size(), 0);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    assertEquals(holder.toString(), "1\t3\t[1]");
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    assertEquals(holder.toString(), "2\t3\t[1, 2]");
    BQModelHolder holderc = (BQModelHolder)holder.clone();
    assertEquals(holderc, holder);
    holderc.add(m1);
    holderc.add(m2);
    assertEquals(holderc.size(), 3);
    holderc.removeFirst();
    assertEquals(holderc, holder);
    assertEquals(holderc.size(), 2);
  }
  
  public void testAdd() {
    BQModelHolder holder = new BQModelHolder(3);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    assertEquals(holder.toString(), "1\t3\t[1]");
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    assertEquals(holder.toString(), "2\t3\t[1, 2]");
    DummyModel m3 = new DummyModel(3);
    holder.add(m3);
    assertEquals(holder.toString(), "3\t3\t[1, 2, 3]");
    DummyModel m4 = new DummyModel(4);
    holder.add(m4);
    assertEquals(holder.toString(), "3\t3\t[2, 3, 4]");
  }
  
  public void testRemove() {
    BQModelHolder holder = new BQModelHolder(3);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    DummyModel m3 = new DummyModel(3);
    holder.add(m3);
    DummyModel m4 = new DummyModel(4);
    holder.add(m4);
    DummyModel r = (DummyModel)holder.removeFirst();
    assertEquals(holder.toString(), "2\t3\t[3, 4]");
    assertEquals(r, m2);
    holder.add(m1);
    assertEquals(holder.toString(), "3\t3\t[3, 4, 1]");
    r = (DummyModel)holder.remove(1);
    assertEquals(holder.toString(), "2\t3\t[3, 1]");
    assertEquals(r, m4);
    holder.remove(1);
    assertEquals(holder.toString(), "1\t3\t[3]");
    holder.remove(0);
    assertEquals(holder.toString(), "0\t3\t[]");
  }
  
  public void testGet() {
    BQModelHolder holder = new BQModelHolder(3);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    DummyModel m3 = new DummyModel(3);
    holder.add(m3);
    DummyModel r = (DummyModel)holder.getModel(1);
    assertEquals(r, m2);
    r = (DummyModel)holder.getModel(0);
    assertEquals(r, m1);
    r = (DummyModel)holder.getModel(2);
    assertEquals(r, m3);
  }
  
  public void testSet() {
    BQModelHolder holder = new BQModelHolder(3);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    DummyModel m3 = new DummyModel(3);
    holder.add(m3);
    DummyModel m4 = new DummyModel(4);
    holder.setModel(1, m4);
    assertEquals(holder.toString(), "3\t3\t[1, 4, 3]");
    holder.setModel(1, m2);
    holder.setModel(0, m4);
    assertEquals(holder.toString(), "3\t3\t[4, 2, 3]");
    holder.setModel(0, m1);
    holder.setModel(2, m4);
    assertEquals(holder.toString(), "3\t3\t[1, 2, 4]");
    holder.setModel(2, m3);
    assertEquals(holder.toString(), "3\t3\t[1, 2, 3]");
  }
  
  public void testClear() {
    BQModelHolder holder = new BQModelHolder(3);
    DummyModel m1 = new DummyModel(1);
    holder.add(m1);
    DummyModel m2 = new DummyModel(2);
    holder.add(m2);
    DummyModel m3 = new DummyModel(3);
    holder.add(m3);
    BQModelHolder h = new BQModelHolder(3);
    assertFalse(holder.equals(h));
    holder.clear();
    assertEquals(holder, h);
  }

}

class DummyModel implements Model {
  private static final long serialVersionUID = -5350358220086409657L;
  
  private final int data;
  public DummyModel(int data) {
    this.data = data;
  }
  
  public DummyModel(DummyModel a) {
    data = a.data;
  }
  
  public Object clone() {
    return new DummyModel(this);
  }
  
  public boolean equals(Object o) {
    if (o instanceof DummyModel) {
      DummyModel m = (DummyModel)o;
      if (data == m.data) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void init(String prefix) {
  }
  
  public String toString() {
    return "" + data;
  }
  
  @Override
  public double getAge() {
    return 0.0;
  }
  
}