set term post color
set output "n_hidden_nodes.ps"

set title 'Nodes in hidden layer'
set nokey

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

set xlabel "Number of nodes in hidden layer"
set ylabel "RMSE"
                         
plot 'n_hidden_nodes.dat' using ($5/5):7 title "RMSE after training" with points pointtype 2 pointsize 1
