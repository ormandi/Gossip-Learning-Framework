package gossipLearning.models.bandits;

import gossipLearning.interfaces.Model;

public interface BanditModel extends Model {
  public double predict(int armIdx);
  public long numberOfPlayes(int armIdx);
  public long numberOfAllPlayes();
}
