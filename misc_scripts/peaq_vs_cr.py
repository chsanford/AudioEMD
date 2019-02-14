import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys
import colorsys


def HSVToRGB(h, s, v):
	(r, g, b) = colorsys.hsv_to_rgb(h, s, v)
	return (r,g,b)
 
def getDistinctColors(n):
	huePartition = 1.0 / (n + 1)
	return [HSVToRGB(huePartition * value, 1.0, 1.0) for value in range(0, n)]

assert len(sys.argv) > 0

input_file = sys.argv[1]
print(input_file)

ps_dataset = pd.read_csv(input_file)

num_functions = max(ps_dataset.FUNCTION_INDEX) + 1

colors = getDistinctColors(num_functions)
print(colors)

plt.figure()
for f in range(num_functions):
	filtered_dataset = ps_dataset[ps_dataset.FUNCTION_INDEX == f]

	plt.scatter(
		filtered_dataset.PEAQ_OBJECTIVE_DIFFERENCE,
		filtered_dataset.COMPRESSION_RATIO,
		c=colors[f],
		marker='.',
		alpha=0.5,
		edgecolor='none',
		label=filtered_dataset.FUNCTION_NAME.tolist()[0])

plt.xlabel("PEAQ Objective Difference")
plt.ylabel("Compression Ratio")
plt.title("Comparison of Criteria over Samples with Different Functions")
plt.legend()
plt.show()


