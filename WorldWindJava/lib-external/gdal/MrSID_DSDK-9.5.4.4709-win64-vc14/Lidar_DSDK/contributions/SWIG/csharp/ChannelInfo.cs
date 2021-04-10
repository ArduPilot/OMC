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

public class ChannelInfo : IDisposable {
  private HandleRef swigCPtr;
  protected bool swigCMemOwn;

  public ChannelInfo(IntPtr cPtr, bool cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr(ChannelInfo obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }

  ~ChannelInfo() {
    Dispose();
  }

  public virtual void Dispose() {
    lock(this) {
      if (swigCPtr.Handle != IntPtr.Zero) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          LidarDSDKPINVOKE.delete_ChannelInfo(swigCPtr);
        }
        swigCPtr = new HandleRef(null, IntPtr.Zero);
      }
      GC.SuppressFinalize(this);
    }
  }

      private System.Object refToOwner;
      internal void setOwnerObject(System.Object owner) { refToOwner = owner; }
   
  public ChannelInfo() : this(LidarDSDKPINVOKE.new_ChannelInfo(), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(string name, DataType datatype, int bits, double quantization) {
    LidarDSDKPINVOKE.ChannelInfo_init__SWIG_0(swigCPtr, name, (int)datatype, bits, quantization);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(string name, DataType datatype, int bits) {
    LidarDSDKPINVOKE.ChannelInfo_init__SWIG_1(swigCPtr, name, (int)datatype, bits);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(ChannelInfo info) {
    LidarDSDKPINVOKE.ChannelInfo_init__SWIG_2(swigCPtr, ChannelInfo.getCPtr(info));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public string getName() {
    string ret = LidarDSDKPINVOKE.ChannelInfo_getName(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public DataType getDataType() {
    DataType ret = (DataType)LidarDSDKPINVOKE.ChannelInfo_getDataType(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public uint getBits() {
    uint ret = LidarDSDKPINVOKE.ChannelInfo_getBits(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public double getQuantization() {
    double ret = LidarDSDKPINVOKE.ChannelInfo_getQuantization(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public void setQuantization(double value) {
    LidarDSDKPINVOKE.ChannelInfo_setQuantization(swigCPtr, value);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public bool Equals(ChannelInfo rhs) {
    bool ret = LidarDSDKPINVOKE.ChannelInfo_Equals(swigCPtr, ChannelInfo.getCPtr(rhs));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

}

}
