package firstproject.vas.sk.com.googlelocation;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeofencingService extends IntentService {

    public GeofencingService() {
        super("GeofencingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                // ...
                return;
            }

            int transation = event.getGeofenceTransition();
            List<Geofence> list = event.getTriggeringGeofences();
            Location location = event.getTriggeringLocation();
            if (transation == Geofence.GEOFENCE_TRANSITION_ENTER) {
                // ...
            } else if (transation == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // ...
            } else {
                // ...
            }

        }
    }


}
