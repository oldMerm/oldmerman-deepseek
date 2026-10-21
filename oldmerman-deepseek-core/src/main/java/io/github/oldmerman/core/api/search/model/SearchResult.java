package io.github.oldmerman.core.api.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
        @JsonProperty("rerank_score") float rerankScore,
        String title,
        String url,
        String snippet,
        String summary,
        String content,
        @JsonProperty("site_name") String siteName,
        @JsonProperty("site_icon") String siteIcon,
        String date, Integer authority
) {
    @Override
    public String toString() {
        return "rerankScore: %s,\ntitle: %s,\nurl: %s,\nsnippet: %s,\nsummary: %s,\ncontent: %s,\nsiteName: %s,\nsiteIcon: %s,\ndate: %s,\nauthority: %s.".formatted(
                rerankScore, title, url, snippet, summary, content, siteName, siteIcon,
                date, authority
        );
    }
}
