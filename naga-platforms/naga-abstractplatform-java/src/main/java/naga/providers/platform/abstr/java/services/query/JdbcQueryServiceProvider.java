package naga.providers.platform.abstr.java.services.query;

import naga.platform.services.datasource.ConnectionDetails;
import naga.platform.services.query.spi.remote.RemoteQueryServiceProvider;
import naga.platform.services.query.spi.QueryServiceProvider;
import naga.providers.platform.abstr.java.services.JdbcConnectedServiceProviderProvider;

/**
 * @author Bruno Salmon
 */
public class JdbcQueryServiceProvider extends RemoteQueryServiceProvider {

    @Override
    protected QueryServiceProvider createLocalConnectedProvider(ConnectionDetails connectionDetails) {
        return new JdbcConnectedServiceProviderProvider(connectionDetails);
    }

}
