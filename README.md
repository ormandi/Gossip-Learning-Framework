Gossip Learning Framework (GoLF)
================================

This open source benchmarking framework allows you to *build* your own
__P2P learning algorithm__ and *evaluate* it in a simulated but
realistic -- where you can model message delay, drop or churn --
networked environment. Moreover it contains the prototype
implementations of some well-known machine learning algorithms like SVM
and Logistic Regression. (More will be coming soon.)

The project is related to our academic research and it is partially
supported by the Future and Emerging Technologies programme FP7-COSI-ICT
of the European Commission through project
[QLectives](http://www.qlectives.eu/) (grant no.: 231200), by the European Union and the European Social Fund through project FuturICT.hu (grant no.: TAMOP-4.2.2.C-11/1/KONV-2012-0013). Some related
publications can be found on our personal home pages
([here](http://www.inf.u-szeged.hu/~ormandi/index.php?menu=publications)
and [here](http://www.inf.u-szeged.hu/~ihegedus/publ.php)) and on
[arXiv](http://arxiv.org/abs/1109.1396).


Getting Started
===============

This framework includes some predefined learning scenarios based on the
prototype implementation of the machine learning algorithms and the
well-known [Iris learning database](http://archive.ics.uci.edu/ml/datasets/Iris). To play with
them you have to perform the following steps:

* __getting the source__: First you have to download the source code of
the framework. Probably the easiest way to do that is cloning this git
repository by typing `git clone -b multicore https://github.com/RobertOrmandi/Gossip-Learning-Framework.git`. 
Additional possibilities are to download as [zip archive](https://github.com/RobertOrmandi/Gossip-Learning-Framework/archive/multicore.zip).

* __building it__: The building process is supported with *ant*. To create a jar you have
to type `ant` in the root directory of the
project. This will produce *gossipLearning.jar* in the *bin*
directory of the project. (All of the libraries which are necessary for
building or running the project are included in the *lib* directory of
the project.)

Running a predefined simulation
---------------------------------------
To run a simulation applying one of the predefined scenarios on the 
[Iris](http://archive.ics.uci.edu/ml/datasets/Iris) dataset you have to type the 
following code in the project directory: 
`export classpath="bin/gossipLearning.jar:."`
`java -cp $classpath gossipLearning.main.LocalRun res/config/LocalLearning.txt`. 
This will run a local SGD (but not a P2P) learning based on the defined configuration file `LocalLearning.txt`.

The meaning of the configuration file:

    ITER 100 #number of SGD iterations
    SEED 1234567890 #random seed
    NUMEVALS 10 #number of evaluations

    SAMPLING uniform #uniform/iterative default is uniform
    #NORMALIZATION standardize #standardize/normalize not required

    dbReader gossipLearning.utils.DataBaseReader
    trainingFile res/db/iris_versicolor_virginica_train.dat #training set
    evaluationFile res/db/iris_versicolor_virginica_eval.dat #evaluation/test set

    learner.PegasosSVM gossipLearning.models.learning.P2Pegasos #learning method
    learner.PegasosSVM.lambda 0.01 #parameters of the learning method

    evaluators gossipLearning.evaluators.ZeroOneError #type of evaluation
    printPrecision 4 #evaluation precision (number of floating points)

Here the learning method and the type of evaluation can be changed or can be used
more (by using a comma separated list). Basically this is a [Peersim](http://peersim.sourceforge.net/) configuration file.

* __learning methods:__
  * Pegasos SVM: `gossipLearning.models.learning.P2Pegasos`
  * Logistic Regression: `gossipLearning.models.learning.LogisticRegression`
  * ...
* __types of evaluation__
  * 0-1 error: `gossipLearning.evaluators.ZeroOneError`
  * mean absolute error: `gossipLearning.evaluators.MAError`
  * ...

We assume that the training and evaluation datasets are presented in [SVMLight
format](http://svmlight.joachims.org/). The result will be printed on the standard output and 
the comments on the standard error channel.

* __understanding the results__:

For the above shown configuration file the result is the following:

    Loading parameters from res/config/LocalLearning.txt
        Number of iterations: 100
        Random seed: 1234567890
        training file: res\db\iris_versicolor_virginica_train.dat
        evaluation file: res\db\iris_versicolor_virginica_eval.dat
        Sampling method: uniform
        Normalization method: none
        Batch size: 1
    Reading data set.
    Start learning.
    #iter   mean    dev     min     max             -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    0       0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    1       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    2       0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    3       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    4       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    5       0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    6       0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    7       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    8       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    9       0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    10      0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    20      0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    30      0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    40      0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    50      0.6325  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    60      0.3162  0.3000  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    70      0.7746  0.4899  0.0000  1.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    80      0.0000  0.0000  0.0000  0.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    90      0.0000  0.0000  0.0000  0.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    100     0.0000  0.0000  0.0000  0.0000  -       -1      learner.PegasosSVM      gossipLearning.evaluators.RMSError
    Final result:
    learner.PegasosSVM:
    0.0     0.0     0.0     0.0
    
    ELAPSED TIME: ???ms

The first column represents the iteration number, after that the mean error rate with 
its deviation, minimum and maximum. Of course here were evaluated only one model, so 
the error deviation is 0 and the mean, min and max are equals. But in P2P setting they can give important information.

* __running a P2P simulation__ 
`java -cp $classpath peersim.Simulator res/config/configFile`
 * use the `P2PLearning.txt` configuration file.
 * with network failures use the `P2PLearningFailures.txt` configuration file

* __write your own model__

Copy this code into the DummyModel.java in your project folder:

    import gossipLearning.interfaces.models.LearningModel;
    import gossipLearning.utils.SparseVector;
    
    public class DummyModel implements LearningModel {
      public DummyModel(String prefix) {
        // get configuration parameters
      } 
      public DummyModel(DummyModel a) {
        // copy constructor
      }
      public Object clone() {
        return new DummyModel(this);
      }
      public void update(SparseVector instance, double label) {
        // do nothing, but updates the model
      }
      public void update(InstanceHolder instances) {
        // for full batch update
      }
      public void update(InstanceHolder instances, int epoch, int batchSize) {
        // for mini-batch update
      }
      public double predict(SparseVector instance) {
        // predicts the label, here always 0
        return 0.0;
      }
      public double getAge() {
        return 0.0;
      }
      public void setAge(double age) {
      }
      public void setParameters(int numberOfClasses, int numberOfFeatures) {
        // to handle database related parameters
      }
      public void clear() {
        // to clear the model
      }
      
    }

Compile the file
`javac -cp $classpath DummyModel.java`

Add this line to the configuration file

    learner.Dummy DummyModel

run the simulation.

Running a recommender system model
-------------------------------------------------

We have a matrix factorization based recommender system model in our framework, that can be used both local and P2P settings.

* __local setting__
`java -cp $classpath gossipLearning.main.RecSysRun res/config/LocalRecSys.txt`
* __P2P setting__
`java -cp $classpath peersim.Simulator res/config/P2PRecSys.txt`

This is just the tip of the iceberg since the framework provides an
*API* which makes it extensible, i.e. you can implement new learning
algorithms or protocols. Or you can define other network scenarios using
the configuration mechanism of
[Peersim](http://peersim.sourceforge.net/).


Further Reading
===============

Since the GoLF is built on the top of
[Peersim](http://peersim.sourceforge.net/), *for the deeper understanding
of the underlying mechanism* you should be __familiar with Peersim__.
You should understand the following tutorials:
[this](http://peersim.sourceforge.net/tutorial1/tutorial1.pdf) and
[this](http://peersim.sourceforge.net/tutorial2/tutorial2.pdf).
This is also necessary for understanding the configuration
files of GoLF.

*To develop a new algorithm or protocol* you have to know the
__details of the Gossip Learning Framework__. This was described in
[this paper](http://arxiv.org/abs/1109.1396) and a slightly simplified version
can be found in the wiki of the project.

You are almost done. But before *you start development* be sure you
understand __the inner design concepts of the implementation of GoLF__.
You can read about this part of the project wiki where a class diagram
is also shown.


Citation
========

If you use GoLF in your scientific work or just you want to refer to
GoLF somewhere, please cite the followings
[paper1](http://dx.doi.org/10.1007/978-3-642-23400-2_49) and 
[paper2](http://dx.doi.org/10.1002/cpe.2858). The full
citations are

	@inproceedings{ormandi2011asynchronP2PDM,
	  author = {R{\'o}bert Orm{\'a}ndi and Istv{\'a}n Heged\H{u}s and M{\'a}rk Jelasity},
	  title = {Asynchronous Peer-to-Peer Data Mining with Stochastic Gradient Descent},
	  booktitle = {17th International European Conference on Parallel and Distributed Computing (Euro-Par 2011)},
	  year = {2011},
	  pages = {528-540},
	  series = {Lecture Notes in Computer Science},
	  volume = {6852},
	  publisher = {Springer-Verlag},
	  ee = {http://dx.doi.org/10.1007/978-3-642-23400-2_49},
	  bibsource = {http://www.inf.u-szeged.hu/~ormandi/papers/ormandi2011asynchronP2PDM.bib}
	}
	@article {CPE:CPE2858,
	  author = {Orm\'{a}ndi, R\'{o}bert and Heged\H{u}s, Istv\'{a}n and Jelasity, M\'{a}rk},
	  title = {Gossip learning with linear models on fully distributed data},
	  journal = {Concurrency and Computation: Practice and Experience},
	  volume = {25},
	  number = {4},
	  publisher = {John Wiley & Sons, Ltd},
	  issn = {1532-0634},
	  url = {http://dx.doi.org/10.1002/cpe.2858},
	  doi = {10.1002/cpe.2858},
	  pages = {556--571},
	  keywords = {P2P, gossip, bagging, online learning, stochastic gradient descent, random walk},
	  year = {2013},
	}

