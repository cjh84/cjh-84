set term post color
set output "n_learner.ps"

set title 'Type of learner'
set nokey

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

set xlabel "Type of learner"
set xrange [-1:3]
set ylabel "RMSE"
                                                  
plot 'n_learner.dat' using 1:7 title "RMSE after training" with points pointtype 2 pointsize 1

