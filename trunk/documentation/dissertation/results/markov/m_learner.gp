set term post color
set output "m_learner.ps"

set title 'Type of learner'
set key outside

set xlabel "Learner"

set ylabel "Accuracy"
set yrange [0:100]

set y2label "Time taken in ms"
#set y2range [0:25000]
set y2tics

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

plot 'm_learner.dat' using 5:xtic(1) title "Correct", \
                         '' using 6:xtic(1) title "False positives", \
                         '' using 7:xtic(1) title "False negatives", \
                         '' using 8:xtic(1) title "Incorrect", \
                         '' using 9:($4/1000000) title "Time taken" with points pointtype 5 pointsize 2 axes x1y2
