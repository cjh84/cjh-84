set term post color
set output "n_epochs.ps"

set title 'Epochs'
set nokey

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

set xlabel "Number of epochs"        
set ylabel "RMSE"
                         
plot 'n_epochs.dat' using ($4):7 title "RMSE after training" with points pointtype 2 pointsize 1

