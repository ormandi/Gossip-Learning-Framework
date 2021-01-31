package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.DenseCompressibleModel;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.messages.PushPullMessage;
import gossipLearning.models.DenseCompressedModel;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.codecs.Codec;

import peersim.util.RandPermutation;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

import java.util.Map;
import java.util.TreeMap;

/**
 * A gossip learning protocol that performs weighted robust push-pull averaging on Addable LearningModels.
 * DenseCompressibleModels are compressed using the specified Codec.
 */
public class DensePushPullLearningProtocol extends LearningProtocol {
  
  /**
  * Greediness parameter, from the range (0;1].
  * @config
  */
  private static final String PAR_ETA = "eta";
  
  /**
  * The Codec that the DenseCompressibleModels are compressed with.
  * @config
  */
  private static final String PAR_CODEC = "codec";
  
  /** 
   * The neighbour sampling strategy. 0 - don't use permutation (independent sampling); 1 - use permutation (it's re-generated after use)
   * @config
   */
  private static final String PAR_PERM = "permutation";
  
  protected final double eta;
  protected final Codec proto;
  protected final int permMode;
  
  /** Contains the same references as LearningProtocol.models. */
  private BQModelHolder latestModelHolder;
  private Map<Long,Connection> outgoing, incoming;
  protected RandPermutation neighborPerm = new RandPermutation(CommonState.r);
  
  /** Constructor which parses the contents of a standard Peersim configuration file. */
  public DensePushPullLearningProtocol(String prefix) {
    super(prefix);
    eta = Configuration.getDouble(prefix+"."+PAR_ETA);
    proto = (Codec)Configuration.getInstance(prefix+"."+PAR_CODEC);
    permMode = Configuration.getInt(prefix+"."+PAR_PERM,0);
  }
  
  /** To be used as part of Peersim initialization only. */
  protected DensePushPullLearningProtocol(DensePushPullLearningProtocol a) {
    super(a);
    if (a.latestModelHolder!=null)
      throw new UnsupportedOperationException("Cloning is not supported after any event is processed.");
    eta = a.eta;
    proto = a.proto; // the state of this instance is never changed
    permMode = a.permMode;
  }

  @Override
  public DensePushPullLearningProtocol clone() {
    return new DensePushPullLearningProtocol(this);
  }
  
  @Override
  public void activeThread() {
    if (!nodeIsOnline(currentNode,currentProtocolID))
      return;
    init();
    evaluate();
    train();
    // send push message to a random neighbor
    Linkable overlay = getOverlay();
    if (permMode==0)
      neighborPerm.reset(overlay.degree());
    for (int i=0; i<2; i++) {
      while (neighborPerm.hasNext()) {
        Node target = overlay.getNeighbor(neighborPerm.next());
        if (nodeIsOnline(target,currentProtocolID)) {
          getConnection(outgoing,target).sendPush();
          return;
        }
      }
      neighborPerm.reset(overlay.degree());
    }
  }
  
  @Override
  protected void updateModels(ModelHolder modelHolder) {
    init();
    PushPullMessage msg = (PushPullMessage)modelHolder;
    getConnection(msg.reply?outgoing:incoming,msg.getSource()).processMsg(msg);
  }
  
  @Override
  public void forceEvaluate(int pid) {
    init();
    super.forceEvaluate(pid);
  }
  
  private void init() {
    if (latestModelHolder!=null)
      return;
    outgoing = new TreeMap<Long,Connection>();
    incoming = new TreeMap<Long,Connection>();
    latestModelHolder = new BQModelHolder(models.length);
    for (int i = 0; i < models.length; i++)
      latestModelHolder.add(models[i]);
    train();
  }
  
  private void train() {
    for (int i = 0; i < latestModelHolder.size(); i++)
      ((LearningModel)latestModelHolder.getModel(i)).update(instances,epoch,batch);
  }
  
