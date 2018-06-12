import os
import sys
import string
import random
import time
from collections import Counter

REPETITIONS = 5
#sudo yum update -y
#sudo yum install -y python36
#sudo yum-config-manager --enable epel
#sudo yum install -y p7zip

# https://stackoverflow.com/questions/2257441/random-string-generation-with-upper-case-letters-and-digits-in-python?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
def genRandStringFile(size=666, path="out.txt"):
    chars = string.ascii_uppercase + string.digits + string.ascii_lowercase + string.punctuation + " " + "\n"
    with open(path,"w") as out:
        out.write(''.join(random.choice(chars) for _ in range(size)))


# https://stackoverflow.com/questions/40985203/counting-letter-frequency-in-a-string-python?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
def genLetterFreq(inputPath="in.txt", outputPath="out.txt"):
    word=""
    with open(inputPath,"r") as inFile:
        word = " ".join(inFile.readlines())

    counts = Counter(word)  # Counter({'l': 2, 'H': 1, 'e': 1, 'o': 1})

    with open(outputPath,"w") as outFile:
        for i in word:
            outFile.write(str(i) + "\t" + str(counts[i]) + "\n")


def executeTask(task):
    start = time.time()

    if isinstance(task[1],str):
        os.system(task[1])  # exec os-cmd
    else:
        # exec script-intern function
        task[1](*task[2])

    end = time.time()
    return task[0] + "\t"+ str(end-start)

t1_cmd = "sha512sum ./out/T2.1.txt ./out/T2.2.txt ./out/T2.3.txt ./out/T2.4.txt >> ./results/SHA512SUMS.txt" + " && 7za a ./out/T2.zip ./out/T2*.txt"
t3_cmd = "cat ./out/T2.1.txt ./out/T2.2.txt ./out/T2.3.txt ./out/T2.4.txt > ./out/T2_concat.txt && sha512sum ./out/T2_concat.txt && cd out &&  split -d --number 4 T2_concat.txt"
t5_cmd = "7za a ./out/T4.zip ./out/*.txt ./out/*.zip ./out/x*"
tasks = [
    # generate textfiles
    ("T2.1",genRandStringFile,[6000000,"./out/T2.1.txt"]),
    ("T2.2",genRandStringFile,[3000000,"./out/T2.2.txt"]),
    ("T2.3",genRandStringFile,[1500000,"./out/T2.3.txt"]),
    ("T2.4",genRandStringFile,[10000000,"./out/T2.4.txt"]),

    # calculate checksums and zip created files
    ("T1", t1_cmd, []),

    # unite partial results of T2, calculate a checksum and split the summary-file into four separate files
    ("T3", t3_cmd, []),

    # calc letter frequencies
    ("T4.1", genLetterFreq,["./out/x00","./out/4.1.txt"]),
    ("T4.2", genLetterFreq,["./out/x00","./out/4.2.txt"]),
    ("T4.3", genLetterFreq,["./out/x00","./out/4.3.txt"]),
    ("T4.4", genLetterFreq,["./out/x00","./out/4.4.txt"]),

    # zip all partial results
    ("T5", t5_cmd, [])
]


if len(sys.argv) < 2:
    print("usage: python3 " + sys.argv[0] + " <EC2InstanceName>")
    exit()

resultDict = {}

if not os.path.isdir("results"):
    os.mkdir("results")

instanceName = "./results/" + sys.argv[1]

for repetition in range(0,REPETITIONS):
    random.seed(666) # reset random-generator
    print("<repetition " + str(repetition) + ">")

    if os.path.isdir("out"):
        os.system("rm -rf out")
    os.mkdir("out")

    with open(instanceName + "_" + str(repetition) + ".txt", "w") as logfile:
        for task in tasks:
            entry = executeTask(task)
            logfile.write(entry + "\n")
            print("\t" + entry)

            # store indiv. results for statistics
            parts = entry.split('\t')
            if parts[0] not in resultDict:
                resultDict[parts[0]] = [float(parts[1])]
            else:
                resultDict[parts[0]].append(float(parts[1]))

    print("</repetition " + str(repetition) + ">\n")

if os.path.isdir("out"):
    os.system("rm -rf out")
print("done")

print("\nstatistics:")
header = "task\tavg-time\ttimes"
with open(instanceName +"_statistics.txt","w") as statFile:
    statFile.write(header + "\n")
    print(header)
    for key in sorted(resultDict):
        line = str(key) + "\t" + str(sum(resultDict[key])/len(resultDict[key])) + "\t" + str(resultDict[key])
        statFile.write(line + "\n")
        print(line)