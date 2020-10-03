# JAMPI: Java Assisted Matrix Product with Inter-task communication

[![DOI](https://zenodo.org/badge/265725621.svg)](https://zenodo.org/badge/latestdoi/265725621)

Source repository for Foldi, von Csefalvay and Perez (2020), _JAMPI: efficient matrix multiplication in Spark using Barrier Execution Mode_.

## Building the paper

To build the paper, go to the `/paper` folder, and use the `make` function:

* `make distill`: returns camera-ready PDF
* `make plots`: renders automated figures
* `make snapshot`: creates a snapshot PDF, appending the Github hash to the filename
* `make view`: opens typeset file
* `make clean`: removes TeX compilation artifacts

*The MPI code used for matrix multiplication and referenced in this paper can be found in the [MPI Matrix Multiplication](https://github.com/anicolaspp/Parallel-Computing-MPI-Matrix-Multiplication) repository*