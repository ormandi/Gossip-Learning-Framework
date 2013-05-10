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
    age = 0.0;
    maxIndex = 0;
    numberOfClasses = 0;
  }
  
  public SimpleNaiveBayes(SimpleNaiveBayes a) {
    mus = new SparseVector[a.numberOfClasses];
    sigmas = new SparseVector[a.numberOfClasses];
    counts = new double[a.numberOfClasses];
    for (int i = 0; i < a.numberOfClasses; i++) {
      mus[i] = (SparseVector)a.mus[i].clone();
      sigmas[i] = (SparseVector)a.sigmas[i].clone();
      counts[i] = a.counts[i];
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
  
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double[] res = new double[numberOfClasses];
    //double[] featureCoefs = new double[maxIndex];
    double p;
    double pc;
    double sum = 0.0;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < numberOfClasses; i++) {
      p = 0.0;
      pc = Math.log(counts[i] / age);
      Iterator<VectorEntry> muIter = mus[i].iterator();
      Iterator<VectorEntry> sigmaIter = sigmas[i].iterator();
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
        sigma = Math.sqrt(Math.abs(sigma - mu * mu));
        sigma = (sigma == 0.0) ? Utils.EPS : sigma;
        double ptmp = logProb(value, mu, sigma);
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
      res[i] = pc + p;
      if (res[i] > 0.0 || Double.isNaN(res[i]) || Double.isInfinite(res[i])) {
        res[i] = -1.0/Utils.EPS;
      }
      if (res[i] > max) {
        max = res[i];
      }
    }
    for (int i = 0; i < res.length; i++) {
      res[i] -= max;
      res[i] = Math.exp(res[i]);
      sum += Math.abs(res[i]);
    }
    for (int i = 0; i < res.length; i++) {
      res[i] /= sum;
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
    return res;
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
    for (int i = 0; i < numberOfClasses; i++) {
      mus[i] = new SparseVector();
      sigmas[i] = new SparseVector();
      counts[i] = 0.0;
    }
  }

  private static double logProb(double x, double mu, double sigma) {
    double z = (x - mu) / sigma;
    double frac = -Math.log(Utils.SQRT2PI * sigma);
    double pow = (-z * z) / 2.0;
    return frac + pow;
  }

}
