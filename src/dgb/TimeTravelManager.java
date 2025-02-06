package dgb;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.List;

public class TimeTravelManager {
    private final List<Location> locations = new ArrayList<>();
    private final List<Object> nonDetValues = new ArrayList<>();
    // ... plus d'infos si besoin (ex. calls par step)

    public void recordStep(int step, Location loc) {
        // agrandir la liste si besoin
        while (locations.size() <= step) {
            locations.add(null);
        }
        locations.set(step, loc);
    }

    public void recordNonDetValue(int step, Object val) {
        // ...
    }

    public Location getLocationAtStep(int step) {
        if (step < locations.size()) return locations.get(step);
        return null;
    }

}
