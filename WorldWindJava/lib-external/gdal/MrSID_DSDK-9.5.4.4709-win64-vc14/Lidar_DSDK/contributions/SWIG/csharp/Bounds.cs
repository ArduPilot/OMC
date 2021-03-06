/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

namespace LizardTech.LidarSDK {

using System;
using System.Runtime.InteropServices;

public class Bounds : IDisposable {
  private HandleRef swigCPtr;
  protected bool swigCMemOwn;

  public Bounds(IntPtr cPtr, bool cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr(Bounds obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }

  ~Bounds() {
    Dispose();
  }

  public virtual void Dispose() {
    lock(this) {
      if (swigCPtr.Handle != IntPtr.Zero) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          LidarDSDKPINVOKE.delete_Bounds(swigCPtr);
        }
        swigCPtr = new HandleRef(null, IntPtr.Zero);
      }
      GC.SuppressFinalize(this);
    }
  }

  public Range x {
    set {
      LidarDSDKPINVOKE.Bounds_x_set(swigCPtr, Range.getCPtr(value));
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    } 
    get {
      IntPtr cPtr = LidarDSDKPINVOKE.Bounds_x_get(swigCPtr);
      Range ret = (cPtr == IntPtr.Zero) ? null : new Range(cPtr, false);
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
      return ret;
    } 
  }

  public Range y {
    set {
      LidarDSDKPINVOKE.Bounds_y_set(swigCPtr, Range.getCPtr(value));
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    } 
    get {
      IntPtr cPtr = LidarDSDKPINVOKE.Bounds_y_get(swigCPtr);
      Range ret = (cPtr == IntPtr.Zero) ? null : new Range(cPtr, false);
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
      return ret;
    } 
  }

  public Range z {
    set {
      LidarDSDKPINVOKE.Bounds_z_set(swigCPtr, Range.getCPtr(value));
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    } 
    get {
      IntPtr cPtr = LidarDSDKPINVOKE.Bounds_z_get(swigCPtr);
      Range ret = (cPtr == IntPtr.Zero) ? null : new Range(cPtr, false);
      if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
      return ret;
    } 
  }

  public static Bounds Huge() {
    Bounds ret = new Bounds(LidarDSDKPINVOKE.Bounds_Huge(), false);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public Bounds(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_0(xmin, xmax, ymin, ymax, zmin, zmax), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(double xmin, double xmax, double ymin, double ymax, double zmin) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_1(xmin, xmax, ymin, ymax, zmin), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(double xmin, double xmax, double ymin, double ymax) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_2(xmin, xmax, ymin, ymax), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(double xmin, double xmax, double ymin) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_3(xmin, xmax, ymin), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(double xmin, double xmax) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_4(xmin, xmax), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(double xmin) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_5(xmin), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds() : this(LidarDSDKPINVOKE.new_Bounds__SWIG_6(), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public Bounds(Range ax, Range ay, Range az) : this(LidarDSDKPINVOKE.new_Bounds__SWIG_7(Range.getCPtr(ax), Range.getCPtr(ay), Range.getCPtr(az)), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public bool Equals(Bounds b) {
    bool ret = LidarDSDKPINVOKE.Bounds_Equals(swigCPtr, Bounds.getCPtr(b));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public bool contains(double ax, double ay, double az) {
    bool ret = LidarDSDKPINVOKE.Bounds_contains(swigCPtr, ax, ay, az);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public bool overlaps(Bounds b) {
    bool ret = LidarDSDKPINVOKE.Bounds_overlaps(swigCPtr, Bounds.getCPtr(b));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public bool empty() {
    bool ret = LidarDSDKPINVOKE.Bounds_empty(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public double volume() {
    double ret = LidarDSDKPINVOKE.Bounds_volume(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public void shift(double dx, double dy, double dz) {
    LidarDSDKPINVOKE.Bounds_shift(swigCPtr, dx, dy, dz);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void scale(double dx, double dy, double dz) {
    LidarDSDKPINVOKE.Bounds_scale(swigCPtr, dx, dy, dz);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void clip(Bounds r) {
    LidarDSDKPINVOKE.Bounds_clip(swigCPtr, Bounds.getCPtr(r));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void grow(Bounds r) {
    LidarDSDKPINVOKE.Bounds_grow__SWIG_0(swigCPtr, Bounds.getCPtr(r));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void grow(double ax, double ay, double az) {
    LidarDSDKPINVOKE.Bounds_grow__SWIG_1(swigCPtr, ax, ay, az);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public static double overlapFraction(Bounds r1, Bounds r2) {
    double ret = LidarDSDKPINVOKE.Bounds_overlapFraction(Bounds.getCPtr(r1), Bounds.getCPtr(r2));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

}

}
