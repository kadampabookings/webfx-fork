package mongoose.activities.shared.generic.eventdependent;

import javafx.beans.value.ObservableValue;
import mongoose.services.EventService;
import mongoose.services.EventServiceMixin;
import naga.framework.activity.domain.DomainActivityContext;
import naga.framework.activity.domain.DomainActivityContextMixin;
import naga.framework.activity.uiroute.UiRouteActivityContext;
import naga.framework.activity.uiroute.UiRouteActivityContextMixin;

/**
 * @author Bruno Salmon
 */
public interface EventDependentMixin
        <C extends DomainActivityContext<C> & UiRouteActivityContext<C>>

        extends UiRouteActivityContextMixin<C>,
        EventServiceMixin,
        DomainActivityContextMixin<C> {

    default Object getEventId() {
        return eventIdProperty().getValue();
    }

    ObservableValue<Object> eventIdProperty();

    default EventService getEventService() {
        return EventService.getOrCreate(getEventId(), getDataSourceModel());
    }
}
