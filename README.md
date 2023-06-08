# cinematic

This project preserves the information about 732 Chinese films produced in the "Seventeen Years Period" (1949-1966).

## Data

### Sources

[Modelled data of the 656 entries](metadata.csv) in *The Catalogue of Chinese Artistic Films* (*中国艺术影片编目*; China Film Archive, 1982) with romanised title, original title, translated title, release year, production, colour, length in film reels, and recorded special aspects

[The staff information and plot summary](metadata-staff_plot.csv) of the 656 entries, separated from the main file due to the large file sizes

[An extra collection](metadata-extra.csv) of 76 entries not included by the book

Some [formatted csv files](Network/csv) that can be directly imported to **Gephi** for social network analysis (7293 nodes and 259622 edges)

[Results of Topic Modelling](Topic/Topics_Summarised.csv) generated with a Gensim model trained with 9312 stopwords (including 7150 names of fictional characters from the OCR results) and 16 topics

### Visualisation

[A filmography visualiser in JavaScript](https://el-mundo.github.io/cinematic/Visualiser/main.html) for previewing the dataset

[A Plotly scatter map](https://htmlpreview.github.io/?https://github.com/El-Mundo/cinematic/blob/master/GIS/Plotly/filmmaker_map-scatter2d.html) of filmmakers/actors/actresses' geographical movement

[A Gephi file](Network/Gephi-all.gephi) of a social network generated based on the staff information of all 732 entries
