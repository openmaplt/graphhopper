package lt.openmap.graphhopper;

import com.graphhopper.routing.ev.DefaultImportRegistry;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;

public class OpenmapImportRegistry extends DefaultImportRegistry {
    @Override
    public ImportUnit createImportUnit(String name) {
        if (VehicleAccess.key("kayak").equals(name)) {
            return ImportUnit.create(name, props -> VehicleAccess.create("kayak"),
                    KayakAccessParser::new);
        } else if (WaterwayObstacle.KEY.equals(name)) {
            return ImportUnit.create(name, props -> WaterwayObstacle.create(),
                    (lookup, props) -> new WaterwayObstacleParser(lookup));
        } else if (VehicleSpeed.key("kayak").equals(name)) {
            return ImportUnit.create(name, props -> new DecimalEncodedValueImpl(
                            name, props.getInt("speed_bits", 4), props.getDouble("speed_factor", 1), true),
                    (lookup, props) -> new KayakAverageSpeedParser(lookup)
            );
        }
        return super.createImportUnit(name);
    }
}
