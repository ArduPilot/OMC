/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 * 
 * This file is not intended to be easily readable and contains a number of 
 * coding conventions designed to improve portability and efficiency. Do not make
 * changes to this file unless you know what you are doing--modify the SWIG 
 * interface file instead. 
 * ----------------------------------------------------------------------------- */

#ifndef SWIG_LidarDSDK_WRAP_H_
#define SWIG_LidarDSDK_WRAP_H_

namespace Swig {
  class Director;
}


class SwigDirector_ProgressDelegate : public LizardTech::ProgressDelegate, public Swig::Director {

public:
    SwigDirector_ProgressDelegate(VALUE self);
    virtual ~SwigDirector_ProgressDelegate();
    virtual void reportProgress(double progress, char const *message);
    virtual bool getCancelled();
    virtual void displayWarning(char const *message);
};


#endif
