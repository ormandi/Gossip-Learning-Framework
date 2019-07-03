package gossipLearning.interfaces.models;

import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

/**
 * This interface can be used for those models, that used for some kind of 
 * matrix factorization. Where the matrix can be reproduced as M=U*V. In this 
 * case the model is the V(kXn) matrix, and the update process is based on a row 
 * of the M(mXn) and the corresponding row of the U(mXk) matrices.
 * @author István Hegedűs
 */
public interface MatrixBasedModel extends Model {
  /**
   * Updates the current model based on the specified arguments (for optimizing 
   * M=U*V).
   * @param rowModel the (ith) row of the matrix U
   * @param instance the (ith) row of the matrix M
   * @return the updated rowModel (the ith row of the U)
   */
  public double[] update(double[] rowModel, SparseVector instance);
  /**
   * Predicts the m_ij element of the M matrix (the optimization problem is: 
   * M=U*V).
   * @param rowModel the (ith) row of U
   * @param columnIndex the index (j) of the column of m_ij
   * @return the predicted value of m_ij
   */
  public double predict(double[] rowModel, int columnIndex);
  
  public MatrixBasedModel clone();
  
  public Matrix getV();
  public Matrix getUSi(double[] ui);
}
