package com.db.awmd.challenge.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TransferData {

    @NotNull
    @NotEmpty
    private String accountFromId;

    @NotNull
    @NotEmpty
    @JsonProperty
    private String accountToId;

    @NotNull
    @NotEmpty
    @Min(value = 0, message = "Initial amount must be positive.")
    private BigDecimal amount;

    public TransferData(String accountFromId, String accountToId, BigDecimal amount) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }
}
