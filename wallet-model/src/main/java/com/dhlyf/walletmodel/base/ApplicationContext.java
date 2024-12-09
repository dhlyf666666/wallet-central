package com.dhlyf.walletmodel.base;

import com.dhlyf.walletmodel.common.RequestAttributes;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

public class ApplicationContext {
    //    private static final boolean jsfPresent = ClassUtils.isPresent("javax.faces.context.FacesContext", SSOContext.class.getClassLoader());
    private static final ThreadLocal<RequestAttributes> requestAttributesHolder = new NamedThreadLocal("SSOContext attributes");
//    private static final ThreadLocal<SSOAttributes> inheritableRequestAttributesHolder = new NamedInheritableThreadLocal("SSOContext     context");

    public ApplicationContext() {
    }

    public static void resetRequestAttributes() {
        requestAttributesHolder.remove();
//        inheritableRequestAttributesHolder.remove();
    }

    public static void setRequestAttributes(@Nullable RequestAttributes attributes) {
        setRequestAttributes(attributes, false);
    }

    public static void setRequestAttributes(@Nullable RequestAttributes attributes, boolean inheritable) {
        if (attributes == null) {
            resetRequestAttributes();
        } else if (inheritable) {
//            inheritableRequestAttributesHolder.set(attributes);
            requestAttributesHolder.remove();
        } else {
            requestAttributesHolder.set(attributes);
//            inheritableRequestAttributesHolder.remove();
        }

    }

    @Nullable
    public static RequestAttributes getRequestAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();
        if (attributes == null) {
            attributes = RequestAttributes.builder().build();
        }
        return attributes;
    }

//    public static SSOAttributes currentRequestAttributes() throws IllegalStateException {
//        SSOAttributes attributes = getRequestAttributes();
//        if (attributes == null) {
//            if (jsfPresent) {
//                attributes = SSOContext.FacesRequestAttributesFactory.getFacesRequestAttributes();
//            }
//
//            if (attributes == null) {
//                throw new IllegalStateException("No thread-bound request found: Are you referring to request attributes outside of an actual web request, or processing a request outside of the originally receiving thread? If you are actually operating within a web request and still receive this message, your code is probably running outside of DispatcherServlet: In this case, use RequestContextListener or RequestContextFilter to expose the current request.");
//            }
//        }
//
//        return attributes;
//    }

//    private static class FacesRequestAttributesFactory {
//        private FacesRequestAttributesFactory() {
//        }
//
//        @Nullable
//        public static SSOAttributes getFacesRequestAttributes() {
//            FacesContext facesContext = FacesContext.getCurrentInstance();
//            return facesContext != null ? new FacesRequestAttributes(facesContext) : null;
//        }
//    }


}
