package com.ai.common.results;

import com.ai.common.enums.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(StatusCode sc, T data) {
        Result<T> result = new Result<>();
        result.setCode(sc.getCode());
        result.setMsg(sc.getMsg());
        result.setData(data);
        return result;
    }

    public static Result<Void> success(StatusCode sc) {
        Result<Void> result = new Result<>();
        result.setCode(sc.getCode());
        result.setMsg(sc.getMsg());
        return result;
    }

    public static <T> Result<T> error(StatusCode sc) {
        Result<T> result = new Result<>();
        result.setCode(sc.getCode());
        result.setMsg(sc.getMsg());
        return result;
    }
}
