package naga.toolkit.properties.markers;

import javafx.beans.property.Property;
import naga.toolkit.drawing.shapes.Parent;

/**
 * @author Bruno Salmon
 */
public interface HasParentProperty {

    Property<Parent> parentProperty();
    default void setParent(Parent parent) {
        parentProperty().setValue(parent);
    }
    default Parent getParent() {
        return parentProperty().getValue();
    }

}
