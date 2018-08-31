package mongoose.activities.bothends.logic.work.business.logic;

import mongoose.activities.bothends.book.shared.FeesGroup;
import mongoose.activities.bothends.book.shared.FeesGroupBuilder;
import mongoose.entities.DateInfo;
import mongoose.entities.Option;
import mongoose.entities.Site;
import mongoose.aggregates.EventAggregate;
import mongoose.util.Labels;
import webfx.framework.orm.entity.EntityList;
import webfx.util.collection.Collections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class FeesGroupLogic {

    public static FeesGroup[] createFeesGroups(EventAggregate eventAggregate) {
        List<FeesGroup> feesGroups = new ArrayList<>();
        EntityList<DateInfo> dateInfos = eventAggregate.getEventDateInfos();
        List<Option> defaultOptions = OptionLogic.selectDefaultOptions(eventAggregate);
        List<Option> accommodationOptions = eventAggregate.selectOptions(o -> o.isConcrete() && o.isAccommodation());
        if (!dateInfos.isEmpty())
            for (DateInfo dateInfo : dateInfos)
                populateFeesGroups(eventAggregate, dateInfo, defaultOptions, accommodationOptions, feesGroups);
        else if (eventAggregate.getEvent() != null) // May happen if event is empty (ie has no option)
            populateFeesGroups(eventAggregate, null, defaultOptions, accommodationOptions, feesGroups);
        return Collections.toArray(feesGroups, FeesGroup[]::new);
    }

    private static void populateFeesGroups(EventAggregate eventAggregate, DateInfo dateInfo, List<Option> defaultOptions, List<Option> accommodationOptions, List<FeesGroup> feesGroups) {
        Map<Site, List<Option>> accommodationOptionsBySite = new LinkedHashMap<>(); // accommodationOptions.stream().collect(Collectors.groupingBy(Option::getSite));
        Collections.forEach(accommodationOptions, o -> accommodationOptionsBySite.computeIfAbsent(o.getSite(), k -> new ArrayList<>()).add(o));
        boolean multiAccommodationSites = accommodationOptionsBySite.size() > 1;
        if (multiAccommodationSites)
            feesGroups.add(createFeesGroup(eventAggregate, null, "NoAccommodation", dateInfo, defaultOptions, java.util.Collections.emptyList(), true));
        boolean addNoAccommodationOption = !multiAccommodationSites && !eventAggregate.getEvent().getName().contains("Overnight");
        for (Map.Entry<Site, List<Option>> entry : accommodationOptionsBySite.entrySet())
            feesGroups.add(createFeesGroup(eventAggregate, entry.getKey(), null, dateInfo, defaultOptions, entry.getValue(), addNoAccommodationOption));
    }

    private static FeesGroup createFeesGroup(EventAggregate eventAggregate, Object label, String i18nKey, DateInfo dateInfo, List<Option> defaultOptions, List<Option> accommodationOptions, boolean addNoAccommodationOption) {
        return new FeesGroupBuilder(eventAggregate)
                .setLabel(Labels.bestLabelOrName(label))
                .setI18nKey(i18nKey)
                .setDateInfo(dateInfo)
                .setDefaultOptions(defaultOptions)
                .setAccommodationOptions(accommodationOptions)
                .setAddNoAccommodationOption(addNoAccommodationOption)
                .build();
    }
}