  private Connection getConnection(Map<Long,Connection> map, Node node) {
    Long key = node.getID();
    if (!map.containsKey(key))
      map.put(key,new Connection(node));
    return map.get(key);
  }
  
  /**
   * Container for Addable Models where the model parameters are weighted by age.
   */
  static class WeightedHolder {
  
    private ModelHolder holder;
    
    /** The argument will be used as the internal store for the WeightedHolder. */
    public WeightedHolder(ModelHolder mh) {
      holder = mh;
      convert(true);
    }
    
    /** Adds the specified WeightedHolder, multiplied by the specified factor, to the current instance. Returns a reference to the current instance. */
    public WeightedHolder add(WeightedHolder b, double factor) {
      for (int i=0; i<holder.size(); i++)
        ((Addable)holder.getModel(i)).add(b.holder.getModel(i),factor);
      return this;
    }
    
    /** Multiplies the current instance by the specified factor. Returns a reference to the current instance. */
    public WeightedHolder multiply(double factor) {
      return add(this,factor-1);
    }
    
    /** Adds the current instance, multiplied by the specified factor, to the specified ModelHolder. */
    public void addTo(ModelHolder mh, double factor) {
      new WeightedHolder(mh).add(this,factor).convert(false);
    }
    
    /** Performs the conversion of the internal ModelHolder to and from the weighted representation. */
    private void convert(boolean toWeighted) {
      for (int i=0; i<holder.size(); i++) {
        Model m = holder.getModel(i);
        double age = m.getAge();
        if (age<=0)
          throw new RuntimeException("Model age is not positive. Try decreasing eta.");
        Model m2 = m.clone();
        m.clear();
        ((Addable)m).add(m2,toWeighted?age:1/age);
        m.setAge(age);
      }
    }
    
  }
  
  /**
   * An outgoing or incoming connection that is used to communicate with a given remote node.
   * Outgoing connections send push messages and receive pull messages.
   * Incoming connections receive push messages and send pull messages.
   */
  class Connection {
  
    /** The remote node. */
    private Node node;
    /** Serial number of the last push message sent or received (and not rejected). */
    private int id;
    /** Number of times codec state was updated and a transaction was performed (minus rollbacks). */
    private int updates;
    /** Codec used for encoding local ModelHolder. */
    private HolderCodec local = new HolderCodec();
    /** Snapshot of local. (Used only by incoming connections.) */
    private HolderCodec localSnapshot;
    /** Codec used for decoding remote ModelHolder. */
    private HolderCodec remote = new HolderCodec();
    /** Snapshot of remote. (Used only by incoming connections.) */
    private HolderCodec remoteSnapshot;
    /** The last encoded ModelHolder sent. */
    private BQModelHolder sent;
    /** Last transfer. (Used only by incoming connections.) */
    private WeightedHolder last;
    
    public Connection(Node remoteNode) {
      node = remoteNode;
    }
    
    /** Sends a push message to the remote node. (For outgoing connections only.) */
    public void sendPush() {
      id++;
      sendMsg(false);
    }
    
    /** Processes a message received from the remote node. */
    public void processMsg(PushPullMessage msg) {
      if (msg.reply) { // an outgoing connection received a pull message
        if (id==msg.id) // it is the reply to the last push message
          update(msg);
      } else { // an incoming connection received a push message
        if (msg.id<=id) // message is out of order
          return;
        id = msg.id;
        if (msg.updates!=updates) { // rollback (last pull message was lost or delayed)
          updates--;
          assert msg.updates==updates;
          local = localSnapshot;
          remote = remoteSnapshot;
          last.addTo(latestModelHolder,1);
        }
        sendMsg(true); // sends a pull message to the remote node
        localSnapshot = new HolderCodec(local);
        remoteSnapshot = new HolderCodec(remote);
        last = update(msg);
      }
    }
    
    private void sendMsg(boolean reply) {
      sent = latestModelHolder.clone();
      local.encode(sent);
      getTransport().send(currentNode,node,new PushPullMessage(currentNode,node,sent,currentProtocolID,id,updates,reply),currentProtocolID);
    }
    
