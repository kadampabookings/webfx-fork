package mongoose.domainmodel.loader;

import mongoose.domainmodel.formatters.DateFormatter;
import mongoose.domainmodel.formatters.DateTimeFormatter;
import mongoose.domainmodel.formatters.PriceFormatter;
import mongoose.domainmodel.functions.AbcNames;
import mongoose.domainmodel.functions.DateIntervalFormat;
import mongoose.entities.*;
import mongoose.entities.impl.*;
import webfx.framework.expression.terms.function.DomainClassType;
import webfx.framework.expression.terms.function.Function;
import webfx.framework.expression.terms.function.InlineFunction;
import webfx.framework.orm.domainmodel.DataSourceModel;
import webfx.framework.orm.domainmodel.DomainModel;
import webfx.framework.orm.domainmodel.loader.DomainModelLoader;
import webfx.platform.services.json.Json;
import webfx.platform.services.json.codec.JsonCodecManager;
import webfx.platform.services.json.JsonElement;
import webfx.platform.services.query.QueryResult;
import webfx.platform.services.resource.ResourceService;
import webfx.type.PrimType;
import webfx.type.Type;
import webfx.util.async.Batch;
import webfx.util.async.Future;

import static webfx.framework.orm.entity.EntityFactoryRegistry.registerEntityFactory;
import static webfx.framework.ui.formatter.FormatterRegistry.registerFormatter;

/**
 * @author Bruno Salmon
 */
public class DomainModelSnapshotLoader {

    private static DomainModel domainModel;
    private final static DataSourceModel dataSourceModel = new DataSourceModel() {
        @Override
        public Object getId() {
            return 3;
        }

        @Override
        public DomainModel getDomainModel() {
            return getOrLoadDomainModel();
        }
    };

    public static synchronized DomainModel getOrLoadDomainModel() {
        if (domainModel == null)
            domainModel = loadDomainModelFromSnapshot();
        return domainModel;
    }

    public static DomainModel loadDomainModelFromSnapshot() {
        try {
            // Registering formats
            registerFormatter("price", PriceFormatter.INSTANCE);
            registerFormatter("date", DateFormatter.SINGLETON);
            registerFormatter("dateTime", DateTimeFormatter.SINGLETON);
            // Registering entity java classes
            registerEntityFactory(Attendance.class, "Attendance", AttendanceImpl::new);
            registerEntityFactory(Cart.class, "Cart", CartImpl::new);
            registerEntityFactory(Country.class, "Country", CountryImpl::new);
            registerEntityFactory(DateInfo.class, "DateInfo", DateInfoImpl::new);
            registerEntityFactory(Document.class, "Document", DocumentImpl::new);
            registerEntityFactory(DocumentLine.class, "DocumentLine", DocumentLineImpl::new);
            registerEntityFactory(Event.class, "Event", EventImpl::new);
            registerEntityFactory(GatewayParameter.class, "GatewayParameter", GatewayParameterImpl::new);
            registerEntityFactory(History.class, "History", HistoryImpl::new);
            registerEntityFactory(Image.class, "Image", ImageImpl::new);
            registerEntityFactory(Item.class, "Item", ItemImpl::new);
            registerEntityFactory(ItemFamily.class, "ItemFamily", ItemFamilyImpl::new);
            registerEntityFactory(Label.class, "Label", LabelImpl::new);
            registerEntityFactory(Mail.class, "Mail", MailImpl::new);
            registerEntityFactory(Method.class, "Method", MethodImpl::new);
            registerEntityFactory(Option.class, "Option", OptionImpl::new);
            registerEntityFactory(Organization.class, "Organization", OrganizationImpl::new);
            registerEntityFactory(OrganizationType.class, "OrganizationType", OrganizationTypeImpl::new);
            registerEntityFactory(Person.class, "Person", PersonImpl::new);
            registerEntityFactory(Rate.class, "Rate", RateImpl::new);
            registerEntityFactory(Site.class, "Site", SiteImpl::new);
            registerEntityFactory(Teacher.class, "Teacher", TeacherImpl::new);
            registerEntityFactory(SystemMetricsEntity.class, "Metrics", SystemMetricsEntityImpl::new);
            registerEntityFactory(MoneyTransfer.class, "MoneyTransfer", MoneyTransferImpl::new);
            registerEntityFactory(LtTestSetEntity.class, "LtTestSet", LtTestSetEntityImpl::new);
            registerEntityFactory(LtTestEventEntity.class, "LtTestEvent", LtTestEventEntityImpl::new);
            // Loading the model from the resource snapshot
            Future<String> text = ResourceService.getText("mongoose/domainmodel/DomainModelSnapshot.json");
            String jsonString = text.result(); // LZString.decompressFromBase64(text.result());
            JsonElement json = Json.parseObject(jsonString);
            Batch<QueryResult> snapshotBatch = JsonCodecManager.decodeFromJson(json);
            DomainModel domainModel = new DomainModelLoader(1).generateDomainModel(snapshotBatch);
            // Registering functions
            new AbcNames().register();
            new AbcNames("alphaSearch").register();
            new DateIntervalFormat().register();
            new Function("interpret_brackets", PrimType.STRING).register();
            new Function("compute_dates").register();
            new InlineFunction("searchMatchesDocument", "d", new Type[]{new DomainClassType("Document")}, "d..ref=?searchInteger or d..person_abcNames like ?abcSearchLike or d..person_email like ?searchEmailLike", domainModel.getClass("Document"), domainModel.getParserDomainModelReader()).register();
            new InlineFunction("searchMatchesPerson", "p", new Type[]{new DomainClassType("Person")}, "abcNames(p..firstName + ' ' + p..lastName) like ?abcSearchLike or p..email like ?searchEmailLike", "Person", domainModel.getParserDomainModelReader()).register();
            return domainModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }
}
