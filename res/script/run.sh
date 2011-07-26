#!/bin/bash

export config=`basename $1`;
export memory="1G";
export result=`basename ${config} .txt`"_base_result.txt";
export restricted_result=`basename ${config} .txt`"_result.txt";
export picture=`basename ${config} .txt`".png";
export classPath=`ls ../lib/*\.jar | awk '{printf("../%s:",$0)}END{printf("../../bin/gossipLearning.jar")}'`;

mkdir -p results
cd ../res/config && java -Xmx${memory} -cp ${classPath} peersim.Simulator ${config} >../../bin/results/${result} 2>/dev/null && cd ../../bin
cat results/${result} | awk 'BEGIN{print "#iter\tmodelSim\terrorSimpe\terrorLocalVoted";}{if ($0 ~ /^[0-9]+/ && length($0) > 0) {if ($0 ~ /ModelObserver$/) {c = $1; m=$2;} else if ($0 ~ /\[0\]/) {e0=$2;} else if ($0 ~ /\[9\]/) {print c "\t" m "\t" e0 "\t" $2;}}}' > results/${restricted_result};
../res/script/plot.sh results/${restricted_result} results/${picture};

