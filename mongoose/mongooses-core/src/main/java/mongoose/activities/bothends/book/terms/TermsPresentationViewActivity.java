package mongoose.activities.bothends.book.terms;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongoose.actions.MongooseIcons;
import mongoose.activities.bothends.book.shared.BookingProcessPresentationViewActivity;
import webfx.framework.ui.layouts.LayoutUtil;
import webfx.fxdata.cell.collator.GridCollator;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationViewActivity extends BookingProcessPresentationViewActivity<TermsPresentationModel> {

    private BorderPane termsPanel;

    @Override
    protected void createViewNodes(TermsPresentationModel pm) {
        super.createViewNodes(pm);
        GridCollator termsLetterCollator = new GridCollator("first", "first");
        termsPanel = createSectionPanel(MongooseIcons.certificateMonoSvg16JsonUrl, "TermsAndConditions");
        termsPanel.setCenter(LayoutUtil.createVerticalScrollPaneWithPadding(termsLetterCollator));

        termsLetterCollator.displayResultProperty().bind(pm.termsLetterDisplayResultProperty());
    }

    @Override
    protected Node assemblyViewNodes() {
        return LayoutUtil.createPadding(new VBox(10, LayoutUtil.setVGrowable(termsPanel), LayoutUtil.setMaxWidthToInfinite(backButton)));
    }
}
