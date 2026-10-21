package io.github.oldmerman.core.api.provider.model;

import java.util.List;

public record ModelListResponse(String object, List<ModelInfo> data) {
}
