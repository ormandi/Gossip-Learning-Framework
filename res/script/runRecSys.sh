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
export mem="12G"
export dir=`dirname $0`
export basedir="${dir}/../.."
export cpdelim=`java -help 2>&1 | grep -A 1 "classpath" | tail -n 1 | awk '{print $2}'`
export cp=`${findCmd} ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} -v cpdelim=${cpdelim} '{printf("%s%s",$1,cpdelim);}END{print basedir "/bin/gossipLearning.jar"}'`


if [ -s "${trainingFile}" -a -s "$evaluationFile" -a -s "$config_template" ]; then
  # generate config
  export n=`cat $trainingFile | awk '{if ($1 > max) {max = $1} }END{print max}'`
  echo -e "ITERATIONS ${iter}\nNETWORK_SIZE ${n}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile" | ${dir}/generate_config.sh $config_template > ${out_dir}/config.txt
  
  # run simulation
  nice -n 19 java -Xmx${mem} -cp ${cp} peersim.Simulator ${out_dir}/config.txt | tee ${out_dir}/output.txt
  
  # create gpt script
  echo -e '#!/usr/bin/gnuplot\n\nset term png large nocrop enhanced size 1280,1024\nset output "output.png"\nset xrange [*:*]\nset yrange [*:*]\nset ylabel "Error"\nset xlabel "Cycles"\nset title "Results"\n\nplot "output.txt" u 1:2 w l t "MAE"' > ${out_dir}/output.gpt;

  # create all in one plot
  chmod 755 ${out_dir}/output.gpt
  export currentDir=`pwd`
  cd ${out_dir}
  ./output.gpt
  cd ${currentDir}

else
  echo "Usage: $0 trainingFile evaluationFile iter config_template [out_dir]" >> "/dev/stderr"
fi
