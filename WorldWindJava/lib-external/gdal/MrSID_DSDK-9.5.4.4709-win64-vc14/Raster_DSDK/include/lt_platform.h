/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


/**      
 * @file
 *
 * Preprocessor symbols for canonical identification of OS, architecture,
 * and compiler.  Scrupulous use of these and only these symbols avoids
 * portability problems due to inconsistent platform tests.
 * 
 * For a given target platform XYZ, we define three symbols with the value 1:
 *
 * \li \c LT_OS_XYZ defines the operating system
 * \li \c LT_COMPILER_XYZ defines the compiler
 * \li \c LT_ARCH_XYZ defines the HW architecture
 *
 * Note for Windows, we treat WIN32, WIN64, and WinCE as distinct OS's, but
 * both will define LT_OS_WIN for the typical cases.
 *
 * See the file lt_platform.h for full details.
 *
 * @note This file is C-callable.
 */


#ifndef LT_PLATFORM_H
#define LT_PLATFORM_H

/* Check Compiler */
#if (defined(__GNUC__) || defined(__GNUG__)) && (3 <= __GNUC__ && __GNUC__ <= 5)
   #define LT_COMPILER_GNU 1

   #if defined(__GNUC__)
      #define LT_COMPILER_GCC 1
   #endif
   #if defined(__GNUG__)
      #define LT_COMPILER_GXX 1
   #endif

   #if(__GNUC__ == 3)
      #define LT_COMPILER_GCC3 1
   #elif(__GNUC__ == 4)
      #define LT_COMPILER_GCC4 1
   #else
      #define LT_COMPILER_GCC5 1
   #endif

   #define LT_DEPRECATED(NEW) __attribute__ ((__deprecated__))

#elif defined (__SUNPRO_CC) || defined(__SUNPRO_C) 
   #define LT_COMPILER_SUN 1

   #if defined (__SUNPRO_CC)
      #define LT_COMPILER_SUNPRO_CC 1
   #endif
   #if defined(__SUNPRO_C)
      #define LT_COMPILER_SUNPRO_C 1
   #endif

   #define LT_DEPRECATED(NEW)

#elif defined(_MSC_VER) &&  (1300 <= _MSC_VER && _MSC_VER <= 1910)  
   #define LT_COMPILER_MS 1
   #if _MSC_VER < 1400
      #define LT_COMPILER_MS7 1
   #elif _MSC_VER < 1500
      #define LT_COMPILER_MS8 1
   #elif _MSC_VER < 1600
      #define LT_COMPILER_MS9 1
   #elif _MSC_VER < 1700
      #define LT_COMPILER_MS10 1
   #elif _MSC_VER < 1800
      #define LT_COMPILER_MS11 1
   #elif _MSC_VER < 1900
      #define LT_COMPILER_MS12 1
   #elif _MSC_VER < 1910
      #define LT_COMPILER_MS14 1
   #else
      #define LT_COMPILER_MS17 1
   #endif

   #if !defined(_CRT_SECURE_NO_DEPRECATE) && _MSC_VER >= 1400
      #define _CRT_SECURE_NO_DEPRECATE
      #define _CRT_SECURE_NO_WARNINGS
      #define _CRT_NONSTDC_NO_WARNINGS
      #define LT_DEPRECATED(NEW) __declspec(deprecated("was declared deprecated and replaced by "#NEW))
   #else
      #define LT_DEPRECATED(NEW)
   #endif

#else
   #error PLATFORM ERROR: unknown compiler
#endif

/* Check Architecture and Endian */
/* We do not define LT_BIG_ENDIAN, as that would just confuse things;
   either you're little endian, or you're not. */

#if defined(__ppc) || defined(__ppc__)
   #define LT_ARCH_PPC 1
#elif defined(__ppc64) || defined(__ppc64__)
   #define LT_ARCH_PPC64 1
#elif defined(__sparcv9) || defined(__sparcv9__)
   #define LT_ARCH_SPARC64 1
#elif defined(__sparc) || defined(__sparcv9__)
   #define LT_ARCH_SPARC 1
#elif defined(__i386__) || defined(i386) || \
      defined(_M_IX86) || defined(_X86_) || defined(x86)
   #define LT_ARCH_IA32 1
   #define LT_LITTLE_ENDIAN
#elif defined(__x86_64) || defined(__x86_64__) || \
      defined(__amd64) || defined(__amd64__) || \
      defined(_M_AMD64)
   #define LT_ARCH_AMD64 1  /* x86 w/ 64-extensions ("x86-64") */
   #define LT_LITTLE_ENDIAN
#elif defined(__hppa__)
   #define LT_ARCH_PARISC 1
#elif defined(_M_IX86)
   #define LT_ARCH_IA64 1   /* Itanium */
   #define LT_LITTLE_ENDIAN
#elif defined(ARM) || defined(_ARM_) || defined(__arm__)
   #define LT_ARCH_ARM 1
   #define LT_LITTLE_ENDIAN
#elif defined(__arm64) || defined(__arm64__) || defined (__ARM_NEON)
   #define LT_ARCH_ARM64 1
   #define LT_LITTLE_ENDIAN
#elif defined(__MIPSEL__)
   #define LT_ARCH_MIPSEL 1
   #define LT_LITTLE_ENDIAN
#else
   #error PLATFORM ERROR: unknown architecture
#endif

/* Check OS */
#if defined(_WIN64) || defined(WIN64)
   #define LT_OS_WIN 1
   #define LT_OS_WIN64 1
#elif defined(_WIN32_WCE)
   #define LT_OS_WIN 1
   #define LT_OS_WINCE 1
#elif defined(_WIN32) || defined(WIN32)
   #define LT_OS_WIN 1
   #define LT_OS_WIN32 1
#elif defined(__APPLE__) && defined(__MACH__)
   #define LT_OS_UNIX 1
   #define LT_OS_DARWIN 1
#elif defined(__hpux) 
   #define LT_OS_UNIX 1
   #define LT_OS_HPUX 1
#elif defined (__sun)
   #define LT_OS_UNIX 1
   #define LT_OS_SUNOS 1
#elif defined (linux) || defined (__linux__) || defined (__linux)
   #define LT_OS_UNIX 1
   #define LT_OS_LINUX 1
#else
   #error PLATFORM ERROR: unsupported platform
#endif

#if !defined(LT_OS_WIN) && !defined(LT_OS_UNIX)
   #error PLATFORM ERROR: set LT_OS_WIN or LT_OS_UNIX
#endif

/* backwards compatability, deprecated stuff */

#if defined(LT_LITTLE_ENDIAN) && !defined(_LITTLE_ENDIAN)
   /* this is deprecated! */
   #define _LITTLE_ENDIAN
#endif

#ifdef LT_OS_UNIX
   #define LT_UNIX 1
#endif

#endif /* LT_PLATFORM_H */

