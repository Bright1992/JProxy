package util;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;

public class Status {
    static HttpResponseStatus getSuccess() {
        HttpResponseStatus ret=null;
        try {
            Method newStatus = HttpResponseStatus.class.getDeclaredMethod("newStatus", int.class, String.class);
            newStatus.setAccessible(true);
            ret = (HttpResponseStatus) newStatus.invoke(null, 200, "Connection Established");
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
    public static final HttpResponseStatus SUCCESS = getSuccess();
}
