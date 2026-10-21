package io.github.oldmerman.core.api.search.model;

import java.util.List;

public record SearchResponse(List<SearchResult> results) {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            builder.append("search result %s\n".formatted(i)).append(results.get(i));
        }
        return builder.toString();
    }
}
