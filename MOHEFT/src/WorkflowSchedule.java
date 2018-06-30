import java.util.ArrayList;
import java.util.TreeMap;

public class WorkflowSchedule {
    double totalCost=0, totalTime=0, N=0;

    TreeMap<String, ArrayList<String>> schedule = new TreeMap<>();
    public WorkflowSchedule(int N){
        this.N = N;
    }

    public WorkflowSchedule(WorkflowSchedule that) {
        this.N = that.N;
        this.totalCost = that.totalCost;
        this.totalTime = that.totalTime;

        for(String key : that.schedule.keySet()){
            schedule.put(key, (ArrayList<String>)that.schedule.get(key).clone());
        }
    }

    public void schedule(String ressource, String task, double cost, double time){
        if(!schedule.containsKey(ressource)){
            schedule.put(ressource, new ArrayList<String>());
        }

        schedule.get(ressource).add(task);

        if(countRessources() > N){
            totalCost = Integer.MAX_VALUE;
            totalTime = Integer.MAX_VALUE;
        }else{
            totalCost += cost;
            totalTime += time;
        }
    }

    private int countRessources(){
        return schedule.keySet().size();
    }
}
