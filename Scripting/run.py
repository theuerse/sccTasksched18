import os
from subprocess import check_output
from operator import itemgetter

# overall settings
RESULT_FOLDER = os.path.abspath("./results")
BIN_FOLDER = os.path.abspath("./bin")
MOHEFT_RUNS = 2
TASK_SCHEDULER_SETUP = "javac ../MOHEFT/src/*.java && mv ../MOHEFT/src/*.class " + BIN_FOLDER
TASK_SCHEDULER_APP = "MOHEFT"
COMPARATION_HELPERS_SETUP = "javac ../Hypervolume/src/*.java && mv ../Hypervolume/src/*.class " + BIN_FOLDER + " && cp ../Hypervolume/python/*.py " + BIN_FOLDER
NADIR_APP = "NadirPoint"
UTOPIA_APP = "UtopiaPoint"
HYPER_VOLUME_APP = "calculateHyperVolume.py"

# general settings (written to config-file)
N=20
K=6
MAX_INSTANCES_PER_TYPE=5
DAG_FILE="/home/theuers/sccTasksched18/MOHEFT/data/dag.txt"
COST_FILE="/home/theuers/sccTasksched18/MOHEFT/data/costs.CSV"
MAKESPAN_FILE="/home/theuers/sccTasksched18/MOHEFT/data/makespan.CSV"
# automatically generated
MOHEFT_OUTPUT_FILE=""
NADIR_OUTPUT_FILE = os.path.join(RESULT_FOLDER, "Nadir_point.txt")
UTOPIA_OUTPUT_FILE = os.path.join(RESULT_FOLDER,"Utopia_point.txt")


def writeLogFile(path):
    content = []
    content.append("N="+str(N))
    content.append("K="+str(K))
    content.append("MAX_INSTANCES_PER_TYPE="+str(MAX_INSTANCES_PER_TYPE))
    content.append("DAG_FILE="+str(DAG_FILE))
    content.append("COST_FILE="+str(COST_FILE))
    content.append("MAKESPAN_FILE="+str(MAKESPAN_FILE))
    content.append("MOHEFT_OUTPUT_FILE="+str(MOHEFT_OUTPUT_FILE))
    content.append("NADIR_OUTPUT_FILE="+str(NADIR_OUTPUT_FILE))
    content.append("UTOPIA_OUTPUT_FILE="+str(UTOPIA_OUTPUT_FILE))

    with open(path, "w") as configFile:
        configFile.write("\n".join(content))



currentDir = os.path.abspath("./")
configFilePath = os.path.join(currentDir, "config.txt")

# create result-folder if not already existing
if not os.path.isdir(RESULT_FOLDER):
    os.mkdir(RESULT_FOLDER)
# clear result-folder
os.system("rm " + RESULT_FOLDER + "/*")

# create bin-folder if not already existing
if not os.path.isdir(BIN_FOLDER):
    os.mkdir(BIN_FOLDER)
# clear bin-folder
os.system("rm " + BIN_FOLDER + "/*")


# set up task scheduler
os.system(TASK_SCHEDULER_SETUP)

# set up helper programs for comparison
os.system(COMPARATION_HELPERS_SETUP)


for run in range(0,MOHEFT_RUNS):
    print("run " + str(run))

    # adapt output-path
    MOHEFT_OUTPUT_FILE = os.path.join(RESULT_FOLDER, "tradeoffs_run_" + str(run) +".CSV")

    # create config-file in current directory
    writeLogFile(configFilePath)

    # run scheduling algorithm
    MOHEFT_STDOUT_FILE = os.path.join(RESULT_FOLDER, "tradeoffs_run_" + str(run) +".txt")
    os.system("cd " + BIN_FOLDER + " && java " + TASK_SCHEDULER_APP + " " + configFilePath + " > " + MOHEFT_STDOUT_FILE)


# calculate Nadir point
os.system("cd " + BIN_FOLDER + " && java " + NADIR_APP + " " + configFilePath)

# calculate Utopia point
os.system("cd " + BIN_FOLDER + " && java " + UTOPIA_APP + " " + configFilePath)


# calculate optimum hypervolume
optimum_hv = float(check_output(["python", os.path.join(BIN_FOLDER, HYPER_VOLUME_APP), os.path.join(RESULT_FOLDER, "Utopia_point.txt"), os.path.join(RESULT_FOLDER, "Nadir_point.txt")]))


hypervols = {}
# calculate hypervolumes of all run-results
for resultfile in os.listdir(RESULT_FOLDER):
    if resultfile.startswith("tradeoffs_run_") and resultfile.endswith(".CSV"):
        hypervols[resultfile] = float(check_output(["python", os.path.join(BIN_FOLDER, HYPER_VOLUME_APP), os.path.join(RESULT_FOLDER, resultfile), os.path.join(RESULT_FOLDER, "Nadir_point.txt")]))



# sort by descending hypervalue
sortedHVs = sorted(hypervols.items(), key=itemgetter(1), reverse=True)

# print HVs of all tradeoff-solutions
print("\nAll runs:")
for hv in sortedHVs:
    print(hv)

# print more info about the best tradeoff-solution
print("\n\n\n\n--------------------------------------------------------------------------------")
print("\nBest run: ")
print(str(sortedHVs[0])+"\n")

# print stdout of scheduling-app of best solution
with open(os.path.join(RESULT_FOLDER,sortedHVs[0][0]), "r") as outputFile:
    print("".join(outputFile.readlines()))

print("\n")

with open(os.path.join(RESULT_FOLDER,sortedHVs[0][0].replace(".CSV",".txt")), "r") as outputFile:
    print("".join(outputFile.readlines()))
