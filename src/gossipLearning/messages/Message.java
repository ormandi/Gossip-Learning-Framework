package gossipLearning.messages;

/**
 * It is an annotation based interface which indicates that
 * the implementing class is a message i.e. it can be sent
 * through the network.<br/>
 * There is one very important, non-syntactical requirement for the
 * implementation: each piece of data in the message has to be a
 * <b>deep copy</b> of the original one. Breaking this requirement
 * the simulation could not be performed realistically!
 * 
 * @author Róbert Ormándi
 *
 */
public interface Message {
}
