package de.stiffi.hivemq.plugin.callbacks;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hivemq.spi.aop.cache.Cached;
import com.hivemq.spi.callback.cluster.ClusterDiscoveryCallback;
import com.hivemq.spi.callback.cluster.ClusterNodeAddress;
import com.hivemq.spi.callback.schedule.ScheduledCallback;
import com.hivemq.spi.metrics.annotations.Timed;
import com.hivemq.spi.services.configuration.GeneralConfigurationService;
import de.stiffi.hivemq.plugin.configuration.Configuration;
import de.stiffi.hivemq.plugin.database.JdbcDatabaseClusterStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Callback Class that manages the cluster Nodes
 */
public class ClusterCallback implements ClusterDiscoveryCallback, ScheduledCallback {

    private static final Logger log = LoggerFactory.getLogger(ClusterCallback.class);

    private final JdbcDatabaseClusterStore clusterStore;
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    private final Configuration config;

    private String clusterId;

    @Inject
    public ClusterCallback(JdbcDatabaseClusterStore clusterStore, Configuration config) {
        this.clusterStore = clusterStore;
        this.config = config;
    }

    @Override
    public void init(String clusterId, ClusterNodeAddress ownAddress) {
        this.clusterId = clusterId;
        log.info("Add HiveMQ with clusterId {} and clusterAddress {}:{} to Cluster.", clusterId, ownAddress.getHost(), ownAddress.getPort());
        clusterStore.addClusterNode(clusterId, ownAddress);
    }

    @Override
    public ListenableFuture<List<ClusterNodeAddress>> getNodeAddresses() {
        log.trace("getNodeAddresses()...");
        ListenableFuture<List<ClusterNodeAddress>> future = executorService.submit(new Callable<List<ClusterNodeAddress>>() {
            @Timed(name = "dbcluster.getnodeaddresses")
            @Override
            public List<ClusterNodeAddress> call() throws Exception {
                updateLastSeen();
                return clusterStore.getNodeAddresses();
            }
        });
        return future;
    }

    @Override
    public void destroy() {
        log.info("Removing HiveMQ with clusterId {} from central registry", clusterId);
        clusterStore.remove(clusterId);
    }

    @Timed(name = "dbcluster.updatelastseen")
    @Cached(timeToLive = 1000, timeUnit = TimeUnit.MILLISECONDS)
    private void updateLastSeen() {
        if (clusterId == null) {
            //Updating is only necessary once we have a valid clusterId
            return;
        }
        log.trace("Update Last Seen for clusterId {}.", clusterId);
        clusterStore.updateLastSeen(clusterId);
    }


    /**
     * Execute ScheduledCallback (update lastSeen)
     */
    @Override
    public void execute() {
        log.info("execute()");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                updateLastSeen();
            }
        });
    }

    /**
     * CronExpression for ScheduledCallback.
     *
     * @return
     */
    @Override
    public String cronExpression() {
        long inactiveTimeoutMs = config.getInactiveTimeoutMs();

        //Double the execution interval of theh inactiveTimeout
        int secondsDelay = (int) (inactiveTimeoutMs / 1000 / 2);

        return "0/" + secondsDelay + " * * * * ?";

    }
}
