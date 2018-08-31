package mongoose.operations.backend.route;

import mongoose.operations.bothends.route.RouteToFeesRequest;
import mongoose.aggregates.EventAggregate;
import webfx.platform.client.url.history.History;
import webfx.util.async.Future;

/**
 * @author Bruno Salmon
 */
final class RouteToNewBackendBookingExecutor {

    static Future<Void> executeRequest(RouteToNewBackendBookingRequest rq) {
        return execute(rq.getEventId(), rq.getHistory());
    }

    private static Future<Void> execute(Object eventId, History history) {
        // When made in the backend, we don't want to add the new booking to the last visited booking cart (as
        // opposed to the frontend), so we clear the reference to the current booking cart (if set) before routing
        EventAggregate eventAggregate = EventAggregate.get(eventId);
        if (eventAggregate != null)
            eventAggregate.setCurrentCart(null);
        // Now that the current cart reference is cleared, we can route to the fees page
        new RouteToFeesRequest(eventId, history).execute();
        return Future.succeededFuture();
    }



}
