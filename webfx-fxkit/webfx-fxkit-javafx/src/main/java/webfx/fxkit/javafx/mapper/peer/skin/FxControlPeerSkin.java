package webfx.fxkit.javafx.mapper.peer.skin;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import webfx.fxkit.javafx.mapper.peer.FxNodePeer;
import webfx.fxkit.javafxgraphics.mapper.spi.NodePeer;
import webfx.fxkit.javafxgraphics.mapper.spi.SceneRequester;
import webfx.platform.client.services.uischeduler.UiScheduler;
import webfx.fxkit.javafxgraphics.mapper.spi.NodePeerFactoryRegistry;

/**
 * @author Bruno Salmon
 */
public final class FxControlPeerSkin<C extends Control> extends SkinBase<C> {

    public FxControlPeerSkin(C control) {
        super(control);
        NodePeer<C> nodePeer = NodePeerFactoryRegistry.createNodePeer(control);
        if (nodePeer != null) {
            nodePeer.bind(control, new SceneRequester() {
                @Override
                public void requestNodePeerPropertyUpdate(Node node, ObservableValue changedProperty) {
                    UiScheduler.runInUiThread(() ->
                            nodePeer.updateProperty(changedProperty)
                    );
                }

                @Override
                public void requestNodePeerListUpdate(Node node, ObservableList changedList, ListChangeListener.Change change) {
                    UiScheduler.runInUiThread(() ->
                            nodePeer.updateList(changedList, change)
                    );
                }
            });
            if (nodePeer instanceof FxNodePeer)
                getChildren().setAll(((FxNodePeer) nodePeer).getFxNode());
        }
    }
}
