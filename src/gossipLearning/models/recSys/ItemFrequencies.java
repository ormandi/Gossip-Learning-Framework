package gossipLearning.models.recSys;

import gossipLearning.utils.MultiBloomFilter;

import java.io.Serializable;
import java.util.TreeMap;

//import peersim.core.CommonState;

/**
 * Space efficient item frequency and likeability estimator class based on counting Bloom Filter implementation.
 *  
 * @author Róbert Ormándi
 */
public class ItemFrequencies implements Serializable {
  private static final long serialVersionUID = 1348239L;
  private TreeMap<Double, MultiBloomFilter> ratingSet;
  private TreeMap<Double, MultiBloomFilter> likeabilitySet;
  
  private double counter;
  private int prevNodeId;
  
  private int m;
  private int k;
  
  /**
   * Creates a new instance.
   * 
   * @param m number of counters applied by the wrapped bloom filters
   * @param k number of hash functions applied by the inner bloom filters
   */
  public ItemFrequencies(int m, int k) {
    this.m = m;
    this.k = k;
    counter = 0;
    prevNodeId = -1;
    ratingSet = new TreeMap<Double, MultiBloomFilter>();
    likeabilitySet = new TreeMap<Double, MultiBloomFilter>();
  }
  
  /**
   * Copy constructor which produce a deep copy of the set using the clone methods of the bloom filters.
   * 
   * @param other the instance that should be copied
   */
  public ItemFrequencies(ItemFrequencies other) {
    // copy simple fields
    m = other.m;
    k = other.k;
    counter = other.counter;
    prevNodeId = other.prevNodeId;
    
    // deep copy of rating set
    ratingSet = (other.ratingSet == null) ? null : new TreeMap<Double, MultiBloomFilter>();
    if (other.ratingSet != null) {
      for (double key : other.ratingSet.keySet()) {
        ratingSet.put(key, (MultiBloomFilter)other.ratingSet.get(key).clone());
      }
    }
    //for (int i = 0; other.ratingSet != null && i < other.ratingSet.length; i ++) {
    //  ratingSet.put = (other.ratingSet[i] != null) ? (MultiBloomFilter) other.ratingSet[i].clone() : null;
    //}
    
    // deep copy of likeability set
    likeabilitySet = (other.likeabilitySet == null) ? null : new TreeMap<Double, MultiBloomFilter>();
    if (other.likeabilitySet != null) {
      for (double key : other.likeabilitySet.keySet()) {
        likeabilitySet.put(key, (MultiBloomFilter)other.likeabilitySet.get(key));
      }
    }
    //for (int i = 0; other.likeabilitySet != null && i < other.likeabilitySet.length; i ++) {
    //  likeabilitySet[i] = (other.likeabilitySet[i] != null) ? (MultiBloomFilter) other.likeabilitySet[i].clone() : null;
    //}
  }
  
  /**
   * It produces a deep copy applying the copy constructor.
   */
  public Object clone() {
    return new ItemFrequencies(this);
  }
  
  /**
   * Add an item to the data structure
   * 
   * @param itemID ID of the item which is added
   * @param rating rating of the item given by a user
   * @param userAvgRating average rating value of the user who adds the item (this is necessary for computing likeability)
   */
  public void add(int itemID, double rating, double userAvgRating) {
    /*if (counter >= q) {
      return;
    }*/
    //int userID = (int)CommonState.getNode().getID();
    //if (itemID == 0) {
    //  System.out.println("========== SET UPDATE =============: r(" + userID + "," + itemID + ")=" + rating); 
    
    /*if (prevNodeId != CommonState.getNode().getID()) {
      counter ++;
      prevNodeId = (int)CommonState.getNode().getID();
    }*/
    counter ++;
    // get rating ID
    //int rateID = (int)rating - 1;

    // add to rate set
    if (!ratingSet.containsKey(rating)) {
      ratingSet.put(rating, new MultiBloomFilter(m, k));
    }
    ratingSet.get(rating).add(itemID);
    //if (0 <= rateID && rateID < ratingSet.length) {
      // initialize bloom filter if necessary
      //if (ratingSet[rateID] == null) {
        //ratingSet[rateID] = new MultiBloomFilter(m, k);
      //}
      // add item
      //ratingSet[rateID].add(itemID);
    //} else {
      //throw new RuntimeException("Rating " + rating + " out of range in item set with length " + ratingSet.length);
    //}
    
    // add to likeability
    if (!likeabilitySet.containsKey(rating)) {
      likeabilitySet.put(rating, new MultiBloomFilter(m, k));
    }
    if (rating > userAvgRating) {
      likeabilitySet.get(rating).add(itemID);
    }
    //if (0 <= rateID && rateID < ratingSet.length) {
      // initialize if necessary
      //if (likeabilitySet[rateID] == null) {
        //likeabilitySet[rateID] = new MultiBloomFilter(m, k);
      //}
      // add item if necessary
      //if (rating > userAvgRating) {
        //likeabilitySet[rateID].add(itemID);
      //}
    //} else {
      //throw new RuntimeException("Rating " + rating + " out of range in likeability set with length " + likeabilitySet.length);
    //}
  }
  
