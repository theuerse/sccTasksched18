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

    // returns -1 if "this" dominates "otherSchedule"
    // return 0 if "this" and "otherSchedule" are non-dominant
    // return 1 if "this" is dominated by "otherSchedule"
    public int checkDomination(WorkflowSchedule otherSchedule){
        if(totalTime < otherSchedule.totalTime && totalCost < otherSchedule.totalCost){
            return -1;
        }else if (totalTime > otherSchedule.totalTime && totalCost > otherSchedule.totalCost){
            return 1;
        }else {
            return 0;
        }
    }

    public String toString(){
        String str = "totalCost: " + totalCost + "\ttotalTime: " + totalTime + "\n";
        for(String key : schedule.keySet()){
            str += "\t" + key + "\t" + schedule.get(key) + "\n";
        }

        return str;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof WorkflowSchedule))return false;
        WorkflowSchedule otherWS = (WorkflowSchedule) other;

        if(otherWS.N != this.N) return false;
        if(otherWS.totalTime != this.totalTime) return false;
        if(otherWS.totalCost != this.totalCost) return false;
        if(!otherWS.schedule.equals(this.schedule)) return false;

        return true;
    }
}
