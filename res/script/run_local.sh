#!/bin/bash

export trainingFile="$1"
export evaluationFile="$2"
export iter=$3
export config_template="$4"

if [ $# -eq 5 -a -d "$5" ]; then
  export out_dir="$5";
else
  export out_dir="."
fi

export findCmd="/usr/bin/find"
export mem="1G"
export dir=`dirname $0`
export basedir="${dir}/../.."
export cpdelim=`java -help 2>&1 | grep -A 1 "classpath" | tail -n 1 | awk '{print $2}'`
export cp=`${findCmd} ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} -v cpdelim=${cpdelim} '{printf("%s%s",$1,cpdelim);}END{print basedir "/bin/gossipLearning.jar"}'`


if [ -s "${trainingFile}" -a -s "$evaluationFile" -a -s "$config_template" ]; then
  # generate config
  echo -e "ITERATIONS ${iter}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile" | ${dir}/generate_config.sh $config_template > ${out_dir}/config.txt
  
  # run simulation
  nice -n 19 java -Xmx${mem} -cp ${cp} gossipLearning.main.LocalRun ${out_dir}/config.txt | tee ${out_dir}/output.txt

  export modelName=`head -n 1 ${out_dir}/output.txt | awk '{print $NF}'`;
  
  # plot file
  plot_file="${out_dir}/result.png"
  
  gnuplot << gptend
set term png large nocrop enhanced size 1280,1024
set output "${plot_file}"

set xrange [*:*]
set yrange [*:*]
set ylabel "Error"
set xlabel "Iterations"

set title "${modelName}"

plot "${out_dir}/output.txt" u 1:2 w l t "TrainingErr", \
     "${out_dir}/output.txt" u 1:3 w l t "TestErr";
gptend
  
else
  echo "Usage: $0 trainingFile evaluationFile iter config_template [out_dir]" >> "/dev/stderr"
fi
