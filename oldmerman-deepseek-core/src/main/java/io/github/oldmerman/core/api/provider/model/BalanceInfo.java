package io.github.oldmerman.core.api.provider.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BalanceInfo(String currency,
                          @JsonProperty("total_balance") String totalBalance,
                          @JsonProperty("granted_balance") String grantedBalance,
                          @JsonProperty("topped_up_balance") String toppedUpBalance
){}
