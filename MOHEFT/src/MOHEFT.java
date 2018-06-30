import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class MOHEFT {
    int N, K, MAX_INSTANCES_PER_TYPE;
    String[] instanceTypes = null;
    HashMap<String, Double> makeSpanMap = new HashMap<String, Double>();
    HashMap<String, Double> costMap = new HashMap<String, Double>();
    ArrayList<String> R = new ArrayList<>();
    ArrayList<WorkflowSchedule> S = new ArrayList<>();
    String[] dag;
    String[] bRankedTasks;

    public MOHEFT(String configFilePath){
        initFromConfigFile(configFilePath);
    }

    private void initFromConfigFile(String configFilePath){
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFilePath));
        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        N = Integer.parseInt(props.getProperty("N","20"));
        K = Integer.parseInt(props.getProperty("K","6"));
        MAX_INSTANCES_PER_TYPE = Integer.parseInt(props.getProperty("MAX_INSTANCES_PER_TYPE","5"));


        makeSpanMap = readMatrixFile(props.getProperty("MAKESPAN_FILE"));
        costMap = readMatrixFile(props.getProperty("COST_FILE"));

        // read dag from file (adjacency list)
        try {
            List<String> list = Files.readAllLines(new File(props.getProperty("DAG_FILE")).toPath(), Charset.defaultCharset());
            list.remove(0); // dump header
            dag = list.toArray(new String[0]);
        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        bRankedTasks = getBRankedTasks();
        System.out.println("B-ranked task order: " + Arrays.toString(bRankedTasks));

        // init ressources
        for(String instanceType : instanceTypes){
            for(int i=0; i < MAX_INSTANCES_PER_TYPE; i++){
                R.add(instanceType+"_"+Integer.toString(i));
            }
        }
        System.out.println("Ressources: " + R);

        // start calculation of schedules
        scheduleTasks(bRankedTasks);
    }

    private void scheduleTasks(String[] bRankedTasks){
        WorkflowSchedule w;
        String key;

        // set up initial set of workflow schedules
        for(int i = 0; i < K; i++){
            S.add(new WorkflowSchedule(N));
        }

        // Iterate over the ranked tasks
        for(int i = 0; i < bRankedTasks.length; i++){
            ArrayList<WorkflowSchedule> S_tmp = new ArrayList<>();

            // Iterate over all ressources
            for(int j = 0; j < R.size(); j++){

                // Iterate over all tradeoff schedules
                for(int k = 0; k < K; k++){
                    // Extend all intermediate schedules
                    w = new WorkflowSchedule(S.get(k));
                    key = bRankedTasks[i] + "@" + R.get(j).split("_")[0];
                    w.schedule(R.get(j),bRankedTasks[i],costMap.get(key),makeSpanMap.get(key));
                    S_tmp.add(w);
                }
            }

            // sort S_tmp according to crowding distance
            ArrayList<WorkflowSchedule> tmp = getParetoFront(S_tmp);
            S_tmp = tmp;

            // choose K schedules with highest crowding distance (first K schedules in sorted list)
            S_tmp.subList(K,S_tmp.size()).clear();
            S = S_tmp;
        }

        // finished!
        System.out.println("¸n\nFound tradeoff schedules: ");
        for(WorkflowSchedule s : S){
            System.out.println(s);
        }

    }

    private ArrayList<WorkflowSchedule> getParetoFront(ArrayList<WorkflowSchedule> schedules){
        ArrayList<WorkflowSchedule> paretoFront = new ArrayList<>();
        ArrayList<WorkflowSchedule> dominatedSolutions = new ArrayList<>();
        paretoFront.add(schedules.get(0));
        int dom=0;

        for(WorkflowSchedule s : schedules.subList(1,schedules.size())){
             dominatedSolutions.clear();
             for(WorkflowSchedule frontSched : paretoFront){
                 if(frontSched.equals(s)) {
                     dom = -1; break;
                 }

                 dom = frontSched.checkDomination(s); // , 0 if non-dominant, 1 if frontSched is dominated by s
                 if(dom == -1){
                     // -1 if frontSched dominates s -> forget about s
                     break;
                 }else if(dom == 1){
                     dominatedSolutions.add(frontSched);
                 }
             }
             // -1 ... dominated is the only case in which s is not added
             if(dom != -1){
                 paretoFront.add(s);
                 paretoFront.removeAll(dominatedSolutions);
             }
        }

        return paretoFront;
    }

    private ArrayList<WorkflowSchedule> sortByCrowdingDistance(ArrayList<WorkflowSchedule> schedules){
        ArrayList<WorkflowSchedule> sortedSchedule = new ArrayList<>();

        return sortedSchedule;
    }

    private HashMap<String,Double> readMatrixFile(String path){
        HashMap<String,Double> resultMap = new HashMap<>();

        try {
            List<String> list = Files.readAllLines(new File(path).toPath(), Charset.defaultCharset());
            String line = list.get(0); // read header
            String[] parts = line.replace("task,","").split(",");
            String[] iTypes = parts;

            // integrity-check
            if(instanceTypes == null){
                instanceTypes = iTypes;
            }else{
                if(! Arrays.equals(instanceTypes, iTypes)){
                    throw new IllegalArgumentException("instanceTypes from configFiles do not match");
                }
            }

            String task = "";

            for(int i = 1; i < list.size(); i++){
                line = list.get(i);
                parts = line.split(",");
                task = parts[0];

                for(int j = 1; j < parts.length; j++){
                    resultMap.put(task+"@"+iTypes[j-1],Double.parseDouble(parts[j]));
                }
            }

        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        return resultMap;
    }

    private String[] getBRankedTasks(){
        ArrayList<String> predecessors;
        ArrayList<String> resultList = new ArrayList<>();

        // find node to begin with
        predecessors = getPredecessors("end");

        if(predecessors.size() != 1){
            throw new IllegalArgumentException("DAG has to have one and only one end-node!");
        }

        HashMap<String,Double> timeOffSetMap = getTime(predecessors.get(0), new HashMap<String,Double>());
        ValueComparator bvc = new ValueComparator(timeOffSetMap);
        TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
        sorted_map.putAll(timeOffSetMap);

        return sorted_map.keySet().toArray(new String[0]);
    }

    private HashMap<String,Double> getTime(String node, HashMap<String,Double> map){
        double ownTime = getTaskLength(node);
        double maxPredecessorTime = 0;

        for(String predecessor : getPredecessors(node)){
            map = getTime(predecessor, map);
            maxPredecessorTime = Math.max(maxPredecessorTime, map.get(predecessor));
        }

        map.put(node, ownTime + maxPredecessorTime);
        return map;
    }

    // avg of makespans as a concession to our data-model (actual measurements)
    private double getTaskLength(String task){
        double makespanSum=0, count=0;
        for(String instance : instanceTypes){
            makespanSum+=makeSpanMap.get(task + "@" + instance);
            count++;
        }
        return makespanSum/count;
    }

    private ArrayList<String> getPredecessors(String vertice){
        ArrayList<String> predecessors = new ArrayList<>();
        String[] parts;
        String nd;

        for(String adjacency : dag){
            parts = adjacency.split(":");
            nd = parts[0];

            parts = parts[1].split(",");
            for(String successor : parts) {
                if (successor.equals(vertice)) {
                    predecessors.add(nd);
                    break;
                }
            }
        }

        return predecessors;
    }









    public static void main (String[] args){
        if(args.length != 1){
            System.out.println("locaction of config-File needs to be given!");
            System.exit(-1);
        }else  {
            System.out.println("using config-file from: " + args[0]);
            MOHEFT m = new MOHEFT(args[0]);
        }
    }
}
