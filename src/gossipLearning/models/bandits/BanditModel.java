package gossipLearning.models.bandits;

import gossipLearning.interfaces.Model;

public interface BanditModel extends Model {
  public double predict(int armIdx);
  public int numberOfPlayes(int armIdx);
  public long numberOfAllPlayes();
}
