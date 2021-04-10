using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows;

namespace LizardTech.SampleWpfViewer
{
   public class Interop
   {
      private enum LtiColorSpace
      {
         // these values taken directly from lti_types.h
         Alpha = 0x010000,
         Grayscale = 0x000101,
         GrayscaleA = Grayscale | Alpha,
         Rgb = 0x000301,
         RgbA = Rgb | Alpha,
         Multispectral = 0x00FF01,
         MultispectralA = Multispectral | Alpha
      }

      private enum LtiDataType
      {
         // these values taken directly from lti_types.h
         Uint8 = 1,
         Sint8 = 2,
         Uint16 = 3,
         Sint16 = 4,
         Float32 = 7,
      }

      #region interop decls

      // declare the C functions which we are going to call via Interop
      // (these are declared in ltic_api.h)

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_getMrSIDGeneration(string fileName, out int gen, out int raster);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_openMrSIDImageFile(out IntPtr image, string fileName);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern uint ltic_getWidth(IntPtr image);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern uint ltic_getHeight(IntPtr image);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern ushort ltic_getNumBands(IntPtr image);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_getColorSpace(IntPtr image);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_getDataType(IntPtr image);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_decode(IntPtr image,
                                   double xUpperLeft,
                                   double yUpperLeft,
                                   double width,
                                   double height,
                                   double magnification,
                                   IntPtr buffers);

      [DllImport("lti_dsdk_cdll_9.5.dll", CallingConvention=CallingConvention.Cdecl)]
      static extern int ltic_closeImage(IntPtr image);
      #endregion

      IntPtr m_image = IntPtr.Zero;   // this is essentially our void* pointer

      public int Width { get; private set; }
      public int Height { get; private set; }

      private int Bands { get; set; }
      private LtiColorSpace ColorSpace { get; set; }
      private LtiDataType DataType { get; set; }

      private PixelFormat Format { get; set; }
      private bool HasAlpha { get; set; }

      public Interop()
      {

         return;
      }

      public void Open(string infile)
      {
         int sts;

         int gen, raster;
         sts = ltic_getMrSIDGeneration(infile, out gen, out raster);
         Check(sts);

         if (raster == 0)
         {
            throw new InvalidOperationException("this is a LiDAR file -- use the LiDAR SDK to work with it");
         }


         sts = ltic_openMrSIDImageFile(out m_image, infile);
         Check(sts);

         // get some basic properties of the image
         Height = (int)ltic_getHeight(m_image);
         Width = (int)ltic_getWidth(m_image);

         Bands = (int)ltic_getNumBands(m_image);

         DataType = (LtiDataType)ltic_getDataType(m_image);
         switch (DataType)
         {
            case LtiDataType.Uint8:
               break;
            case LtiDataType.Sint8:
            case LtiDataType.Uint16:
            case LtiDataType.Sint16:
            case LtiDataType.Float32:
            default:
               throw new InvalidOperationException("unsupported datatype");
         }

         ColorSpace = (LtiColorSpace)ltic_getColorSpace(m_image);
         switch (ColorSpace)
         {
            case LtiColorSpace.Rgb:
            case LtiColorSpace.Grayscale:
            case LtiColorSpace.Multispectral:
               Format = PixelFormats.Rgb24;
               HasAlpha = false;
               break;
            case LtiColorSpace.RgbA:
            case LtiColorSpace.GrayscaleA:
            case LtiColorSpace.MultispectralA:
               Format = PixelFormats.Bgra32;
               HasAlpha = true;
               break;
            default:
               throw new InvalidOperationException("unsupported colorspace");
         }

         return;
      }

      public void Close()
      {
         if (m_image == IntPtr.Zero)
            return;

         // close the image
         int sts = ltic_closeImage(m_image);
         Check(sts);

         m_image = IntPtr.Zero;
      }

