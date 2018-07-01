import sys
from hv import *


#python calculateHyperVolume.py /home/theuers/sccTasksched18/MOHEFT/data/MOHEFT_tradeoffs.CSV /home/theuers/sccTasksched18/MOHEFT/data/Nadir_point.txt > MOHEFT_hv.txt
#python calculateHyperVolume.py /home/theuers/sccTasksched18/MOHEFT/data/Utopia_point.txt /home/theuers/sccTasksched18/MOHEFT/data/Nadir_point.txt > optimum_hv.txt
if len(sys.argv)!=3:
    print("usage: python " + sys.argv[0] + "<path to pareto front file> <path to nadir point file>")
    exit(-1)

referencePoint = [] #[2, 2, 2]
front = [] #[[1,0,1], [0,1,0]]

# read pareto front
with open(sys.argv[1],"r") as pareto_front_file:
    lines = pareto_front_file.readlines()
    for line in lines[1:]:
        if len(line) > 0:
            parts = line.rstrip().split(",")
            front.append([float(parts[0]), float(parts[1])])

# read reference point
with open(sys.argv[2],"r") as nadir_point_file:
    lines = nadir_point_file.readlines()
    parts = lines[1].rstrip().split(",")
    referencePoint.append(float(parts[0]))
    referencePoint.append(float(parts[1]))


# calculate hypervolume
hv = HyperVolume(referencePoint)
volume = hv.compute(front)
print(volume)





#totalCost,totalTime
#0.14010251196649326,39.995515203
#0.014562991724941455,78.248910758
#0.034925928229623315,59.957036067999994
#0.03270003677619825,75.64581517900001
#0.06345037450088392,51.06394562799999
#0.11284857065902806,43.900655650999994

#Nadir point (cost,time):
#0.30369981226152787,80.37198839200002