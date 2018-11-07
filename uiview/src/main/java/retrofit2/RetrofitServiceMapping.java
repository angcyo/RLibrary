package retrofit2;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.angcyo.uiview.BuildConfig;
import com.angcyo.uiview.utils.Reflect;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/06
 */
public class RetrofitServiceMapping {
    public static boolean enableMapping = BuildConfig.DEBUG;
    public static Map<String, String> defaultMap = new ArrayMap<>();

    public static void init(boolean enableMapping, Map<String, String> methodMapping) {
        RetrofitServiceMapping.enableMapping = enableMapping;
        defaultMap = methodMapping;
    }

    /**
     * 请在调用
     * <pre>
     *     Retrofit.create()
     *     </pre>
     * 之前调用.
     * <p>
     * 暂不支持 Retrofit 的单例模式.
     */
    public static Retrofit mapping(@NonNull Retrofit retrofit, @NonNull Class<?> service) {
        if (defaultMap != null && !defaultMap.isEmpty() && enableMapping) {
            configRetrofit(retrofit, service, defaultMap);
        }
        return retrofit;
    }

    private static void configRetrofit(@NonNull Retrofit retrofit, @NonNull Class<?> service, @NonNull Map<String, String> map) {
        for (Method method : service.getDeclaredMethods()) {
            try {
                String mapUrl = map.get(method.getName());
                if (!TextUtils.isEmpty(mapUrl)) {
                    ServiceMethod.Builder methodBuilder = new ServiceMethod.Builder(retrofit, method);
                    ServiceMethod serviceMethod = methodBuilder.build();
                    Reflect.setFieldValue(serviceMethod, "relativeUrl", mapUrl);
                    Map<Method, ServiceMethod> serviceMethodCache = (Map<Method, ServiceMethod>) Reflect.getMember(retrofit, "serviceMethodCache");
                    serviceMethodCache.put(method, serviceMethod);
                    //Log.i("angcyo", "succeed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
