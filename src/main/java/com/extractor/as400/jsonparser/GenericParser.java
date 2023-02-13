package com.extractor.as400.jsonparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.extractor.as400.interfaces.IParser;

public class GenericParser implements IParser {

    @Override
    public <T> T parseFrom(String data, Class<T> type, final T into) throws Exception {
        Gson gson = new GsonBuilder().registerTypeAdapter(type, (InstanceCreator<T>) t -> into).create();

        return gson.fromJson(data, type);
    }

    @Override
    public String parseTo(Object data) {
        Gson gson = new Gson();

        return gson.toJson(data);
    }
}
