import javax.imageio.IIOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class MOHEFT {
    int N, K;
    String[] instanceTypes;
    HashMap<String, Double> makeSpanMap = new HashMap<String, Double>();
    HashMap<String, Double> costMap = new HashMap<String, Double>();

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

    }

    private HashMap<String,Double> readMatrixFile(String path){
        HashMap<String,Double> resultMap = new HashMap<>();

        try {
            List<String> list = Files.readAllLines(new File(path).toPath(), Charset.defaultCharset());
            String line = list.get(0); // read header
            String[] parts = line.replace("task,","").split(",");
            String[] instanceTypes = parts;
            String task = "";

            for(int i = 1; i < list.size(); i++){
                line = list.get(i);
                parts = line.split(",");
                task = parts[0];

                for(int j = 1; j < parts.length; j++){
                    resultMap.put(task+"@"+instanceTypes[j-1],Double.parseDouble(parts[j]));
                }
            }

        }catch(IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        return resultMap;
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
