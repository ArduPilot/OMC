/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.intel.missioncontrol.persistence.insight.primitives.*;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public final class ProjectTest {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @NotNull
    private final String adminUserName = "Admin1";

    @NotNull
    private final String adminPassword = "password";

    @NotNull
    private final String user1C1 = "test.user1-c1@intel.com";

    @NotNull
    private final String user2C1 = "test.user2-c1@intel.com";

    @NotNull
    private final String user1C2 = "test.user1-c2@intel.com";

    @NotNull
    private String user2C2 = "test.user2-c2@intel.com";

    @NotNull
    private String user;

    @NotNull
    private String testServerUrl;

    @NotNull
    private String userPassword;

    @NotNull
    private String usedLoginUser;

    @NotNull
    private String usedLoginPassword;

    @NotNull
    private String usedTestServer;

    private Gson gson;

    @NotNull
    private static InsightContext iC;

    @NotNull
    private static HashMap headers;

    @NotNull
    private static String randomTestIdentifier;

    @NotNull
    private static String testProjectName;

    @Nullable
    private String createdTestProjectId;

    @Nullable
    private String createdProjectTimespamp;

    @Nullable
    private String modifiedProjectTimespamp;

    @Nullable
    private String createdTestMissionId;

    @Nullable
    private String createdAnnotationId;

    @Nullable
    private String createdFlightPlanId;

    @Nullable
    private String createdFlightId;

    @Nullable
    private String createdCameraId;

    @Nullable
    private PhotoPrep createdPhotoPrep;

    @NotNull
    private List photoSearchResp;

    private final List testDataCoordinates;
    private final List testDataBBox;

    @NotNull
    private final String testDataMissionSurveyDate;

    @NotNull
    private final String testDataVerticalSrsWkt;

    @NotNull
    private final String testDataHorizontalSrsWkt;

    @BeforeAll
    public static final void loginToTestServer() throws ExecutionException, InterruptedException {
        iC.logInAsync().get();
        headers = iC.getHeaders();
        String var7 = UUID.randomUUID().toString();

        randomTestIdentifier = var7;
        testProjectName = testProjectName + randomTestIdentifier;
    }

    @NotNull
    public final Projects searchProjectByName(@NotNull String name) {
        JsonObject search = new JsonObject();
        search.addProperty("search", name);
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.post(this.usedTestServer + "uisrv/projects/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)search))
                .asString();
        Gson var10000 = this.gson;

        Projects resp = (Projects)var10000.fromJson((String)reqs.getBody(), Projects.class);
        assertEquals(reqs.getStatus(), 200);

        return resp;
    }

    @Nullable
    public final com.intel.missioncontrol.persistence.insight.primitives.Project getProjectById(@NotNull String id) {
        HttpResponse resp =
            ((GetRequest)Unirest.get(this.usedTestServer + "dxpm/projects/" + id).headers((Map)this.headers))
                .asString();
        com.intel.missioncontrol.persistence.insight.primitives.Project project =
            new com.intel.missioncontrol.persistence.insight.primitives.Project();

        if (resp.getStatus() == 200) {
            Object var10000 =
                this.gson.fromJson(
                    (String)resp.getBody(), com.intel.missioncontrol.persistence.insight.primitives.Project.class);

            project = (com.intel.missioncontrol.persistence.insight.primitives.Project)var10000;
        }

        return project;
    }

    @Nullable
    public final com.intel.missioncontrol.persistence.insight.primitives.Mission getMissionById(@NotNull String id) {
        HttpResponse resp =
            ((GetRequest)Unirest.get(this.usedTestServer + "dxpm/missions/" + id).headers((Map)this.headers))
                .asString();
        com.intel.missioncontrol.persistence.insight.primitives.Mission mission =
            new com.intel.missioncontrol.persistence.insight.primitives.Mission();

        if (resp.getStatus() == 200) {
            Object var10000 =
                this.gson.fromJson(
                    (String)resp.getBody(), com.intel.missioncontrol.persistence.insight.primitives.Mission.class);

            mission = (com.intel.missioncontrol.persistence.insight.primitives.Mission)var10000;
        }

        return mission;
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new Project")
    public final void createNewProject() throws ExecutionException, InterruptedException {
        // this.loginToTestServer();
        JsonObject project = new JsonObject();
        project.addProperty("name", this.testProjectName);
        project.addProperty("addProjectToUsers", true);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "uisrv/projects").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)project))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        ProjectResponse resp = (ProjectResponse)this.gson.fromJson((String)reqs.getBody(), ProjectResponse.class);
        com.intel.missioncontrol.persistence.insight.primitives.Project var10001 = resp.getProject();
        if (var10001 == null) {}

        assertNotEquals("-1", var10001.getId());
        String var10000 = this.testProjectName;
        var10001 = resp.getProject();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getName());
        var10001 = resp.getProject();
        if (var10001 == null) {}

        this.createdTestProjectId = var10001.getId();
        var10001 = resp.getProject();
        if (var10001 == null) {}

        this.createdProjectTimespamp = var10001.getCreated();
        var10001 = resp.getProject();
        if (var10001 == null) {}

        this.modifiedProjectTimespamp = var10001.getModificationDate();
        StringBuilder var6 = (new StringBuilder()).append("Created Project Id: ");
        var10001 = resp.getProject();
        if (var10001 == null) {}

        String var4 = var6.append(var10001.getId()).toString();
        boolean var5 = false;
        System.out.println(var4);
    }

    @Test
    @Order(2)
    public final void findProject() {
        Projects projects = this.searchProjectByName(this.testProjectName);
        assertEquals(projects.getProjects().size(), 1);
    }

    @Test
    @Order(3)
    public final void updateProject() {
        String var10001 = this.createdTestProjectId;
        if (var10001 == null) {}

        com.intel.missioncontrol.persistence.insight.primitives.Project projectToUpdate = this.getProjectById(var10001);
        String var10000 = this.createdTestProjectId;
        if (projectToUpdate == null) {}

        assertEquals(var10000, projectToUpdate.getId());
        this.testProjectName = this.testProjectName + " updated";
        String oldName = projectToUpdate.getName();
        projectToUpdate.setName(this.testProjectName);
        projectToUpdate.setPlaceName("Tampere Hervanta");
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "dxpm/projects/" + projectToUpdate.getId())
                        .headers((Map)this.headers))
                .body(this.gson.toJson(projectToUpdate))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        com.intel.missioncontrol.persistence.insight.primitives.Project resp =
            (com.intel.missioncontrol.persistence.insight.primitives.Project)
                this.gson.fromJson(
                    (String)reqs.getBody(), com.intel.missioncontrol.persistence.insight.primitives.Project.class);
        String var5 = "Updated Project Id: " + resp.getId();
        boolean var6 = false;
        System.out.println(var5);
        var10001 = this.createdTestProjectId;
        if (var10001 == null) {}

        com.intel.missioncontrol.persistence.insight.primitives.Project projectAfterUpdate =
            this.getProjectById(var10001);
        if (projectAfterUpdate == null) {}

        assertEquals(projectAfterUpdate.getName(), this.testProjectName);
        try {
            assertTrue(
                this.dateFormat
                    .parse(projectAfterUpdate.getModificationDate())
                    .after(this.dateFormat.parse(projectToUpdate.getModificationDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Projects projects = this.searchProjectByName(this.testProjectName);
        assertTrue(projects.getProjects().size() > 0);
        Projects oldProjects = this.searchProjectByName("Test Hervanta Project");
        assertTrue(oldProjects.getProjects().size() == 0);
        assertEquals("Tampere Hervanta", resp.getPlaceName());
        projectToUpdate.setPlaceName("Tampere Hervanta 2");
        reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "dxpm/projects/" + projectToUpdate.getId())
                        .headers((Map)this.headers))
                .body(this.gson.toJson(projectToUpdate))
                .asString();
        Integer var10 = 200;

        assertNotEquals(var10, reqs.getStatus());
        projects = this.searchProjectByName(this.testProjectName);
        assertTrue(projects.getProjects().size() > 0);
    }

    @NotNull
    public final String getTestDataMissionSurveyDate() {
        return this.testDataMissionSurveyDate;
    }

    @NotNull
    public final String getTestDataVerticalSrsWkt() {
        return this.testDataVerticalSrsWkt;
    }

    @NotNull
    public final String getTestDataHorizontalSrsWkt() {
        return this.testDataHorizontalSrsWkt;
    }

    @Test
    @Order(4)
    public final void createCamera() {
        Camera cameraReq = new Camera();
        cameraReq.setModel("TEST_CAM_SONY_UMC-R10C_24");
        cameraReq.setWidth(5456);
        cameraReq.setHeight(3632);
        cameraReq.setFnumber(6);
        cameraReq.setAspectRatio(1.502202643171806D);
        cameraReq.setFocalLength(20);
        cameraReq.setCalibration(Arrays.asList(new String[] {"test", "calibration"}));
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/cameras").headers((Map)this.headers))
                .body(this.gson.toJson(cameraReq))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        Camera camera = (Camera)this.gson.fromJson(((String)reqs.getBody()).toString(), Camera.class);
        assertEquals("TEST_CAM_SONY_UMC-R10C_24", camera.getModel());
        assertEquals(5456, camera.getWidth());
        assertEquals(3632, camera.getHeight());
        assertEquals(6, camera.getFnumber());
        assertEquals(1.502202643171806D, camera.getAspectRatio());
        assertEquals(20, camera.getFocalLength());
        assertEquals(Arrays.asList(new String[] {"test", "calibration"}), camera.getCalibration());
        assertNotNull(camera.getId());
        this.createdCameraId = camera.getId();
        String var4 = "Created or Got Camera Id: " + camera.getId();
        boolean var5 = false;
        System.out.println(var4);
    }

    private void assertNotNull(Object body) {
        assertNotEquals(null, body);
    }

    @NotNull
    public final com.intel.missioncontrol.persistence.insight.primitives.Mission fillMissionData(
            @NotNull com.intel.missioncontrol.persistence.insight.primitives.Mission missionReq) {
        Application appl = new Application();
        appl.setName("Mission Application Name");
        appl.setOptions(Arrays.asList(new String[] {"Opt1", "Opt2"}));
        missionReq.setApplication(appl);
        Delivery delivery = new Delivery();
        delivery.setProperties("Delivery Prop");
        missionReq.setDelivery(delivery);
        Precision precision = new Precision();
        Gsd gsd = new Gsd();
        gsd.setValue(5);
        gsd.setSelected(false);
        precision.setGsd(gsd);
        Xy xy = new Xy();
        xy.setSelected(true);
        precision.setXy(xy);
        Z z = new Z();
        z.setSelected(false);
        precision.setZ(z);
        missionReq.setPrecision(precision);
        missionReq.setArea(382.858803548348D);
        missionReq.setLength(0);
        Geometries geometries = new Geometries();
        geometries.setType("GeometryCollection");
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Polygon");
        coordinates.setCoordinates(this.testDataCoordinates);
        geometries.setGeometries(Arrays.asList(new Coordinates[] {coordinates}));
        missionReq.setGeometry(geometries);
        RealBbox realBbox = new RealBbox();
        realBbox.setType("Polygon");
        realBbox.setCoordinates(this.testDataCoordinates);
        realBbox.setBbox(this.testDataBBox);
        missionReq.setRealBbox(realBbox);
        missionReq.setEpsg("EPSG Test String");
        Status status = new Status();
        status.setName("uploading");
        status.setId(30);
        missionReq.setStatus(status);
        List var20 = Collections.emptyList();
        missionReq.setDeliverables(var20);
        missionReq.setProject(this.createdTestProjectId);
        missionReq.setName("Mission test ProjectId" + this.createdTestProjectId);
        missionReq.setSurveyDate(this.testDataMissionSurveyDate);
        ProcessSettings processSettings = new ProcessSettings();
        processSettings.setToolsSet("pix4d");
        processSettings.setMapType("Map Type");
        var20 = Collections.emptyList();
        processSettings.setAnalytics(var20);
        Inspection inspection = new Inspection();
        inspection.setVideo(false);
        processSettings.setInspection(inspection);
        Mapping mapping = new Mapping();
        mapping.setPreset("SPEED");
        mapping.setMesh(true);
        mapping.setProcessingAreaSetting("none");
        Crs crs = new Crs();
        Output output = new Output();
        output.setVerticalSrsWkt(this.testDataVerticalSrsWkt);
        output.setHorizontalSrsWkt(this.testDataHorizontalSrsWkt);
        crs.setOutput(output);
        Gcp gcp = new Gcp();
        crs.setGcp(gcp);
        Image image = new Image();
        image.setVerticalSrsWkt(this.testDataVerticalSrsWkt);
        image.setHorizontalSrsWkt(this.testDataHorizontalSrsWkt);
        crs.setImage(image);
        mapping.setCrs(crs);
        processSettings.setMapping(mapping);
        missionReq.setProcessSettings(processSettings);
        String var10001 = this.createdCameraId;
        if (var10001 == null) {}

        missionReq.setCameras(Arrays.asList(var10001));
        missionReq.setHwConfig("HW12345");
        missionReq.setReferencePoint(Arrays.asList(new Double[] {123.4D, 567.8D}));
        missionReq.setTerrainAdjustmentMode("missionReq.terrainAdjustmentMode");
        return missionReq;
    }

    public final void testMissionData(
            @NotNull com.intel.missioncontrol.persistence.insight.primitives.Mission missionResp) {
        assertEquals(missionResp.getProject(), this.createdTestProjectId);
        Application var10001 = missionResp.getApplication();
        if (var10001 == null) {}

        assertEquals("Mission Application Name", var10001.getName());
        List var10000 = Arrays.asList(new String[] {"Opt1", "Opt2"});
        var10001 = missionResp.getApplication();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getOptions());
        Delivery var4 = missionResp.getDelivery();
        if (var4 == null) {}

        assertEquals("Delivery Prop", var4.getProperties());
        Integer var2 = 5;
        Precision var5 = missionResp.getPrecision();
        if (var5 == null) {}

        Gsd var6 = var5.getGsd();
        if (var6 == null) {}

        assertEquals(var2, var6.getValue());
        Boolean var3 = false;
        var5 = missionResp.getPrecision();
        if (var5 == null) {}

        var6 = var5.getGsd();
        if (var6 == null) {}

        assertEquals(var3, var6.getSelected());
        var3 = true;
        var5 = missionResp.getPrecision();
        if (var5 == null) {}

        Xy var8 = var5.getXy();
        if (var8 == null) {}

        assertEquals(var3, var8.getSelected());
        var3 = false;
        var5 = missionResp.getPrecision();
        if (var5 == null) {}

        Z var9 = var5.getZ();
        if (var9 == null) {}

        assertEquals(var3, var9.getSelected());
        assertEquals(382.858803548348D, missionResp.getArea());
        assertEquals(0, missionResp.getLength());
        Geometries var10 = missionResp.getGeometry();
        if (var10 == null) {}

        assertEquals("GeometryCollection", var10.getType());
        var10 = missionResp.getGeometry();
        if (var10 == null) {}

        List var11 = var10.getGeometries();
        if (var11 == null) {}

        var10000 = this.testDataCoordinates;
        var10 = missionResp.getGeometry();
        if (var10 == null) {}

        var11 = var10.getGeometries();
        if (var11 == null) {}

        RealBbox var12 = missionResp.getRealBbox();
        if (var12 == null) {}

        assertEquals("Polygon", var12.getType());
        var10000 = this.testDataCoordinates;
        var12 = missionResp.getRealBbox();
        if (var12 == null) {}

        assertEquals(var10000, var12.getCoordinates());
        var10000 = this.testDataBBox;
        var12 = missionResp.getRealBbox();
        if (var12 == null) {}

        assertEquals(var10000, var12.getBbox());
        assertEquals("EPSG Test String", missionResp.getEpsg());
        Status var13 = missionResp.getStatus();
        if (var13 == null) {}

        assertEquals("approved", var13.getName());
        assertEquals(this.createdTestProjectId, missionResp.getProject());
        assertEquals("Mission test ProjectId" + this.createdTestProjectId, missionResp.getName());
        ProcessSettings var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        assertEquals("pix4d", var14.getToolsSet());
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        assertEquals("Map Type", var14.getMapType());
        var3 = false;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        Inspection var15 = var14.getInspection();
        if (var15 == null) {}

        assertEquals(var3, var15.getVideo());
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        Mapping var16 = var14.getMapping();
        if (var16 == null) {}

        assertEquals("SPEED", var16.getPreset());
        var3 = true;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        assertEquals(var3, var16.getMesh());
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        assertEquals("none", var16.getProcessingAreaSetting());
        String var7 = this.testDataVerticalSrsWkt;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        Crs var17 = var16.getCrs();
        if (var17 == null) {}

        Output var18 = var17.getOutput();
        if (var18 == null) {}

        assertEquals(var7, var18.getVerticalSrsWkt());
        var7 = this.testDataHorizontalSrsWkt;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        var17 = var16.getCrs();
        if (var17 == null) {}

        var18 = var17.getOutput();
        if (var18 == null) {}

        assertEquals(var7, var18.getHorizontalSrsWkt());
        var7 = this.testDataVerticalSrsWkt;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        var17 = var16.getCrs();
        if (var17 == null) {}

        Image var19 = var17.getImage();
        if (var19 == null) {}

        assertEquals(var7, var19.getVerticalSrsWkt());
        var7 = this.testDataHorizontalSrsWkt;
        var14 = missionResp.getProcessSettings();
        if (var14 == null) {}

        var16 = var14.getMapping();
        if (var16 == null) {}

        var17 = var16.getCrs();
        if (var17 == null) {}

        var19 = var17.getImage();
        if (var19 == null) {}

        assertEquals(var7, var19.getHorizontalSrsWkt());
        var7 = this.createdCameraId;
        if (var7 == null) {}

        var11 = missionResp.getCameras();
        if (var11 == null) {}

        assertEquals("HW12345", missionResp.getHwConfig());
        assertEquals(Arrays.asList(new Double[] {123.4D, 567.8D}), missionResp.getReferencePoint());
        assertEquals("missionReq.terrainAdjustmentMode", missionResp.getTerrainAdjustmentMode());
    }

    @Test
    @Order(5)
    public final void createMissionByProjectId() {
        Projects projects = this.searchProjectByName(this.testProjectName);
        assertEquals(projects.getProjects().size(), 1);
        JsonObject project = new JsonObject();
        JsonObject missionObj = new JsonObject();
        project.addProperty("_id", this.createdTestProjectId);
        missionObj.addProperty("name", "create new Mission with Project ID");
        missionObj.addProperty("area", (Number)100.0D);
        missionObj.addProperty("created", Instant.now().toString());
        missionObj.add("project", (JsonElement)project);
        String missionReq = this.gson.toJson((JsonElement)missionObj);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/missions").headers((Map)this.headers))
                .body(missionReq)
                .asString();

        assertEquals(reqs.getStatus(), 200);
        com.intel.missioncontrol.persistence.insight.primitives.Mission mission =
            (com.intel.missioncontrol.persistence.insight.primitives.Mission)
                this.gson.fromJson(
                    ((String)reqs.getBody()).toString(),
                    com.intel.missioncontrol.persistence.insight.primitives.Mission.class);
        String var7 = "Created Mission Id: " + mission.getId() + " project_id: " + mission.getProject();
        boolean var8 = false;
        System.out.println(var7);
        this.createdTestMissionId = mission.getId();
        assertEquals(mission.getProject(), this.createdTestProjectId);
    }

    @Test
    @Order(6)
    public final void searchMissionsByProjectId() {
        String var10001 = this.createdTestProjectId;
        if (var10001 == null) {}

        com.intel.missioncontrol.persistence.insight.primitives.Project projectToUpdate = this.getProjectById(var10001);
        JsonObject missionSearch = new JsonObject();
        missionSearch.addProperty("project_id", this.createdTestProjectId);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/missions/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)missionSearch))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        Missions missions = (Missions)this.gson.fromJson((String)reqs.getBody(), Missions.class);
        assertEquals(missions.getCount(), 1);
    }

    @Test
    @Order(7)
    public final void checkIfMissionExistsInTheProject() {
        Projects projects = this.searchProjectByName(this.testProjectName);
        assertEquals(projects.getProjects().size(), 1);
    }

    @Test
    @Order(8)
    public final void updateMissionById() {
        String var10001 = this.createdTestMissionId;
        if (var10001 == null) {}

        com.intel.missioncontrol.persistence.insight.primitives.Mission storedMission = this.getMissionById(var10001);
        String var10000 = this.createdTestMissionId;
        if (storedMission == null) {}

        assertEquals(var10000, storedMission.getId());
        com.intel.missioncontrol.persistence.insight.primitives.Mission missionToUpdate =
            this.fillMissionData(storedMission);
        StringBuilder var8 = (new StringBuilder()).append(this.usedTestServer).append("dxpm/missions/");
        if (missionToUpdate == null) {}

        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.put(var8.append(missionToUpdate.getId()).toString()).headers((Map)this.headers))
                .body(this.gson.toJson(missionToUpdate))
                .asString();

        assertEquals(200, reqs.getStatus());
        com.intel.missioncontrol.persistence.insight.primitives.Mission resp =
            (com.intel.missioncontrol.persistence.insight.primitives.Mission)
                this.gson.fromJson(
                    (String)reqs.getBody(), com.intel.missioncontrol.persistence.insight.primitives.Mission.class);

        this.testMissionData(resp);
        String var5 = "Updated Mission Id: " + resp.getId();
        boolean var6 = false;
        System.out.println(var5);
        Projects projects = this.searchProjectByName(this.testProjectName);
        assertEquals(projects.getProjects().size(), 1);
        missionToUpdate.setName("Unsuccessfully updated name");
        reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "dxpm/missions/" + missionToUpdate.getId())
                        .headers((Map)this.headers))
                .body(this.gson.toJson(missionToUpdate))
                .asString();
        Integer var9 = 200;

        assertNotEquals(var9, reqs.getStatus());
    }

    @NotNull
    public final Annotation createAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setProjectId(this.createdTestProjectId);
        annotation.setMissionId(this.createdTestMissionId);
        Target target = new Target();
        target.setType("2d");
        annotation.setTarget(target);
        Feature feature = new Feature();
        Geometry geometry = new Geometry();
        geometry.setType("Polygon");
        geometry.setCoordinates(
            Arrays.asList(
                Arrays.asList(
                    new List[] {
                        Arrays.asList(new Double[] {23.6817423389732D, 61.5175723803629D}),
                        Arrays.asList(new Double[] {23.6817489101276D, 61.5175717291433D}),
                        Arrays.asList(new Double[] {23.6817475184906D, 61.5175737322661D}),
                        Arrays.asList(new Double[] {23.6817423389732D, 61.5175723803629D})
                    })));
        feature.setGeometry(geometry);
        annotation.setFeature(feature);
        return annotation;
    }

    @Test
    @Order(9)
    public final void createAnnotationUseProjectIdAndMissionId() {
        JsonObject missionSearch = new JsonObject();
        missionSearch.addProperty("project_id", this.createdTestProjectId);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/missions/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)missionSearch))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        Missions missions = (Missions)this.gson.fromJson((String)reqs.getBody(), Missions.class);
        Annotation annotationReq = this.createAnnotation();
        reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "uisrv/annotations").headers((Map)this.headers))
                .body(this.gson.toJson(annotationReq))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        AnnotationResponse resp =
            (AnnotationResponse)this.gson.fromJson(((String)reqs.getBody()).toString(), AnnotationResponse.class);
        StringBuilder var10000 = (new StringBuilder()).append("Created Annotation Id: ");
        Annotation var10001 = resp.getAnnotation();
        if (var10001 == null) {}

        String var6 = var10000.append(var10001.getId()).toString();
        boolean var7 = false;
        System.out.println(var6);
        var10001 = resp.getAnnotation();
        if (var10001 == null) {}

        this.createdAnnotationId = var10001.getId();
        Annotation var8 = resp.getAnnotation();
        if (var8 == null) {}

        assertEquals(var8.getProjectId(), this.createdTestProjectId);
        missionSearch = new JsonObject();
        missionSearch.addProperty("project_id", this.createdTestProjectId);
        reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/missions/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)missionSearch))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        missions = (Missions)this.gson.fromJson((String)reqs.getBody(), Missions.class);
    }

    @Test
    @Order(10)
    public final void addAnnotationParametersToAnnotation() {
        JsonObject annotationSearch = new JsonObject();
        annotationSearch.addProperty("project_id", this.createdTestProjectId);
        annotationSearch.addProperty("mission_id", this.createdTestMissionId);
        HttpResponse reqsA =
            ((HttpRequestWithBody)
                    Unirest.post(this.usedTestServer + "uisrv/annotations/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)annotationSearch))
                .asString();

        assertEquals(reqsA.getStatus(), 200);
        assertNotNull(reqsA.getBody());
        Annotations respA = (Annotations)this.gson.fromJson((String)reqsA.getBody(), Annotations.class);
        JsonObject request = new JsonObject();
        AnnotationParameters parameters = new AnnotationParameters();
        parameters.setName("Test Annotation Parameters");
        parameters.setGsd(10.1D);
        parameters.setDistance(11.11D);
        parameters.setYaw(12.12D);
        parameters.setType(AreaOfInterestType.COPTER3D);
        parameters.setForwardOverlap(13.13D);
        parameters.setForwardOverlapMin(14.14D);
        parameters.setLateralOverlap(15.15D);
        parameters.setLateralOverlapMin(16.16D);
        parameters.setMinGroundDistance(17.17D);
        parameters.setScanDirection("Test String 1");
        parameters.setStartCapture("Test String 2");
        parameters.setStartCaptureVertically("Test String 3");
        parameters.setSingleDirection(true);
        parameters.setCameraRoll(18.18D);
        parameters.setCameraRollOffset(19.19D);
        parameters.setCameraTilt(20.2D);
        parameters.setRotationDirection(21.21D);
        parameters.setMaxPitchChange(22.22D);
        parameters.setMaxYawRollChange(23.23D);
        parameters.setCameraPitchOffsetLineBegin(24.24D);
        request.add("parameters", this.gson.toJsonTree(parameters));
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "uisrv/annotations/" + this.createdAnnotationId + "/parameters")
                        .headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)request))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        AnnotationResponse resp =
            (AnnotationResponse)this.gson.fromJson(((String)reqs.getBody()).toString(), AnnotationResponse.class);
        SimpleDateFormat var10000 = this.dateFormat;
        if (resp == null) {}

        Annotation var10001 = resp.getAnnotation();
        if (var10001 == null) {}

        request = new JsonObject();
        parameters.setName("Test Annotation Parameters 2");
        request.add("parameters", this.gson.toJsonTree(parameters));
        reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "uisrv/annotations/" + this.createdAnnotationId + "/parameters")
                        .headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)request))
                .asString();
        Gson var10 = this.gson;

        resp = (AnnotationResponse)var10.fromJson(((String)reqs.getBody()).toString(), AnnotationResponse.class);
        Annotation var11 = resp.getAnnotation();
        if (var11 == null) {}

        assertNotEquals(var11.getV(), 0);
        StringBuilder var12 = (new StringBuilder()).append("Added AnnotationParameters to Annotation Id: ");
        var10001 = resp.getAnnotation();
        if (var10001 == null) {}

        String var8 = var12.append(var10001.getId()).toString();
        boolean var9 = false;
        System.out.println(var8);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        AnnotationParameters var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getName(), "Test Annotation Parameters 2");
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getGsd(), 10.1D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getDistance(), 11.11D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getYaw(), 12.12D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getType(), AreaOfInterestType.COPTER3D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getForwardOverlap(), 13.13D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getForwardOverlapMin(), 14.14D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getLateralOverlap(), 15.15D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getLateralOverlapMin(), 16.16D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getMinGroundDistance(), 17.17D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getScanDirection(), "Test String 1");
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getStartCapture(), "Test String 2");
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getStartCaptureVertically(), "Test String 3");
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getSingleDirection(), true);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getCameraRoll(), 18.18D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getCameraRollOffset(), 19.19D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getCameraTilt(), 20.2D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getRotationDirection(), 21.21D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getMaxPitchChange(), 22.22D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getMaxYawRollChange(), 23.23D);
        var11 = resp.getAnnotation();
        if (var11 == null) {}

        var13 = var11.getParameters();
        if (var13 == null) {}

        assertEquals(var13.getCameraPitchOffsetLineBegin(), 24.24D);
    }

    @Test
    @Order(11)
    public final void searchAnnotationByMissionAndProjectId() {
        JsonObject annotationSearch = new JsonObject();
        annotationSearch.addProperty("project_id", this.createdTestProjectId);
        annotationSearch.addProperty("mission_id", this.createdTestMissionId);
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.post(this.usedTestServer + "uisrv/annotations/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)annotationSearch))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        Annotations resp = (Annotations)this.gson.fromJson((String)reqs.getBody(), Annotations.class);
        Collection var4 = (Collection)resp.getAnnotations();
        byte var6 = 1;
        boolean var5 = false;
        int var7 = var4.size();
        assertEquals(var6, var7);
    }

    @Test
    @Order(12)
    public final void createFlightPlan() throws JSONException {
        com.intel.missioncontrol.persistence.insight.primitives.FlightPlan flightPlanReq =
            new com.intel.missioncontrol.persistence.insight.primitives.FlightPlan();
        flightPlanReq.setName("Polygon 123");
        flightPlanReq.setType("SURVEY");
        flightPlanReq.setProjectId(this.createdTestProjectId);
        flightPlanReq.setMissionId(this.createdTestMissionId);
        Camera camera = new Camera();
        camera.setModel("TEST_CAM_SONY_UMC-R10C_24");
        camera.setWidth(5456);
        camera.setHeight(3632);
        camera.setFnumber(6);
        camera.setAspectRatio(1.502202643171806D);
        camera.setFocalLength(20);
        camera.setCalibration(Arrays.asList(new String[] {"test", "calibration"}));
        flightPlanReq.setCamera(camera);
        flightPlanReq.setHwConfig("HW CONFIG");
        com.intel.missioncontrol.persistence.insight.primitives.Waypoint waypoint =
            new com.intel.missioncontrol.persistence.insight.primitives.Waypoint();
        com.intel.missioncontrol.persistence.insight.primitives.Waypoint waypoint2 =
            new com.intel.missioncontrol.persistence.insight.primitives.Waypoint();
        ArrayList point = new ArrayList();
        ArrayList point2 = new ArrayList();
        point.add(90.0D);
        point.add(40.0D);
        point2.add(91.0D);
        point2.add(41.0D);
        waypoint.setTargetPoint((List)point);
        waypoint.setDronePoint((List)point);
        waypoint.setWarning("No warnings here");
        waypoint2.setTargetPoint((List)point2);
        waypoint2.setDronePoint((List)point2);
        waypoint2.setWarning("No warnings here2");
        flightPlanReq.getWaypoints().add(waypoint);
        flightPlanReq.getWaypoints().add(waypoint2);
        ArrayList takeoff = new ArrayList();
        takeoff.add(92.0D);
        takeoff.add(42.0D);
        flightPlanReq.setTakeoff((List)takeoff);
        flightPlanReq.setLanding((List)takeoff);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "uisrv/flight-plan").headers((Map)this.headers))
                .body(this.gson.toJson(flightPlanReq))
                .asJson();

        assertEquals(reqs.getStatus(), 201);
        assertNotNull(reqs.getBody());
        JSONObject body = ((JsonNode)reqs.getBody()).getObject();
        Object subResp = body.get("flight_plan");
        com.intel.missioncontrol.persistence.insight.primitives.FlightPlan flightPlanResp =
            (com.intel.missioncontrol.persistence.insight.primitives.FlightPlan)
                this.gson.fromJson(
                    subResp.toString(), com.intel.missioncontrol.persistence.insight.primitives.FlightPlan.class);
        this.createdFlightPlanId = flightPlanResp.getId();
        assertEquals(this.createdTestMissionId, flightPlanResp.getMissionId());
        assertEquals(this.createdTestProjectId, flightPlanReq.getProjectId());
        assertEquals("Polygon 123", flightPlanResp.getName());
        assertEquals("SURVEY", flightPlanResp.getType());
        Collection var12 = (Collection)flightPlanResp.getWaypoints();
        byte var14 = 2;
        boolean var13 = false;
        int var15 = var12.size();
        assertEquals(var14, var15);
        Camera var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals("TEST_CAM_SONY_UMC-R10C_24", var10001.getModel());
        Integer var10000 = 5456;
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getWidth());
        var10000 = 3632;
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getHeight());
        var10000 = 6;
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getFnumber());
        Double var16 = 1.502202643171806D;
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var16, var10001.getAspectRatio());
        var10000 = 20;
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getFocalLength());
        List var17 = Arrays.asList(new String[] {"test", "calibration"});
        var10001 = flightPlanResp.getCamera();
        if (var10001 == null) {}

        assertEquals(var17, var10001.getCalibration());
        assertEquals("HW CONFIG", flightPlanResp.getHwConfig());

        assertEquals(
            Arrays.asList(new Double[] {91.0D, 41.0D}),
            ((com.intel.missioncontrol.persistence.insight.primitives.Waypoint)flightPlanResp.getWaypoints().get(1))
                .getTargetPoint());
        assertEquals(
            Arrays.asList(new Double[] {91.0D, 41.0D}),
            ((com.intel.missioncontrol.persistence.insight.primitives.Waypoint)flightPlanResp.getWaypoints().get(1))
                .getDronePoint());
        assertEquals("No warnings here2", ((Waypoint)flightPlanResp.getWaypoints().get(1)).getWarning());
        assertEquals(Arrays.asList(new Double[] {92.0D, 42.0D}), flightPlanResp.getTakeoff());
        assertEquals(Arrays.asList(new Double[] {92.0D, 42.0D}), flightPlanResp.getLanding());
    }

    @NotNull
    public final FlightPlans searchFlightPlans(@NotNull String projectId, @NotNull String missionId) {
        JsonObject fpSearchReq = new JsonObject();
        fpSearchReq.addProperty("project_id", projectId);
        fpSearchReq.addProperty("mission_id", missionId);
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.post(this.usedTestServer + "uisrv/flight-plan/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)fpSearchReq))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        String var5 = "SearchFlightPlan By: MissionId: " + missionId + " ProjectId; " + projectId;
        boolean var6 = false;
        System.out.println(var5);
        FlightPlans flightPlans =
            (FlightPlans)this.gson.fromJson(((String)reqs.getBody()).toString(), FlightPlans.class);

        return flightPlans;
    }

    @Test
    @Order(13)
    public final void searchFlightPlan() {
        String var10001 = this.createdTestProjectId;
        if (var10001 == null) {}

        String var10002 = this.createdTestMissionId;
        if (var10002 == null) {}

        FlightPlans fp = this.searchFlightPlans(var10001, var10002);
        Collection var2 = (Collection)fp.getFlightPlans();
        byte var4 = 1;
        boolean var3 = false;
        int var5 = var2.size();
        assertEquals(var4, var5);
    }

    @Test
    @Order(14)
    public final void updateFlightPlan() throws JSONException {
        String var10001 = this.createdTestProjectId;
        if (var10001 == null) {}

        String var10002 = this.createdTestMissionId;
        if (var10002 == null) {}

        FlightPlans flightPlansResp = this.searchFlightPlans(var10001, var10002);
        Collection var2 = (Collection)flightPlansResp.getFlightPlans();
        byte var9 = 1;
        boolean var3 = false;
        int var10 = var2.size();
        assertEquals(var9, var10);
        com.intel.missioncontrol.persistence.insight.primitives.FlightPlan fp =
            (com.intel.missioncontrol.persistence.insight.primitives.FlightPlan)flightPlansResp.getFlightPlans().get(0);
        fp.setName("Polygon 123 Updated");
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "uisrv/flight-plan/" + fp.getId()).headers((Map)this.headers))
                .body(this.gson.toJson(fp))
                .asJson();

        assertEquals(reqs.getStatus(), 200);
        assertNotNull(reqs.getBody());
        JSONObject body = ((JsonNode)reqs.getBody()).getObject();
        Object subResp = body.get("flight_plan");
        com.intel.missioncontrol.persistence.insight.primitives.FlightPlan flightPlanResp =
            (com.intel.missioncontrol.persistence.insight.primitives.FlightPlan)
                this.gson.fromJson(subResp.toString(), FlightPlan.class);
        assertEquals("Polygon 123 Updated", flightPlanResp.getName());
        SimpleDateFormat var10000 = this.dateFormat;
        if (flightPlanResp == null) {}

        String var7 = "Update FlightPlan Id: " + flightPlanResp.getId();
        boolean var8 = false;
        System.out.println(var7);
        fp.setName("Unsuccessfully updated name");
        reqs =
            ((HttpRequestWithBody)
                    Unirest.put(this.usedTestServer + "uisrv/flight-plan/" + fp.getId()).headers((Map)this.headers))
                .body(this.gson.toJson(fp))
                .asJson();

        assertNotEquals(reqs.getStatus(), 200);
    }

    @Test
    @Order(15)
    public final void createFlight() {
        Flight flightReq = new Flight();
        new Target();
        Data data = new Data();
        data.setBbox(
            Arrays.asList(new Double[] {23.6817489101276D, 61.5175717291433D, 23.6818489101276D, 61.5178717291433D}));
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Polygon");
        coordinates.setCoordinates(
            Arrays.asList(
                Arrays.asList(
                    new List[] {
                        Arrays.asList(new Double[] {23.6817423389732D, 61.5175723803629D}),
                        Arrays.asList(new Double[] {23.6817489101276D, 61.5175717291433D}),
                        Arrays.asList(new Double[] {23.6817475184906D, 61.5175737322661D}),
                        Arrays.asList(new Double[] {23.6817423389732D, 61.5175723803629D})
                    })));
        data.setGeometry(coordinates);
        flightReq.setData(data);
        flightReq.setCameras(Arrays.asList("5cf8b4f6552eb41a4760fda3"));
        flightReq.setNumberOfPhotos(0);
        flightReq.setId((String)null);
        flightReq.setProject(this.createdTestProjectId);
        flightReq.setMission(this.createdTestMissionId);
        flightReq.setFlightPlan(this.createdFlightPlanId);
        flightReq.setName("Testing Flight Creatiation");
        String var10000 = Instant.now().toString();

        String surveyDate = var10000;
        flightReq.setSurveyDate(surveyDate);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/flights").headers((Map)this.headers))
                .body(this.gson.toJson(flightReq))
                .asString();

        assertEquals(200, reqs.getStatus());
        assertNotNull(reqs.getBody());
        Flight flightResp = (Flight)this.gson.fromJson((String)reqs.getBody(), Flight.class);
        assertNotNull(flightResp.getSurveyDate());
        assertNotNull(flightResp.getId());
        assertNotNull(flightResp.getCreated());
        assertNotNull(flightResp.getUser());
        assertEquals(this.createdTestProjectId, flightResp.getProject());
        assertEquals(this.createdTestMissionId, flightResp.getMission());
        assertEquals(this.createdFlightPlanId, flightResp.getFlightPlan());
        assertEquals("Testing Flight Creatiation", flightResp.getName());
        Data var10001 = flightResp.getData();
        if (var10001 == null) {}

        Coordinates var10 = var10001.getGeometry();
        if (var10 == null) {}

        String var11 = var10.getType();
        if (var11 == null) {}

        assertEquals("Polygon", var11);
        assertEquals(0, flightResp.getNumberOfPhotos());
        String var8 = "Created Flight Id: " + flightResp.getId();
        boolean var9 = false;
        System.out.println(var8);
        this.createdFlightId = flightResp.getId();
    }

    public final void createPhotos() {
        PhotoUpload photoUploadReq = new PhotoUpload();
        photoUploadReq.setProject(this.createdTestProjectId);
        photoUploadReq.setMission(this.createdTestMissionId);
        photoUploadReq.setFlight(this.createdFlightId);
        Photo photo = new Photo();
        photo.setSeq("R10C000778_1523881013400");
        photo.setAltitude(127.73401534526855D);
        photo.setUtc("2018-04-16T12:21:15.000Z");
        photo.setRtc(10);
        photo.setCamera(this.createdCameraId);
        photo.setWidth(5456);
        photo.setHeight(3632);
        photo.setVerticalSrsWkt(
            "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]");
        photo.setHorizontalSrsWkt(
            "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
        photo.setUploadId("SONY_UMC-R10C_24_R10C000778_1523881013400_2018-04-16T12:21:15.000Z");
        photo.setFlight(this.createdFlightId);
        photo.setStatus("uploading");
        photo.setSharpened(false);
        photo.setShutter(12.5D);
        PhotoGeometry photoGeometry = new PhotoGeometry();
        photoGeometry.setType("Point");
        photoGeometry.setCoordinates(Arrays.asList(new Double[] {23.68175290486565D, 61.51757510020247D}));
        photo.setGeometry(photoGeometry);
        photo.setTags(Arrays.asList("NicePhoto"));
        photo.setPhi(91.3D);
        photo.setPsi(0.0D);
        photo.setTheta(90.0D);
        photoUploadReq.setPhotos(Arrays.asList(photo));
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/photos").headers((Map)this.headers))
                .body(this.gson.toJson(photoUploadReq))
                .asString();

        assertEquals(200, reqs.getStatus());
        assertNotNull(reqs.getBody());
        PhotoPrep photoPrep = (PhotoPrep)this.gson.fromJson((String)reqs.getBody(), PhotoPrep.class);
        Collection var6 = (Collection)photoPrep.getPhotos();
        byte var9 = 1;
        boolean var7 = false;
        int var10 = var6.size();
        assertEquals(var9, var10);
        Photo photoCreated = (Photo)((LinkedTreeMap)photoPrep.getPhotos().get(0)).get(0);
        assertEquals("R10C000778_1523881013400", photoCreated.getSeq());
        assertEquals(127.73401534526855D, photoCreated.getAltitude());
        assertEquals("2018-04-16T12:21:15.000Z", photoCreated.getUtc());
        assertEquals(10, photoCreated.getRtc());
        assertEquals(this.createdCameraId, photoCreated.getCamera());
        assertEquals(5456, photoCreated.getWidth());
        assertEquals(3632, photoCreated.getHeight());
        assertEquals(
            "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]",
            photoCreated.getVerticalSrsWkt());
        assertEquals(
            "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]",
            photoCreated.getHorizontalSrsWkt());
        assertEquals("SONY_UMC-R10C_24_R10C000778_1523881013400_2018-04-16T12:21:15.000Z", photoCreated.getUploadId());
        assertEquals(this.createdFlightId, photoCreated.getFlight());
        assertEquals("uploading", photoCreated.getStatus());
        assertEquals(false, photoCreated.getSharpened());
        assertEquals(12.5D, photoCreated.getShutter());
        assertEquals(Arrays.asList("NicePhoto"), photoCreated.getTags());
        assertEquals(91.3D, photoCreated.getPhi());
        assertEquals(0.0D, photoCreated.getPsi());
        assertEquals(90.0D, photoCreated.getTheta());
        PhotoGeometry var10001 = photoCreated.getGeometry();
        if (var10001 == null) {}

        assertEquals("Point", var10001.getType());
        List var10000 = Arrays.asList(new Double[] {23.68175290486565D, 61.51757510020247D});
        var10001 = photoCreated.getGeometry();
        if (var10001 == null) {}

        assertEquals(var10000, var10001.getCoordinates());
        String var12 = "Created Photos Id: " + photoCreated.getId();
        boolean var8 = false;
        System.out.println(var12);
        this.createdPhotoPrep = photoPrep;
    }

    @NotNull
    public final String md5(@NotNull byte[] foo) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String var10000 = (new BigInteger(1, md.digest(foo))).toString(16);

        return var10000; // StringsKt.padStart(var10000, 32, '0');
    }

    public final boolean uploadPhoto(@NotNull String filePath, @NotNull String photoId) throws FileNotFoundException {
        /*

        File jpgPhoto = new File(filePath);
        boolean var6 = false;
        byte[] content = ByteStreamsKt.readBytes((InputStream) (new FileInputStream(jpgPhoto)));
        Map uploadHeaders = MapsKt.toMutableMap((Map) this.headers);
        uploadHeaders.put("Content-Type", "application/octet-stream");
        uploadHeaders.put("Content-MD5", this.md5(content));
        HttpResponse reqs = ((HttpRequestWithBody) Unirest.put(this.usedTestServer + "dxds/photos/jpg/" + photoId).headers(uploadHeaders)).body(content).asString();

        assertEquals(200, reqs.getStatus());
        return reqs.getStatus() == 200;*/
        return true;
    }

    @Test
    @Order(17)
    public final void uploadPhotos() {
        /*PhotoPrep var10000 = this.createdPhotoPrep;
                if (var10000 == null) {

                }

                Iterable $this$forEach$iv = (Iterable) var10000.getPhotos();
                int $i$f$forEach = false;
                Iterator var3 = $this$forEach$iv.iterator();

                while (var3.hasNext()) {
                    Object element$iv = var3.next();
                    Photo it = (Photo) element$iv;
                    int var6 = false;
                    assertNotNull(it);
                    assertNotNull(it.getId());
                    Path path = Path.of(System.getProperty("user.dir"), "src", "test", "resources", "images", "test1.jpg");
                    String var10001 = path.toString();
                    String var10002 = it.getId();
                    if (var10002 == null) {

                    }

                    this.uploadPhoto(var10001, var10002);
                    String var8 = "Uploaded Photo Id: " + it.getId();
                    boolean var9 = false;
                    System.out.println(var8);
                }
        */
    }

    @Test
    @Order(18)
    public final void updateFlightStatus() {
        JsonObject statusObj = new JsonObject();
        statusObj.addProperty("_id", this.createdFlightId);
        statusObj.addProperty("status", "complete");
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.post(this.usedTestServer + "dxpm/flights/" + this.createdFlightId + "/uploads/status")
                        .headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)statusObj))
                .asString();

        assertEquals(200, reqs.getStatus());
        String var3 = "Flight && Photo Upload Status Completed";
        boolean var4 = false;
        System.out.println(var3);
    }

    public final void searchPhotosByFlightId() {
        JsonObject photosReq = new JsonObject();
        JsonArray flightIds = new JsonArray();
        flightIds.add(this.createdFlightId);
        photosReq.add("flights_id", (JsonElement)flightIds);
        HttpResponse reqs =
            ((HttpRequestWithBody)Unirest.post(this.usedTestServer + "dxpm/photos/search").headers((Map)this.headers))
                .body(this.gson.toJson((JsonElement)photosReq))
                .asString();

        assertEquals(200, reqs.getStatus());
        assertNotNull(reqs.getBody());
        Type photosSearchRespType = (new TypeToken() {}).getType();
        List photosSearchResp = (List)this.gson.fromJson((String)reqs.getBody(), photosSearchRespType);

        Collection var6 = (Collection)photosSearchResp;
        byte var9 = 1;
        boolean var7 = false;
        int var10 = var6.size();
        assertEquals(var9, var10);
        Photo photo = (Photo)photosSearchResp.get(0);
        this.photoSearchResp = photosSearchResp;
        String var12 = "Found Photos By Flight Id: " + this.createdFlightId;
        boolean var8 = false;
        System.out.println(var12);
    }

    public final void getPhotos() {
        /*  List pictureSize = Arrays.asList(new String[]{"tiny/", "small/", "jpg/"});
        Photo photo = (Photo) Collections.first(this.photoSearchResp);
        Thread.sleep(3000L);
        GetRequest var10000 = (GetRequest) ((GetRequest) Unirest.get(this.usedTestServer + "dxds/photos/" + (String) Collections.first(pictureSize) + photo.getId()).queryString("driver", ((StorageLocation) Collections.first(photo.getStorageLocations())).getDriver())).queryString("location_name", ((StorageLocation) Collections.first(photo.getStorageLocations())).getLocationName());
        String var4 = this.iC.getOauthResponse().getAccessToken();
        String var7 = "access_token";
        GetRequest var6 = var10000;
        boolean var5 = false;
        String var9 = var4;
        if (var4 == null) {
            var9 = "";
        }

        String var8 = var9;
        HttpResponse reqs = ((GetRequest) var6.queryString(var7, var8)).asString();

        assertEquals(200, reqs.getStatus());
        assertNotNull(reqs.getBody());
        var4 = "Downloaded resized Tiny Photo By Id: " + photo.getId();
        var5 = false;
        System.out.println(var4);*/
    }

    @Test
    @Order(21)
    public final void getAllProjectsByTimestamp() {
        var search = new JsonObject();
        var changedDate = "2019-07-12T12:53:14.356Z";
        search.addProperty("modified", changedDate);

        var reqs =
            Unirest.post(usedTestServer + "/dxpm/projects/changed-projects")
                .headers(headers)
                .body(gson.toJson(search))
                .asString();
        assertEquals(reqs.getStatus(), 200);
    }

    @Test
    @Order(23)
    public final void deleteFlightById() {}

    @Test
    @Order(24)
    public final void deleteFlightPlanById() throws JSONException {
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.delete(this.usedTestServer + "uisrv/flight-plan/" + this.createdFlightPlanId)
                        .headers((Map)this.headers))
                .asJson();

        assertEquals(200, reqs.getStatus());
        assertNotNull(reqs.getBody());
        JSONObject body = ((JsonNode)reqs.getBody()).getObject();
        JSONObject fp = body.getJSONObject("flight_plan");
        Object deleted = fp.get("deleted");
        assertNotNull(deleted);
        String var5 = "Deleted FlightPlan Id: " + this.createdFlightPlanId + " : " + deleted;
        boolean var6 = false;
        System.out.println(var5);
    }

    @Test
    @Order(25)
    public final void deleteMissionById() {
        HttpResponse reqs =
            ((HttpRequestWithBody)
                    Unirest.delete(this.usedTestServer + "dxpm/missions/" + this.createdTestMissionId)
                        .headers((Map)this.headers))
                .asString();

        assertEquals(reqs.getStatus(), 200);
        com.intel.missioncontrol.persistence.insight.primitives.Mission mission =
            (com.intel.missioncontrol.persistence.insight.primitives.Mission)
                this.gson.fromJson((String)reqs.getBody(), Mission.class);
        String var3 = "Deleted Mission Id: " + mission.getId();
        boolean var4 = false;
        System.out.println(var3);
        assertEquals(this.createdTestMissionId, mission.getId());
    }

    @Test
    @Order(26)
    public final void deleteProjectByName() {
        Projects projects = this.searchProjectByName("Hello project!");
        Iterator var3 = projects.getProjects().iterator();

        com.intel.missioncontrol.persistence.insight.primitives.Project pr;
        while (var3.hasNext()) {
            pr = (com.intel.missioncontrol.persistence.insight.primitives.Project)var3.next();
            ((HttpRequestWithBody)
                    Unirest.delete(this.usedTestServer + "dxpm/projects/" + pr.getId()).headers((Map)this.headers))
                .asString();
        }

        projects = this.searchProjectByName("Test Project");
        var3 = projects.getProjects().iterator();

        while (var3.hasNext()) {
            pr = (Project)var3.next();
            ((HttpRequestWithBody)
                    Unirest.delete(this.usedTestServer + "dxpm/projects/" + pr.getId()).headers((Map)this.headers))
                .asString();
        }
    }

    public ProjectTest() {
        this.user = this.user1C1;
        this.testServerUrl = "https://imc-insight-dev.tm.intel.com/";
        this.userPassword = "password";
        this.usedLoginUser = this.user;
        this.usedLoginPassword = this.userPassword;
        this.usedTestServer = this.testServerUrl;
        this.gson = (new GsonBuilder()).excludeFieldsWithoutExposeAnnotation().create();
        this.iC = new InsightContext();
        this.headers = new HashMap();
        this.randomTestIdentifier = "";
        this.testProjectName = "Test Project ";
        this.createdTestProjectId = "";
        this.createdProjectTimespamp = "";
        this.modifiedProjectTimespamp = "";
        this.createdTestMissionId = "";
        this.createdAnnotationId = "";
        this.createdFlightPlanId = "";
        this.createdFlightId = "";
        this.createdCameraId = "";
        this.photoSearchResp = Collections.emptyList();
        String var2 = System.getenv("IMC_LOCAL_TEST_SERVER");
        boolean var3 = false;
        String var10000 = var2;
        if (var2 == null) {
            var10000 = "";
        }

        String localTestServer = var10000;
        CharSequence var5 = (CharSequence)localTestServer;
        var3 = false;
        boolean var4 = false;
        if (var5 != null && var5.length() != 0) {
            this.usedLoginUser = this.adminUserName;
            this.usedLoginPassword = this.adminPassword;
            this.usedTestServer = this.testServerUrl;
        }

        this.testDataCoordinates =
            Arrays.asList(
                Arrays.asList(
                    new List[] {
                        Arrays.asList(new Double[] {23.6815629732815D, 61.5174814933482D}),
                        Arrays.asList(new Double[] {23.6819236556582D, 61.5174814933482D}),
                        Arrays.asList(new Double[] {23.6819236556582D, 61.5176611129723D}),
                        Arrays.asList(new Double[] {23.6815629732815D, 61.5176611129723D}),
                        Arrays.asList(new Double[] {23.6815629732815D, 61.5174814933482D})
                    }));
        this.testDataBBox =
            Arrays.asList(new Double[] {23.6815629732815D, 61.5174814933482D, 23.6819236556582D, 61.5176611129723D});
        String var10001 = Instant.now().toString();

        this.testDataMissionSurveyDate = var10001;
        this.testDataVerticalSrsWkt =
            "VERT_CS[\"EGM96 geoid (meters)\",VERT_DATUM[\"EGM96 geoid\",2005,EXTENSION[\"PROJ4_GRIDS\",\"egm96_15.gtx\"],AUTHORITY[\"EPSG\",\"5171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Up\",UP]]";
        this.testDataHorizontalSrsWkt =
            "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
    }
}
