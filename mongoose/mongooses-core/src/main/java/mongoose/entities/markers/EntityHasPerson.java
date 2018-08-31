package mongoose.entities.markers;

import mongoose.entities.Person;
import webfx.framework.orm.entity.Entity;
import webfx.framework.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasPerson extends Entity, HasPerson {

    @Override
    default void setPerson(Object person) {
        setForeignField("person", person);
    }

    @Override
    default EntityId getPersonId() {
        return getForeignEntityId("person");
    }

    @Override
    default Person getPerson() {
        return getForeignEntity("person");
    }

}
