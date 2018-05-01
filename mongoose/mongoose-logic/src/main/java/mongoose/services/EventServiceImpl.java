package mongoose.services;

import mongoose.activities.shared.book.event.shared.FeesGroup;
import mongoose.activities.shared.logic.preselection.OptionsPreselection;
import mongoose.activities.shared.logic.work.WorkingDocument;
import mongoose.activities.shared.logic.work.business.logic.FeesGroupLogic;
import mongoose.entities.Cart;
import mongoose.entities.Event;
import mongoose.entities.Option;
import mongoose.entities.Rate;
import naga.framework.orm.domainmodel.DataSourceModel;
import naga.framework.orm.entity.*;
import naga.platform.client.bus.WebSocketBusOptions;
import naga.platform.services.query.QueryArgument;
import naga.platform.services.query.QueryResultSet;
import naga.platform.services.query.QueryService;
import naga.platform.spi.Platform;
import naga.util.Numbers;
import naga.util.Objects;
import naga.util.async.Future;
import naga.util.async.FutureBroadcaster;
import naga.util.collection.Collections;
import naga.util.function.Predicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
class EventServiceImpl implements EventService {

    private final static Map<Object, EventService> services = new HashMap<>();

    static EventService get(Object eventId) {
        return services.get(toKey(eventId));
    }

    static EventService getOrCreate(Object eventId, EntityStore store) {
        EventService service = get(eventId);
        if (service == null) {
            eventId = toKey(eventId);
            services.put(eventId, service = new EventServiceImpl(eventId, store));
        }
        return service;
    }

    static EventService getOrCreate(Object eventId, DataSourceModel dataSourceModel) {
        EventService service = get(eventId);
        if (service == null)
            service = getOrCreate(eventId, EntityStore.create(dataSourceModel));
        return service;
    }

    private static Object toKey(Object eventId) {
        if (eventId instanceof EntityId)
            eventId = ((EntityId) eventId).getPrimaryKey();
        if (eventId instanceof Number)
            eventId = eventId.toString();
        return eventId;
    }

    private final Object eventId;
    private final EntityStore store;
    private Event event;
    private Cart currentCart;
    private PersonService personService;

    private EventServiceImpl(Object eventId, EntityStore store) {
        this.eventId = eventId;
        this.store = store;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return store.getDataSourceModel();
    }

    @Override
    public EntityStore getEventStore() {
        return store;
    }

    @Override
    public PersonService getPersonService() {
        if (personService == null)
            personService = PersonService.getOrCreate(store);
        return personService;
    }

    private FutureBroadcaster<Event> eventFutureBroadcaster;

    @Override
    public Future<Event> onEvent() {
        if (getEvent() != null)
            return Future.succeededFuture(event);
/*
        if (eventOptionsFutureBroadcaster != null)
            return eventOptionsFutureBroadcaster.newClient().map(this::getEvent);
*/
        if (eventFutureBroadcaster == null)
            eventFutureBroadcaster = new FutureBroadcaster<>(
                store.executeQuery("select <frontend_loadEvent> from Event where id=" + eventId, EVENTS_LIST_ID)
                .map(this::getEvent));
        return eventFutureBroadcaster.newClient();
    }

    // Event options loading method
    private FutureBroadcaster<EntityList<Option>> eventOptionsFutureBroadcaster;

    @Override
    public Future<EntityList<Option>> onEventOptions() {
        if (eventOptionsFutureBroadcaster == null) {
            String host = getHost();
            Object[] parameters = {eventId, host, host, false /* isDeveloper */};
            // Loading event options
            String optionCondition = "event.(id=? and (host=null or host=? or ?='localhost')) and online and (!dev or ?=true)";
            String siteIds = "(select site.id from Option where " + optionCondition + ")";
            String rateCondition = "site.id in " + siteIds + " and (startDate is null or startDate <= site.event.endDate) and (endDate is null or endDate >= site.event.startDate) and (onDate is null or onDate <= now()) and (offDate is null or offDate > now())";
            eventOptionsFutureBroadcaster = new FutureBroadcaster<>(store.executeQueryBatch(
                      new EntityStoreQuery("select <frontend_loadEvent> from Option where " + optionCondition + " order by ord", parameters, OPTIONS_LIST_ID)
                    , new EntityStoreQuery("select <frontend_loadEvent> from Site where id in " + siteIds, parameters, SITES_LIST_ID)
                    , new EntityStoreQuery("select <frontend_loadEvent> from Rate where " + rateCondition, parameters, RATES_LIST_ID)
                    , new EntityStoreQuery("select <frontend_loadEvent> from DateInfo where event=? order by id", new Object[]{eventId}, DATE_INFOS_LIST_ID)
            ).map(this::getEventOptions));
        }
        return eventOptionsFutureBroadcaster.newClient();
    }

    // Event options accessors (once loaded)

    @Override
    public Event getEvent() {
        if (event == null) {
            event = store.getEntity("Event", eventId); // eventId may be from the wrong type (ex: String) because coming from the url
            if (event == null) // If not found, trying now with integer (should work for Java platforms)
                event = store.getEntity("Event", Numbers.toInteger(eventId));
            if (event == null) // If not found, trying now with double (should work for Web platforms)
                event = store.getEntity("Event", Numbers.toDouble(eventId));
        }
        return event;
    }

    @Override
    public <E extends Entity> EntityList<E> getEntityList(Object listId) {
        return store.getEntityList(listId);
    }

    @Override
    public void clearEntityList(Object listId) {
        store.clearEntityList(listId);
    }

