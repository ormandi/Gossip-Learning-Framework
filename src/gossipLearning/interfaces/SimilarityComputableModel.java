package gossipLearning.interfaces;

public interface SimilarityComputableModel<I> extends Model<I> {
  public double similarity(SimilarityComputableModel<I> a);
}
