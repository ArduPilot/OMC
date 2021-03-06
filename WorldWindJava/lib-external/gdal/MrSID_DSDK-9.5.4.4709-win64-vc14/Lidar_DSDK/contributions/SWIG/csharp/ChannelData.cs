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

public class ChannelData : ChannelInfo {
  private HandleRef swigCPtr;

  public ChannelData(IntPtr cPtr, bool cMemoryOwn) : base(LidarDSDKPINVOKE.ChannelData_SWIGUpcast(cPtr), cMemoryOwn) {
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr(ChannelData obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }

  ~ChannelData() {
    Dispose();
  }

  public override void Dispose() {
    lock(this) {
      if (swigCPtr.Handle != IntPtr.Zero) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          LidarDSDKPINVOKE.delete_ChannelData(swigCPtr);
        }
        swigCPtr = new HandleRef(null, IntPtr.Zero);
      }
      GC.SuppressFinalize(this);
      base.Dispose();
    }
  }

  public ChannelData() : this(LidarDSDKPINVOKE.new_ChannelData(), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(ChannelInfo info, uint numSamples) {
    LidarDSDKPINVOKE.ChannelData_init(swigCPtr, ChannelInfo.getCPtr(info), numSamples);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public uint getNumSamples() {
    uint ret = LidarDSDKPINVOKE.ChannelData_getNumSamples(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public static void copy(ChannelData dst, uint dstOffset, ChannelData src, uint srcOffset, uint length) {
    LidarDSDKPINVOKE.ChannelData_copy(ChannelData.getCPtr(dst), dstOffset, ChannelData.getCPtr(src), srcOffset, length);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public static void convert(ChannelData dst, uint dstOffset, ChannelData src, uint srcOffset, uint length) {
    LidarDSDKPINVOKE.ChannelData_convert__SWIG_0(ChannelData.getCPtr(dst), dstOffset, ChannelData.getCPtr(src), srcOffset, length);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public static void convert(ChannelData dst, uint dstOffset, ChannelData src, uint srcOffset, double offset, double scale, uint length) {
    LidarDSDKPINVOKE.ChannelData_convert__SWIG_1(ChannelData.getCPtr(dst), dstOffset, ChannelData.getCPtr(src), srcOffset, offset, scale, length);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void resize(uint newNumSamples) {
    LidarDSDKPINVOKE.ChannelData_resize(swigCPtr, newNumSamples);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void setOffset(uint offset) {
    LidarDSDKPINVOKE.ChannelData_setOffset(swigCPtr, offset);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void setData(SWIGTYPE_p_void data, bool deleteData) {
    LidarDSDKPINVOKE.ChannelData_setData(swigCPtr, SWIGTYPE_p_void.getCPtr(data), deleteData);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public double getValue(uint i) {
    double ret = LidarDSDKPINVOKE.ChannelData_getValue(swigCPtr, i);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

}

}
