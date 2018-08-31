package mongoose.operations.bothends.route;

import mongoose.activities.bothends.book.fees.FeesRouting;
import webfx.framework.operations.route.RoutePushRequest;
import webfx.platform.client.url.history.History;

/**
 * @author Bruno Salmon
 */
public final class RouteToFeesRequest extends RoutePushRequest {

    public RouteToFeesRequest(Object eventId, History history) {
        super(FeesRouting.getFeesPath(eventId), history);
    }

}
