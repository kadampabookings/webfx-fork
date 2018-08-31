package mongoose.services.authn;

import mongoose.domainmodel.loader.DomainModelSnapshotLoader;
import webfx.framework.expression.sqlcompiler.sql.SqlCompiled;
import webfx.framework.orm.domainmodel.DataSourceModel;
import webfx.framework.orm.domainmodel.HasDataSourceModel;
import webfx.framework.services.authn.spi.AuthenticationServiceProvider;
import webfx.framework.services.authn.UsernamePasswordCredentials;
import webfx.platform.services.query.QueryArgument;
import webfx.platform.services.query.QueryService;
import webfx.util.async.Future;

/**
 * @author Bruno Salmon
 */
public class MongooseAuthenticationServiceProviderImpl implements AuthenticationServiceProvider, HasDataSourceModel {

    private final DataSourceModel dataSourceModel;

    public MongooseAuthenticationServiceProviderImpl() {
        this(DomainModelSnapshotLoader.getDataSourceModel());
    }

    public MongooseAuthenticationServiceProviderImpl(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    @Override
    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    @Override
    public Future<MongooseUserPrincipal> authenticate(Object userCredentials) {
        if (!(userCredentials instanceof UsernamePasswordCredentials))
            return Future.failedFuture(new IllegalArgumentException("MongooseAuthenticationServiceProviderImpl requires a UsernamePasswordCredentials argument"));
        UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) userCredentials;
        Object[] parameters = {1, usernamePasswordCredentials.getUsername(), usernamePasswordCredentials.getPassword()};
        SqlCompiled sqlCompiled = getDomainModel().compileSelect("select id,frontendAccount.id from Person where frontendAccount.(corporation=? and username=? and password=?) order by id limit 1", parameters);
        return QueryService.executeQuery(new QueryArgument(sqlCompiled.getSql(), parameters, getDataSourceId()))
                .compose(result -> result.getRowCount() != 1 ? Future.failedFuture("Wrong user or password")
                      : Future.succeededFuture(new MongooseUserPrincipal(result.getValue(0, 0), result.getValue(0, 1)))
                );
    }
}
