package com.github.ant.job;

import java.util.Map;
import java.util.Objects;

public class HttpTask extends TaskParam<HttpTask>{
    private String getOrPost;
    private String url;
    private Boolean successCode;
    private Long timeout;
    private String param;
    private Map<String,String> header;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpTask httpTask = (HttpTask) o;
        return Objects.equals(getOrPost, httpTask.getOrPost) &&
                Objects.equals(url, httpTask.url) &&
                Objects.equals(successCode, httpTask.successCode) &&
                Objects.equals(timeout, httpTask.timeout) &&
                Objects.equals(param, httpTask.param) &&
                Objects.equals(header, httpTask.header);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrPost, url, successCode, timeout, param, header);
    }

    public HttpTask(String getOrPost, String url, String param, Map<String, String> header, Boolean successCode, Long timeout) {
        this.getOrPost = getOrPost;
        this.url = url;
        this.param = param;
        this.header = header;
        this.successCode = successCode;
        this.timeout = timeout;
    }

    public HttpTask(String getOrPost, String url, Boolean successCode, Long timeout) {
        this.getOrPost = getOrPost;
        this.url = url;
        this.successCode = successCode;
        this.timeout = timeout;
    }

    public HttpTask() {
    }

    public String getGetOrPost() {
        return getOrPost;
    }

    public void setGetOrPost(String getOrPost) {
        this.getOrPost = getOrPost;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Boolean getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(Boolean successCode) {
        this.successCode = successCode;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
