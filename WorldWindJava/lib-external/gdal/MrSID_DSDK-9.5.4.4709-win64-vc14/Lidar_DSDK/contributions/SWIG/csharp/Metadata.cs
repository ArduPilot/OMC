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

public class Metadata : IDisposable {
  private HandleRef swigCPtr;
  protected bool swigCMemOwn;

  public Metadata(IntPtr cPtr, bool cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr(Metadata obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }

  ~Metadata() {
    Dispose();
  }

  public virtual void Dispose() {
    lock(this) {
      if (swigCPtr.Handle != IntPtr.Zero) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          LidarDSDKPINVOKE.delete_Metadata(swigCPtr);
        }
        swigCPtr = new HandleRef(null, IntPtr.Zero);
      }
      GC.SuppressFinalize(this);
    }
  }

  public Metadata() : this(LidarDSDKPINVOKE.new_Metadata(), true) {
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public uint getNumRecords() {
    uint ret = LidarDSDKPINVOKE.Metadata_getNumRecords(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public bool has(string key) {
    bool ret = LidarDSDKPINVOKE.Metadata_has(swigCPtr, key);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public uint getKeyIndex(string key) {
    uint ret = LidarDSDKPINVOKE.Metadata_getKeyIndex(swigCPtr, key);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public string getKey(uint idx) {
    string ret = LidarDSDKPINVOKE.Metadata_getKey(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public string getDescription(uint idx) {
    string ret = LidarDSDKPINVOKE.Metadata_getDescription(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public MetadataDataType getDataType(uint idx) {
    MetadataDataType ret = (MetadataDataType)LidarDSDKPINVOKE.Metadata_getDataType(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public System.Object getValue(uint idx) {
   IntPtr ptr = LidarDSDKPINVOKE.Metadata_getValue(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
   int length = (int)LidarDSDKPINVOKE.Metadata_getValueLength(swigCPtr, idx);
   switch((MetadataDataType)LidarDSDKPINVOKE.Metadata_getDataType(swigCPtr, idx))
   {
      case MetadataDataType.METADATA_DATATYPE_STRING:
         return System.Runtime.InteropServices.Marshal.PtrToStringAnsi(ptr);
      case MetadataDataType.METADATA_DATATYPE_BLOB:
         byte[] blob = new byte[length];
         System.Runtime.InteropServices.Marshal.Copy(ptr, blob, 0, length);
         return blob;
      case MetadataDataType.METADATA_DATATYPE_REAL_ARRAY:
         double[] array = new double[length];
         System.Runtime.InteropServices.Marshal.Copy(ptr, array, 0, length);
         return array;
      default:
         return null;
   }
}

  public uint getValueLength(uint idx) {
    uint ret = LidarDSDKPINVOKE.Metadata_getValueLength(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
    return ret;
  }

  public void add(string key, string description, MetadataDataType datatype, SWIGTYPE_p_void value, uint length) {
    LidarDSDKPINVOKE.Metadata_add__SWIG_0(swigCPtr, key, description, (int)datatype, SWIGTYPE_p_void.getCPtr(value), length);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void add(Metadata meta) {
    LidarDSDKPINVOKE.Metadata_add__SWIG_1(swigCPtr, Metadata.getCPtr(meta));
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void remove() {
    LidarDSDKPINVOKE.Metadata_remove__SWIG_0(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void remove(uint idx) {
    LidarDSDKPINVOKE.Metadata_remove__SWIG_1(swigCPtr, idx);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void remove(string key) {
    LidarDSDKPINVOKE.Metadata_remove__SWIG_2(swigCPtr, key);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

  public void sort() {
    LidarDSDKPINVOKE.Metadata_sort(swigCPtr);
    if (LidarDSDKPINVOKE.SWIGPendingException.Pending) throw LidarDSDKPINVOKE.SWIGPendingException.Retrieve();
  }

}

}
