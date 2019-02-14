import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys

assert len(sys.argv) > 0

input_file = sys.argv[1]
print(input_file)

bf_dataset = pd.read_csv(input_file)

# function_names = ['Lame MP3 Compression V1', 'Lame MP3 Compression V2',
# 	'Lame MP3 Compression V3', 'Lame MP3 Compression V4',
# 	'Lame MP3 Compression V5', 'Lame MP3 Compression V6',
# 	'Lame MP3 Compression V7', 'Lame MP3 Compression V8',
# 	'Lame MP3 Compression V9']

function_names = sorted(list(set(bf_dataset.FUNCTION_INDEX)))


criterion_names = ['PEAQ Objective Difference', 'PEAQ Objective Difference ^ 2',
    'PEAQ Objective Difference Variance', 'Compression Ratio', 'Compression Ratio ^ 2',
    'Compression Ratio Variance']

num_iterations = max(bf_dataset.ITERATION) + 1
num_functions = len(function_names)
num_criteria = round(max(bf_dataset.CRITERION_INDEX) + 1)

num_samples_I = np.full(num_iterations, np.nan)
obj_mean_IF = np.full((num_iterations, num_functions), np.nan)
obj_min_IF = np.full((num_iterations, num_functions), np.nan)
obj_max_IF = np.full((num_iterations, num_functions), np.nan)
crit_mean_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)
crit_min_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)
crit_max_IFC = np.full((num_iterations, num_functions, num_criteria), np.nan)

for _, row in bf_dataset.iterrows():
	i = int(float(row['ITERATION']))
	f = function_names.index((row['FUNCTION_INDEX']))
	print(row['CRITERION_INDEX'])
	c = int(float(row['CRITERION_INDEX']))
	num_samples_I[i] = row['NUMBER_SAMPLES']
	obj_mean_IF[i, f] = row['OBJECTIVE_MEAN']
	obj_min_IF[i, f] = row['OBJECTIVE_MIN']
	obj_max_IF[i, f] = row['OBJECTIVE_MAX']
	crit_mean_IFC[i, f, c] = row['CRITERION_MEAN']
	crit_min_IFC[i, f, c] = row['CRITERION_MIN']
	crit_max_IFC[i, f, c] = row['CRITERION_MAX']

# num_samples_I = np.asarray(
# 	[bf_dataset.NUMBER_SAMPLES[i * num_functions * num_criteria] \
# 	for i in range(num_iterations)])

# obj_mean_IF = np.asarray([[
# 	bf_dataset.OBJECTIVE_MEAN[i * num_functions * num_criteria + f * num_criteria]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])
# obj_min_IF = np.asarray([[
# 	bf_dataset.OBJECTIVE_MIN[i * num_functions * num_criteria + f * num_criteria]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])
# obj_max_IF = np.asarray([[
# 	bf_dataset.OBJECTIVE_MAX[i * num_functions * num_criteria + f * num_criteria]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])

# crit_mean_IFC = np.asarray([[[
# 	bf_dataset.CRITERION_MEAN[i * num_functions * num_criteria + f * num_criteria + c]
# 	for c in range(num_criteria)]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])
# crit_min_IFC = np.asarray([[[
# 	bf_dataset.CRITERION_MIN[i * num_functions * num_criteria + f * num_criteria + c]
# 	for c in range(num_criteria)]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])
# crit_max_IFC = np.asarray([[[
# 	bf_dataset.CRITERION_MAX[i * num_functions * num_criteria + f * num_criteria + c]
# 	for c in range(num_criteria)]
# 	for f in range(num_functions)]
# 	for i in range(num_iterations)])


# fig, ax = plt.subplots()
# plt.plot(
# 	num_samples_I,
# 	obj_mean_IF[:, 0])
# plt.fill_between(
# 	num_samples_I,
# 	obj_min_IF[:, 0],
# 	obj_max_IF[:, 0],
# 	alpha=0.2)
# ax.semilogx()
# plt.xlabel("Sample Size")
# plt.ylabel("Objective Value")
# plt.title("Mean Objectives and Confidence Intervals for Increasing Sample Size")
# plt.legend(function_names)
# fig.savefig(output_file)
# plt.show()


fig, ax = plt.subplots()
for f in range(num_functions):
	plt.plot(
		num_samples_I,
		obj_mean_IF[:, f])
	plt.fill_between(
		num_samples_I,
		obj_min_IF[:, f],
		obj_max_IF[:, f],
		alpha=0.2)
ax.semilogx()
plt.xlabel("Sample Size")
plt.ylabel("Objective Value")
plt.title("Mean Objectives and Confidence Intervals for Increasing Sample Size")
plt.legend(function_names)
fig.savefig(output_file)
plt.show()

# for c in range(num_criteria):
# 	fig, ax = plt.subplots()
# 	for f in range(num_functions):
# 		plt.plot(
# 			num_samples_I,
# 			crit_mean_IFC[:, f, c])
# 		plt.fill_between(
# 			num_samples_I,
# 			crit_min_IFC[:, f, c],
# 			crit_max_IFC[:, f, c],
# 			alpha=0.2)
# 	ax.semilogx()
# 	plt.xlabel("Sample Size")
# 	plt.ylabel("Criteria Value")
# 	plt.title("Mean Criteria " + criterion_names[c] + 
# 		" and Confidence Intervals for Increasing Sample Size")
# 	plt.legend(function_names)
# 	plt.show()

