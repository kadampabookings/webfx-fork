package naga.toolkit.fx.spi.viewer.base;

import javafx.beans.value.ObservableValue;
import naga.toolkit.fx.scene.SceneRequester;
import naga.toolkit.fx.scene.control.Labeled;

/**
 * @author Bruno Salmon
 */
public abstract class LabeledViewerBase
        <N extends Labeled, NB extends LabeledViewerBase<N, NB, NM>, NM extends LabeledViewerMixin<N, NB, NM>>

        extends ControlViewerBase<N, NB, NM> {

    @Override
    public void bind(N labeled, SceneRequester sceneRequester) {
        super.bind(labeled, sceneRequester);
        requestUpdateOnPropertiesChange(sceneRequester
                , node.textProperty()
                , node.graphicProperty()
                , node.fontProperty()
                , node.textAlignmentProperty()
        );
    }

    @Override
    public boolean updateProperty(ObservableValue changedProperty) {
        return super.updateProperty(changedProperty)
                || updateProperty(node.textProperty(), changedProperty, mixin::updateText)
                || updateProperty(node.graphicProperty(), changedProperty, mixin::updateGraphic)
                || updateProperty(node.fontProperty(), changedProperty, mixin::updateFont)
                || updateProperty(node.textAlignmentProperty(), changedProperty, mixin::updateTextAlignment)
                ;
    }
}
