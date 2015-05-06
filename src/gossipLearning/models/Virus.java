package gossipLearning.models;

import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.SparseVector;

public class Virus implements Model {
  private static final long serialVersionUID = 3929739149066767906L;
  
  protected SparseVector seenPeers;
  protected double age;
  protected boolean isInfected;
  
  public Virus(String prefix) {
    seenPeers = new SparseVector();
    age = 0.0;
    isInfected = false;
  }
  
  public Virus(Virus a) {
    seenPeers = new SparseVector(a.seenPeers);
    age = a.age;
    isInfected = a.isInfected;
  }
  
  @Override
  public Object clone() {
    return new Virus(this);
  }

  @Override
  public double getAge() {
    return age;
  }

  public boolean isInfecter() {
    return isInfected;
  }
  
  public void setInfected() {
    isInfected = true;
  }

  public void update(int rowIndex) {
    seenPeers.add(rowIndex, 1.0);
    age ++;
  }
  
  public SparseVector getVector() {
    return seenPeers;
  }
  
  public void reset() {
    isInfected = false;
    seenPeers.clear();
    age = 0.0;
  }

}
