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
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.util.parsers.AbstractAverageSpeedParser;
import com.graphhopper.routing.util.parsers.TagParser;

/**
 * Speed parser for kayak/canoe routing on OSM waterways.
 * <p>
 * Default speeds (km/h) by waterway type:
 *   river  = 5 km/h (average with mild current)
 *   canal  = 4 km/h (calm, artificial)
 *   stream = 3 km/h (narrower, shallower)
 *   drain  = 2 km/h
 * <p>
 * Barrier edges (portages/dams) get very low speed to model time penalty.
 * Upstream direction is 60% of downstream speed.
 * <p>
 * The whitewater:rapid_grade tag (1-6) reduces speed for technical rapids.
 */
public class KayakAverageSpeedParser extends AbstractAverageSpeedParser implements TagParser {

    // Portage speed: walking with kayak ~2 km/h over a short distance
    // This adds realistic time penalty at dams/obstacles
    static final double PORTAGE_SPEED = 1.0;

    public KayakAverageSpeedParser(EncodedValueLookup lookup) {
        this(lookup.getDecimalEncodedValue(VehicleSpeed.key("kayak")));
    }

    protected KayakAverageSpeedParser(DecimalEncodedValue speedEnc) {
        super(speedEnc);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way) {
        String waterway = way.getTag("waterway");
        if (waterway == null)
            return;

        // Portage/barrier edge: use very low speed as time penalty
        // Note: gh:barrier_edge is stored as Boolean, so use hasTag() not getTag()
        if (way.hasTag("gh:barrier_edge")) {
            setSpeed(false, edgeId, edgeIntAccess, PORTAGE_SPEED);
            setSpeed(true, edgeId, edgeIntAccess, PORTAGE_SPEED);
            return;
        }

        double speed = baseSpeed(waterway);

        // whitewater:rapid_grade (1-6): reduce speed for higher grades
        String gradeTag = way.getTag("whitewater:rapid_grade");
        if (gradeTag != null) {
            try {
                int grade = Integer.parseInt(gradeTag.trim().substring(0, 1));
                if (grade >= 5)      speed = Math.max(PORTAGE_SPEED, speed - 3); // grade V-VI: very technical
                else if (grade >= 3) speed = Math.max(1.0, speed - 1);           // grade III-IV: difficult
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
            }
        }

        // Downstream
        setSpeed(false, edgeId, edgeIntAccess, speed);
        // Upstream: paddling against current is slower
        setSpeed(true, edgeId, edgeIntAccess, Math.max(avgSpeedEnc.getSmallestNonZeroValue(), speed * 0.6));
    }

    private double baseSpeed(String waterway) {
        return switch (waterway) {
            case "river"    -> 5.0;
            case "canal"    -> 4.0;
            case "stream"   -> 3.0;
            case "drain"    -> 2.0;
            default         -> 4.0;
        };
    }
}
