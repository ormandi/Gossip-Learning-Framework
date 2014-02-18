package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Iterator;

/**
 * This class represents a Naive Bayes classification algorithm.</br>
 * The independent probability distributions, P(Fi,Cj), are modelled 
 * by one dimensional normal distributions.
 * @author István Hegedűs
 *
 */
public class SimpleNaiveBayes extends ProbabilityModel {
  private static final long serialVersionUID = -1077414909739530823L;
  
  protected SparseVector[] mus;
  protected SparseVector[] sigmas;
  protected double[] counts;
  protected int numberOfClasses;
  protected int maxIndex;

  public SimpleNaiveBayes() {
    mus = null;
    sigmas = null;
    counts = null;
    distribution = null;
    age = 0.0;
    maxIndex = 0;
    numberOfClasses = 0;
  }
  
  public SimpleNaiveBayes(SimpleNaiveBayes a) {
    mus = new SparseVector[a.numberOfClasses];
    sigmas = new SparseVector[a.numberOfClasses];
    counts = new double[a.numberOfClasses];
    distribution = new double[a.numberOfClasses];
    for (int i = 0; i < a.numberOfClasses; i++) {
      mus[i] = (SparseVector)a.mus[i].clone();
      sigmas[i] = (SparseVector)a.sigmas[i].clone();
      counts[i] = a.counts[i];
      distribution[i] = a.distribution[i];
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
    if (instance.maxIndex() >= maxIndex) {
      maxIndex = instance.maxIndex() + 1;
    }
    age ++;
    double count = counts[(int)label] + 1.0;
    counts[(int)label] =  count;
    SparseVector v = mus[(int)label];
    v.mul(1.0 - 1.0 / count).add(instance, 1.0 / count);
    SparseVector vs = sigmas[(int)label];
    SparseVector copy = new SparseVector(instance);
    vs.mul(1.0 - 1.0 / count).add(copy.pointMul(instance), 1.0 / count);
  }
  //private boolean isPrint = true;
  
  protected double[] distribution;
  
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    //double[] featureCoefs = new double[maxIndex];
    double p;
    double pc;
    double sum = 0.0;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < numberOfClasses; i++) {
      distribution[i] = 0.0;
      pc = Math.log(counts[i] / age);
      p = condLogProb(instance, mus[i], sigmas[i], maxIndex);
      
      distribution[i] = pc + p;
      if (distribution[i] > 0.0 || Double.isNaN(distribution[i]) || Double.isInfinite(distribution[i])) {
        distribution[i] = -1.0/Utils.EPS;
      }
      if (distribution[i] > max) {
        max = distribution[i];
      }
    }
    for (int i = 0; i < distribution.length; i++) {
      distribution[i] -= max;
      distribution[i] = Math.exp(distribution[i]);
      sum += Math.abs(distribution[i]);
    }
    for (int i = 0; i < distribution.length; i++) {
      distribution[i] /= sum;
    }
    /*if (age == 10000 && isPrint) {
      isPrint = false;
      TreeMap<Double, Integer> map = new TreeMap<Double, Integer>();
      for (int i = 0; i < featureCoefs.length; i++) {
        featureCoefs[i] /= numberOfClasses;
        map.put(featureCoefs[i], (i+1));
      }
      System.out.println("Features: " + map.descendingMap().values() + "\t" + map.descendingMap().keySet());
    }*/
    return distribution;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses < 2) {
      throw new RuntimeException("The specified value sould be greater than 1 instead " + numberOfClasses);
    }
    this.numberOfClasses = numberOfClasses;
    mus = new SparseVector[numberOfClasses];
    sigmas = new SparseVector[numberOfClasses];
    counts = new double[numberOfClasses];
    distribution = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      mus[i] = new SparseVector();
      sigmas[i] = new SparseVector();
      counts[i] = 0.0;
      distribution[i] = 0.0;
    }
  }
  
  private static double condLogProb(SparseVector instance, SparseVector mu, SparseVector sigma, int maxIndex) {
    double p = 0.0;
    Iterator<VectorEntry> muIter = mu.iterator();
    Iterator<VectorEntry> sigmaIter = sigma.iterator();
    Iterator<VectorEntry> instIter = instance.iterator();
    VectorEntry muE = muIter.hasNext() ? muIter.next() : new VectorEntry(-1, 0.0);
    VectorEntry sigmaE = sigmaIter.hasNext() ? sigmaIter.next() : new VectorEntry(-1, 0.0);
    VectorEntry instE = instIter.hasNext() ? instIter.next() : new VectorEntry(-1, 0.0);
    for (int j = 0; j < maxIndex; j++) {
      double m = 0.0;
      double s = 0.0;
      double value = 0.0;
      if (j == muE.index) {
        m = muE.value;
        if (muIter.hasNext()) {
          muE = muIter.next();
        }
      }
      if (j == sigmaE.index) {
        s = sigmaE.value;
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
      s = Math.sqrt(Math.abs(s - m * m));
      s = (s == 0.0) ? Utils.EPS : s;
      double ptmp = logProb(value, m, s);
      p += (ptmp > 0.0) ? 0.0 : ptmp;
      // featureCoefs can be used for feature selection
      /*if (age == 10000) {
        for (int c = 0; c < i; c++) {
          double m = mus[c].get(j);
          double s = sigmas[c].get(j);
          s = (s == 0.0) ? Utils.EPS : s;
          featureCoefs[j] += Math.abs(Utils.cdf(m, mu, sigma) - Utils.cdf(mu, m, s));
        }
      }*/
    }
    return p;
  }

  private static double logProb(double x, double mu, double sigma) {
    double z = (x - mu) / sigma;
    double frac = -Math.log(Utils.SQRT2PI * sigma);
    double pow = (-z * z) / 2.0;
    return frac + pow;
  }

}
