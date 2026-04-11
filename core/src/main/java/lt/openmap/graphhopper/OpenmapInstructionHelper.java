package lt.openmap.graphhopper;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

public class OpenmapInstructionHelper {

    /**
     * Builds a human-readable instruction text for a waterway obstacle node.
     * Use node-specific name if available, but NOT the general waterway name (river name).
     */
    public static String buildWaterwayObstacleText(WaterwayObstacle obstacle, String nodeName) {
        String suffix = (nodeName != null && !nodeName.isEmpty()) ? ": " + nodeName : "";
        return switch (obstacle) {
            case DAM, PORTAGE -> "Persinešti baidarę" + suffix;
            case HAZARD  -> "Dėmesio – pavojinga vieta" + suffix;
            case PUT_IN  -> "Įlipimo vieta" + suffix;
            case EGRESS  -> "Išlipimo vieta" + suffix;
            case BRIDGE  -> "Tiltas" + suffix;
            case MILESTONE -> nodeName != null ? nodeName : "Atstumo žymeklis";
            default      -> nodeName != null ? nodeName : "";
        };
    }

    /**
     * Creates an Instruction for a waterway obstacle, including metadata like description and milestones.
     */
    public static Instruction createWaterwayObstacleInstruction(EdgeIteratorState edge, WaterwayObstacle obstacle, boolean is3D) {
        // Use specifically injected node-level OSM "name", NOT the edge's "STREET_NAME" (river name).
        String nodeName = (String) edge.getValue("waterway_obstacle_node_name");
        
        String obstacleText = buildWaterwayObstacleText(obstacle, nodeName);
        Instruction instr = new Instruction(Instruction.CONTINUE_ON_STREET, obstacleText, new PointList(2, is3D));
        instr.setUseRawName();
        instr.setExtraInfo("waterway_obstacle", obstacle.name().toLowerCase());
        
        // OSM node description from KValues
        String waterwayDesc = (String) edge.getValue("waterway_obstacle_description");
        if (waterwayDesc != null && !waterwayDesc.isEmpty())
            instr.setExtraInfo("waterway_obstacle_description", waterwayDesc);
            
        // Waterway milestone value
        String waterwayMilesVal = (String) edge.getValue("waterway_milestone_value");
        if (waterwayMilesVal != null && !waterwayMilesVal.isEmpty()) {
            instr.setExtraInfo("waterway_milestone_value", waterwayMilesVal);
            if (obstacleText.equals("Atstumo žymeklis") || obstacleText.startsWith("Atstumo žymeklis: ")) {
                obstacleText = "Atstumo žymeklis: " + waterwayMilesVal + " km";
                instr.setName(obstacleText);
            }
        }
        return instr;
    }
}
