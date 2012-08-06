package gossipLearning.models.regression;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.models.kernels.Kernel;
import gossipLearning.models.kernels.RBFKernel;
import gossipLearning.models.losses.Loss;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import peersim.config.Configuration;

public class NormaSVM implements Model {
  private static final long serialVersionUID = 18888123L;
  
  /**
   * Parameter Tau - shrinking constant
   */
  protected static final String PAR_TAU = "normasvm.tau";
  protected static final int DEFAULT_TAU = 100;
  protected int tau = DEFAULT_TAU;
  
  /**
   * Parameter Lambda - regularization constant
   */
  protected static final String PAR_LAMBDA = "normasvm.lambda";
  protected static final double DEFAULT_LAMBDA = 0.0001;
  protected double lambda = DEFAULT_LAMBDA;
  
  /**
   * Parameter Kernel - name of kernel class
   */
  protected static final String PAR_KERNEL = "normasvm.kernel";
  protected static final String DEFAULT_KERNEL = "gossipLearning.models.kernels.LinearKernel";
  protected String kernelClassName = DEFAULT_KERNEL;
  
  /**
   * Parameter Loss - name of the applied loss class
   */
  protected static final String PAR_LOSS = "normasvm.loss";
  protected static final String DEFAULT_LOSS = "gossipLearning.models.losses.SquaredLoss";
  protected String lossClassName = DEFAULT_LOSS;
  
  /**
   * Model
   */
  private double b;
  private BoundedQueue q; 
  
  private Kernel kernel;
  private Loss loss;
  
  private long age;
  
  private int numberOfClasses;
  
  public NormaSVM() {
    try {
      // initialize parameters using their default values
      kernel = (Kernel) Class.forName(kernelClassName).newInstance();
      loss = (Loss) Class.forName(lossClassName).newInstance();
      
      // initialize model
      age = 0;
      q = new BoundedQueue(tau);
      b = 0.0;
    } catch (Exception e) {
      throw new RuntimeException("Error in NormaSVM.defaultConstructor", e);
    }
  }
  
  public NormaSVM(NormaSVM o) {
    // copy parameters
    tau = o.tau;
    lambda = o.lambda;
    kernelClassName = new String(o.kernelClassName);
    lossClassName = new String(o.lossClassName);
    
    // copy model
    b = o.b;
    q = (BoundedQueue) o.q.clone();
    kernel = o.kernel;  // deep copy is not needed
    loss = o.loss;      // deep copy is not needed
    age = o.age;
    numberOfClasses = o.numberOfClasses;
  }
  
  public Object clone() {
    return new NormaSVM(this);
  }
  
