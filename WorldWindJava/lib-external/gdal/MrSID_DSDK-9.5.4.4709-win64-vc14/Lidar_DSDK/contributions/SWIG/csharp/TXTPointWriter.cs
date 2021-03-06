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

public class TXTPointWriter : SimplePointWriter {
  private HandleRef swigCPtr;

  public TXTPointWriter(IntPtr cPtr, bool cMemoryOwn) : base(LidarDSDKPINVOKE.TXTPointWriter_SWIGUpcast(cPtr), cMemoryOwn) {
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr(TXTPointWriter obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }

  ~TXTPointWriter() {
    Dispose();
  }

  public override void Dispose() {
    lock(this) {
      if (swigCPtr.Handle != IntPtr.Zero) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          LidarDSDKPINVOKE.delete_TXTPointWriter(swigCPtr);
        }
        swigCPtr = new HandleRef(null, IntPtr.Zero);
      }
      GC.SuppressFinalize(this);
      base.Dispose();
    }
  }

  public void init(PointSource src, string path, string format) {
    LidarDSDKPINVOKE.TXTPointWriter_init__SWIG_0(swigCPtr, PointSource.getCPtr(src), path, format);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(PointSource src, IO io, string format) {
    LidarDSDKPINVOKE.TXTPointWriter_init__SWIG_1(swigCPtr, PointSource.getCPtr(src), IO.getCPtr(io), format);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(PointSource src, string path, PointInfo fieldInfo) {
    LidarDSDKPINVOKE.TXTPointWriter_init__SWIG_2(swigCPtr, PointSource.getCPtr(src), path, PointInfo.getCPtr(fieldInfo));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void init(PointSource src, IO io, PointInfo fieldInfo) {
    LidarDSDKPINVOKE.TXTPointWriter_init__SWIG_3(swigCPtr, PointSource.getCPtr(src), IO.getCPtr(io), PointInfo.getCPtr(fieldInfo));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public TXTPointWriter() : this(LidarDSDKPINVOKE.new_TXTPointWriter(), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

}

}
