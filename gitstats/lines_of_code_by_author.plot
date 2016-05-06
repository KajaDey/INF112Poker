set terminal png transparent size 640,240
set size 1.0,1.0

set terminal png transparent size 640,480
set output 'lines_of_code_by_author.png'
set key left top
set yrange [0:]
set xdata time
set timefmt "%s"
set format x "%Y-%m-%d"
set grid y
set ylabel "Lines"
set xtics rotate
set bmargin 6
plot 'lines_of_code_by_author.dat' using 1:2 title "Morten Lohne" w lines, 'lines_of_code_by_author.dat' using 1:3 title "Jostein Kringlen" w lines, 'lines_of_code_by_author.dat' using 1:4 title "Kristian Rosland" w lines, 'lines_of_code_by_author.dat' using 1:5 title "Andre Dyrstad" w lines, 'lines_of_code_by_author.dat' using 1:6 title "INF112v16-kro050" w lines, 'lines_of_code_by_author.dat' using 1:7 title "INF112v16-jkr028" w lines, 'lines_of_code_by_author.dat' using 1:8 title "Ragnhild Aalvik" w lines, 'lines_of_code_by_author.dat' using 1:9 title "INF112v16-kde005" w lines, 'lines_of_code_by_author.dat' using 1:10 title "INF112v16-ady006" w lines, 'lines_of_code_by_author.dat' using 1:11 title "INF112v16-vro006" w lines, 'lines_of_code_by_author.dat' using 1:12 title "INF112v16-raa009" w lines, 'lines_of_code_by_author.dat' using 1:13 title "INF112v16-hny003" w lines, 'lines_of_code_by_author.dat' using 1:14 title "Vegar Rørvik" w lines, 'lines_of_code_by_author.dat' using 1:15 title "INF112v16-nug003" w lines, 'lines_of_code_by_author.dat' using 1:16 title "INF112v16-yeh002" w lines, 'lines_of_code_by_author.dat' using 1:17 title "dsa002" w lines, 'lines_of_code_by_author.dat' using 1:18 title "Mariah Vaarum" w lines, 'lines_of_code_by_author.dat' using 1:19 title "Mariah" w lines, 'lines_of_code_by_author.dat' using 1:20 title "André Dyrstad" w lines
