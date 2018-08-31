package mongoose.activities.backend.operations;

import javafx.scene.layout.Pane;
import mongoose.activities.bothends.generic.MongooseDomainPresentationLogicActivityBase;
import webfx.framework.activity.base.combinations.viewapplication.ViewApplicationContext;
import webfx.framework.orm.entity.Entity;
import webfx.framework.ui.filter.ReactiveExpressionFilterFactoryMixin;
import webfx.framework.ui.graphic.controls.sheet.PropertySheet;
import webfx.util.function.Factory;

/**
 * @author Bruno Salmon
 */
final class OperationsPresentationLogicActivity
        extends MongooseDomainPresentationLogicActivityBase<OperationsPresentationModel>
        implements ReactiveExpressionFilterFactoryMixin {

    private final String expressionColumns = "['name','operationCode','i18nCode','backend','frontend','public']";

    OperationsPresentationLogicActivity() {
        this(OperationsPresentationModel::new);
    }

    private OperationsPresentationLogicActivity(Factory<OperationsPresentationModel> presentationModelFactory) {
        super(presentationModelFactory);
    }

    @Override
    protected void startLogic(OperationsPresentationModel pm) {
        // Loading the domain model and setting up the reactive filter
        createReactiveExpressionFilter("{class: 'Operation', alias: 'o', orderBy: 'name'}")
            // Search box condition
            .combineIfNotEmptyTrim(pm.searchTextProperty(), s -> "{where: 'lower(name) like `%" + s.toLowerCase() + "%`'}")
            // Limit condition
            .combineIfPositive(pm.limitProperty(), l -> "{limit: '" + l + "'}")
            .setExpressionColumns(expressionColumns)
            .displayResultInto(pm.genericDisplayResultProperty())
            .setSelectedEntityHandler(pm.genericDisplaySelectionProperty(), this::editOperation)
            .start();
    }

    private void editOperation(Entity operation) {
        Pane parent = (Pane) ViewApplicationContext.getViewApplicationContext().getNode();
        PropertySheet.editEntity(operation, expressionColumns, parent);
    }
}
