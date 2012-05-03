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
[QLectives](http://www.qlectives.eu/) (grant no.: 231200). Some related
publications can be found on our personal homepages
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
repository by typing `git clone git://github.com/RobertOrmandi/Gossip-Learning-Framework.git`.

* __building it__: The building process is supported with *ant*. To create a jar you have
to type `ant` in the root directory of the
project. This will produce *gossipLearning.jar* in the *bin*
directory of the project. (All of the libraries which are necessary for
building or running the project are included in the *lib* directory of
the project.)

* __running a predefined simulation__: To run a simulation applying one of the predefined scenario on the 
[Iris](http://archive.ics.uci.edu/ml/datasets/Iris) dataset you have to type the 
following code snippet `res/script/run.sh training_db evaluation_db 100 scenario result` 
(assuming a standard unix environment with installed java and
gnuplot). 
The parameters of the `run.sh` are pretty intuitives and you can find examples in the package.
The first two parameters refer to the training and evaluation databases respectively presented in [SVMLight
format](http://svmlight.joachims.org/). You can use the `res/db/iris_setosa_versicolor_train.dat` and 
`res/db/iris_setosa_versicolor_eval.dat` files respectively. The third parameter defines the
number of iterations. The fourth one describes the simulation environment.
Basically this is [Peersim](http://peersim.sourceforge.net/) 
config file template (config file with some variables that are 
instantiated based on the used training set). Here you can use 
`res/config/no_failure_applying_more_learners_voting10.txt` configuration file.
The results are generated in the *results* subdirectory given in the fifth parameter 
(it has to be created before the call of `run.sh`). 
Make sure to delete previously generated results before you rerun the simulation!
In the *res* directory of the
project you can find additional training datasets (*db* subdirectory) and other
configuration templates (*config* subdirectory). 

* __understanding the results__: The result charts can be found in the
*results* subdirectory of the project's. It should be similar to
[this](http://www.inf.u-szeged.hu/rgai/~ormandi/iris_setosa_versicolor.png)
figure. Each curve belongs to a certain type of learning algorithm
(see labels) and each point of the curves corresponds to a time moment
(see label of x-axis). Each point shows the averaged 0-1 error over the
different machine learning models stored by the nodes of the network
measured on a separated (i.e. not known by the learning algorithm)
evaluation set. As you can see each line drops down after a certain point
in time which means each algorithm converges.

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

*To set up your development environment* you should
__read our step-by-step guide__ which can be found here specifically
for Eclipse tIDE.


Citation
========

If you use GoLF in your scientific work or just you want to refer to
GoLF somewhere, please cite the following
[paper](http://dx.doi.org/10.1007/978-3-642-23400-2_49). The full
citation is

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

