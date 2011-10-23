#!/bin/bash

export config=`basename $1`;
export memory="1G";
export result=`basename ${config} .txt`"_base_result.txt";
export restricted_result=`basename ${config} .txt`"_result.txt";
export picture=`basename ${config} .txt`".png";
export classPath=`find -L ../lib/ -name "*.jar"  | awk '{printf("../%s:",$0)}END{printf("../../bin/gossipLearning.jar")}'`;

mkdir -p results
cd ../res/config && java -Xmx${memory} -cp ${classPath} peersim.Simulator ${config} >../../bin/results/${result} && cd ../../bin
if [ -e results/${result} ]; then
  awk -f ../res/script/result.awk results/${result} > results/${restricted_result};
  ../res/script/plot.sh results/${restricted_result} results/${picture};
fi
