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

#ifndef __LIDAR_OBJECT_H__
#define __LIDAR_OBJECT_H__

#include "lidar/Atomic.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * Object is the base class for implementing reference counting.
 *
 * The Object class is a base for implementing reference counting.  When an
 * object is created, it has a reference count of 1. When an object is
 * retained, the reference count is incremented. When it is released the
 * reference count is decremented, and when the reference count goes to zero
 * the object gets deleted.
 *
 * Derived classes should supply a static create() function to allocate new
 * objects.
 *
 * Constructors and destructors should be protected so users must
 * go through the create/retain/release functions.
 */
class Object
{
/**
 * Macros for defining boilerplate parts of derived Object classes.
 */

/**
 * ABSTRACT_OBJECT(): should be used when deriving classes that will
 * not be instantiated directly.  (TYPE::create() is not declared.)
 */

#define ABSTRACT_OBJECT(classname) \
   DISABLE_COPY(classname); \
   protected: \
      classname(void); \
      virtual ~classname(void)
   
/**
 * CONCRETE_OBJECT(): should be used when deriving classes
 * that are concrete.
 */
#define CONCRETE_OBJECT(classname) \
   ABSTRACT_OBJECT(classname); \
   public: \
      static classname *create(void)

#define IMPLEMENT_OBJECT_CREATE(classname) \
   classname * classname::create(void) { return new classname; }
   
   ABSTRACT_OBJECT(Object);
public:
   /**
    * Increment the reference count by one.
    */
   int retain(void) const;

   /**
    * Decrement the reference count by one.  When the reference count
    * goes to zero the object gets deleted.
    */
   int release(void) const;

private:
   mutable AtomicInt m_refCount;
};

/**
 * Helper function for calling Object::retain().  RETAIN() tests for NULL
 * before calling retain() and returns obj.
 */
template<typename OBJECT> static inline OBJECT *RETAIN(OBJECT *obj)
{
   if(obj != NULL)
      obj->retain();
   return obj;
}

/**
 * Helper function for calling Object::release().  RELEASE() tests for NULL
 * before calling release() and set obj to NULL.
 */
template<typename OBJECT> static inline void RELEASE(OBJECT *&obj)
{
   if(obj != NULL)
   {
      obj->release();
      obj = NULL;
   }
}

/**
 * Scoped is a wrapper class around Object that gives it block scoping.
 *
 * Scoped is convenience class that give block scoping to reference counted
 * Objects. Scoped<TYPE> tries to act like a TYPE *.
 *
 * As a convenience class you don't have to use it.  Some people find it
 * easier to manage the reference counting themselves.
 *
 * Example:
 *  Without Scoped:
 * \code
 * {
 *    FileIO *file = FileIO::create();
 *    try
 *    {
 *       file->init(path, mode);
 *       // use the file object
 *       if(return early)
 *       {
 *          file->release();
 *          return;
 *       }
 *       // use the file object some more
 *       file->release();
 *    }
 *    catch(...)
 *    {
 *       file->release();
 *       throw;
 *    }
 * }
 * \endcode
 *  With Scoped:
 * \code
 * {
 *    Scoped<FileIO> file;
 *    file->init(path, mode);
 *    // use the file object
 *    if(return early)
 *       return;
 *    // use the file object some more
 * }
 * \endcode
 * @note be careful when using this outside of blocked scope.  
 * The = operator increments the count which can lead to memory leaks.  
 */
template<typename TYPE>
class Scoped
{
public:
   /** Releases the object when Scoped<> goes out of scope */
   ~Scoped(void) { RELEASE(m_object); }

   /** Create an object on the heap. */
   Scoped(void) : m_object(TYPE::create()) {}

   /**
    * Manage an existing object.
    *
    * This constructor does not reatin() object, use operator= if you want to
    * retain the object.
    *
    * @note Use Scoped<TYPE> object(NULL) to get an empty wrapper.
    */
   Scoped(TYPE *object) : m_object(object) {}

   /**
    * Assignment operator
    *
    * The current object is released and the new object is retained.
    */
   Scoped &operator=(TYPE *object)
   {
      if(object != m_object)
      {
         RELEASE(m_object);
         m_object = RETAIN(object);
      }
      return *this;
   }
   Scoped &operator=(const TYPE *object)
   {
      if(object != m_object)
      {
         RELEASE(m_object);
         m_object = RETAIN(const_cast<TYPE *>(object));
      }
      return *this;
   }

   /**
    * Copy constructor.
    *
    * Retain the object.
    */
   Scoped(const Scoped &object) : m_object(RETAIN(object.m_object)) {}

   /** Assignment operator
    *
    * The current object is released and the new object is retained.
    * 
    */
   Scoped &operator=(const Scoped &object)
   {
      if(m_object != object.m_object)
      {
         RELEASE(m_object);
         m_object = RETAIN(object.m_object);
      }
      return *this;
   }

   /** Make Scoped behave like a pointer to TYPE. */
   TYPE *operator->(void) { return m_object; }
   const TYPE *operator->(void) const { return m_object; }
   /** Make Scoped behave like a pointer to TYPE. */
   TYPE &operator*(void) { return *m_object; }
   const TYPE &operator*(void) const { return *m_object; }
#ifndef SWIG
   /** Make Scoped behave like a pointer to TYPE. (use the reference with care)*/
   operator TYPE *&(void) { return m_object; }
   operator const TYPE *&(void) const { return const_cast<const TYPE *&>(m_object); }
#endif

private:
   TYPE *m_object;
};

template<typename OBJECT> static inline OBJECT *RETAIN(Scoped<OBJECT> &obj)
{
   if(obj != NULL)
      obj->retain();
   return obj;
}

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_OBJECT_H__
