package naga.toolkit.drawing.shapes;

import naga.toolkit.drawing.paint.Paint;
import naga.toolkit.drawing.shapes.impl.CircleImpl;
import naga.toolkit.properties.markers.HasCenterXProperty;
import naga.toolkit.properties.markers.HasCenterYProperty;
import naga.toolkit.properties.markers.HasRadiusProperty;

/**
 * @author Bruno Salmon
 */
public interface Circle extends Shape,
        HasCenterXProperty,
        HasCenterYProperty,
        HasRadiusProperty {

    static Circle create() {
        return new CircleImpl();
    }

    static Circle create(double radius) {
        return new CircleImpl(radius);
    }

    static Circle create(double radius, Paint fill) {
        return new CircleImpl(radius, fill);
    }

    static Circle create(double centerX, double centerY, double radius) {
        return new CircleImpl(centerX, centerY, radius);
    }

    static Circle create(double centerX, double centerY, double radius, Paint fill) {
        return new CircleImpl(centerX, centerY, radius, fill);
    }

}
