package com.ai.common.results;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Set;

@RestControllerAdvice
public class ResultHttpStatusAdvice implements ResponseBodyAdvice<Object> {
    private static final int MODULUS = 1000;

    private static final Set<HttpStatus> ALLOWED = Set.of(
        HttpStatus.OK, HttpStatus.CREATED,
        HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED,
        HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND,
        HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.CONFLICT,
        HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.TOO_MANY_REQUESTS,
        HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
        HttpStatus.SERVICE_UNAVAILABLE
    );

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> r) {
            HttpStatus status = resolveHttpStatus(r.getCode());
            response.setStatusCode(status);
        }
        return body;
    }

    private HttpStatus resolveHttpStatus(int code) {
        // 业务码 映射 HTTP状态码
        HttpStatus mapped = HttpStatus.resolve(code / MODULUS);
        if (mapped != null && ALLOWED.contains(mapped)) {
            return mapped;
        }

        return HttpStatus.BAD_REQUEST;
    }
}