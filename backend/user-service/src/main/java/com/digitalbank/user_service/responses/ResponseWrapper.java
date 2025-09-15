package com.digitalbank.user_service.responses;

public class ResponseWrapper<T> {
    private T data;
    private String error;

    public ResponseWrapper(T data) {
        this.data = data;
        this.error = null;
    }

    public ResponseWrapper(String error) {
        this.error = error;
        this.data = null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
