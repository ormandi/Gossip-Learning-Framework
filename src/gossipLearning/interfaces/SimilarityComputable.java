package gossipLearning.interfaces;

/**
 * This interface describes the models that have an extra property, 
 * namely in these case a model can computes the similarity between 
 * itself and an other model.
 * @author Istvan
 *
 * @param <T> the type of the model that can compute the similarity.
 */
public interface SimilarityComputable<T extends Model> {
  
  /**
   * Returns the similarity between the actual and the specified models.
   * @param model - model for computing the similarity
   * @return similarity
   */
  public double computeSimilarity(final T model);
}