    @Override
    public void clearEventOptions() {
        clearEntityList(OPTIONS_LIST_ID);
        feesGroups = null;
        eventOptionsFutureBroadcaster = null;
    }

    @Override
    public List<Option> getChildrenOptions(Option parent) {
        return selectOptions(o -> o.getParent() == parent);
    }

    //// Breakfast option
    private Option breakfastOption; // cached for better performance

    @Override
    public Option getBreakfastOption() {
        return breakfastOption;
    }

    @Override
    public void setBreakfastOption(Option breakfastOption) {
        this.breakfastOption = breakfastOption;
    }

    @Override
    public void setDefaultDietOption(Option defaultDietOption) {
        this.defaultDietOption = defaultDietOption;
    }

    //// Diet option
    private Option defaultDietOption; // cached for better performance

    @Override
    public Option getDefaultDietOption() {
        return defaultDietOption;
    }

    private Rate findFirstRate(Predicate<? super Rate> predicate) {
        return Collections.findFirst(getEventRates(), predicate);
    }

    private boolean hasRate(Predicate<? super Rate> predicate) {
        return findFirstRate(predicate) != null;
    }

    private Boolean hasUnemployedRate;
    @Override
    public boolean hasUnemployedRate() {
        if (hasUnemployedRate == null)
            hasUnemployedRate = hasRate(rate -> Objects.anyNotNull(rate.getUnemployedPrice(), rate.getUnemployedDiscount()));
        return hasUnemployedRate;
    }

    private Boolean hasFacilityFeeRate;
    @Override
    public boolean hasFacilityFeeRate() {
        if (hasFacilityFeeRate == null)
            hasFacilityFeeRate = hasRate(rate -> Objects.anyNotNull(rate.getFacilityFeePrice(), rate.getFacilityFeeDiscount()));
        return hasFacilityFeeRate;
    }

    // Fees groups loading method

    private FeesGroup[] feesGroups;

    @Override
    public FeesGroup[] getFeesGroups() {
        if (feesGroups == null)
            feesGroups = FeesGroupLogic.createFeesGroups(this);
        return feesGroups;
    }

    @Override
    public Future<FeesGroup[]> onFeesGroups() {
        if (feesGroups != null)
            return Future.succeededFuture(feesGroups);
        return onEventOptions().map(this::getFeesGroups);
    }

    // Event availability loading method
    private FutureBroadcaster<QueryResultSet> eventAvailabilitiesFutureBroadcaster;

    @Override
    public Future<QueryResultSet> onEventAvailabilities() {
        if (eventAvailabilitiesFutureBroadcaster == null)
            eventAvailabilitiesFutureBroadcaster = new FutureBroadcaster<>(() -> QueryService.executeQuery(new QueryArgument(
                    "with ra as (select * from resource_availability_by_event_items(?) where max>0)," + // resources with max(=max_online)=0 (like private rooms) are not displayed in the frontend
                    // let's see if some options for this event require to have the per day availabilities details
                    " pda as (select site_id,item_id,item_family_id from option where per_day_availability and event_id=?)" +
                    // for such options we keep all the details: site, item and date (this applies to availabilities having site=option.site and item=option.item if set, item_family=item.family otherwise)
                    " (select row_number,      site_id as site,      item_id as item,      date,         max - current as available,      i.ord as ord      from ra join item i on i.id=item_id where     exists(select * from pda where site_id=ra.site_id and (item_id=ra.item_id or item_id is null and item_family_id=i.family_id)) )" +
                    " union " + // union of both queries
                    // for others, we group by site and item (=> dates disappears => simpler and less data to transfer to browser) and keep the min values for availability all over the event time range
                    " (select min(row_number), min(site_id) as site, min(item_id) as item, null as date, min(max - current) as available, min(i.ord) as ord from ra join item i on i.id=item_id where not exists(select * from pda where site_id=ra.site_id and (item_id=ra.item_id or item_id is null and item_family_id=i.family_id)) group by site_id,item_id)" +
                    // finally we order this query union by site, item and date
                    " order by site,ord,date", new Object[]{eventId, eventId}, getDataSourceId()))
                    .map(rs -> eventAvailabilities = rs));
        return eventAvailabilitiesFutureBroadcaster.newClient();
    }

    // Event availability accessor

    private QueryResultSet eventAvailabilities;
    @Override
    public QueryResultSet getEventAvailabilities() {
        return eventAvailabilities;
    }


    // Private implementation methods

    private static String getHost() {
        return ((WebSocketBusOptions) Platform.getBusOptions()).getServerHost();
    }

    private OptionsPreselection selectedOptionsPreselection;
    @Override
    public void setSelectedOptionsPreselection(OptionsPreselection selectedOptionsPreselection) {
        this.selectedOptionsPreselection = selectedOptionsPreselection;
    }

    @Override
    public OptionsPreselection getSelectedOptionsPreselection() {
        return selectedOptionsPreselection;
    }

    private WorkingDocument workingDocument;
    @Override
    public void setWorkingDocument(WorkingDocument workingDocument) {
        this.workingDocument = workingDocument;
    }

    @Override
    public WorkingDocument getWorkingDocument() {
        if (workingDocument == null && selectedOptionsPreselection != null)
            workingDocument = selectedOptionsPreselection.getWorkingDocument();
        return workingDocument;
    }

    @Override
    public Cart getCurrentCart() {
        return currentCart;
    }

    @Override
    public void setCurrentCart(Cart currentCart) {
        this.currentCart = currentCart;
    }
}
