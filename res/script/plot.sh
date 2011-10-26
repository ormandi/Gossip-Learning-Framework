#!/bin/bash

export fName=$1
export outName=$2

gnuplot << gptend
set term png large nocrop enhanced size 1280,1024
#set term png large nocrop enhanced font "/usr/share/fonts/truetype/arial.ttf" 14 size 1280,1024
set output "${outName}"

set style line 1 lt 1 lw 1 pt 3 lc rgb "red"
set style line 2 lt 1 lw 1 pt 3 lc rgb "blue"
set style line 3 lt 3 lw 1 pt 3 lc rgb "green"
set style line 4 lt 3 lw 1 pt 3 lc rgb "orange"
set style line 5 lt 4 lw 1 pt 3 lc rgb "purple"

set logscale x
set xrange [*:*]
set yrange [*:*]

#set view 0,180,1,1

set title "${fName}"

set ylabel "Avg. 0-1 Error"
set xlabel "Cycles"

plot \
	"$fName" u 1:2 w l t "P2Pegasos", \
	"$fName" u 1:3 w l t "PegasosMU", \
	"$fName" u 1:4 w l t "LogReg", \
	"$fName" u 1:5 w l t "LogRegMU", \
	"$fName" u 1:5 w l t "AdalineP";
gptend
