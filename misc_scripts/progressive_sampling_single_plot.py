import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys
import math

fig, ax1 = plt.subplots()
fig.set_figheight(4.2)
ax2 = ax1.twinx()

colors = ['r', 'b', 'g', 'y']

assert len(sys.argv) > 3

num_input_files = len(sys.argv) - 3
objective_name = sys.argv[1]
if objective_name == 'PEAQ-CR':
	objective_name = 'PEAQ + CR'
elif objective_name == 'PEAQ-2CR':
	objective_name = '1/3PEAQ + 2/3CR'
output_file = sys.argv[2]
for j in range(num_input_files):
	input_file = sys.argv[j + 3]

	ps_dataset = pd.read_csv(input_file)

	function_names = sorted(list(set(ps_dataset.FUNCTION_INDEX)))

	num_iterations = max(ps_dataset.ITERATION) + 1
	num_functions = len(function_names)
	num_criteria = max(ps_dataset.CRITERION_INDEX) + 1

	num_samples_I = np.full(num_iterations, np.nan)
	obj_mean_IF = np.full((num_iterations, num_functions), np.nan)
	obj_min_IF = np.maximum(0, np.full((num_iterations, num_functions), np.nan))
	obj_max_IF = np.minimum(1, np.full((num_iterations, num_functions), np.nan))
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

	if objective_name == "PEAQ + CR":
		obj_mean_IF  = obj_mean_IF / 2
		obj_min_IF  = obj_min_IF / 2
		obj_max_IF  = obj_max_IF / 2
	elif objective_name == "1/3PEAQ + 2/3CR":
		obj_mean_IF  = obj_mean_IF / 3
		obj_min_IF  = obj_min_IF / 3
		obj_max_IF  = obj_max_IF / 3

	obj_min_IF = np.maximum(0, obj_min_IF)
	obj_max_IF = np.minimum(1, obj_max_IF)



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

	# plots intervals for best objective
	ax1.plot(
		num_samples_I,
		obj_mean_IF[:, min_obj],
		colors[j],
		marker='o')
	ax1.fill_between(
		num_samples_I,
		obj_min_IF[:, min_obj],
		obj_max_IF[:, min_obj],
		color=colors[j],
		alpha=0.2)
	

	#plots number of remaining functions
	ax2.plot(num_samples_I, num_remaining, colors[j] + '--')
	

ax1.set_xlabel('Number of Samples')
ax1.set_ylabel('Objective Value')
if objective_name == 'PEAQ':
	ax1.set_ylim(0, 0.3)
elif objective_name == 'CR':
	ax1.set_ylim(0.15, 0.35)
elif objective_name == 'PEAQ + CR':
	ax1.set_ylim(0.2, 0.4)
elif objective_name == '1/3PEAQ + 2/3CR':
	ax1.set_ylim(0.25, 0.45)
ax1.semilogx()

lgd = ax1.legend(['Finite Sample EMD', 'Hoeffding-Union', 'Asymptotic EMD', 'Gaussian-Chernoff'],
	loc='upper center', bbox_to_anchor=(0.5, -0.15), ncol=2)

ax2.set_ylabel('Number of Remaining Functions')#, color='r')
ax2.set_ylim(-0.5, num_functions + 0.5)
ax2.semilogx()

plt.title('PSP Results for ' + objective_name + ' Objective')

fig.tight_layout()
fig.savefig(output_file, bbox_extra_artists=(lgd,), bbox_inches='tight')

plt.show()