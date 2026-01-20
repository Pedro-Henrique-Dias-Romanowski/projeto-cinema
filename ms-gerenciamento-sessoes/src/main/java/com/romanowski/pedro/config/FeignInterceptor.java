package com.romanowski.pedro.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignInterceptor implements RequestInterceptor {

    private static final ThreadLocal<String> tituloThreadLocal = new ThreadLocal<>();

    @Override
    public void apply(RequestTemplate template) {
        String titulo = tituloThreadLocal.get();
        if (titulo != null) {
            template.header("titulo", titulo);
        }
    }

    public static void setTitulo(String titulo) {
        tituloThreadLocal.set(titulo);
    }

    public static void clearTitulo() {
        tituloThreadLocal.remove();
    }
}
