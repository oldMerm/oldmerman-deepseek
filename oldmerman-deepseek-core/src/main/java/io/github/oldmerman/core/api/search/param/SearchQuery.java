package io.github.oldmerman.core.api.search.param;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQuery {
    private final List<String> limitSites;
    private final List<String> excludeSites;
    private final List<String> excludeKeyword;
    private final List<String> excludePhrase;
    private final List<String> specifyPhrase;
    private final List<String> lang;
    private final String after;
    private final String before;

    public String toQuery(String query) {
        StringBuilder builder = new StringBuilder(query);
        builder.append(' ');
        if (limitSites != null && !limitSites.isEmpty()) {
            for (int i = 0; i < limitSites.size(); i++) {
                if (i == limitSites.size() - 1) {
                    builder.append("site:").append(limitSites.get(i)).append(' ');
                    break;
                }
                builder.append("site:").append(limitSites.get(i)).append(" OR ");
            }
        }

        if (excludeSites != null && !excludeSites.isEmpty()) {
            builder.append("-site:");
            for (String excludeSite : excludeSites) {
                builder.append(excludeSite).append(' ');
            }
        }

        if (excludeKeyword != null && !excludeKeyword.isEmpty()) {
            builder.append("-");
            for (String s : excludeKeyword) {
                builder.append(s).append(' ');
            }
        }

        if (excludePhrase != null && !excludePhrase.isEmpty()) {
            builder.append("-\"");
            for (int i = 0; i < excludePhrase.size(); i++) {
                if (i == excludePhrase.size() - 1) {
                    builder.append(excludePhrase.get(i));
                    break;
                }
                builder.append(excludePhrase.get(i)).append(' ');
            }
            builder.append("\"").append(' ');
        }

        if (specifyPhrase != null && !specifyPhrase.isEmpty()) {
            builder.append("\"");
            for (int i = 0; i < specifyPhrase.size(); i++) {
                if (i == specifyPhrase.size() - 1) {
                    builder.append(specifyPhrase.get(i));
                    break;
                }
                builder.append(specifyPhrase.get(i)).append(' ');
            }
            builder.append("\"").append(' ');
        }

        if (lang != null && !lang.isEmpty()) {
            builder.append("lang:");
            for (int i = 0; i < lang.size(); i++) {
                if (i == lang.size() - 1) {
                    builder.append(lang.get(i)).append(' ');
                    break;
                }
                builder.append(lang.get(i)).append(',');
            }
        }

        if (after != null) {
            builder.append("after:").append(after).append(' ');
        }

        if (before != null) {
            builder.append("before:").append(before);
        }

        return builder.toString();
    }

    private SearchQuery(List<String> limitSites, List<String> excludeSites, List<String> excludeKeyword, List<String> excludePhrase,
                         List<String> specifyPhrase, List<String> lang, String after, String before){
        this.limitSites = limitSites;
        this.excludeSites = excludeSites;
        this.excludeKeyword = excludeKeyword;
        this.excludePhrase = excludePhrase;
        this.specifyPhrase = specifyPhrase;
        this.lang = lang;
        this.after = after;
        this.before = before;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private List<String> limitSites = null;
        private List<String> excludeSites = null;
        private List<String> excludeKeyword = null;
        private List<String> excludePhrase = null;
        private List<String> specifyPhrase = null;
        private List<String> lang = null;
        private String after = null;
        private String before = null;

        public Builder limitSites(List<String> limitSites){this.limitSites = limitSites; return this;}
        public Builder excludeSites(List<String> excludeSites){this.excludeSites = excludeSites; return this;}
        public Builder excludeKeyword(List<String> excludeKeyword){this.excludeKeyword = excludeKeyword; return this;}
        public Builder excludePhrase(List<String> excludePhrase){this.excludePhrase = excludePhrase; return this;}
        public Builder specifyPhrase(List<String> specifyPhrase){this.specifyPhrase = specifyPhrase; return this;}
        public Builder lang(List<String> lang){this.lang = lang; return this;}
        public Builder after(String after){this.after = after; return this;}
        public Builder before(String before){this.before = before; return this;}

        public SearchQuery build(){
            return new SearchQuery(limitSites, excludeSites, excludeKeyword, excludePhrase, specifyPhrase, lang, after, before);
        }
    }

    public List<String> getLimitSites() {
        return limitSites;
    }

    public List<String> getExcludeSites() {
        return excludeSites;
    }

    public List<String> getExcludeKeyword() {
        return excludeKeyword;
    }

    public List<String> getExcludePhrase() {
        return excludePhrase;
    }

    public List<String> getSpecifyPhrase() {
        return specifyPhrase;
    }

    public List<String> getLang() {
        return lang;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }
}
