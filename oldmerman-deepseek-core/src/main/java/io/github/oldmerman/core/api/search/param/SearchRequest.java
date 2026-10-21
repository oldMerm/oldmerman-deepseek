package io.github.oldmerman.core.api.search.param;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {
    private String query;
    private Integer count;
    @JsonProperty("ori_question")
    private String oriQuestion;
    private String localization;
    @JsonProperty("need_content")
    private Boolean needContent;

    private SearchRequest(String query, Integer count, String oriQuestion, String localization, Boolean needContent) {
        this.query = query;
        this.count = count;
        this.oriQuestion = oriQuestion;
        this.localization = localization;
        this.needContent = needContent;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private String query;
        private Integer count = null;
        private String oriQuestion = null;
        private String localization = null;
        private Boolean needContent = null;

        public Builder query(String query){
            this.query = query;
            return this;
        }
        public Builder count(int count){
            this.count = count;
            return this;
        }
        public Builder oriQuestion(String oriQuestion){
            this.oriQuestion = oriQuestion;
            return this;
        }
        public Builder localization(String localization){
            this.localization = localization;
            return this;
        }
        public Builder needContent(boolean needContent){
            this.needContent = needContent;
            return this;
        }

        public SearchRequest build(){
            return new SearchRequest(query, count, oriQuestion, localization, needContent);
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getOriQuestion() {
        return oriQuestion;
    }

    public void setOriQuestion(String oriQuestion) {
        this.oriQuestion = oriQuestion;
    }

    public String getLocalization() {
        return localization;
    }

    public void setLocalization(String localization) {
        this.localization = localization;
    }

    public Boolean getNeedContent() {
        return needContent;
    }

    public void setNeedContent(Boolean needContent) {
        this.needContent = needContent;
    }
}
