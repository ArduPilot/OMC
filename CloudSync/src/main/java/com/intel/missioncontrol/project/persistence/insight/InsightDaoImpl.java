/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence.insight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.intel.missioncontrol.config.CloudSync;
import com.intel.missioncontrol.project.insight.Annotation;
import com.intel.missioncontrol.project.insight.AnnotationResponse;
import com.intel.missioncontrol.project.insight.Annotations;
import com.intel.missioncontrol.project.insight.Camera;
import com.intel.missioncontrol.project.insight.Flight;
import com.intel.missioncontrol.project.insight.FlightPlan;
import com.intel.missioncontrol.project.insight.FlightPlans;
import com.intel.missioncontrol.project.insight.Flights;
import com.intel.missioncontrol.project.insight.Mission;
import com.intel.missioncontrol.project.insight.Missions;
import com.intel.missioncontrol.project.insight.Project;
import com.intel.missioncontrol.project.insight.ProjectLastChangesResponse;
import com.intel.missioncontrol.project.insight.ProjectResponse;
import com.intel.missioncontrol.project.insight.Projects;
import com.intel.missioncontrol.project.insight.Search;
import com.intel.missioncontrol.project.persistence.InsufficientRightsException;
import com.intel.missioncontrol.project.persistence.InvalidFormatException;
import com.intel.missioncontrol.project.persistence.PersistenceException;
import com.intel.missioncontrol.project.persistence.UnauthorizedException;

