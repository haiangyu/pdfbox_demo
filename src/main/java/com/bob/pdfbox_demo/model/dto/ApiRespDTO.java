package com.bob.pdfbox_demo.model.dto;

import com.bob.pdfbox_demo.enums.StatusCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(builderMethodName = "innerBuilder")
public class ApiRespDTO<T> {

    @Builder.Default
    private String code = "00000";

    @Builder.Default
    private String message = "Success!!";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private transient T data;

    public static <T> ApiRespDTOBuilder<T> builder () {
        return innerBuilder();
    }

    public static <T> ApiRespDTOBuilder<T> builder(StatusCodeEnum statusCode, String message) {
        return (ApiRespDTOBuilder<T>) innerBuilder().code(statusCode.name()).message(message);
    }
}
