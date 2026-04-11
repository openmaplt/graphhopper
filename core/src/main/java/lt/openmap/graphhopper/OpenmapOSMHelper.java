package lt.openmap.graphhopper;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.search.KVStorage;
import com.graphhopper.search.KVStorage.KValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenmapOSMHelper {

    /**
     * For waterway barrier edges (dams, hazards, etc.): inject obstacle description
     * from the barrier node's OSM tags into the edge key-values.
     */
    public static Map<String, KValue> injectWaterwayMetadata(ReaderWay way, List<Map<String, Object>> nodeTags, Map<String, KValue> existingMap) {
        if (existingMap == null) existingMap = Collections.emptyMap();
        
        if (way.hasTag("gh:barrier_edge") && way.hasTag("waterway")) {
            for (Map<String, Object> tags : nodeTags) {
                Map<String, KValue> newMap = new LinkedHashMap<>(existingMap);
                boolean changed = false;

                // OSM specific node name (e.g. "Aukštadvario užtvanka")
                Object nodeName = tags.get("name");
                if (nodeName != null && !nodeName.toString().isEmpty()) {
                    newMap.put("waterway_obstacle_node_name",
                            new KValue(KVStorage.cutString(nodeName.toString())));
                    changed = true;
                }
                
                // OSM description
                Object desc = tags.get("description");
                if (desc != null && !desc.toString().isEmpty()) {
                    newMap.put("waterway_obstacle_description",
                            new KValue(KVStorage.cutString(desc.toString())));
                    changed = true;
                }
                
                // Waterway milestone km value
                Object milestone = tags.get("waterway:milestone");
                if (milestone == null) milestone = tags.get("distance");
                if (milestone != null && !milestone.toString().isEmpty()) {
                    newMap.put("waterway_milestone_value",
                            new KValue(KVStorage.cutString(milestone.toString())));
                    changed = true;
                }
                
                if (changed) return newMap;
            }
        }
        return existingMap;
    }
}
