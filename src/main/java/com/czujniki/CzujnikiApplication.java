package com.czujniki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class CzujnikiApplication {

    private static List<LinkedHashMap<String,Object>> sensorList;
    private static final String rawGithubUrl = "://raw.githubusercontent.com";
    private static final String githubUrl = "://github.com";
    private static final String githubBlob = "blob/";

    private static final String id = "id";
    private static final String value = "value";
    private static final String engine = "engine";
    private static final String type = "type";
    private static final String master_sensor_id = "master-sensor-id";
    private static final String min_value = "min_value";
    private static final String max_value = "max_value";

    @RequestMapping(value = "/engines", method = RequestMethod.GET)
    public List<String> getBadEngines(@RequestParam("pressure_threshold") int pressureThreshold, @RequestParam("temp_threshold") int tempThreshold){
//        System.out.println(sensorList);

        List<Object> listMasterIdBadTemSensors = sensorList.stream()
                .filter(sensor -> sensor.containsKey(master_sensor_id))
                .filter(x -> (Integer) x.get(value)>tempThreshold)
                .map(x -> x.get(master_sensor_id))
                .collect(Collectors.toList());
        Set<Object> setMasterIdBadTemSensors = new HashSet<Object>(listMasterIdBadTemSensors);

//        System.out.println(setMasterIdBadTemSensors);

        List<String> listBadEngins = sensorList.stream()
                .filter(x -> setMasterIdBadTemSensors.contains(x.get(id)))
                .filter(sensor -> (Integer) sensor.get(value)<pressureThreshold)
                .map(x -> (String) x.get(engine))
                .collect(Collectors.toList());

//        System.out.println(listBadEngins);
        return listBadEngins;
    }

    @RequestMapping(value = "/sensors/{sensorId}", method = RequestMethod.POST)
    public void updateEngines(@PathVariable int sensorId, @RequestBody UpdateSensorModel updateSensorModel){
        System.out.println(sensorId+" "+updateSensorModel.getOperation()+" "+updateSensorModel.getValue());
        int valI = Integer.parseInt(updateSensorModel.getValue());

        for (LinkedHashMap<String,Object> param : sensorList) {
            if (sensorId == Integer.parseInt(param.get(id).toString())) {
                if(updateSensorModel.getOperation().equals("set")){
                    if(isValueCorrect(valI, (Integer) param.get(min_value), (Integer) param.get(max_value))){
                        param.put(value, valI);
                    }
                } else if(updateSensorModel.getOperation().equals("increment")){
                    System.out.println(param.get("value"));
                    valI = (Integer) param.get(value) + valI;
                    if(isValueCorrect(valI, (Integer) param.get(min_value), (Integer) param.get(max_value))){
                        param.put(value, valI);
                    }
                } else if(updateSensorModel.getOperation().equals("decrement")){
                    valI = valI - (Integer) param.get(value);
                    if(isValueCorrect(valI, (Integer) param.get(min_value), (Integer) param.get(max_value))){
                        param.put(value, valI);
                    }
                }
            }
        }

    }

    public static void main(String[] args) {

        String url = "https://github.com/relayr/pdm-test/blob/master/sensors.yml";
        if(url.contains(githubUrl)) url = url.replaceFirst(githubUrl, rawGithubUrl).replaceFirst(githubBlob, "");

        Yaml yaml = new Yaml();
        try (InputStream in = new URL(url).openStream()) {
            sensorList = yaml.load(in);
            System.out.println(sensorList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SpringApplication.run(CzujnikiApplication.class, args);
    }

    private boolean isValueCorrect(int val, int min, int max){
        if(val<=max && val>=min){
            return true;
        }
        return false;
    }


}
