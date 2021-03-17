package com.intel.dronekitsample.anp;

import com.google.gson.*;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AnpLoader {

    static class PositionDeserializer implements JsonDeserializer<LatLongAlt> {
        @Override
        public LatLongAlt deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject obj = jsonElement.getAsJsonObject();
            double h = obj.get("height").getAsDouble();
            double lat = obj.get("latitude").getAsDouble();
            double lon = obj.get("longitude").getAsDouble();
            return new LatLongAlt(lat, lon, h);
        }
    }

    static Reader getAnpReader(File file) throws IOException {
        String fname = file.getName().toLowerCase();
        boolean anpFormat = !fname.endsWith(".json") || fname.endsWith(".anp") || fname.endsWith(".zip");

        Reader reader = null;
        if (anpFormat) {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry("project.json");
            if (entry != null) {
                reader = new InputStreamReader(zipFile.getInputStream(entry));
            }
        } else {
            reader = Files.newBufferedReader(file.toPath());
        }

        return reader;
    }

    /**
     * De-serialized ANP mission
     *
     * @param file either an ANP file (.anp) or a .json file
     * @return decoded mission
     * @throws IOException
     */
    public static AnpMission decode(File file) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LatLongAlt.class, new PositionDeserializer());
        Gson g = builder.create();

        AnpMission anp = null;
        try (Reader reader = getAnpReader(file)) {
            anp = g.fromJson(reader, AnpMission.class);
        }
        return anp;
    }

    public static void test(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: anparser <FILENAME>");
            return;
        }
        File file = new File(args[0]);

        System.out.println("Trying to decode ANP '" + file.exists());
        try {
            AnpMission mission = decode(file);
            System.out.println("decoded mission " + mission.description);
            System.out.println("got " + mission.getWaypoints().size() + " waypoints");
        } catch (IOException e) {
            System.err.println("error decoding mission file '" + file + "'");
            e.printStackTrace();
        }
    }


}