  @Override
  public void init(String prefix) {
    try {
      // read parameters
      tau = Configuration.getInt(prefix + "." + PAR_TAU, DEFAULT_TAU);
      lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, DEFAULT_LAMBDA);
      kernelClassName = Configuration.getString(prefix + "." + PAR_KERNEL, DEFAULT_KERNEL);
      kernel = (Kernel) Class.forName(kernelClassName).newInstance();
      lossClassName = Configuration.getString(prefix + "." + PAR_LOSS, DEFAULT_LOSS);
      loss = (Loss) Class.forName(lossClassName).newInstance();
      
      // initialize model
      age = 0;
      q = new BoundedQueue(tau);
      b = 0.0;
      
    } catch (Exception e) {
      throw new RuntimeException("Error in NormaSVM.init", e);
    }
  }

  @Override
  public void update(SparseVector x, double y) {
    // increment age
    ++age;
    
    // compute nu
    final double nu = nu();
    
    // create multiplier
    final double d = (1.0 - nu * lambda);
    
    // provide prediction and gradient of loss
    final double pred = predict(x);
    final double lossGrad = loss.lossGrad(pred, y);
    
    // update elementary model parts
    for (ElementaryModel p : q) {
      p.update(d);
    }
    
    // add new elementary model part
    q.offer(new ElementaryModel(age, x, - nu * lossGrad));
    
    // update bias
    b -= nu * lossGrad;
  }
  
  /**
   * It returns the value of nu depend on the current state of the model.
   * 
   * @return value of nu
   */
  protected double nu() {
    return 1.0/(double) age;
  }

  @Override
  public double predict(SparseVector x) {
    double v = 0;
    
    // sum up model parts
    for (ElementaryModel p : q) {
      v += p.alpha() * kernel.kernel(p.x(), x);
    }
    
    // add bias
    v += b;
    
    return v;
  }
  
  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }
  
  private class ElementaryModel implements Serializable, Comparable<ElementaryModel> {
    private static final long serialVersionUID = 6124398178368647304L;
    private final SparseVector x;
    private double alpha;
    private final long age;
    
    public ElementaryModel(long age, SparseVector x, double initialAlpha) {
      this.age = age;
      this.x = (x == null) ? null : (SparseVector) x.clone();
      this.alpha  = initialAlpha;
    }
    
    public Object clone() {
      return new ElementaryModel(age, x, alpha);
    }
    
    public void update(double multiplier) {
      alpha *= multiplier;
    }
    
    public double alpha() {
      return alpha;
    }
    
    public SparseVector x() {
      return x;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o != null && o instanceof ElementaryModel) {
        ElementaryModel m = (ElementaryModel) o;
        return m.x.equals(x) && m.alpha == alpha && m.age == age;
      }
      return false;
    }
    
    @Override
    public int compareTo(ElementaryModel o) {
      if (age < o.age) {
        return 1;
      }
      if (age > o.age) {
        return -1;
      }
      return 0;
    }
    
    @Override
    public String toString() {
      return age + "";
    }
  }
  
  private class BoundedQueue  implements Iterable<ElementaryModel> {
    private final Queue<ElementaryModel> q;
    private final int size;
    
    public BoundedQueue(int k) {
      size = k;
      q = new LinkedList<ElementaryModel>();
    }
    
    public Object clone() {
      BoundedQueue ret = new BoundedQueue(size);
      for (ElementaryModel p : q) {
        ret.offer((ElementaryModel) p.clone());
      }
      return ret;
    }
    
    public int size() {
      return Math.min(size, q.size());
    }
    
    public Iterator<ElementaryModel> iterator() {
      return new BoundedQueueIterator(q.iterator());
    }
    
    // O(1)
    public boolean offer(ElementaryModel e) {
      if (q.size() >= size) {
        q.poll();
      }
      q.offer(e);
      return true;
    }
    
    // O(1)
    public ElementaryModel poll() {
      return q.poll();
    }
    
    // O(1)
    public ElementaryModel peek() {
      return q.peek();
    }
    
    // O(size)
    public String toString() {
      StringBuffer out = new StringBuffer("Queue:");
      for (ElementaryModel p : this) {
        out.append('\t').append(p.toString());
      }
      return out.toString();
    }
  }
  
  private class BoundedQueueIterator implements Iterator<ElementaryModel> {
    private final Iterator<ElementaryModel> origIter;
    
    public BoundedQueueIterator(Iterator<ElementaryModel> iter) {
      origIter = iter;
    }
    
    @Override
    public boolean hasNext() {
      return origIter.hasNext();
    }

    @Override
    public ElementaryModel next() {
      return origIter.next();
    }

    @Override
    public void remove() {
      throw new RuntimeException("Remove is not supported!");
    }
  }
  
  public void setLambda(double lambda) {
    this.lambda = lambda;
  }
  
  public double getLambda() {
    return this.lambda;
  }
  
  public void setKernel(Kernel kernel) {
    this.kernel = kernel;
  }
  
  public Kernel getKernel() {
    return this.kernel;
  }
  
  private static double evaluate(Model model, InstanceHolder evalSet) {
    double MAError = 0.0;
    for (int i = 0; i < evalSet.size(); i++) {
      double predicted = model.predict(evalSet.getInstance(i));
      double expected = evalSet.getLabel(i);
      MAError += Math.abs(expected - predicted);
    }
    MAError /= evalSet.size();
    return MAError;
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length == 5 || args.length == 6 || args.length == 7) {
      NormaSVM svm = new NormaSVM();
      svm.setLambda(Double.parseDouble(args[2]));
      Kernel kernel = (Kernel) Class.forName(args[3]).newInstance();
      svm.setKernel(kernel);
      
      int iters = Integer.parseInt(args[4]);
      long seed = (args.length >= 6) ? Long.parseLong(args[5]) : 1234567890;
      Random rand = new Random(seed);
      
      if (kernel instanceof RBFKernel && args.length >= 7) {
        ((RBFKernel)kernel).setSigma(Double.parseDouble(args[6]));
      }
      
      System.err.println("Applying NormaSVM with\n  lambda=" + svm.getLambda() + "\n  kernel=" + svm.getKernel().getClass().getCanonicalName() + "\n  iters=" + iters + "\n  seed=" + seed + ((kernel instanceof RBFKernel) ? "\n  sigma=" + ((RBFKernel)kernel).getSigma() : "" ));
      
      DataBaseReader r = DataBaseReader.createDataBaseReader("gossipLearning.DataBaseReader", new File(args[0]), new File(args[1]));
      
      InstanceHolder train = r.getTrainingSet();
      InstanceHolder eval = r.getTrainingSet();
      
      for (int iter = 0; iter < iters*train.size(); iter ++) {
        int i = rand.nextInt(train.size());
        SparseVector x = train.getInstance(i);
        double y = train.getLabel(i);
        
        svm.update(x, y);
        
        System.out.println(iter + "\t" + evaluate(svm, train) + "\t" + evaluate(svm, eval));
      }
    } else {
      System.err.println("Usage: java -cp ... gossipLearning.models.regression.NormaSVM train test lambda kernel iters [seed]");
    }
  }
}
