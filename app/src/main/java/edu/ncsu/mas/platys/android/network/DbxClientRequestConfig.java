package edu.ncsu.mas.platys.android.network;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;

public class DbxClientRequestConfig {

    private static DbxRequestConfig sDbxRequestConfig;

    private DbxClientRequestConfig() {}

    public static DbxRequestConfig getRequestConfig(){
        if(sDbxRequestConfig == null) {
            synchronized (DbxClientRequestConfig.class) {
                if (sDbxRequestConfig == null) {
                    sDbxRequestConfig = DbxRequestConfig.newBuilder("platys-v2")
                            .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                            .build();
                }
            }
        }
        return sDbxRequestConfig;
    }
}
