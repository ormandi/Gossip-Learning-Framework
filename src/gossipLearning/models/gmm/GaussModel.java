package gossipLearning.models.gmm;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.Utils;

import java.util.Random;

public class GaussModel implements Model, Mergeable {
  private static final long serialVersionUID = -37608538566714846L;
  
  protected double mean;
  protected double mean2;
  protected double age;
  
  public GaussModel() {
    mean = 0.0;
    mean2 = 0.0;
    age = 0.0;
  }
  
  public GaussModel(GaussModel a) {
    mean = a.mean;
    mean2 = a.mean2;
    age = a.age;
  }
  
  @Override
  public Object clone() {
    return new GaussModel(this);
  }
  
  public void update(double x) {
    update(x, 1.0);
  }
  
  public void update(double x, double w) {
    age += w;
    mean += w * (x - mean) / age;
    mean2 += w * (x*x - mean2) / age;
  }
  
  @Override
  public Model merge(Model model) {
    GaussModel m = (GaussModel)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age += (m.age - age) * modelWeight;
    mean += (m.mean - mean) * modelWeight;
    mean2 += (m.mean2 - mean2) * modelWeight;
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    GaussModel m = (GaussModel)model;
    age += m.age * times;
    mean += m.mean * times;
    mean2 += m.mean2 * times;
    return this;
  }

  @Override
  public double getAge() {
    return age;
  }
  
  public double getMu() {
    return mean;
  }
  
  public double getSigma() {
    double value = Math.sqrt(Math.abs(mean2 - (mean * mean)));
    return value <= 0.0 ? Utils.EPS : value;
  }
  
  public double generate(Random r) {
    return getMu() + getSigma() * r.nextGaussian();
  }
  
  public double cdf(double x) {
    return Utils.cdf(x, getMu(), getSigma());
  }
  
  public double pdf(double x) {
    double sigma = getSigma();
    double z = (x - mean) / sigma;
    return Math.exp(-z*z / 2.0) / (Utils.SQRT2PI * sigma);
  }
  
  public double logpdf(double x) {
    double sigma = getSigma();
    double z = (x - mean) / sigma;
    double frac = -Math.log(Utils.SQRT2PI * sigma);
    double pow = (-z * z) / 2.0;
    return frac + pow;
  }
  
  public String toString() {
    return String.format("mu: %.5f sigma: %.5f", mean, getSigma());
  }
  
  public static void main(String[] args) {
    double mu = 0;
    double sigma = 1.0;
    double n = 1000000;
    Random r = new Random(System.nanoTime());
    
    GaussModel gm = new GaussModel();
    GMM gmm = new GMM(2, r);
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      double x = Utils.nextNormal(r.nextDouble() < 0.5 ? mu + 10.0 : mu, sigma, r);
      sum += x;
      gm.update(x);
      gmm.update(x);
    }
    System.out.println(gm);
    System.out.println("AVG: " + sum/n);
    System.out.println("5.0\t" + Utils.cdf(5.0, gm.getMu(), gm.getSigma()));
    
    System.out.println(gmm);
  }

}
