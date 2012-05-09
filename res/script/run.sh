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

export mem="1G"
export dir=`dirname $0`
export basedir="${dir}/../.."
export cp=`find ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} '{printf("%s:",$1);}END{print basedir "/bin/gossipLearning.jar"}'`


if [ -s "${trainingFile}" -a -s "$evaluationFile" -a -s "$config_template" ]; then
  # generate config
  export n=`cat $trainingFile | wc -l`
  echo -e "ITERATIONS ${iter}\nNETWORK_SIZE ${n}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile" | ${dir}/generate_config.sh $config_template > ${out_dir}/config.txt
  
  # run simulation
  nice -n 19 java -Xmx${mem} -cp ${cp} peersim.Simulator ${out_dir}/config.txt | tee ${out_dir}/raw_output.txt

  # process output
  cat ${out_dir}/config.txt | grep modelNames | awk '{print $2}' | awk 'BEGIN{FS=",";}{for (i=1; i<=NF; i++) {print $i;}}' > ${out_dir}/tmp_names.txt
  cat ${out_dir}/raw_output.txt | awk -v names=${out_dir}/tmp_names.txt 'BEGIN{while ((getline l < names) > 0) {n[lon++]=l;}}{for (i=0; i < lon; i++) {p="\\t\\[" i "\\]$"; v="\t" n[i]; gsub(p, v);} if (length($0) > 0) {print $0;}}' > ${out_dir}/output.txt
  cat ${out_dir}/output.txt | awk -v out_dir=${out_dir} '{lines ++; if (lines == 1) {header = $0;} else {result=$1; for (i=2; i<=NF; i++) {if ($i ~ /#/) {break;} else {result=result "\t" $i;}}; file=$(i+1); for (j=i+2; j<=NF; j++) {file=file "_" $j;}; gsub("_-_", "_", file); file=out_dir "/" file ".txt"; print result >> file;}}'

  # remove temporary files
  rm -Rf ${out_dir}/raw_output.txt ${out_dir}/tmp_names.txt

  # plot files
  echo -e '#!/usr/bin/gnuplot\n\nset term png large nocrop enhanced size 1280,1024\nset output "all.png"\nset xrange [*:*]\nset yrange [*:*]\nset ylabel "Error"\nset xlabel "Cycles"\nset title "Results"\n\nplot \\' > ${out_dir}/all.gpt;
  
  export num_of_outs=`ls ${out_dir}/*.txt | grep -v output.txt | grep -v config.txt | grep -v all.gpt | wc -l`
  export i=0;
  for out_file in `ls ${out_dir}/*.txt | grep -v output.txt | grep -v config.txt | grep -v all.gpt`; do 
    export i=`expr $i + 1`
    plot_file=`basename ${out_file} .txt`
    plot_file="${out_dir}/${plot_file}.png"
    base_file=`basename ${out_file}`
    
    gnuplot << gptend
set term png large nocrop enhanced size 1280,1024
set output "${plot_file}"

#set logscale
set xrange [*:*]
set yrange [*:*]
set ylabel "Error"
set xlabel "Cycles"

set title "${out_file}"

plot "${out_file}" u 1:2 w l t "${out_file}"
gptend
    
    # create all.gpt
    if [ $i -lt $num_of_outs ]; then
      echo -e "\t\"${base_file}\" u 1:2 w l t \"${out_file}\", \\" >> ${out_dir}/all.gpt
    else
      echo -e "\t\"${base_file}\" u 1:2 w l t \"${out_file}\";" >> ${out_dir}/all.gpt
    fi
  done

  # create all in one plot
  chmod 755 ${out_dir}/all.gpt

else
  echo "Usage: $0 trainingFile evaluationFile iter config_template [out_dir]" >> "/dev/stderr"
fi
