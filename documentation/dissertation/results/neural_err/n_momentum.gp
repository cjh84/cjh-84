set term post color
set output "n_momentum.ps"

set title 'Momentum'
set nokey

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

set xlabel "Momentum"
set ylabel "RMSE"
                         
plot 'n_momentum.dat' using ($3*10):7 title "RMSE after training" with points pointtype 2 pointsize 1
