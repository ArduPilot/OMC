/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008-2010 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef __LIDAR_PROGRESS_DELEGATE_H__
#define __LIDAR_PROGRESS_DELEGATE_H__

#include "lidar/Base.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * ProgressDelegate is the base class for progress and interrupt reporting
 *
 * ProgressDelegate class is used for reporting progress of and interrupting
 * long-running operation such as PointWriter::write() and PointSource::read().
 * During long-running operation the delegate's updateCompleted() method is
 * called which will inturn calls reportProgress() with the fraction completed.
 * Likewise getCancelled() is called frequently and should return true when
 * the delegate wants to stop the long-running operation.
 */
class ProgressDelegate
{
	DISABLE_COPY(ProgressDelegate);
public:
   virtual ~ProgressDelegate(void);

   /**
    * Displays the progress of the operation.
    *
    * This method should be implemented to report to the client application
    * the progress of a long-running operation.
    *
    * \param progress a value between 0 and 1
    *               (progress = <work completed> / <total work>)
    */
   virtual void reportProgress(double progress, const char *message) = 0;
   /**
    * Indicate weather the operation should be cancelled.
    *
    * This method should be implemented to indicate whether some user-defined
    * event indicates that the operation should be terminated.
    *
    * \return return true to cancel the long-running operation
    */
   virtual bool getCancelled(void) = 0;

   /**
    * Set the total amount of work that must be done.
    *
    * This method sets the total amount of work that must be done.  This
    * value is used as denominator.
    *
    * \param total the total work for the ProgressDelgate
    */
   void setTotal(double total);
   /**
    * Update the amount of work that has been done.
    *
    * This method adds delta to the amount of work done.
    *
    * \param delta the amount of work to add to the work accumulator
    * \param message a tag for who the caller is
    */
   void updateCompleted(double delta, const char *message);

   /**
    * Get the fraction the work completed.
    *
    * This method returns the progress.
    *
    * \return returns the a number between 0 and 1.
    */
   double getProgress(void) const;


   void warning(const char *format, ...);
   virtual void displayWarning(const char *message);

protected:
   ProgressDelegate(void);

private:
   double m_completed;
   double m_total;
};

class PercentProgressDelegate : public ProgressDelegate
{
   DISABLE_COPY(PercentProgressDelegate);
public:
   PercentProgressDelegate(void);
   ~PercentProgressDelegate(void);

   void reportProgress(double fractionDone, const char *message);
   bool getCancelled(void);
   void displayWarning(const char *message);

   void setCancelled(bool cancel);
private:
   int m_lastUpdate;
   const char *m_lastMessage;
   bool m_cancelled;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_PROGRESS_DELEGATE_H__
