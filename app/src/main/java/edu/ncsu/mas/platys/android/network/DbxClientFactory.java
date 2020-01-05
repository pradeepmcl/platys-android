package edu.ncsu.mas.platys.android.network;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class DbxClientFactory {
    private static DbxClientV2 sDbxClient;

    private DbxClientFactory() {}

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            synchronized (DbxClientFactory.class) {
                if (sDbxClient == null) {
                    sDbxClient = new DbxClientV2(DbxClientRequestConfig.getRequestConfig(), accessToken);
                }
            }
        }
    }

//    public static void init(DbxCredential credential) {
//        credential = new DbxCredential(credential.getAccessToken(), -1L, credential.getRefreshToken(), credential.getAppKey());
//        if (sDbxClient == null) {
//            sDbxClient = new DbxClientV2(DbxRequestConfigFactory.getRequestConfig(), credential);
//        }
//    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }
}
