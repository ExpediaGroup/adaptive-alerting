import os
import papermill as pm

def train_stl(datasetName, intervalMinutes, numWeeks):
    input = "stl.ipynb"
    output = "out/stl-" + datasetName + ".ipynb"
    pm.execute_notebook(input, output, parameters = dict(
        datasetName = datasetName,
        intervalMinutes = intervalMinutes,
        numWeeks = numWeeks
    ))

def train_all(name, intervalMinutes, numWeeks):
    train_stl(name, intervalMinutes, numWeeks)

dir = "./out"
if not os.path.exists(dir):
    os.mkdir(dir)

# Different training params
train_all('sample_data', 5, 4)