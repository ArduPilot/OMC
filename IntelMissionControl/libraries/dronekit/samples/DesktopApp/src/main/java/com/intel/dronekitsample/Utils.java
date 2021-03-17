package com.intel.dronekitsample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import javafx.beans.property.Property;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static void dumpParametersToJson(File file, Parameters params) {
        List<Parameter> parameters = params.getParameters();

        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(parameters, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T defaultVal(T val, T defaultVal) {
        return val == null ? defaultVal : val;
    }

    public <T> T defaultProp(Property<T> prop, T defaultVal) {
        return prop == null ? defaultVal : defaultVal(prop.getValue(), defaultVal);
    }

    public static Parameters parametersFromJson(File file) {
        List<Parameter> parameters = Collections.emptyList();
        try (Reader reader = new FileReader(file)) {
            Gson gson = new GsonBuilder().create();
            parameters = gson.fromJson(reader, new TypeToken<List<Parameter>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Parameters(parameters);
    }

}
