package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class represents a Naive Bayes classification algorithm.</br>
 * The independent probability distributions, P(Fi,Cj), are modelled 
 * by one dimensional normal distributions.
 * @author István Hegedűs
 *
 */
public class SimpleNaiveBayes extends ProbabilityModel {
  private static final long serialVersionUID = -1077414909739530823L;
  
  protected Vector<SparseVector> mus;
  protected Vector<SparseVector> sigmas;
  protected Vector<Double> counts;
  protected double age;
  protected int numberOfClasses;
  protected int maxIndex;

  public SimpleNaiveBayes() {
    mus = new Vector<SparseVector>();
    sigmas = new Vector<SparseVector>();
    counts = new Vector<Double>();
    age = 0.0;
    maxIndex = 0;
  }
  
  public SimpleNaiveBayes(SimpleNaiveBayes a) {
    this();
    for (int i = 0; i < a.counts.size(); i++) {
      mus.add((SparseVector)a.mus.get(i).clone());
      sigmas.add((SparseVector)a.sigmas.get(i).clone());
      counts.add(a.counts.get(i));
    }
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    maxIndex = a.maxIndex;
  }
  
  @Override
  public Object clone() {
    return new SimpleNaiveBayes(this);
  }

  @Override
  public void init(String prefix) {
  }

  @Override
  public void update(SparseVector instance, double label) {
    if (instance.maxIndex() > maxIndex) {
      maxIndex = instance.maxIndex() + 1;
    }
    if (counts.size() <= label) {
      for (int i = counts.size(); i <= label; i++) {
        mus.add(new SparseVector());
        sigmas.add(new SparseVector());
        counts.add(0.0);
      }
    }
    age ++;
    double count = counts.get((int)label) + 1.0;
    counts.set((int)label, count);
    SparseVector v = mus.get((int)label);
    v.mul(1.0 - 1.0 / count).add(instance, 1.0 / count);
    SparseVector vs = sigmas.get((int)label);
    SparseVector copy = new SparseVector(instance);
    vs.mul(1.0 - 1.0 / count).add(copy.pointMul(instance), 1.0 / count);
  }
  //private static int iter=1;

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double[] res = new double[numberOfClasses];
    //double[] featureCoefs = new double[maxIndex];
    double p;
    double pc;
    double sum = 0.0;
    for (int i = 0; i < numberOfClasses && i < counts.size(); i++) {
      p = 0.0;
      pc = Math.log(counts.get(i) / age);
      Iterator<VectorEntry> muIter = mus.get(i).iterator();
      Iterator<VectorEntry> sigmaIter = sigmas.get(i).iterator();
      Iterator<VectorEntry> instIter = instance.iterator();
      VectorEntry muE = muIter.hasNext() ? muIter.next() : new VectorEntry(-1, 0.0);
      VectorEntry sigmaE = sigmaIter.hasNext() ? sigmaIter.next() : new VectorEntry(-1, 0.0);
      VectorEntry instE = instIter.hasNext() ? instIter.next() : new VectorEntry(-1, 0.0);
      for (int j = 0; j < maxIndex; j++) {
        double mu = 0.0;
        double sigma = 0.0;
        double value = 0.0;
        if (j == muE.index) {
          mu = muE.value;
          if (muIter.hasNext()) {
            muE = muIter.next();
          }
        }
        if (j == sigmaE.index) {
          sigma = sigmaE.value;
          if (sigmaIter.hasNext()) {
            sigmaE = sigmaIter.next();
          }
        }
        if (j == instE.index) {
          value = instE.value;
          if (instIter.hasNext()) {
            instE = instIter.next();
          }
        }
        sigma = Math.sqrt(sigma - mu * mu);
        p += logProb(value, mu, sigma == 0.0 ? Utils.EPS : sigma) - pc;
        // featureCoefs can be used for feature selection
        /*if (iter % 3416 == 0) {
          for (int c = 0; c < i; c++) {
            double m = mus.get(c).get(j);
            double s = sigmas.get(c).get(j);
            s = s == 0.0 ? Utils.EPS : s;
            featureCoefs[j] += Math.abs(Utils.cdf(m, mu, sigma) - Utils.cdf(mu, m, s));
          }
        }*/
      }
      res[i] = Math.exp(p + pc);
      if (res[i] == 0.0 || Double.isNaN(res[i]) || Double.isInfinite(res[i])) {
        res[i] = Utils.EPS;
      }
      sum += res[i];
    }
    for (int i = 0; sum > 0.0 && i < res.length; i++) {
      res[i] /= sum;
    }
    /*if (iter % 3416 == 0) {
      TreeMap<Double, Integer> map = new TreeMap<Double, Integer>();
      for (int i = 0; i < featureCoefs.length; i++) {
        featureCoefs[i] /= numberOfClasses;
        map.put(featureCoefs[i], i);
      }
      System.out.println("Features: " + map.descendingMap().values() + "\t" + map.descendingMap().keySet());
    }
    iter ++;*/
    return res;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }

  private static double logProb(double x, double mu, double sigma) {
    double z = (x - mu) / sigma;
    double frac = Math.log(1.0 / (Utils.SQRT2PI * sigma));
    double pow = (-z * z) / 2.0;
    return frac + pow;
  }

}
