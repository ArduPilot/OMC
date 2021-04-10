/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2010 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef SecurityMetadata_H
#define SecurityMetadata_H

// lt_lib_base
#include "lt_base.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * container for Security metadata
 *
 * This class is a container for all the secuirty metadata
 * for an NITF file.  It is used with the various NITF image
 * manager, reader, and writer classes.
 *
 * For details on the length, formatting, etc, of each field,
 * consult the 2500C NITF specification.
 *
 * Usage Notes:
 * - all "set" calls will make local copies of their strings
 * - caller is responsible for blank-padding of strings
 * - caller is repsonsible for checking proper formatting
 * - all strings are initialized to reasonable defaults (usually just blanks)
 */  
class SecurityMetadata
{
public:
   // not for public use
   SecurityMetadata();

   // not for public use
   SecurityMetadata(const SecurityMetadata&);

   // not for public use
   ~SecurityMetadata();

   // not for public use
   SecurityMetadata& operator=(const SecurityMetadata&);

   /**
    * sets the version field
    */
   LT_STATUS setDataV20(const char*);

   /**
    * gets the version field
    */
   const char* getDataV20() const;     // returns NULL if v21 file

   /**
    * sets the SCLAS field
    */
   LT_STATUS setSCLAS(const char*);

   /**
    * sets the version field
    */
   const char* getSCLAS() const;
  
   /**
    * sets the SCLSY field
    */
   LT_STATUS setSCLSY(const char*);

   /**
    * gets the SCLSY field
    */
   const char* getSCLSY() const;

   /**
    * sets the SCODE field
    */
   LT_STATUS setSCODE(const char*);

   /**
    * gets the SCODE field
    */
   const char* getSCODE() const;

   /**
    * sets the SCTLH field
    */
   LT_STATUS setSCTLH(const char*);

   /**
    * gets the SCTLH field
    */
   const char* getSCTLH() const;

   /**
    * sets the SREL field
    */
   LT_STATUS setSREL(const char*);

   /**
    * gets the SREL field
    */
   const char* getSREL() const;

   /**
    * sets the SCDCTP field
    */
   LT_STATUS setSDCTP(const char*);

   /**
    * gets the SDCTP field
    */
   const char* getSDCTP() const;

   /**
    * sets the SDCDT field
    */
   LT_STATUS setSDCDT(const char*);

   /**
    * gets the SDCDT field
    */
   const char* getSDCDT() const;

   /**
    * sets the SDCXM field
    */
   LT_STATUS setSDCXM(const char*);

   /**
    * gets the SDCXM field
    */
   const char* getSDCXM() const;

   /**
    * sets the SDG field
    */
   LT_STATUS setSDG(const char*);

   /**
    * gets the SDGY field
    */
   const char* getSDG() const;

   /**
    * sets the SDGDT field
    */
   LT_STATUS setSDGDT(const char*);

   /**
    * gets the SDGDT field
    */
   const char* getSDGDT() const;

   /**
    * sets the SCLTX field
    */
   LT_STATUS setSCLTX(const char*);

   /**
    * gets the SCLTX field
    */
   const char* getSCLTX() const;

   /**
    * sets the SCATP field
    */
   LT_STATUS setSCATP(const char*);

   /**
    * gets the SCATP field
    */
   const char* getSCATP() const;

   /**
    * sets the SCAUT field
    */
   LT_STATUS setSCAUT(const char*);

   /**
    * gets the SCAUT field
    */
   const char* getSCAUT() const;

   /**
    * sets the SCRSN field
    */
   LT_STATUS setSCRSN(const char*);

   /**
    * gets the SCRSN field
    */
   const char* getSCRSN() const;

   /**
    * sets the SSRDT field
    */
   LT_STATUS setSSRDT(const char*);

   /**
    * gets the SSRDT field
    */
   const char* getSSRDT() const;

   /**
    * sets the SCTLN field
    */
   LT_STATUS setSCTLN(const char*);

   /**
    * gets the SCTL field
    */
   const char* getSCTLN() const;

private:
   char* m_dataV20;

   char* m_SCLAS;    // 1
   char* m_SCLSY;    // 2
   char* m_SCODE;    // 11
   char* m_SCTLH;    // 2
   char* m_SREL;     // 20
   char* m_SDCTP;    // 2
   char* m_SDCDT;    // 8
   char* m_SDCXM;    // 4
   char* m_SDG;      // 1
   char* m_SDGDT;    // 8
   char* m_SCLTX;    // 43
   char* m_SCATP;    // 1
   char* m_SCAUT;    // 40
   char* m_SCRSN;    // 1
   char* m_SSRDT;    // 8
   char* m_SCTLN;    // 15
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // SecurityMetadata_H
