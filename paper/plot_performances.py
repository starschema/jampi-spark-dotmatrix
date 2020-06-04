#!/usr/bin/env python3

from matplotlib import pyplot as plt
import pandas as pd
import seaborn as sns

def calculate_runtimes(source_file: str = "benchmark_results/results.csv",
                       destination_file: str = "figures/runtimes.pdf") -> None:
    df = pd.read_csv(source_file)[["app", "matrix size", "elapsed"]]

    sns.set(style="ticks", rc={"lines.linewidth": 2.5})
    fig, ax = plt.subplots()
    fig.set_size_inches(8, 8)
    ax.set_yscale('log')
    ax = sns.lineplot(x="matrix size", y="elapsed", hue="app", err_style="bars", ci=95, style="app",
                      style_order=["jampi", "mpi", "MLlib", "MLlib (p64)"],
                      hue_order=["jampi", "mpi", "MLlib", "MLlib (p64)"], data=df)
    ax.set(xlabel="Matrix size", ylabel="Runtime, ms")
    fig = ax.get_figure()
    fig.savefig(destination_file)


if __name__ == '__main__':
    calculate_runtimes()