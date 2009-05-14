set term post color
set output "n_learning_rate.ps"

set title 'Learning rate'
set nokey

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

set xlabel "Learning rate"
set ylabel "RMSE"
                         
plot 'n_learning_rate.dat' using ($2*10):7 title "RMSE after training" with points pointtype 2 pointsize 1 axes x1y2

