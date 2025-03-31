package com.paymentchain.customer.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "This model is used to return errors in RFC 7807 which created a generalized error-handling schema composed by five parts")
@NoArgsConstructor
@Data
public class StandarizedApiExceptionResponse {

    @Schema(description = "The unique uri identifier that categories the error", name = "type",
    requiredMode = Schema.RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized")
    private String type;

    @Schema(description = "A brief, human-readable message about the error", name = "title",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "The user does not have authorization")
    private String title;

    @Schema(description = "The unique error code", name = "code",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "192")
    private String code;

    @Schema(description = "A human-readable explanation of error", name = "detail",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "The user does not have the propertly permission to access the" +
            "resource, please contact with us")
    private String detail;

    @Schema(description = "A URI that identifier the specific ocurrence of the error", name = "instance",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized/01")
    private String instance;

    public StandarizedApiExceptionResponse(String type, String title, String code, String detail) {
        this.type = type;
        this.title = title;
        this.code = code;
        this.detail = detail;
    }
}
