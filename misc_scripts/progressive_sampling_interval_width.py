import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys
import math

fig, ax1 = plt.subplots()

colors = ['r', 'b', 'g', 'y']

assert len(sys.argv) > 2

num_input_files = len(sys.argv) - 2
output_file = sys.argv[1]
for j in range(num_input_files):
	input_file = sys.argv[j + 2]

	ps_dataset = pd.read_csv(input_file)

	function_names = sorted(list(set(ps_dataset.FUNCTION_INDEX)))

	num_iterations = max(ps_dataset.ITERATION) + 1
	num_functions = len(function_names)
	num_criteria = max(ps_dataset.CRITERION_INDEX) + 1

	num_samples_I = np.full(num_iterations, np.nan)
	obj_mean_IF = np.full((num_iterations, num_functions), np.nan)
	obj_min_IF = np.full((num_iterations, num_functions), np.nan)
	obj_max_IF = np.full((num_iterations, num_functions), np.nan)
	crit_mean_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)
	crit_min_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)
	crit_max_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)

	for _, row in ps_dataset.iterrows():
		i = int(row['ITERATION'])
		f = function_names.index(row['FUNCTION_INDEX'])
		c = int(row['CRITERION_INDEX'])
		num_samples_I[i] = row['NUMBER_SAMPLES']
		obj_mean_IF[i, f] = row['OBJECTIVE_MEAN']
		obj_min_IF[i, f] = row['OBJECTIVE_MIN']
		obj_max_IF[i, f] = row['OBJECTIVE_MAX']
		crit_mean_IFC[i, f, c] = row['CRITERION_MEAN']
		crit_min_IFC[i, f, c] = row['CRITERION_MIN']
		crit_max_IFC[i, f, c] = row['CRITERION_MAX']


	# Best Objective
	min_obj = np.nanargmin(obj_mean_IF[-1])
	print(function_names[min_obj])

	# Number of functions remaining at each step
	num_remaining = list()
	for i in range(num_iterations):
		num_remaining.append(len([
			f for f in range(num_functions)
				if obj_min_IF[i, f] <= obj_max_IF[i, min_obj]
					and not math.isnan(obj_min_IF[i, f])
				]))

	# plots width of intervals for best objective
	ax1.plot(
		num_samples_I,
		obj_max_IF[:, min_obj] - obj_min_IF[:, min_obj],
		colors[j],
		marker='o')
		

ax1.set_xlabel('Number of Samples')
ax1.set_ylabel('Objective Confidence Interval Width')#, color='b')
#ax1.tick_params('y', colors='b')
ax1.semilogx()

ax1.legend(['Finite Sample EMD', 'Hoeffding-Union', 'Asymptotic EMD', 'Gaussian-Chernoff'])

plt.title('PSP Results for CR Objective')

fig.tight_layout()
fig.savefig(output_file)

plt.show()