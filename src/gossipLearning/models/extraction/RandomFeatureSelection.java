package gossipLearning.models.extraction;

import java.util.ArrayList;
import java.util.Collections;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class RandomFeatureSelection implements FeatureExtractor, Mergeable<RandomFeatureSelection>, LearningModel{

  private static final long serialVersionUID = -2162129153764701586L;
  private static final String PAR_DIMENSION = "RandomFeatureSelection.dimension";
  private static final String PAR_K = "RandomFeatureSelection.k";
  private static final String PAR_EXAGE = "RandomFeatureSelection.examinationAge";
  private static final String PAR_ERRCAP = "RandomFeatureSelection.errorCapacity";


  private final String prefix;
  private final int dimension;
  private final int numberOfClasses = 2;
  private final int examinationAge;
  private final int k;
  private final int errorCapacity;
  private double age;

  protected ArrayList<Integer> featureSet;
  protected ArrayList<Integer> bestFeatureSet;
  
  protected LogisticRegression bestLR;
  protected ArrayList<Double> bestAprErrList;
  protected LogisticRegression thisLR;


  public RandomFeatureSelection(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_K, PAR_EXAGE, PAR_ERRCAP);
  }

  public RandomFeatureSelection(String prefix, String PAR_DIMENSION, String PAR_K, String PAR_EXAGE, String PAR_ERRCAP) {
    this.prefix = prefix;
    dimension = Configuration.getInt(this.prefix + "." + PAR_DIMENSION);
    examinationAge = Configuration.getInt(this.prefix + "." + PAR_EXAGE);
    k = Configuration.getInt(this.prefix + "." + PAR_K);
    errorCapacity = Configuration.getInt(this.prefix + "." + PAR_ERRCAP);
    age = 0.0;
    featureSet = new ArrayList<Integer>();
    bestFeatureSet = new ArrayList<Integer>();
    thisLR = new LogisticRegression(this.prefix);
    bestLR = new LogisticRegression(this.prefix);
    bestAprErrList = new ArrayList<Double>();
  }

  public RandomFeatureSelection(RandomFeatureSelection rfs) {
    prefix = rfs.prefix;
    dimension = rfs.getDimension();
    k = rfs.getK();
    errorCapacity = rfs.errorCapacity;
    age = rfs.getAge();
    examinationAge = rfs.examinationAge;
    featureSet = deepCopy(rfs.featureSet);
    bestFeatureSet = deepCopy(rfs.bestFeatureSet);
    thisLR = (LogisticRegression)rfs.thisLR.clone();
    bestLR = (LogisticRegression)rfs.bestLR.clone();
    bestAprErrList = deepCopy(rfs.bestAprErrList);
  }

  public Object clone(){
    return new RandomFeatureSelection(this);
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

  public int getDimension() {
    return dimension;
  }

  @Override
  public RandomFeatureSelection merge(RandomFeatureSelection model) {
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
        bestFeatureSet = deepCopy(model.bestFeatureSet);
      }
    }
    return this;
  }

  @Override
  public void update(SparseVector instance, double label) {
    if (age == 0) {
      featureSet = getRandomFeatureList();
      bestFeatureSet = getRandomFeatureList();
    }
    thisLR.update(extract(instance,featureSet), label);
    bestLR.update(extract(instance,bestFeatureSet), label);
    if(bestLR.getAge() >= examinationAge){
      if(bestAprErrList.size() >= errorCapacity) {
        bestAprErrList.remove(0);
      }
      bestAprErrList.add(bestLR.getApproximatedError());
      bestLR = new LogisticRegression(prefix);
      bestLR.update(extract(instance,bestFeatureSet), label);
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
          bestFeatureSet = deepCopy(featureSet);
        }
      }
      thisLR = new LogisticRegression(prefix);
      featureSet = getRandomFeatureList();
      thisLR.update(extract(instance,featureSet), label);
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

  
  private ArrayList<Integer> getRandomFeatureList(){
    return getRandomFeatureList(k);
  }
  
  private ArrayList<Integer> getRandomFeatureList(int size){
    ArrayList<Integer> al = new ArrayList<Integer>();
    for (int i = 0; i < size; i++) {
      al.add(getNextRandomFeature(al));
    }
    return al;
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

  public int getK() {
    return k;
  }

  public int getExaminationAge() {
    return examinationAge;
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
