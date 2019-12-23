package mongoose.server.jobs.sessioncloser;

import webfx.framework.server.services.push.PushServerService;
import webfx.framework.server.services.push.UnresponsivePushClientListener;
import webfx.framework.shared.services.datasourcemodel.DataSourceModelService;
import webfx.platform.shared.services.appcontainer.spi.ApplicationJob;
import webfx.platform.shared.services.log.Logger;
import webfx.platform.shared.services.update.UpdateArgument;
import webfx.platform.shared.services.update.UpdateService;

/**
 * @author Bruno Salmon
 */
public final class MongooseServerUnresponsiveClientSessionCloserJob implements ApplicationJob {

    private UnresponsivePushClientListener disconnectListener;

    @Override
    public void onStart() {
        PushServerService.addUnresponsivePushClientListener(disconnectListener = pushClientId ->
                UpdateService.executeUpdate(new UpdateArgument(DataSourceModelService.getDefaultDataSourceId(), "DQL",
                        "update SessionConnection set end=now() where process=?", pushClientId))
                        .setHandler(ar -> {
                            if (ar.failed())
                                Logger.log("Error while closing session for pushClientId=" + pushClientId, ar.cause());
                            else
                                Logger.log("Closed session for pushClientId=" + pushClientId);
                        }));
    }

    @Override
    public void onStop() {
        PushServerService.removeUnresponsivePushClientListener(disconnectListener);
    }

}