import java.time.Instant;
import java.util.HashMap;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public final class InsightDaoImpl {
    private static final int CODE_UNAUTHORIZED = 401;
    private static final int CODE_INVALID_JSON = 400;
    private static final int CODE_INSUFFICIENT_RIGHTS = 403;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_INTERNAL_ERROR = 500;
    private static final int CODE_ELEMENT_OUTDATED = 400; // TODO ????

    private final String UISRV = "uisrv/";
    private final String DXPM = "dxpm/";
    private final AsyncStringProperty host;
    private final AsyncObjectProperty<HashMap> headers;
    private transient AsyncBooleanProperty insightLoggedIn = new SimpleAsyncBooleanProperty(this);

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public InsightDaoImpl(InsightContext insightContext) {
        this.host = CloudSync.getInsightConfig().insightHostProperty();
        this.headers = insightContext.headersProperty();
        insightLoggedIn.bind(CloudSync.getInsightConfig().insightLoggedInProperty());
    }

    /** Project CRUD operations group */
    @NotNull
    public final Projects searchAllProjects() {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        Search searchRequest = new Search();
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.UISRV + "projects/search")
                .headers(this.headers.get())
                .body(this.gson.toJson(searchRequest))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Projects.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final ProjectLastChangesResponse getProjectLastModificationDate(@NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.post(this.host.get() + "/dxpm/projects/" + projectId + "/last-changes")
                .headers(this.headers.get())
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, ProjectLastChangesResponse.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Project createProject(@NotNull Project project) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.post(this.host.get() + "uisrv/projects")
                .headers(this.headers.get())
                .body(this.gson.toJson(project))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            Project projectResult = this.gson.fromJson(body, ProjectResponse.class).getProject();

            return projectResult;
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @Nullable
    public final Project searchProjectById(@NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.get(this.host.get() + this.DXPM + "projects/" + projectId).headers(this.headers.get()).asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Project.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Project updateProject(@NotNull Project insightProject) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.put(this.host.get() + "dxpm/projects/" + insightProject.getId())
                .headers(this.headers.get())
                .body(this.gson.toJson(insightProject))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Project.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    public final Project deleteProject(String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.delete(this.host.get() + this.DXPM + "projects/" + projectId)
                .headers(this.headers.get())
                .asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Project.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    /** Camera CRUD operations group */
    @NotNull
    public final Camera createCamera(@NotNull Camera model) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.DXPM + "cameras")
                .headers(this.headers.get())
                .body(this.gson.toJson(model))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Camera.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    /** Mission CRUD operations group */
    @NotNull
    public final Mission createMissionByProjectId(@NotNull String project_id, @NotNull Mission mission) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        JsonObject project = new JsonObject();
        JsonObject missionObj = new JsonObject();
        project.addProperty("_id", project_id);
        missionObj.addProperty("name", mission.getName());
        missionObj.addProperty("created", Instant.now().toString());
        missionObj.add("project", (JsonElement)project);
        String missionReq = this.gson.toJson((JsonElement)missionObj);
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + "dxpm/missions").headers(this.headers.get()).body(missionReq).asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Mission.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Mission updateMission(@NotNull Mission mission) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.put(this.host.get() + this.DXPM + "missions/" + mission.getId())
                .headers(this.headers.get())
                .body(this.gson.toJson(mission))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Mission.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Mission searchMissionById(@NotNull String missionId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.get(this.host.get() + this.DXPM + "missions/" + missionId).headers(this.headers.get()).asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Mission.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Missions searchMissionsByProjectId(@NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        JsonObject searchRequest = new JsonObject();
        searchRequest.addProperty("project_id", projectId);
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.DXPM + "missions/search")
                .headers(this.headers.get())
                .body(this.gson.toJson(searchRequest))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Missions.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    /** FlightPlan CRUD operations group */
    @NotNull
    public final FlightPlan createFlightPlan(@NotNull FlightPlan flightPlan) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.post(this.host.get() + "uisrv/flight-plan")
                .headers(this.headers.get())
                .body(this.gson.toJson(flightPlan))
                .asJson();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        JSONObject body = ((JsonNode)httpResponse.getBody()).getObject();

        try {
            Object subResp = body.get("flight_plan");
            return this.gson.fromJson(subResp.toString(), FlightPlan.class);
        } catch (JSONException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final FlightPlan updateFlightPlan(@NotNull FlightPlan flightPlan) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.put(this.host.get() + "uisrv/flight-plan/" + flightPlan.getId())
                .headers(this.headers.get())
                .body(this.gson.toJson(flightPlan))
                .asJson();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        JSONObject body = ((JsonNode)httpResponse.getBody()).getObject();

        try {
            Object subResp = body.get("flight_plan");
            return this.gson.fromJson(subResp.toString(), FlightPlan.class);
        } catch (JSONException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final FlightPlan searchFlightPlanById(@NotNull String flighPlan_id) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.get(this.host.get() + this.UISRV + "flight-plan/" + flighPlan_id)
                .headers(this.headers.get())
                .asJson();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        JSONObject body = ((JsonNode)httpResponse.getBody()).getObject();

        try {
            Object subResp = body.get("flight_plan");
            return this.gson.fromJson(subResp.toString(), FlightPlan.class);
        } catch (JSONException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final FlightPlans searchAllFlightPlansInMission(@NotNull String missionId, @NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        JsonObject searchRequest = new JsonObject();
        searchRequest.addProperty("project_id", projectId);
        searchRequest.addProperty("mission_id", missionId);
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.UISRV + "fligh-plan/search")
                .headers(this.headers.get())
                .body(this.gson.toJson(searchRequest))
                .asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, FlightPlans.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    /** Annotation CRUD operations group */
    @NotNull
    public final Annotation createAnnotation(@NotNull Annotation insightAoi) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        // create annotation first
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + "uisrv/annotations")
                .headers(this.headers.get())
                .body(this.gson.toJson(insightAoi))
                .asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        AnnotationResponse annotationResponse = null;
        try {
            String body = (String)httpResponse.getBody();

            annotationResponse = this.gson.fromJson(body, AnnotationResponse.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }

        String annotation_Id = annotationResponse.getAnnotation().getId();
        JsonObject request = new JsonObject();
        request.add("parameters", this.gson.toJsonTree(insightAoi.getParameters()));

        // update parameters
        httpResponse =
            Unirest.put(this.host.get() + "uisrv/annotations/" + annotation_Id + "/parameters")
                .headers(this.headers.get())
                .body(this.gson.toJson(request))
                .asString();

        status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, AnnotationResponse.class).getAnnotation();
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Annotation updateAnnotation(@NotNull Annotation insightAoi) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        // update parameters

        JsonObject request = new JsonObject();
        request.add("parameters", this.gson.toJsonTree(insightAoi.getParameters()));
        HttpResponse httpResponse =
            Unirest.put(this.host.get() + "uisrv/annotations/" + insightAoi.getId() + "/parameters")
                .headers(this.headers.get())
                .body(this.gson.toJson(request))
                .asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        AnnotationResponse annotationResponse = null;
        try {
            String body = (String)httpResponse.getBody();

            annotationResponse = this.gson.fromJson(body, AnnotationResponse.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }

        // update feature
        request = new JsonObject();
        request.add("feature", this.gson.toJsonTree(insightAoi.getFeature()));
        httpResponse =
            Unirest.put(this.host.get() + "uisrv/annotations/" + insightAoi.getId() + "/feature")
                .headers(this.headers.get())
                .body(this.gson.toJson(request))
                .asString();
        status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, AnnotationResponse.class).getAnnotation();
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Annotation searchAnnotationById(@NotNull String annotationId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.get(this.host.get() + this.UISRV + "annotations/" + annotationId)
                .headers(this.headers.get())
                .asString();

        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Annotation.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Annotations searchAllAnnotationsInMission(@NotNull String missionId, @NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        JsonObject searchRequest = new JsonObject();
        searchRequest.addProperty("project_id", projectId);
        searchRequest.addProperty("mission_id", missionId);
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.UISRV + "annotations/search")
                .headers(this.headers.get())
                .body(this.gson.toJson(searchRequest))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Annotations.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    /** Flight CRUD operations group */
    @NotNull
    public final Flight createFlight(@NotNull Flight model) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.DXPM + "flights")
                .headers(this.headers.get())
                .body(this.gson.toJson(model))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Flight.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Flight updateFlight(@NotNull Flight model) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.put(this.host.get() + this.DXPM + "flights/" + model.getId())
                .headers(this.headers.get())
                .body(this.gson.toJson(model))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Flight.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Flight searchFlightById(@NotNull String flight_id) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        HttpResponse httpResponse =
            Unirest.get(this.host.get() + this.DXPM + "flights/" + flight_id).headers(this.headers.get()).asJson();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Flight.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

    @NotNull
    public final Flights searchFlightsByProjectId(@NotNull String projectId) {
        if (!insightLoggedIn.get()) {
            // will lead to unauthorized exception anyway, so can be thrown now...
            throw new UnauthorizedException();
        }

        JsonObject missionSearch = new JsonObject();
        missionSearch.addProperty("project_id", projectId);
        HttpResponse httpResponse =
            Unirest.post(this.host.get() + this.DXPM + "flights/search")
                .headers(this.headers.get())
                .body(this.gson.toJson(missionSearch))
                .asString();
        int status = httpResponse.getStatus();
        if (status == CODE_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }

        if (status == CODE_INVALID_JSON) {
            throw new InvalidFormatException();
        }

        if (status == CODE_INSUFFICIENT_RIGHTS) {
            throw new InsufficientRightsException();
        }

        if (status == CODE_NOT_FOUND || status == CODE_INTERNAL_ERROR) {
            throw new PersistenceException(String.valueOf(status));
        }

        try {
            String body = (String)httpResponse.getBody();

            return this.gson.fromJson(body, Flights.class);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException(String.valueOf(CODE_INVALID_JSON));
        }
    }

}
