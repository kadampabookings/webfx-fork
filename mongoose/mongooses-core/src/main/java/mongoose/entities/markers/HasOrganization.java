package mongoose.entities.markers;

import mongoose.entities.Organization;
import webfx.framework.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasOrganization {

    void setOrganization(Object organization);

    EntityId getOrganizationId();

    Organization getOrganization();

}
