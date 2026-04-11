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

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.routing.util.parsers.TagParser;

import java.util.List;
import java.util.Map;

/**
 * Parses OSM node tags for waterway obstacles (dams, hazards, portages, milestones).
 * <p>
 * Works identically to {@link OSMCrossingParser}: reads the "node_tags" list
 * from barrier edges and sets the waterway_obstacle encoded value.
 * <p>
 * Required OSM node tags:
 *   whitewater=dam|hazard|put_in|egress|bridge|dam;put_in|...
 *   waterway=milestone  or  waterway:milestone=<km>
 */
public class WaterwayObstacleParser implements TagParser {

    private final EnumEncodedValue<WaterwayObstacle> obstacleEnc;

    public WaterwayObstacleParser(EncodedValueLookup lookup) {
        this.obstacleEnc = lookup.getEnumEncodedValue(WaterwayObstacle.KEY, WaterwayObstacle.class);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        // Only process the actual barrier edge, not the segments leading to/from it.
        // gh:barrier_edge is set as a Boolean flag by WaySegmentParser for artificial edges.
        // Adjacent river segments also contain the barrier node in node_tags, but we only
        // want the obstacle on the tiny artificial edge itself to avoid duplicate instructions.
        if (!way.hasTag("gh:barrier_edge"))
            return;

        List<Map<String, Object>> nodeTags = way.getTag("node_tags", null);
        if (nodeTags == null)
            return;

        for (Map<String, Object> tags : nodeTags) {
            WaterwayObstacle obstacle = WaterwayObstacle.fromOsmTags(tags);
            if (obstacle != WaterwayObstacle.MISSING) {
                obstacleEnc.setEnum(false, edgeId, edgeIntAccess, obstacle);
                return; // first matching node wins for this edge
            }
        }
    }
}
