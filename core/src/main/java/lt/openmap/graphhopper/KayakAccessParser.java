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
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.util.WayAccess;
import com.graphhopper.util.PMap;
import com.graphhopper.routing.util.parsers.AbstractAccessParser;
import com.graphhopper.routing.util.parsers.TagParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Access parser for kayak/canoe routing on OSM waterways.
 * <p>
 * Allowed OSM way tags: waterway=river|stream|canal|drain|waterway
 * Blocked if: canoe=no, boat=no, kayak=no, or access=private/no
 */
public class KayakAccessParser extends AbstractAccessParser implements TagParser {

    private static final Set<String> ALLOWED_WATERWAYS = new HashSet<>(Arrays.asList(
            "river", "stream", "canal", "drain", "waterway"
    ));

    public KayakAccessParser(EncodedValueLookup lookup, PMap properties) {
        this(lookup.getBooleanEncodedValue(VehicleAccess.key("kayak")));
    }

    protected KayakAccessParser(BooleanEncodedValue accessEnc) {
        super(accessEnc, Arrays.asList("canoe", "boat", "kayak", "access"));
        // Remove 'service' from restricted since waterways don't use it
        restrictedValues.remove("service");
    }

    public WayAccess getAccess(ReaderWay way) {
        String waterway = way.getTag("waterway");

        // Waterway ways only - skip highways, paths, etc.
        if (waterway == null || !ALLOWED_WATERWAYS.contains(waterway))
            return WayAccess.CAN_SKIP;

        // Check explicit access restrictions for canoe/kayak/boat
        if (way.hasTag("canoe", "no") || way.hasTag("kayak", "no") || way.hasTag("boat", "no"))
            return WayAccess.CAN_SKIP;

        // Check generic access tag
        String accessTag = way.getTag("access");
        if ("no".equals(accessTag) || "private".equals(accessTag))
            return WayAccess.CAN_SKIP;

        return WayAccess.WAY;
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way) {
        WayAccess access = getAccess(way);
        if (access.canSkip())
            return;

        // Most waterways are bidirectional (can paddle upstream too, just slower)
        // Oneway rivers (e.g. very fast current) could use oneway:canoe=yes but we allow both for now
        accessEnc.setBool(false, edgeId, edgeIntAccess, true);
        accessEnc.setBool(true, edgeId, edgeIntAccess, true);
    }
}
