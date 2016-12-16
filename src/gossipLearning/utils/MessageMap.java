package gossipLearning.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SoloLearningModel;
import gossipLearning.messages.Message;
import gossipLearning.messages.RestartableSoloModelMessage;
import peersim.core.Node;

public class MessageMap implements Map<Integer, Message>{

  private Map<Integer, Message> messagesById;
  private Map<Integer, Node > destsById;


  public MessageMap() {
    this.messagesById = new HashMap<Integer, Message>();
    this.destsById = new HashMap<Integer, Node>();
  }

  public MessageMap(MessageMap messageMap) {
    this.messagesById = new HashMap<Integer, Message>();
    for (Integer iD : messageMap.messagesById.keySet()) {
      messagesById.put(iD, messageMap.messagesById.get(iD));
    }
    this.destsById = new HashMap<Integer, Node>();
    for (Integer iD : messageMap.destsById.keySet()) {
      destsById.put(iD, messageMap.destsById.get(iD));
    }
  }

  @Override
  public Object clone() {
    return new MessageMap(this);
  }

  @Override
  public int size() {
    return messagesById.size();
  }

  @Override
  public boolean isEmpty() {
    return messagesById.isEmpty() && destsById.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return messagesById.containsKey(key) && destsById.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    if(value instanceof Message)
      return messagesById.containsValue(value);
    else if (value instanceof Node)
      return destsById.containsKey(value);
    return false;
  }

  @Override
  @Deprecated
  public Message get(Object key) {
    return getMessage((Integer)key);
  }

  public Set<Message> getMessages(Node value) {
    Set<Message> m = new HashSet<Message>();
    for ( Integer iD : destsById.keySet()) {
      if(destsById.get(iD).getID() == value.getID() ){
        m.add(messagesById.get(iD));
      }
    }
    return m;
  }


  public Set<Integer> getMessageIDs(Node value) {
    Set<Integer> m = new HashSet<Integer>();
    for ( Integer iD : destsById.keySet()) {
      if(destsById.get(iD).getID() == value.getID() ){
        m.add(iD);
      }
    }
    return m;
  }

  public Message getMessage(Integer key) {
    return messagesById.get(key);
  }

  public Node getNode(Integer key) {
    return destsById.get(key);
  }

  public boolean isSendingThisStepID(int stepID) {
    for (Message message : messagesById.values()) {
      if (message instanceof RestartableSoloModelMessage ) {
        Model model = ((RestartableSoloModelMessage)message).getModel();
        if (model instanceof SoloLearningModel) 
          if (stepID == ((SoloLearningModel)model).getStepID())
            return true;
      }
    }
    return false;
  }

  @Override
  @Deprecated
  public Message put(Integer key, Message value) {
    return messagesById.put(key, value);
  }

  public Message put(Integer key, Message valueMessage, Node valueDest){
    put(key, valueDest);
    return put(key,valueMessage);
  }

  @Deprecated
  private Node put(Integer key, Node value){
    return destsById.put(key, value);
  }

  @Override
  public Message remove(Object key) {
    Integer keyI = (Integer)key;
    removeNode(keyI);
    return removeMessage(keyI);
  }

  public Set<Message> remove(Node value){
    Set<Message> m = new HashSet<Message>();
    for ( Integer iD : destsById.keySet()) {
      if(destsById.get(iD).getID() == value.getID() ){
        m.add(messagesById.get(iD));
        remove(iD);
      }
    }
    return m;
  }

  private Message removeMessage(Integer key){
    return messagesById.remove(key);
  }

  private Node removeNode(Integer key){
    return destsById.remove(key);
  }

  @Override
  @Deprecated
  public void putAll(Map<? extends Integer, ? extends Message> m) {
    messagesById.putAll(m);
  }

  public void putAll(MessageMap mm){
    putAll(mm.messagesById);
    putAllWithNodes(mm.destsById);
  }

  private void putAllWithNodes(Map<? extends Integer, ? extends Node> m) {
    destsById.putAll(m);
  }

  @Override
  public void clear() {
    destsById.clear();
    messagesById.clear();
  }

  @Override
  public Set<Integer> keySet() {
    return messagesById.keySet();
  }

  @Override
  @Deprecated
  public Collection<Message> values() {
    return messagesById.values();
  }

  public Collection<Message> valuesMessage() {
    return messagesById.values();
  }

  public Collection<Node> valuesNode() {
    return destsById.values();
  }

  @Override
  @Deprecated
  public Set<java.util.Map.Entry<Integer, Message>> entrySet() {
    return messagesById.entrySet();
  }

  public Set<java.util.Map.Entry<Integer, Message>> entrySetMessage() {
    return messagesById.entrySet();
  }

  public Set<java.util.Map.Entry<Integer, Node>> entrySetNode() {
    return destsById.entrySet();
  }

}