    /** Updates codec state and performs a transaction. */
    private WeightedHolder update(ModelHolder received) {
      updates++;
      WeightedHolder delta = new WeightedHolder(local.decode(sent)).add(new WeightedHolder(remote.decode(received)),-1).multiply(eta/2);
      delta.addTo(latestModelHolder,-1);
      return delta;
    }
    
  }
  
  /**
   * Lossily and adaptively encodes a correlated stream of ModelHolders containing DenseCompressibleModels to a stream of ModelHolders containing DenseCompressedModels, and vice versa.
   * Other Models in the ModelHolders are left unchanged.
   */
  class HolderCodec {
  
    private ModelCodec[] codecs = new ModelCodec[modelNames.length];
    
    public HolderCodec() {
      for (int i=0; i<codecs.length; i++)
        codecs[i] = new ModelCodec();
    }
    
    /** Deep copy constructor. */
    public HolderCodec(HolderCodec a) {
      for (int i=0; i<codecs.length; i++)
        codecs[i] = new ModelCodec(a.codecs[i]);
    }
    
    /** Encodes the next ModelHolder in-place. This does not update the HolderCodec state. */
    public ModelHolder encode(ModelHolder holder) {
      for (int i=0; i<codecs.length; i++) {
        Model model = holder.getModel(i);
        if (model instanceof DenseCompressibleModel)
          holder.setModel(i,codecs[i].encode((DenseCompressibleModel)model));
      }
      return holder;
    }
    
    /** Decodes the next ModelHolder in-place. This also updates the HolderCodec state. */
    public ModelHolder decode(ModelHolder holder) {
      for (int i=0; i<codecs.length; i++) {
        Model model = holder.getModel(i);
        if (model instanceof DenseCompressedModel)
          holder.setModel(i,codecs[i].decode((DenseCompressedModel)model));
      }
      return holder;
    }
    
  }
  
  /**
   * Lossily and adaptively encodes a correlated stream of DenseCompressibleModels to a stream of DenseCompressedModels, and vice versa.
   */
  class ModelCodec {
  
    private Codec[] codecs;
    
    public ModelCodec() {
    }
    
    /** Deep copy constructor. */
    public ModelCodec(ModelCodec a) {
      if (a.codecs!=null) {
        codecs = new Codec[a.codecs.length];
        for (int i=0; i<codecs.length; i++)
          codecs[i] = a.codecs[i].clone();
      }
    }
    
    /** Encodes the next DenseCompressibleModel. The returned DenseCompressedModel retains a reference to the DenseCompressibleModel. This does not update the ModelCodec state. */
    public DenseCompressedModel encode(DenseCompressibleModel model) {
      double[] data = model.getData();
      Object[] compressed = new Object[data.length];
      init(data.length);
      for (int i=0; i<data.length; i++)
        compressed[i] = codecs[i].encode(data[i]);
      return new DenseCompressedModel(compressed,model);
    }
    
    /** Decodes the next DenseCompressibleModel. The modified and returned DenseCompressibleModel instance is the one stored in the DenseCompressedModel. This also updates the ModelCodec state. */
    public DenseCompressibleModel decode(DenseCompressedModel compressedModel) {
      double[] data = new double[compressedModel.compressedData.length];
      init(data.length);
      for (int i=0; i<data.length; i++)
        data[i] = codecs[i].decode(compressedModel.compressedData[i]);
      DenseCompressibleModel model = compressedModel.model;
      model.setData(data);
      //model.setAge(agecodec.decode(agecodec.encode(model.getAge()/1024))*1024);
      return model;
    }
    
    private void init(int length) {
      if (codecs==null) {
        codecs = new Codec[length];
        for (int i=0; i<length; i++)
          codecs[i] = proto.clone();
      }
    }
    
  }
  
  //static Codec agecodec = new gossipLearning.utils.codecs.F16("");

}
