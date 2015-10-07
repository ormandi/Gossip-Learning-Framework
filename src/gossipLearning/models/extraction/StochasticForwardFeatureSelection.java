package gossipLearning.models.extraction;

import java.util.ArrayList;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class StochasticForwardFeatureSelection implements FeatureExtractor, Mergeable<StochasticForwardFeatureSelection>, LearningModel{

  private static final long serialVersionUID = -7268551991500684010L;
  private static final String PAR_DIMENSION = "StochasticForwardFeatureSelection.dimension";
  private static final String PAR_K = "StochasticForwardFeatureSelection.k";
  private static final String PAR_EXAGE = "StochasticForwardFeatureSelection.examinationAge";

  private final int numberOfClasses = 2;
  private final int dimension;
  private final int k;
  private final int examinationAge;
  //private final int bestExaminationAge;

  private final String prefix;
  private double age;
  
  protected ArrayList<Integer> featureSet;
  protected ArrayList<Integer> bestFeatureSet;

  protected ArrayList<Double> kAprErrList;
  protected ArrayList<Double> bestAprErrList;

  protected LogisticRegression thisLR;
  protected LogisticRegression bestLR;
  

  public StochasticForwardFeatureSelection(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_K, PAR_EXAGE);
  }


  public StochasticForwardFeatureSelection(String prefix, String PAR_DIMENSION,
      String PAR_K, String PAR_EXAGE) {
    this.prefix = prefix;
    age = 0;
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    k = Configuration.getInt(prefix + "." + PAR_K);
    examinationAge = Configuration.getInt(this.prefix + "." + PAR_EXAGE);
    featureSet = new ArrayList<Integer>();
    bestFeatureSet = new ArrayList<Integer>();
    kAprErrList = new ArrayList<Double>();
    bestAprErrList = new ArrayList<Double>();
    thisLR = new LogisticRegression(this.prefix);
    bestLR = new LogisticRegression(this.prefix);
  }

  public StochasticForwardFeatureSelection(
      StochasticForwardFeatureSelection sffs) {
    dimension = sffs.getDimension();
    k = sffs.getK();
    examinationAge = sffs.examinationAge;
    this.prefix = sffs.prefix;
    age = sffs.getAge();
    featureSet = deepCopy(sffs.featureSet);
    bestFeatureSet = deepCopy(sffs.bestFeatureSet);
    kAprErrList = deepCopy(sffs.kAprErrList);
    bestAprErrList = deepCopy(sffs.bestAprErrList);
    thisLR = (LogisticRegression)sffs.thisLR.clone();
    bestLR = (LogisticRegression)sffs.bestLR.clone();    
  }

  public Object clone(){
    return new StochasticForwardFeatureSelection(this);
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    if (bestLR.getAge() == 0 && bestFeatureSet.size() < k)
      bestFeatureSet.add(getNextRandomFeature(bestFeatureSet));
    if (thisLR.getAge() == 0 && featureSet.size() < k)
      featureSet.add(getNextRandomFeature(featureSet));
    thisLR.update(extract(instance,featureSet), label);
    bestLR.update(extract(instance, bestFeatureSet), label);
    if(bestLR.getAge() >= examinationAge){
      if (bestAprErrList.size() < k) {
        bestAprErrList.add(bestLR.getApproximatedError());
        if (bestFeatureSet.size() < k){
          bestLR = new LogisticRegression(prefix);
          bestFeatureSet.add(getNextRandomFeature(bestFeatureSet));
          bestLR.update(extract(instance, bestFeatureSet), label);
        }
      } 
    }
    if(thisLR.getAge() >= examinationAge) {
      kAprErrList.add(thisLR.getApproximatedError());
      if (kAprErrList.size()>0 && bestAprErrList.size()>0 ) {
        if(bestAprErrList.get(bestAprErrList.size()-1) > kAprErrList.get(kAprErrList.size()-1)){
          bestAprErrList = deepCopy(kAprErrList);
          bestFeatureSet = deepCopy(featureSet);
          bestLR = new LogisticRegression(prefix);
          if(bestFeatureSet.size() < k)
            bestFeatureSet.add(getNextRandomFeature(bestFeatureSet));
          bestLR.update(extract(instance, bestFeatureSet), label);
        }
      }
      thisLR = new LogisticRegression(prefix);
      if (featureSet.size() >= k) {
        featureSet = new ArrayList<Integer>();
        kAprErrList = new ArrayList<Double>();
      }
      featureSet.add(getNextRandomFeature(featureSet)); 
      thisLR.update(extract(instance,featureSet), label);
    } 
    age++;
  }

  @Override
  public StochasticForwardFeatureSelection merge(StochasticForwardFeatureSelection model) {
    if(featureSet.size()>0){
      int comparisonIndex=Math.min(kAprErrList.size(), model.kAprErrList.size())-1;
      if(comparisonIndex > -1) {
        boolean isBetter = true;
        for (int i = 0; i <= comparisonIndex-1; i++) {
          if(kAprErrList.get(i) < model.kAprErrList.get(i)){
            isBetter = false;
            break;
          }
        }
        if(kAprErrList.get(comparisonIndex) <= model.kAprErrList.get(comparisonIndex)){
          isBetter = false;
        }
        if(isBetter){
          //bestAprErrList = deepCopy(model.kAprErrList);
          //featureSet = deepCopy(model.featureSet);
          kAprErrList = new ArrayList<Double>();
          featureSet = new ArrayList<Integer>();
          for (int i = 0; i <= comparisonIndex; i++) {
            kAprErrList.add(model.kAprErrList.get(i));
            featureSet.add(model.featureSet.get(i));
          }
          thisLR = new LogisticRegression(prefix);
          if(bestAprErrList.size()>0) {
            if(bestAprErrList.get(bestAprErrList.size()-1) > kAprErrList.get(comparisonIndex)){
              bestAprErrList = deepCopy(kAprErrList);
              bestFeatureSet = deepCopy(featureSet);
              bestLR = new LogisticRegression(prefix);
            }
          }
        }
        
      }
      if(bestAprErrList.size() > 0 && model.bestAprErrList.size() > 0 &&
          model.bestFeatureSet.size() >= bestFeatureSet.size()) {
        if(bestAprErrList.get(bestAprErrList.size()-1) > model.bestAprErrList.get(model.bestAprErrList.size()-1)){
          bestAprErrList = deepCopy(model.bestAprErrList);
          bestFeatureSet = deepCopy(model.bestFeatureSet);
          if(bestAprErrList.size() < k)
            bestFeatureSet.remove(bestFeatureSet.size()-1);
          bestLR = new LogisticRegression(prefix);
        }
      } else if (bestAprErrList.size() == 0 && model.bestAprErrList.size() > 0) {
        bestAprErrList = deepCopy(model.bestAprErrList);
        bestFeatureSet = deepCopy(model.bestFeatureSet);
        if(bestAprErrList.size() < k)
          bestFeatureSet.remove(bestFeatureSet.size()-1);
        bestLR = new LogisticRegression(prefix);
      }
    }
    return this;
  }
 
  @Override
  public double predict(SparseVector instance) {
    if(thisLR.getPositiveProbability(instance) >= 0.5) {
      return 1;
    } else {
      return 0;
    }
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

  private int getNextRandomFeature(ArrayList<Integer> featureSet){
    ArrayList<Integer> potentialFeature = new ArrayList<Integer>();
    for (int i = 0; i < dimension; i++) {
      potentialFeature.add(i);
    }
    for (Integer selectedFeature : featureSet) {
      potentialFeature.remove(selectedFeature);
    } 
    return potentialFeature.get(CommonState.r.nextInt(potentialFeature.size()));
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
    return extract(instance,featureSet);
  }

  private SparseVector extract(SparseVector instance,
      ArrayList<Integer> featureSet) {
    SparseVector redInstance = new SparseVector();
    for (Integer featureIdx : featureSet) {
      redInstance.add(featureIdx, instance.get(featureIdx));
    }
    return redInstance;
  }

  private <T> ArrayList<T> deepCopy(ArrayList<T> al){
    ArrayList<T> nal = new ArrayList<T>();
    for (int i = 0; i < al.size(); i++) {
      nal.add(al.get(i));
    }
    return nal;
  }

  public int getK() {
    return k;
  }

  public int getDimension() {
    return dimension;
  }

  @Override
  public double getAge() {
    return age;
  }


  public int getExaminationAge() {
    return examinationAge;
  }

  public ArrayList<Integer> getFeatureSet() {
    return featureSet;
  }

  @Override
  public String toString(){
    String fsstr = "";
    if (bestFeatureSet.size()>0) {
      for (int i=0; i<bestFeatureSet.size()-1;i++) {
        fsstr += (bestFeatureSet.get(i)+1)+",";
      }
      fsstr += (bestFeatureSet.get(bestFeatureSet.size()-1)+1)+"";
    }
    fsstr += ": Model_age->"+age + " ";
    fsstr += " "+bestLR.getAge()+" "+bestLR.getApproximatedError()+" "+bestAprErrList.size()+" ---- ";
    if (featureSet.size()>0) {
      for (int i=0; i<featureSet.size()-1;i++) {
        fsstr += (featureSet.get(i)+1)+",";
      }
      fsstr += (featureSet.get(featureSet.size()-1)+1)+"";
    }
    fsstr += ": "+thisLR.getAge()+" "+thisLR.getApproximatedError();
    /*if (bestAprErrList.size()>0) {
      fsstr +=bestAprErrList.toString();
    }*/
    //fsstr += " ---!!!---best:"+bestFeatureSet.toString()+" "+bestAprErrList.toString();
    /*if (kAprErrList.size()>0) {
      fsstr +=kAprErrList.toString();
    }*/
    //fsstr += " ---!!!---best:"+bestFeatureSet.toString()+" "+bestAprErrList.toString();
    return fsstr;
  }
}
