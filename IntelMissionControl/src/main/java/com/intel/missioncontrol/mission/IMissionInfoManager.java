package com.intel.missioncontrol.mission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface IMissionInfoManager {

    MissionInfo readFromFile(Path folder) throws IOException;

    void saveToFile(MissionInfo missionInfo);

    MissionInfo convertFromLegacySettings(Path folder) throws IOException;

    boolean configExists(File file);

    Path getLegacyConfigFile(File base);

    Path getConfigFile(File base);

}
