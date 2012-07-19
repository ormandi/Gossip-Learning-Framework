package gossipLearning.models.recSys;

import gossipLearning.interfaces.Model;
import peersim.core.Node;

/**
 * <p>This defines a special type model for implementing recommender systems in GoLF.</p>
 * 
 * <p>We assume that when implementing a recommender model, the number of classes contains the number of ratings.<br/>
 * More over the below defined method is responsible for store the number of items which is known for everybody in
 * the network. The main assumption behind this is that it exists a service which spreads the new items
 * in the network one-by-one. In this way the items are known for everybody in the network. 
 * However the global characteristics of these items (i.e. frequencies) is not known i.e. 
 * it has to be computed by the learning protocol both time and size efficiently.</p>
 * 
 * <p>This type of model knows that which node currently holds it. A method for querying this information
 * provided as well.</p> 
 *  
 * @author Róbert Ormándi
 */
public interface RecSysModel extends Model {
  public void setNumberOfItems(int items);
  public int getNumberOfItems();
  
  public Node getNode();
}
