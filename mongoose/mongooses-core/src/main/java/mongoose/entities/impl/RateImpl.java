package mongoose.entities.impl;

import mongoose.entities.Rate;
import webfx.framework.orm.entity.EntityId;
import webfx.framework.orm.entity.EntityStore;
import webfx.framework.orm.entity.impl.DynamicEntity;

/**
 * @author Bruno Salmon
 */
public final class RateImpl extends DynamicEntity implements Rate {

    public RateImpl(EntityId id, EntityStore store) {
        super(id, store);
    }
}
