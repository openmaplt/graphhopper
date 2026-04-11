/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package lt.openmap.graphhopper;

import com.graphhopper.routing.ev.EnumEncodedValue;
import java.util.Map;

/**
 * Encodes waterway obstacle types found on OSM nodes along rivers.
 * Used for kayak/canoe routing to detect portages, hazards, and milestones.
 * <p>
 * OSM tags used: whitewater=*, waterway=milestone, waterway:milestone=*
 */
public enum WaterwayObstacle {
    MISSING,
    DAM,        // whitewater=dam          - nepraplaukiama, reikia persinešti
    PORTAGE,    // whitewater=dam;put_in   - persinešti ir galima tęsti
    HAZARD,     // whitewater=hazard       - pavojinga vieta, įspėjimas
    PUT_IN,     // whitewater=put_in       - baidarės įleidimo vieta
    EGRESS,     // whitewater=egress       - baidarės išleidimo vieta
    BRIDGE,     // whitewater=bridge       - tiltas virš upės
    MILESTONE;  // waterway=milestone      - atstumo žymeklis (km)

    public static final String KEY = "waterway_obstacle";

    public static EnumEncodedValue<WaterwayObstacle> create() {
        return new EnumEncodedValue<>(KEY, WaterwayObstacle.class);
    }

    /**
     * Determine the obstacle type from an OSM node's tags map.
     * Handles combined values like "dam;put_in" or "bridge;egress".
     */
    public static WaterwayObstacle fromOsmTags(Map<String, Object> tags) {
        Object ww = tags.get("whitewater");
        if (ww != null) {
            String val = ww.toString().toLowerCase();
            if (val.contains("dam") && val.contains("put_in")) return PORTAGE;
            if (val.contains("dam"))                           return DAM;
            if (val.contains("hazard"))                        return HAZARD;
            if (val.contains("put_in"))                        return PUT_IN;
            if (val.contains("egress"))                        return EGRESS;
            if (val.contains("bridge"))                        return BRIDGE;
        }
        Object waterway = tags.get("waterway");
        if ("milestone".equals(waterway) || tags.containsKey("waterway:milestone"))
            return MILESTONE;
        return MISSING;
    }
}
