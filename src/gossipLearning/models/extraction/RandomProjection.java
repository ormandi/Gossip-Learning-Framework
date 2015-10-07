package gossipLearning.models.extraction;

import java.util.ArrayList;
import java.util.Collections;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.RandomDistributionTypes;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class RandomProjection implements FeatureExtractor, Mergeable<RandomProjection>, LearningModel{

  private static final long serialVersionUID = -2162129153764701586L;
  private static final String PAR_DIMENSION = "RandomProjection.dimension";
  private static final String PAR_K = "RandomProjection.k";
  private static final String PAR_DISTRIBUTION = "RandomProjection.distribution"; 
  private static final String PAR_EXAGE = "RandomProjection.examinationAge";
  private static final String PAR_ERRCAP = "RandomProjection.errorCapacity";


  private final String prefix;
  private final int dimension;
  private final int numberOfClasses = 2;
  private final int examinationAge;
  private final RandomDistributionTypes distribution;
  private final int k;
  private final int errorCapacity;
  private double age;
  private long seed;
  private long best_seed;
  private Matrix proj;
  private Matrix bestProj; 

  protected LogisticRegression bestLR;
  protected ArrayList<Double> bestAprErrList;
  protected LogisticRegression thisLR;


  public RandomProjection(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_K, PAR_DISTRIBUTION, PAR_EXAGE, PAR_ERRCAP);
  }

  public RandomProjection(String prefix, String PAR_DIMENSION, String PAR_K, String PAR_DISTRIBUTION, String PAR_EXAGE, String PAR_ERRCAP) {
    this.prefix = prefix;
    dimension = Configuration.getInt(this.prefix + "." + PAR_DIMENSION);
    examinationAge = Configuration.getInt(this.prefix + "." + PAR_EXAGE);
    distribution = RandomDistributionTypes.valueOf(Configuration.getString(this.prefix + "." + PAR_DISTRIBUTION));
    k = Configuration.getInt(this.prefix + "." + PAR_K);
    errorCapacity = Configuration.getInt(this.prefix + "." + PAR_ERRCAP);
    age = 0.0;
    seed = CommonState.r.nextLong();
    best_seed = CommonState.r.nextLong();
    thisLR = new LogisticRegression(this.prefix);
    bestLR = new LogisticRegression(this.prefix);
    bestAprErrList = new ArrayList<Double>();
    proj = new Matrix(dimension,k,seed,distribution);
    bestProj = new Matrix(dimension,k,best_seed,distribution);
  }

  public RandomProjection(RandomProjection rp) {
    prefix = rp.prefix;
    dimension = rp.getDimension();
    k = rp.getK();
    errorCapacity = rp.errorCapacity;
    age = rp.getAge();
    examinationAge = rp.examinationAge;
    seed = rp.seed;
    best_seed = rp.best_seed;
    distribution = rp.getDistribution();
    thisLR = (LogisticRegression)rp.thisLR.clone();
    bestLR = (LogisticRegression)rp.bestLR.clone();
    bestAprErrList = deepCopy(rp.bestAprErrList);
    proj = rp.proj; //ref_copy
    bestProj = rp.bestProj; //ref_copy
  }

  public Object clone(){
    return new RandomProjection(this);
  }

  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    InstanceHolder result = new InstanceHolder(instances.getNumberOfClasses(), k);
    for (int i = 0; i < instances.size(); i++) {
      result.add(extract(instances.getInstance(i)), instances.getLabel(i));
    }
    return result;
  }

  @Override
  public SparseVector extract(SparseVector instance) {
    return extract(instance,proj);
  }

  public SparseVector extract(SparseVector instance, Matrix proj) {
    //Matrix proj = new Matrix(dimension,k,seed,distribution);
    Matrix res = proj.mulLeft(instance);
    SparseVector result = new SparseVector(res.getRow(0));
    return result;
  }

  public int getDimension() {
    return dimension;
  }

  @Override
  public RandomProjection merge(RandomProjection model) {
    boolean modelCopy = false;
    if(model.bestAprErrList.size() > 0){
      if(bestAprErrList.size() > 0) {
        Collections.sort(bestAprErrList);
        Collections.sort(model.bestAprErrList);
        if (median(bestAprErrList) > median(model.bestAprErrList))
          modelCopy = true;
      } else 
        modelCopy = true;
      if (modelCopy) {
        bestLR = (LogisticRegression)model.bestLR.clone();  
        bestAprErrList = deepCopy(model.bestAprErrList);
        best_seed = model.best_seed;
        bestProj = model.bestProj;
      }
    }
    return this;
  }

  @Override
  public void update(SparseVector instance, double label) {
    if (age == 0) {
      seed = CommonState.r.nextLong();
      proj = new Matrix(dimension,k,seed,distribution);
      best_seed = CommonState.r.nextLong();
      bestProj = new Matrix(dimension,k,best_seed,distribution);
    }
    thisLR.update(extract(instance,proj), label);
    bestLR.update(extract(instance,bestProj), label);
    if(bestLR.getAge() >= examinationAge){
      if(bestAprErrList.size() >= errorCapacity) {
        bestAprErrList.remove(0);
      }
      bestAprErrList.add(bestLR.getApproximatedError());
      bestLR = new LogisticRegression(prefix);
      bestLR.update(extract(instance,bestProj), label);
    }
    if(thisLR.getAge() >= examinationAge ){
      if(bestAprErrList.size()>0){
        boolean isCopy = false;
        for (int i = 0; i < bestAprErrList.size(); i++) {
          if (bestAprErrList.get(i) > thisLR.getApproximatedError())
            isCopy = true;
        }
        if (isCopy) {
          bestLR = (LogisticRegression)thisLR.clone();
          bestAprErrList = new ArrayList<Double>();
          bestAprErrList.add(thisLR.getApproximatedError());
          best_seed = seed;
          bestProj = proj;
        }
      }
      thisLR = new LogisticRegression(prefix);
      seed = CommonState.r.nextLong();
      proj = new Matrix(dimension,k,seed,distribution);
      thisLR.update(extract(instance,proj), label);
    }
    age++;
  }


  @Override
  public double predict(SparseVector instance) {
    if(thisLR.getPositiveProbability(instance) >= 0.5) {
      return 1;
    } else {
      return 0;
    }
  }

  private <T> ArrayList<T> deepCopy(ArrayList<T> al){
    return deepCopy(al,al.size()-1);
  }

  private <T> ArrayList<T> deepCopy(ArrayList<T> al, int lastIndex){
    if (lastIndex >= al.size()) {
      throw new ArrayIndexOutOfBoundsException(lastIndex);
    }
    ArrayList<T> nal = new ArrayList<T>();
    for (int i = 0; i <= lastIndex; i++) {
      nal.add(al.get(i));
    }
    return nal;
  }

  private double median(ArrayList<Double> ArrList){
    double median = 0.0;
    if (ArrList.size() % 2 == 0)
      median = ((double)ArrList.get(ArrList.size()/2) + (double)ArrList.get(ArrList.size()/2 - 1))/2;
    else
      median = (double) ArrList.get(ArrList.size()/2);
    return median;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses != 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
  }

  @Override
  public double getAge() {
    return age;
  }

  public long getSeed() {
    return seed;
  }

  public int getK() {
    return k;
  }

  public int getExaminationAge() {
    return examinationAge;
  }


  public long getBest_seed() {
    return best_seed;
  }

  public RandomDistributionTypes getDistribution() {
    return distribution;
  }

  @Override
  public String toString(){
    String outStr = "";
    /*if(bestLR.getApproximatedError() < 0.001 && bestLR.getApproximatedError() > 0.0) {
      outStr += "bestSeed"+best_seed+": ";
      outStr += bestLR.getAge()+" "+bestLR.getApproximatedError()+" "+bestAprErrList.toString()+"\n";
    }
    if(thisLR.getApproximatedError() < 0.001 && thisLR.getApproximatedError() > 0.0) {
      outStr += "ThisSeed"+seed+": ";
      outStr += thisLR.getAge()+" "+thisLR.getApproximatedError()+"\n";
    }*/
    /*if(bestAprErrList.size()> 0) 
    	if(bestAprErrList.get(0) < 0.001) {
    		outStr += "Valami gáz ---- ";
    		outStr += "bestSeed"+best_seed+": ";
        	outStr += bestLR.getAge()+" "+bestLR.getApproximatedError()+" "+bestAprErrList.toString()+"\n";	
        	outStr += "ThisSeed"+seed+": ";
        	outStr += thisLR.getAge()+" "+thisLR.getApproximatedError()+"\n";
    	}*/
    outStr += "Model_age: "+age + " ";
    outStr += "bestSeed"+best_seed+": ";
    outStr += bestLR.getAge()+" "+bestLR.getApproximatedError()+" "+bestAprErrList.toString()+"\n";
    //outStr += thisLR.getAge()+" "+thisLR.getApproximatedError();//"\n";
    outStr += bestProj.toString();
    return outStr;
  }

}
