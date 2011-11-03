#!/bin/bash

export fName=$1
export outName=$2

export plot=`head -n 1 ${fName} | awk -v fName=${fName} '{for(i=2;i<=NF;i++){if(i<NF){delimiter=","}else{delimiter=";"}; printf("\"%s\" u 1:%d w l t \"%s\"%s", fName, i, $i, delimiter)}}'`;

gnuplot << gptend
set term png large nocrop enhanced font "/usr/share/fonts/truetype/arial.ttf" 14 size 1280,1024
set output "${outName}"

set style line 1 lt 1 lw 1 pt 3 lc rgb "red"
set style line 2 lt 1 lw 1 pt 3 lc rgb "blue"
set style line 3 lt 3 lw 1 pt 3 lc rgb "green"
set style line 4 lt 3 lw 1 pt 3 lc rgb "orange"

set logscale x
set xrange [*:*]
set yrange [*:*]

#set view 0,180,1,1

set title "${fName}"

set ylabel "Avg. 0-1 Error"
set xlabel "Cycles"

plot ${plot}
gptend







