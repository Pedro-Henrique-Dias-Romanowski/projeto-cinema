package com.romanowski.pedro.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeignInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignInterceptor.class);
    private static final ThreadLocal<String> tituloThreadLocal = new ThreadLocal<>();

    @Override
    public void apply(RequestTemplate template) {
        String titulo = tituloThreadLocal.get();
        logger.info("FeignInterceptor.apply() chamado. Titulo capturado: {}", titulo);
        if (titulo != null) {
            template.header("titulo", titulo);
            logger.info("Header 'titulo' adicionado com valor: {}", titulo);
        } else {
            logger.warn("Titulo não encontrado no ThreadLocal!");
        }
    }

    public static void setTitulo(String titulo) {
        tituloThreadLocal.set(titulo);
    }

    public static void clearTitulo() {
        tituloThreadLocal.remove();
    }
}
