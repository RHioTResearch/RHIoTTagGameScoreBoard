package org.jboss.rhiot.simulators.kura;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudService;

/**
 * Created by starksm on 6/20/16.
 */
public class StandaloneCloudService implements CloudService {
    private String gatewayID;

    public StandaloneCloudService() {
        this("DN2016-GW14");
    }
    public StandaloneCloudService(String gatewayID) {
        this.gatewayID = gatewayID;
    }
    @Override
    public CloudClient newCloudClient(String appID) throws KuraException {
        StandaloneCloudClient scc = new StandaloneCloudClient(appID, gatewayID);
        try {
            scc.start();
        } catch (Exception e) {
            throw new KuraException(KuraErrorCode.INTERNAL_ERROR, e);
        }
        return scc;
    }

    @Override
    public String[] getCloudApplicationIdentifiers() {
        return new String[0];
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