  /**
   * Counts the number of users who rated the give item.
   * 
   * @param itemID ID of the item
   * @return the number of users who rated the item
   */
  public int getNumberOfUsers(int itemID) {
    return sumOccurrences(ratingSet, itemID);
  }
  
  /**
   * Computes the averaged rating of the users who rated the given item.
   * 
   * @param itemID ID of the item
   * @return averaged user rating
   */
  public double getAverageRating(int itemID) {
    double avg = 0.0;
    double sum = 0.0;
    for (double rating : ratingSet.keySet()) {
      double occurs = ratingSet.get(rating).contains(itemID);
      avg += occurs * rating;
      sum += occurs;
    }
    //for (int i = 0; i < ratingSet.length; i ++) {
      //if (ratingSet[i] != null) {
        //avg += ratingSet[i].contains(itemID) * (double)(i+1);
        //sum += ratingSet[i].contains(itemID);
      //}
    //}
    return (sum > 0.0) ? avg/sum : 0.0;
  }
  
  /**
   * Returns the number of users who liked the give item.<br/>
   * Precisely, it computes the number of users who rated the given item and
   * added higher rate than their averaged rate.
   * 
   * @param itemID ID of the item
   * @return number of users who liked the item
   */
  public int getNumberOfLikeableUsers(int itemID) {
    return sumOccurrences(likeabilitySet, itemID);
  }
  
  /**
   * Returns the sum of occurrences of the given item in the sets.
   *  
   * @param sets array of sets in which we seek for occurrences
   * @param itemID item ID
   * @return sum of occurrences
   */
  private int sumOccurrences(TreeMap<Double, MultiBloomFilter> sets, int itemID) {
    int sum = 0;
    for (double rating : sets.keySet()) {
      sum += sets.get(rating).contains(itemID);
    }
    //for (int i = 0; i < sets.length; i ++) {
      //if (sets[i] != null) {
        //sum += sets[i].contains(itemID);
      //}
    //}
    return sum;
  }
  
  /**
   * Merges the specified ItemFrequencies object to this.
   * @param a object to merge
   * @return this
   */
  public ItemFrequencies merge(ItemFrequencies a) {
    throw new RuntimeException("Does not work yet!!!");
    //if (ratingSet != null && likeabilitySet != null && a.ratingSet != null && a.likeabilitySet != null) {
      //for (int i = 0; i < a.ratingSet.length; i++) {
        //if (ratingSet[i] != null && likeabilitySet[i] != null && a.ratingSet[i] != null && a.likeabilitySet[i] != null) {
          //ratingSet[i].merge(a.ratingSet[i]);
          //likeabilitySet[i].merge(a.likeabilitySet[i]);
        //}
      //}
    //}
    //return this;
  }
  
  public void resetCounter() {
    //System.err.println("RESET");
    counter = 0;
    prevNodeId = -1;
  }
  
  public double numOfUpdates() {
    return counter;
  }
  
  @Override
  public String toString() {
    StringBuffer br = new StringBuffer();
    br.append("rating: ");
    StringBuffer bl = new StringBuffer();
    for (double rating : ratingSet.keySet()) {
      br.append(rating + ": " + ratingSet.get(rating) + "\t");
      bl.append(rating + ": " + likeabilitySet.get(rating) + "\t");
    }
    //for (int i = 0; i < ratingSet.length; i++) {
      //br.append(i + ": " +ratingSet[i] + "\t");
      //bl.append(i + ": " + likeabilitySet[i] + "\t");
    //}
    return br.append(", like: ").append(bl).toString();
  }
}
