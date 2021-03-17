/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class SynchronizeProjectContentResponse {
   @SerializedName("date")
   @Expose
   @Nullable
   private String date;
   @SerializedName("project")
   @Expose
   @Nullable
   private Project project;
   @SerializedName("missions")
   @Expose
   @NotNull
   private List<Mission> missions = new ArrayList();
   @SerializedName("flights")
   @Expose
   @NotNull
   private List<Flight> flights = new ArrayList();
   @SerializedName("annotations")
   @Expose
   @NotNull
   private List<Annotation> annotations = new ArrayList();
   @SerializedName("flightPlans")
   @Expose
   @NotNull
   private List<FlightPlan> flightplans = new ArrayList();

   @Nullable
   public final String getDate() {
      return this.date;
   }

   public final void setDate(@Nullable String var1) {
      this.date = var1;
   }

   @Nullable
   public final Project getProject() {
      return this.project;
   }

   public final void setProject(@Nullable Project var1) {
      this.project = var1;
   }

   @NotNull
   public final List<Mission> getMissions() {
      return this.missions;
   }

   public final void setMissions(@NotNull List var1) {
            this.missions = var1;
   }

   @NotNull
   public final List<Flight> getFlights() {
      return this.flights;
   }

   public final void setFlights(@NotNull List var1) {
            this.flights = var1;
   }

   @NotNull
   public final List<Annotation> getAnnotations() {
      return this.annotations;
   }

   public final void setAnnotations(@NotNull List var1) {
            this.annotations = var1;
   }

   @NotNull
   public final List<FlightPlan> getFlightplans() {
      return this.flightplans;
   }

   public final void setFlightplans(@NotNull List var1) {
            this.flightplans = var1;
   }
}
