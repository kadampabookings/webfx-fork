package mongoose.entities.markers;

import mongoose.entities.Site;
import webfx.framework.orm.entity.Entity;
import webfx.framework.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasArrivalSite extends Entity, HasArrivalSite {

    @Override
    default void setArrivalSite(Object arrivalSite) {
        setForeignField("arrivalSite", arrivalSite);
    }

    @Override
    default EntityId getArrivalSiteId() {
        return getForeignEntityId("arrivalSite");
    }

    @Override
    default Site getArrivalSite() {
        return getForeignEntity("arrivalSite");
    }


}
