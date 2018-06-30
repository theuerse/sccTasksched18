import javax.imageio.IIOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class MOHEFT {
    int N, K;
    String[] instanceTypes = null;
    HashMap<String, Double> makeSpanMap = new HashMap<String, Double>();
    HashMap<String, Double> costMap = new HashMap<String, Double>();

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

        // start calculation of schedules
        System.out.println("B-ranked task order: " + Arrays.toString(bRankedTasks));
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
