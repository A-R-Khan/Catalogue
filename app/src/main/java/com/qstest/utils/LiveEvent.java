package com.qstest.utils;

/*
    Utility class to ensure events are only consumed once
 */

public class LiveEvent<T> {

    protected Boolean eventHandled;
    protected T data;

    public LiveEvent(){
        eventHandled = false;
    }

    public LiveEvent(T data){
        eventHandled = false;
        this.data = data;
    }

    private T getDataOnce(){
        if(eventHandled){
            return null;
        }
        else{
            eventHandled = true;
            return data;
        }
    }

    public T getDataOnceAndReset(){
        T _data = getDataOnce();
        data = null;
        return _data;
    }

    private T getData(){
        return data;
    }
}
