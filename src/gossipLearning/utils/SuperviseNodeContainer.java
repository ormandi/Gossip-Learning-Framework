package gossipLearning.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import peersim.core.Node;

public class SuperviseNodeContainer<T extends Comparable<T>> implements Map<Long, HashSet<Integer>>{

  private HashMap<Long, Node> containerForNodeIDToNode;
  private HashMap<Long, HashSet<Integer>> containerForNodeIDMapToModelIDHashSet;
  private HashMap<Integer, T> containerForModelIDMapToContent;

  public SuperviseNodeContainer() {
    containerForNodeIDToNode = new HashMap<Long, Node>();
    containerForNodeIDMapToModelIDHashSet = new HashMap<Long, HashSet<Integer>>();
    containerForModelIDMapToContent = new HashMap<Integer,T>();    
  }

  public SuperviseNodeContainer(SuperviseNodeContainer<T> o) {
    containerForNodeIDToNode = new HashMap<Long, Node>();
    containerForNodeIDToNode.putAll(o.containerForNodeIDToNode);
    containerForNodeIDMapToModelIDHashSet = new HashMap<Long, HashSet<Integer>>();
    for (Long nodeID : o.containerForNodeIDMapToModelIDHashSet.keySet()) {
      HashSet<Integer> newSet = new HashSet<Integer>(); 
      newSet.addAll(o.containerForNodeIDMapToModelIDHashSet.get(nodeID));
      containerForNodeIDMapToModelIDHashSet.put(nodeID, newSet);
    }
    containerForModelIDMapToContent = new HashMap<Integer,T>();   
    containerForModelIDMapToContent.putAll(o.containerForModelIDMapToContent);
  }

  @Override
  public Object clone(){
    return new SuperviseNodeContainer<T>(this);
  }

  @Override
  public int size() {
    return containerForModelIDMapToContent.size();
  }

  @Override
  public boolean isEmpty() {
    return containerForNodeIDMapToModelIDHashSet.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return containerForNodeIDMapToModelIDHashSet.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return containerForNodeIDMapToModelIDHashSet.containsValue(value);
  }

  public boolean containsValueInSet(Integer modelID) {
    for (HashSet<Integer> innerSet : containerForNodeIDMapToModelIDHashSet.values()) {
      if(innerSet.contains(modelID))
        return true;
    }
    return false;
  }

  public boolean containsNode(Node node){
    return containerForNodeIDToNode.containsValue(node);
  }

  @Override
  public HashSet<Integer> get(Object key) {
    return containerForNodeIDMapToModelIDHashSet.get(key);
  }

  public HashSet<T> getContent(Object key) {
    HashSet<T> returnSet = new HashSet<T>();
    if(containerForNodeIDMapToModelIDHashSet.containsKey(key)){
      for (Integer modelID : containerForNodeIDMapToModelIDHashSet.get(key)) {
        returnSet.add(containerForModelIDMapToContent.get(modelID));
      }
    }
    return returnSet;
  }

  @Override
  public HashSet<Integer> remove(Object key) {
    HashSet<Integer> removedSet = containerForNodeIDMapToModelIDHashSet.remove(key);
    containerForNodeIDToNode.remove(key);
    for (Integer modelID : removedSet) {
      containerForModelIDMapToContent.remove(modelID);
    }
    return removedSet;
  }

  public boolean removeFromInnerSetByModelID(Long key, Integer modelID){
    if(containerForNodeIDMapToModelIDHashSet.containsKey(key)){
      HashSet<Integer> examinedSet = containerForNodeIDMapToModelIDHashSet.get(key);
      if(examinedSet.contains(modelID)){
        if(examinedSet.size() <= 1){
          containerForNodeIDMapToModelIDHashSet.remove(key);
          containerForNodeIDToNode.remove(key);
        } else {
          examinedSet.remove(modelID);
        }
        containerForModelIDMapToContent.remove(modelID);
        return true;
      } 
    } 
    return false;
  }

  @Override
  public void clear() {
    containerForNodeIDToNode.clear();
    containerForNodeIDMapToModelIDHashSet.clear();
    containerForModelIDMapToContent.clear();
  }

  @Override
  public Collection<HashSet<Integer>> values() {
    return containerForNodeIDMapToModelIDHashSet.values();
  }

  public Collection<Node> getNodes(){
    return containerForNodeIDToNode.values();
  }

  @Deprecated
  @Override
  public HashSet<Integer> put(Long key, HashSet<Integer> value) {
    HashSet<Integer> returnSet = containerForNodeIDMapToModelIDHashSet.put(key, value);
    return returnSet;
  }

  public T add(Node node, Integer modelID, T value){
    if(containerForModelIDMapToContent.containsKey(modelID)){
      return containerForModelIDMapToContent.get(modelID);
    }
    long nodeID = node.getID();
    if(!containerForNodeIDMapToModelIDHashSet.containsKey(nodeID)){
      HashSet<Integer> newSet = new HashSet<Integer>();
      newSet.add(modelID);
      containerForNodeIDMapToModelIDHashSet.put(nodeID, newSet);
      containerForNodeIDToNode.put(nodeID, node);
    } else {
      containerForNodeIDMapToModelIDHashSet.get(nodeID).add(modelID);
    }
    containerForModelIDMapToContent.put(modelID, value);
    return value;
  }

  @Deprecated
  @Override
  public void putAll(Map<? extends Long, ? extends HashSet<Integer>> m) {
    containerForNodeIDMapToModelIDHashSet.putAll(m);
  }

  @Override
  public Set<Long> keySet() {
    return containerForNodeIDMapToModelIDHashSet.keySet();
  }

  @Override
  public Set<java.util.Map.Entry<Long, HashSet<Integer>>> entrySet() {
    return containerForNodeIDMapToModelIDHashSet.entrySet();
  }

  @Override
  public String toString() {
    String outStr = "";
    for (Long nodeID : containerForNodeIDMapToModelIDHashSet.keySet()) {
      outStr+=nodeID+" "+containerForNodeIDToNode.get(nodeID).isUp()+":";
      for (Integer modelID : containerForNodeIDMapToModelIDHashSet.get(nodeID)) {
        try{
          outStr+=containerForModelIDMapToContent.get(modelID).toString();
        } catch (NullPointerException e){
          outStr+="NullPointerException"+modelID;
        }
      }
      outStr+=" ";
    }
    return outStr;
  }
}
