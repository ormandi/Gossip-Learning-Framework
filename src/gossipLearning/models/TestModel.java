package gossipLearning.models;

import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class TestModel implements Model {
  private static final long serialVersionUID = -3322059351428305126L;
  private double age;
  private long time;
  private SparseVector seenPeers;
  
  private static long counter = 0;
  private long id = 0;
  
  public TestModel(String prefix) {
    age = 0.0;
    time = -1;
    seenPeers = new SparseVector();
    id = counter;
    counter ++;
  }
  
  public TestModel(TestModel a) {
    age = a.age;
    seenPeers = a.seenPeers.clone();
    id = a.id;
    time = a.time;
  }
  
  public TestModel clone() {
    return new TestModel(this);
  }

  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public void setAge(double age) {
    this.age = age;
  }
  
  @Override
  public void clear() {
    age = 0.0;
    time = -1;
    seenPeers.clear();
  }
  
  public double lastUpdate() {
    return time;
  }
  
  public SparseVector getVector() {
    return seenPeers;
  }
  
  public void update(int peerIdx, long time) {
    age ++;
    this.time = time;
    seenPeers.add(peerIdx, 1.0);
  }
  
  public TestModel setId() {
    id = counter;
    counter ++;
    return this;
  }
  
  public long getId() {
    return id;
  }
  
  public String toString() {
    double min = age;
    double max = 0.0;
    double avg = seenPeers.sum() / seenPeers.size();
    for (VectorEntry e : seenPeers) {
      if (e.value < min) {
        min = e.value;
      }
      if (max < e.value) {
        max = e.value;
      }
    }
    return id + ": (" + min + ", " + max + ", " + avg + "; " + age + ", " + seenPeers.size() + ") " + seenPeers;
  }

}
