package com.enioka.scanner.bt;

public interface CommandCallback<T> {
    void onSuccess(T data);

    //void onFailure();
}
