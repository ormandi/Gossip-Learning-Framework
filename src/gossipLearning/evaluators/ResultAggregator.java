package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

public class ResultAggregator implements Serializable, Iterable<AggregationResult> {
  private static final long serialVersionUID = 2242497407807240938L;
  protected static final ReentrantLock lock = new ReentrantLock(true);
  
  protected static Map<Integer, Evaluator[][]> aggregations;
  protected static Map<Integer, String[]> pid2ModelNames;
  protected static Map<Integer, StringBuffer[]> pid2ModelAges;
  protected static Map<Integer, String[]> pid2EvalNames;
  protected static InstanceHolder evalSet;
  
  protected final Evaluator[][] evaluators;
  protected final String[] modelNames;
  protected final double[] modelAges;
  protected final String[] evalNames;
  
  public ResultAggregator(String[] modelNames, String[] evalNames) {
    try {
      if (aggregations == null) {
        aggregations = new TreeMap<Integer, Evaluator[][]>();
        pid2ModelNames = new TreeMap<Integer, String[]>();
        pid2ModelAges = new TreeMap<Integer, StringBuffer[]>();
        pid2EvalNames = new TreeMap<Integer, String[]>();
      }
      this.modelNames = modelNames;
      this.evalNames = evalNames;
      this.modelAges = new double[modelNames.length];
      this.evaluators = new Evaluator[modelNames.length][evalNames.length];
      for (int i = 0; i < modelNames.length; i++) {
        modelAges[i] = 0.0;
        for (int j = 0; j < evalNames.length; j++) {
          this.evaluators[i][j] = (Evaluator)Class.forName(evalNames[j]).newInstance();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception was occured in ResultAggregator: ", e);
    }
  }
  
  public void push(int pid, int index, ModelHolder modelHolder, FeatureExtractor extractor) {
    if (modelHolder.size() == 0) {
      return;
    }
    InstanceHolder eval = extractor.extract(evalSet);
    LearningModel model = (LearningModel)modelHolder.getModel(modelHolder.size() - 1);
    modelAges[index] = model.getAge();
    for (int i = 0; i < eval.size(); i++) {
      double expected = eval.getLabel(i);
      double predicted = model.predict(eval.getInstance(i));
      for (int j = 0; j < evaluators[index].length; j++) {
        evaluators[index][j].evaluate(expected, predicted);
      }
    }
    push(pid, index);
  }
  
  protected void push(int pid, int index) {
    lock.lock();
    Evaluator[][] evaluator = aggregations.get(pid);
    if (!pid2ModelNames.containsKey(pid)) {
      pid2ModelNames.put(pid, modelNames);
      pid2ModelAges.put(pid, new StringBuffer[modelAges.length]);
      pid2EvalNames.put(pid, evalNames);
    }
    StringBuffer[] buffs = pid2ModelAges.get(pid);
    if (evaluator == null) {
      evaluator = new Evaluator[modelNames.length][evalNames.length];
      for (int i = 0; i < modelNames.length; i++) {
        if (AggregationResult.isPrintAges) {
          buffs[i] = new StringBuffer();
        }
        for (int j = 0; j < evalNames.length; j++) {
          evaluator[i][j] = (Evaluator)evaluators[i][j].clone();
          evaluators[i][j].clear();
        }
      }
      aggregations.put(pid, evaluator);
    } else {
      if (AggregationResult.isPrintAges) {
        buffs[index].append(' ');
        buffs[index].append(modelAges[index]);
      }
      for (int i = 0; i < evalNames.length; i++) {
        evaluator[index][i].merge(evaluators[index][i]);
      }
    }
    lock.unlock();
  }

  public void setEvalSet(InstanceHolder evalSet) {
    lock.lock();
    ResultAggregator.evalSet = evalSet;
    lock.unlock();
  }
  
  @Override
  public Iterator<AggregationResult> iterator() {
    lock.lock();
    try {
    List<AggregationResult> results = new LinkedList<AggregationResult>();
    for (Entry<Integer, Evaluator[][]> entry : aggregations.entrySet()) {
      for (int i = 0; i < entry.getValue().length; i++) {
        if (AggregationResult.isPrintAges) {
          System.out.println("##Ages\t" + entry.getKey() + "\t" + pid2ModelNames.get(entry.getKey())[i] + "\t" + pid2ModelAges.get(entry.getKey())[i]);
          pid2ModelAges.get(entry.getKey())[i] = new StringBuffer();
        }
        for (int j = 0; j < entry.getValue()[i].length; j++) {
          results.add(new AggregationResult(entry.getKey(), pid2ModelNames.get(entry.getKey())[i], pid2EvalNames.get(entry.getKey())[j], entry.getValue()[i][j].getNames(), entry.getValue()[i][j].getResults()));
        }
      }
    }
    return results.iterator();
    } finally {
      lock.unlock();
    }
  }
  
  @Override
  public String toString() {
    lock.lock();
    try {
    StringBuffer sb = new StringBuffer();
    for (Entry<Integer, Evaluator[][]> entry : aggregations.entrySet()) {
      for (int i = 0; i < entry.getValue().length; i++) {
        for (int j = 0; j < entry.getValue()[i].length; j++) {
          sb.append(entry.getValue()[i][j]);
          sb.append('\n');
        }
      }
    }
    return sb.toString();
    } finally {
      lock.unlock();
    }
  }

}
