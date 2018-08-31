package mongoose.activities.backend.letter;

import mongoose.activities.bothends.generic.routing.MongooseRoutingUtil;
import webfx.framework.activity.base.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import webfx.framework.ui.uirouter.UiRoute;

/**
 * @author Bruno Salmon
 */
public final class LetterRouting {

    private final static String PATH = "/letter/:letterId";

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(PATH
                , false
                , LetterActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }

    public static String getEditLetterPath(Object letterId) {
        return MongooseRoutingUtil.interpolateLetterIdInPath(letterId, PATH);
    }
}
