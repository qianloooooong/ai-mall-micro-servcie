package com.ai.common.handler;

import com.ai.common.enums.StatusCode;
import com.ai.common.exception.BaseException;
import com.ai.common.results.Result;
import com.ai.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.ai.common.enums.StatusCode.ACCESS_DENIED;
import static com.ai.common.enums.StatusCode.FAILURE;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    @ExceptionHandler
    public Result<String> handleException(Exception e){
        log.error("捕获Exception异常", e);
        return Result.error(FAILURE);
    }

    @ExceptionHandler
    public Result<String> handleException(BaseException e){
        StatusCode sc = e.getStatusCode();
        log.error("捕获{}异常: code={}, desc={}", e.getClass().getSimpleName(), sc.getCode(), e.getMessage());
        return Result.error(sc);
    }

    @ExceptionHandler
    public Result<String> handleException(AuthorizationDeniedException e){
        log.warn("捕获AuthorizationDeniedException异常: UserId={}, Roles={}", UserContext.getUserId(), UserContext.getRoles());
        return Result.error(ACCESS_DENIED);
    }
}
