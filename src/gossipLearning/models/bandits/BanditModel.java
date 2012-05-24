package gossipLearning.models.bandits;

import gossipLearning.interfaces.Model;

public interface BanditModel extends Model {
  public int update();
  public double predict(int armIdx);
  public double numberOfPlayes(int armIdx);
  public double numberOfAllPlayes();
}
