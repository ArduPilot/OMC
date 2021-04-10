This directory contains several sample MrSID images.


  ** THESE IMAGES ARE FOR SDK TESTING PURPOSES ONLY     **
  ** AND MAY NOT BE RELEASED OR USED EXTERNALLY WITHOUT **
  ** EXPLICIT PERMISSION FROM LIZARDTECH.               **  


meg.bip  - original image (RAW format), 640x480, 3-band, 8-bit
meg.hdr  - header file for meg.bip

meg_lossless.sid  - image compressed losslessly
meg_cr20.sid      - image compressed at roughly 20:1

meg_crop.sid      - lossless image, cropped to rectangle
                    UL=(200,200), LR=(400,400)
meg_scale.sid     - lossless image, scaled to remove 2 levels

meg_locked.sid    - lossless image, password protected
                    password is "foo"

meg_cr20_MG2.sid   - image compressed at roughly 20:1, using
                     the previous version of the MrSID file
                     format ("MG2")
