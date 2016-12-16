package gossipLearning.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import gossipLearning.interfaces.models.MultiLearningModel;
import peersim.config.Configuration;

public class ModelContainer implements Set<MultiLearningModel>{

  private static final String PAR_MAXSIZEINMBIT = "modelContainerSizeLimit";
  public final double MAX_SIZE_IN_MBIT;
  private double recentSize;
  private LinkedHashSet<MultiLearningModel> modelContainer;

  public ModelContainer(String prefix) {
    modelContainer = new LinkedHashSet<MultiLearningModel>();
    recentSize = 0;
    MAX_SIZE_IN_MBIT = Configuration.getDouble(prefix + "." + PAR_MAXSIZEINMBIT);
  }
  
  public ModelContainer(ModelContainer modelContainer) {
    this.modelContainer = new LinkedHashSet<MultiLearningModel>();
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    while (it.hasNext()) {
      this.modelContainer.add(it.next());
    }
    this.recentSize = modelContainer.recentSize;
    this.MAX_SIZE_IN_MBIT = modelContainer.MAX_SIZE_IN_MBIT;
  }

  public Object clone(){
    return new ModelContainer(this);
  }
  
  public MultiLearningModel getModel(ModelInfo mi){
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    MultiLearningModel matching = null;
    while (it.hasNext()) {
      MultiLearningModel retmlm = it.next();
      if(retmlm.getModelInfo().compareTo(mi) == 0){
        matching=retmlm;
      }
    }
    if(matching != null)
      return matching;
    else
      throw new RuntimeException("Exception occured when get an element that the collection does not contain.");
  }
  
  public MultiLearningModel getModel(MultiLearningModel mlm){
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    while (it.hasNext()) {
      MultiLearningModel retmlm = it.next();
      if(retmlm.compareTo(mlm) == 0){
        return retmlm;
      }
    }
    throw new RuntimeException("Exception occured when get an element that the collection does not contain.");
  }
  
  @Override
  public int size() {
    return modelContainer.size();
  }

  @Override
  public boolean isEmpty() {
    return modelContainer.isEmpty();
  }

  public void removeModelIfNotEqualWalkID(int modelID, int walkID){
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    HashSet<MultiLearningModel> removeSet = new HashSet<MultiLearningModel>();
    while (it.hasNext()) {
      MultiLearningModel mlm = it.next();
      if (mlm.getModelID() == modelID && mlm.getWalkID() != walkID)
        removeSet.add(mlm);
    }
    for (MultiLearningModel multiLearningModel : removeSet) {
      this.remove(multiLearningModel);
    }
  }
  
  public boolean contains(ModelInfo mi){
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    while (it.hasNext()) {
      MultiLearningModel mlm = it.next();
      if (mlm.getModelInfo().compareTo(mi) == 0)
        return true;
    }
    return false;
  }
  
  public boolean contains(MultiLearningModel mlm) {
    return modelContainer.contains(mlm);
  }
  
  @Override
  public boolean contains(Object o) {
    return modelContainer.contains(o);
  }

  @Override
  public Iterator<MultiLearningModel> iterator() {
    return modelContainer.iterator();
  }

  @Override
  public Object[] toArray() {
    return modelContainer.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return modelContainer.toArray(a);
  }

  @Override
  public boolean add(MultiLearningModel e) {
    if(modelContainer.contains(e)){
      this.remove(e);
    }
    Set<MultiLearningModel> removeCandidates = new HashSet<MultiLearningModel>();
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    while(e.getModelSize()+this.recentSize>MAX_SIZE_IN_MBIT){
      MultiLearningModel mlm = it.next();
      removeCandidates.add(mlm);
      this.recentSize-=mlm.getModelSize();
    }
    modelContainer.removeAll(removeCandidates);
    this.recentSize+=e.getModelSize(); 
    return modelContainer.add(e);

  }

  @Override
  public boolean remove(Object o) {
    recentSize-=((MultiLearningModel)o).getModelSize();
    return modelContainer.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return modelContainer.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends MultiLearningModel> c) {
    boolean returnBoolean = true;
    Iterator<? extends MultiLearningModel> it = c.iterator();
    while(it.hasNext()) {
      if(!this.add(it.next())){
        returnBoolean = false;
      }
    }
    return returnBoolean;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    Set<MultiLearningModel> removeCandidates = new HashSet<MultiLearningModel>();
    Iterator<MultiLearningModel> it = modelContainer.iterator();
    while (it.hasNext()) {
      MultiLearningModel mlm = it.next();
      if(!c.contains(mlm)){
        removeCandidates.add(mlm);
        this.recentSize-=mlm.getModelSize();
      }
    }
    return modelContainer.removeAll(removeCandidates);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean returnBoolean = true;
    for (Object object : c) {
      if(!this.remove(object)){
        returnBoolean = false;
      }
    }
    return returnBoolean;
  }

  @Override
  public void clear() {
    modelContainer.clear();
    recentSize = 0;
  }

}