      private void SwizzleBands(byte[][] dst, byte[][] src)
      {
         if (ColorSpace == LtiColorSpace.Rgb)
         {
            dst[0] = src[0];
            dst[1] = src[1];
            dst[2] = src[2];
            dst[3] = null;
         }
         else if (ColorSpace == LtiColorSpace.RgbA)
         {
            dst[0] = src[2];
            dst[1] = src[1];
            dst[2] = src[0];
            dst[3] = src[src.Length - 1];
         }
         else if (ColorSpace == LtiColorSpace.Grayscale)
         {
            dst[0] = src[0];
            dst[1] = src[0];
            dst[2] = src[0];
            dst[3] = null;
         }
         else if (ColorSpace == LtiColorSpace.GrayscaleA)
         {
            dst[0] = src[0];
            dst[1] = src[0];
            dst[2] = src[0];
            dst[3] = src[src.Length - 1];
         }
         else if (ColorSpace == LtiColorSpace.Multispectral && Bands < 3)
         {
            dst[0] = src[0];
            dst[1] = src[0];
            dst[2] = src[0];
            dst[3] = null;
         }
         else if (ColorSpace == LtiColorSpace.Multispectral && Bands >= 3)
         {
            dst[0] = src[0];
            dst[1] = src[1];
            dst[2] = src[2];
            dst[3] = null;
         }
         else if (ColorSpace == LtiColorSpace.MultispectralA && Bands < 4)
         {
            dst[0] = src[0];
            dst[1] = src[0];
            dst[2] = src[0];
            dst[3] = src[src.Length - 1];
         }
         else if (ColorSpace == LtiColorSpace.MultispectralA && Bands >= 4)
         {
            dst[0] = src[2];
            dst[1] = src[1];
            dst[2] = src[0];
            dst[3] = src[src.Length - 1];
         }
         else
         {
            throw new InvalidOperationException("unsupported colorspace");
         }

         return;
      }

      public WriteableBitmap CreateBitmap(int width, int height)
      {
         // decode as large of a scene as we can for the request
         width = Math.Min(Width, width);
         height = Math.Min(Height, height);

         WriteableBitmap bitmap = new WriteableBitmap(width, height, 96.0, 96.0, Format, null);

         // read the data into a local buffer
         byte[][] buf = Decode(width, height);

         bitmap.Lock();

         // copy the data from the local BSQ buffer into the BIP bitmap
         unsafe
         {
            byte* dst = (byte*)bitmap.BackBuffer.ToPointer();

            int numBands = (HasAlpha ? 4 : 3);

            for (int h = 0; h < height; h++)
            {
               for (int w = 0; w < width; w++)
               {
                  int pSrc = h * width + w;
                  int pDst = h * bitmap.BackBufferStride + w * numBands;

                  for (int b = 0; b < numBands; b++)
                  {
                     dst[pDst + b] = buf[b][pSrc];
                  }
               }
            }
         }

         Int32Rect rect = new Int32Rect(0, 0, width, height);
         bitmap.AddDirtyRect(rect);

         bitmap.Unlock();

         return bitmap;
      }

      private byte[][] Decode(int width, int height)
      {
         byte[][] ret = new byte[4][];

         GCHandle[] pinnedBuffers = new GCHandle[Bands];

         byte[][] data = new byte[Bands][];
         for (int i = 0; i < Bands; i++)
         {
            data[i] = new byte[width * height];
         }

         // aside: this is all a bit of a kludge, as the vagaries of passing
         // nontrivial types via Interop (such as void** pointers) can be daunting;
         // experience has shown that it is often best to create your C API such that
         // the interop marshalling works out nicely.  Consult the docs for help.         
         
         unsafe
         {
            IntPtr[] buffers = new IntPtr[Bands];   // an array of pointers, one to each band buffer
            for (int i = 0; i < Bands; i++)
            {
               pinnedBuffers[i] = GCHandle.Alloc(data[i], GCHandleType.Pinned);
               IntPtr p = pinnedBuffers[i].AddrOfPinnedObject();
               buffers[i] = p;
            }

            // lock down the array and make a pointer to it
            fixed (IntPtr* p = buffers)
            {
               IntPtr pp = (IntPtr)p;

               // just grab a scene from the upper left
               int sts = ltic_decode(m_image, 0.0, 0.0, width, height, 1.0, pp);
               Check(sts);
            }

            for (int i = 0; i < Bands; i++)
            {
               pinnedBuffers[i].Free();
            }
         }

         SwizzleBands(ret, data);

         return ret;
      }

      private static void Check(int sts)
      {
         if (sts == 0) return;
         throw new InvalidOperationException("Error - sts = " + sts);
      }
   }
}
