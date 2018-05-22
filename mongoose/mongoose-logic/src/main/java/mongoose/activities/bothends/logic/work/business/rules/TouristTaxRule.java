package mongoose.activities.bothends.logic.work.business.rules;

import mongoose.activities.bothends.logic.work.WorkingDocument;
import mongoose.entities.Option;

import static mongoose.activities.bothends.logic.work.business.logic.OptionLogic.isTouristTaxOption;

/**
 * @author Bruno Salmon
 */
public class TouristTaxRule extends BusinessRule {

    @Override
    public void apply(WorkingDocument wd) {
        if (!wd.hasAccommodation())
            wd.removeTouristTax();
        else if (!wd.hasTouristTax()) {
            Option touristTaxOption = wd.getEventAggregate().findFirstOption(o -> isTouristTaxOption(o) && (o.hasNoParent() || wd.getAccommodationLine() != null && o.getParent().getItem() == wd.getAccommodationLine().getItem()));
            if (touristTaxOption != null)
                addNewDependentLine(wd, touristTaxOption, wd.getAccommodationLine(), 0);
        }
    }
}
