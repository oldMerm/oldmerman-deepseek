package io.github.oldmerman.core.api.provider.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BillingResponse(
        @JsonProperty("is_available") Boolean isAvailable,
        @JsonProperty("balance_infos") List<BalanceInfo> infos
) {
    public BalanceInfo info(){
        return infos.get(0);
    }
}
