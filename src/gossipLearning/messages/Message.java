package gossipLearning.messages;

/**
 * It is an annotation based interface which indicates that
 * the implementing class is a message i.e. it can be sent
 * through the network.<br/>
 * There is one very important, non-syntactical requirements for the
 * implementation: each data piece contained by the message has to be a 
 * <b>deep copy</b> of the original one. Breaking this requirement
 * the simulation could not be performed realisticly!
 * 
 * @author Róbert Ormándi
 *
 */
public @interface Message {
}
