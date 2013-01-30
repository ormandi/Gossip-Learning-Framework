package gossipLearning.utils;

import java.io.Serializable;

import peersim.core.Node;

public class NodeDescriptor implements Serializable, Comparable<NodeDescriptor> {
  private static final long serialVersionUID = -8582247148380060765L;
  
  private final Node node;
  private final SparseVector descriptor;
  private double similarity;
  
  public NodeDescriptor(Node node, SparseVector descriptor) {
    this.node = node;
    this.descriptor = descriptor;
  }
  
  protected NodeDescriptor(NodeDescriptor a) {
    node = (Node)a.node.clone();
    descriptor = (SparseVector)a.descriptor.clone();
    similarity = a.similarity;
  }
  
  @Override
  public Object clone() {
    return new NodeDescriptor(this);
  }
  
  public Node getNode() {
    return node;
  }
  
   public SparseVector getDescriptor() {
     return descriptor;
   }
  
  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }
  
  public double getSimilarity() {
    return similarity;
  }

  @Override
  public int compareTo(NodeDescriptor a) {
    if (similarity < a.similarity) {
      return -1;
    } else if (similarity > a.similarity) {
      return 1;
    }
    return 0;
  }
  
  public double computeSimilarity(NodeDescriptor a) {
    return descriptor.cosSim(a.descriptor);
  }
  
}
