package io.github.oldmerman.core.api.chat.param;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.oldmerman.core.enums.ImageDetail;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Image (String url, ImageDetail detail){

}
