Gossip Learning Framework (GoLF)
================================

This open source benchmarking framework allows you to *build* your own __P2P learning algorithm__ and *evaluate* it in a simulated but realistic -- where you can model message delay, drop or churn -- networked environment. Moreover it contains the prototype implementations of some well-known machine learning algorithms like SVM and Logistic Regression. (More will be coming soon.)

The project is related to our academic research and it is partially supported by the Future and Emerging Technologies programme FP7-COSI-ICT of the European Commission through project [QLectives](http://www.qlectives.eu/) (grant no.: 231200). Some related publications can be found on our personal homepages ([here](http://www.inf.u-szeged.hu/~ormandi/index.php?menu=publications) and [here](http://www.inf.u-szeged.hu/~ihegedus/publ.php)) and on the [arxiv](http://arxiv.org/abs/1109.1396).


Getting Started
===============

This framework includes some predefined learning scenarios based on the prototype implementation of the machine learning algorithms and the well-known [Iris learning database](http://archive.ics.uci.edu/ml/datasets/Iris). To play with them you have to perform the following steps:



This is just the tip of the iceberg since the framework provides an *API* which makes it extensible i.e. you can implement new learning algorithm or protocol. Or you can define other network scenarios using the configuration mechanism of [Peersim](http://peersim.sourceforge.net/). 

Further Reading
===============

Since the GoLF is built on the top of [Peersim](http://peersim.sourceforge.net/) for a deeper understanding of the underlying mechanism you should be familiar with [this](http://peersim.sourceforge.net/tutorial1/tutorial1.pdf) and [this](http://peersim.sourceforge.net/tutorial2/tutorial2.pdf). This is also necessary for understanding the configuration files.

To develop a new algorithm you have to know the details of the Gossip Learning Framework. This was described in [this paper](http://arxiv.org/abs/1109.1396) and a slightly simplified version can be found in the wiki of the project.

You are almost done. But before you start development be sure you understand the inner design concepts of GoLF. You can read about it here. 

To set up your development environment you can find a step-by-step guide here.

Citation
========

If you use GoLF in your scientific work or just you want to refer to GoLF somewhere, please cite the following [paper](http://dx.doi.org/10.1007/978-3-642-23400-2_49). The full citation is 
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
	bibsource = {DBLP, http://dblp.uni-trier.de}
	}


