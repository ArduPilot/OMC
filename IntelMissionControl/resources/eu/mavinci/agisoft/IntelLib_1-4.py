# MAVinci -> Photoscan Processing Script 
import os
import os.path
import sys
import math
import datetime
import PhotoScan
import traceback
from PySide2 import QtGui, QtCore, QtWidgets
import subprocess
import time
import multiprocessing
import locale

#disable automatically showing of models after loading a project
QtCore.QSettings().setValue('main/show_model', 0) #disabling starting project in model view since this is too slow
QtCore.QSettings().setValue('main/navigation_mode', 1) #enabling new navigation mode (terrain) by default

mavinciRelease = u"{{mavinci.release}}"
exportHeader = u"{{mavinci.exportHeader}}"


global qualities
qualities = [u"[[high]]",u"[[medium]]",u"[[low]]"]
qualitiesEN = ["high","medium","low"]
modelTileSizes = ['256','512','1024','2048','4096','8192']
APIaccuracy = [PhotoScan.HighAccuracy,PhotoScan.MediumAccuracy,PhotoScan.LowAccuracy]
APIfaceCount = [PhotoScan.HighFaceCount,PhotoScan.MediumFaceCount,PhotoScan.LowFaceCount]


qualitiesDense = [u"[[ultrahigh]]",u"[[high]]",u"[[medium]]",u"[[low]]",u"[[lowest]]"]
qualitiesDenseEN = ["ultrahigh","high","medium","low","lowest"]
APIdense = [1,2,4,8,16]

global gpsTypes
gpsTypes = ['GPS','DGPS','DGPS_RTK']

def storeLog():
    doc = PhotoScan.app.document
    if doc ==None:
      return
    if len(doc.path)==0:
      return
    folder = os.path.abspath(os.path.join(doc.path, os.pardir))
    filename = os.path.join(folder,time.strftime("PhotoScan-%Y%m%d-%H%M%S.log"))
    with open(filename, "w") as text_file:
      text_file.write(PhotoScan.app.console.contents)
      print("stored console content to %s " % filename)

def openFile(filePath):
  print(u"[[openFile]]" % filePath)
  if sys.platform.startswith('darwin'):
    subprocess.call(('open', filePath))
  elif os.name == 'nt':
    os.startfile(filePath)
  elif os.name == 'posix':
    env = os.environ.copy()
    env["LD_LIBRARY_PATH"] = ""
    subprocess.Popen(('xdg-open', filePath), env=env)
    
def openMAVinciDesktop():
  try:
    doc = PhotoScan.app.document
    if doc ==None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
    
    folder = os.path.abspath(os.path.join(doc.path, os.pardir))
    matching = os.path.join(folder,'dataset.ptg')
    openFile(matching)
    return True
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
    

def checkSetup():
  try:
    saveTimestamp("checkSetup.start")
    print(u"[[processing.checkOpenCL]]")
    
    if not PhotoScan.app.activated:
      PhotoScan.app.messageBox(u"[[notActivated]]" )
      return False
    
    doc = PhotoScan.app.document
    if doc != None and doc.chunk != None and doc.chunk.crs == None:
      PhotoScan.app.messageBox(u"[[SRSnonSupported]]" % doc.meta['mavinci.wktName'])
    
    
    mask = 0
    i = 1
    for device in devices:
      vendor = device['vendor'].lower() 
      vendor = vendor.replace('corporation','')#eg. intel corperation was matched as ATI
      name = device['name'].lower()
      if 'nvidia' in vendor or 'ati' in vendor or 'amd' in vendor or 'advanced micro devices' in vendor or 'quadro' in name or 'geforce' in name:
        mask += i
      i *= 2
    
    print(u"computed optimal GPU mask = %s, current GPU mask = %s" % (mask,PhotoScan.app.gpu_mask))  
    
    if PhotoScan.app.gpu_mask != mask or PhotoScan.app.cpu_enable != True:
      msgBox = QtWidgets.QMessageBox(QtWidgets.QApplication.instance().activeWindow())
      msgBox.setText(u"[[processing.askFixOpenCL]]")
      msgBox.setStandardButtons(QtWidgets.QMessageBox.Yes | QtWidgets.QMessageBox.No | QtWidgets.QMessageBox.Cancel)
      msgBox.setDefaultButton(QtWidgets.QMessageBox.Yes)
      reply = msgBox.exec_()
      msgBox.close()
      if reply == QtWidgets.QMessageBox.Yes:
        PhotoScan.app.gpu_mask = mask
        PhotoScan.app.cpu_enable = True
        saveTimestamp("checkSetup.fixed")
        print(u"[[processing.fixOpenCL]]")
      elif reply == QtWidgets.QMessageBox.No:
        print(u"[[processing.notFixOpenCL]]")    
      else:
        return False
    saveTimestamp("checkSetup.end")
    return True
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False


def process(showReady=True):
  PhotoScan.app.console.clear()
  print("Intel Plugin: %s" % exportHeader)
  try:
      ts = time.time()
      if showReady:
          if not checkSetup():
              return False
      saveTimestamp("process.start")
      if not process1(showReady=False):
          return False
      
      if not process2(showReady=False):
          return False
    
      t = str(datetime.timedelta(seconds=round(time.time() - ts)))
      print(u"[[processing.ready]]" % t)
      if showReady:
          PhotoScan.app.messageBox(u"[[processing.ready]]" % t)
      
      return True
  finally:
      storeLog()
  
def testOp(succeed,name):
  #if not succeed:
  #str = u"[[OperationFailed]] NOT" % name
  #print(str)
  #PhotoScan.app.messageBox(str)
  #raise Exception(str)  
  str = u"[[OperationOK]]" % name
  print(str)

def process1(showReady=True):
  try:
    if showReady:
        PhotoScan.app.console.clear()
        print("Intel Plugin: %s" % exportHeader)
        if not checkSetup():
          return False
    saveTimestamp("process1.1start")
    fixSettings()
      
    saveTimestamp("process1.2")
    
    print(u"[[processing1.start]]")
    ts = time.time()
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    doc.meta['mavinci.plugin.release'] = mavinciRelease
    doc.meta['mavinci.plugin.exportHeaderCore'] = exportHeader
    
    acc = APIaccuracy[qualitiesEN.index(doc.meta['mavinci.align.quality'])]
    
    accuracy = float(doc.meta['mavinci.camAccuracy'])
    
    print(u"[[gpsAccuracyAssumption]]" % (accuracy) )
    saveTimestamp("process1.3")
    chunk.camera_location_accuracy = [accuracy,accuracy,accuracy]#in this step we have to stay with non zscale, zscale is only important at END of optimization step<
    print("Accuracy")
    print(acc)
    if acc != PhotoScan.LowAccuracy:
      saveTimestamp("process1.4")
      testOp(chunk.matchPhotos(accuracy=acc, preselection = PhotoScan.ReferencePreselection,filter_mask=doc.meta['mavinci.useMasks']=='1', keypoint_limit = 40000, tiepoint_limit=4000),u"match photos")
      saveTimestamp("process1.7")
      testOp(chunk.alignCameras(),u"[[alignCameras]]")
      saveTimestamp("process1.8")
      if not mavinciOptimize(False):
        return False
      saveTimestamp("process1.5")
      saveTimestamp("process1.7")
      testOp(chunk.alignCameras(),u"[[alignCameras]]")
      saveTimestamp("process1.8")
      chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True)
      doc.save()
    else:
      saveTimestamp("process1.6")
      testOp(chunk.matchPhotos(accuracy=acc, preselection = PhotoScan.ReferencePreselection,filter_mask=doc.meta['mavinci.useMasks']=='1', keypoint_limit = 20000, tiepoint_limit=1000),u"match photos")
      saveTimestamp("process1.7")
      testOp(chunk.alignCameras(),u"[[alignCameras]]")
      saveTimestamp("process1.8")
    #saveTimestamp("process1.7")
    #testOp(chunk.alignCameras(),u"[[alignCameras]]")
    #saveTimestamp("process1.8")
    chunk.resetRegion()
    doc.save()
    PhotoScan.app.document.open(doc.path)#try to fix memory fragmentation by doing this
    doc = PhotoScan.app.document
    chunk = doc.chunk
 
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"[[processing1.ready]]" % t)
    if showReady:
      PhotoScan.app.messageBox(u"[[processing1.ready]]" % t)
    return True
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
  finally:
    if showReady:
      storeLog()

def correctLevelArm():
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
        return False
    if doc.meta['mavinci.cam.x']==None:
        return False
    if chunk.sensors[0].antenna.fixed:
        return False
    vec = PhotoScan.Vector((float(doc.meta['mavinci.cam.x']),float(doc.meta['mavinci.cam.y']),float(doc.meta['mavinci.cam.z'])))
    mat = PhotoScan.utils.ypr2mat(chunk.sensors[0].antenna.rotation)
    chunk.sensors[0].antenna.location_ref = mat*vec
    return True



def medianOnSorted(l):
    if len(l)==0:
        return 0
    if len(l) % 2 == 0:
        return (l[int(len(l) / 2 - 1)] + l[int(len(l) / 2 )]) /  2
    else: 
        return l[int(len(l) // 2)]

def mavinciOptimize(showReady=True):
  try:
    if showReady:
        PhotoScan.app.console.clear()
        print("Intel Plugin: %s" % exportHeader)
    fixSettings()
    ts = time.time()
    saveTimestamp("mavinciOptimize.01")
    
    print(u"[[mavinciOptimize.start]]")
    saveTimestamp("mavinciOptimize.02")
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    saveTimestamp("mavinciOptimize.03")
    #qual = doc.meta['mavinci.quality']
    if doc.meta['mavinci.gpsType']=='DGPS_RTK' :
      print(u"[[processing.RTK.optimisation.start]]")
      saveTimestamp("mavinciOptimize.04") 
      cams = doc.chunk.cameras

      for cam in cams:
        cam.reference.enabled = True

      saveTimestamp("mavinciOptimize.05")
      acc=0.2
      chunk.camera_location_accuracy = [acc,acc,acc]#in this step we have to stay with non zscale, zscale is only important at END of optimization step
      correctLevelArm()
      chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True)
      lastpercentage=0
      
      saveTimestamp("mavinciOptimize.06")
      #accScale = 8*0.75/chunk.accuracy_tiepoints
      while acc>=0.005:
           posenabled=0
           mean=0
           totalcnt=0
           errs=[]
           # just count how many we would throw out
           for cam in cams:
             totalcnt += 1
             if cam.center==None:
               continue#maybe it was not possible at all to compute a estimated position
             if not cam.reference.enabled:
               continue
             transform = chunk.transform.matrix * cam.transform
             location = transform.mulp(PhotoScan.Vector((0, 0, 0))) + transform.rotation() * PhotoScan.Matrix.Diag((1, -1, -1)) * cam.sensor.antenna.location
             
             localframe = chunk.crs.localframe(location)
             err = localframe.rotation() * (location - chunk.crs.unproject(cam.reference.location))
             err = err.norm()
             mean += err
             posenabled += 1
             errs.append(err)
           percentage = posenabled/totalcnt
           err = 99999
           if posenabled>0:
             err = mean/posenabled;
           errs.sort()
           medianErr = medianOnSorted(errs)
           errCutOffMed = medianErr * 6
           errCutOffQuant = errs[int(len(errs) *.9)]#make sure at least 90% will survive
           errCutOff = max(errCutOffQuant,errCutOffMed)
           
           posenabled=0
           mean=0
           # just count how many we would throw out
           for cam in cams:
             if cam.center==None:
               continue#maybe it was not possible at all to compute a estimated position
             if not cam.reference.enabled:
               continue
             transform = chunk.transform.matrix * cam.transform
             location = transform.mulp(PhotoScan.Vector((0, 0, 0))) + transform.rotation() * PhotoScan.Matrix.Diag((1, -1, -1)) * cam.sensor.antenna.location
             
             localframe = chunk.crs.localframe(location)
             err = localframe.rotation() * (location - chunk.crs.unproject(cam.reference.location))
             err = err.norm()
             if err <= errCutOff :
                mean += err
                posenabled += 1
           if posenabled>0:
             err = mean/posenabled;
           
           
           s = 'gpsAccuracy='+repr(acc)+'m -> mean tiePoint error was ' +repr(err)+'m, '+repr(posenabled)+' of '+repr(totalcnt)+' are within '+repr(errCutOff)+'m = max('+repr(errCutOffMed)+','+repr(errCutOffQuant)+')('+repr(percentage*100)+'%)'
           print(s)
           if percentage<0.6:
             print('Stop optimization at '+repr(doc.chunk.camera_location_accuracy[0])+'m camera accuracy, since less then 60% would be used.')
             break
           #if percentage<0.90*lastpercentage:
             #print('Stop optimization at '+repr(doc.chunk.camera_location_accuracy[0])+'m camera accuracy, since removed amount dropped more then 10%.')
             #break
           lastpercentage = percentage
           # now really do it
           for cam in cams:
             if cam.center==None:
               cam.reference.enabled = False
               continue
             transform = chunk.transform.matrix * cam.transform
             location = transform.mulp(PhotoScan.Vector((0, 0, 0))) + transform.rotation() * PhotoScan.Matrix.Diag((1, -1, -1)) * cam.sensor.antenna.location
             
             localframe = chunk.crs.localframe(location)
             err = localframe.rotation() * (location - chunk.crs.unproject(cam.reference.location))
             err = err.norm()
             if err > errCutOff :
               cam.reference.enabled = False
           acc /= 1.5
           doc.chunk.camera_location_accuracy = [acc,acc,acc]
           correctLevelArm()
           testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
           chunk.resetRegion()
    
      acc *= 1.5 #undo last decrease
      print('reoptimize with different trust level in Z')
      zScale = float(doc.meta['mavinci.zScale'])
      doc.chunk.camera_location_accuracy = [acc,acc,acc*zScale]#in this step we have to stay with non zscale, zscale is only important at END of optimization step
      correctLevelArm()
      testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
      chunk.resetRegion()
#     acc = 0.01
#     bestzerr = 1000
#     while acc<0.10:
#          doc.chunk.camera_location_accuracy = acc
#          testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
#          mean = 0
#           camcnt = 0
#           for cam in cams:
#             if cam.center==None:
#               continue #maybe it was not possible at all to compute a estimated position
#             if cam.reference.enabled == False:
#               continue
#             camcnt = camcnt + 1
#             #worldsource = doc.chunk.crs.unproject(cam.reference.location)
#             #cc = doc.chunk.transform.matrix.mulp(cam.center)
#             worldsource = cam.reference.location
#             cc = doc.chunk.crs.project(doc.chunk.transform.matrix.mulp(cam.center))
#
#             cc.size=3
#             worldc = cc
#             err = (worldc - worldsource)[2] # distance in m
#             mean = mean + err*err
#             #s = 'z err of cam ' +repr(err)
#             #print(s)
#           mean = math.sqrt(mean / camcnt)
#           s = 'mean z error was ' +repr(mean)+' at acc setting '+repr(acc)
#           print(s)
#           zerr = mean/acc
#           if abs(zerr-1) < abs(bestzerr-1):
#              bestzerr = zerr
#              bestacc = acc
#           acc = acc * 1.1

#      doc.chunk.camera_location_accuracy = bestacc
#      testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
           
      saveTimestamp("mavinciOptimize.07")
    else:
    #elif qual != 'low':   #non RTK GPS used
      saveTimestamp("mavinciOptimize.08")
      correctLevelArm()
      testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
      chunk.resetRegion()
      saveTimestamp("mavinciOptimize.09")
      correctLevelArm()
      testOp(chunk.optimizeCameras(fit_f=True,fit_cx=True,fit_cy=True,fit_b1=True,fit_b2=True,fit_k1=True,fit_k2=True,fit_k3=True,fit_k4=True,fit_p1=True,fit_p2=True,fit_p3=True,fit_p4=True),u"optimize cameras")
        
    
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"[[mavinciOptimize.ready]]" % t)
    if showReady:
        PhotoScan.app.messageBox(u"[[mavinciOptimize.ready]]" % t)
    return True
    
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
  finally:
    if showReady:
      storeLog()

def process2(showReady=True):
  try:
    if showReady:
        PhotoScan.app.console.clear()
        print("Intel Plugin: %s" % exportHeader)
        if not checkSetup():
            return False
    fixSettings()
    saveTimestamp("process2.01")
    ts = time.time()
    
    print(u"[[processing2.start]]")
    saveTimestamp("process2.02")
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    
    doc.meta['mavinci.plugin.release'] = mavinciRelease
    doc.meta['mavinci.plugin.exportHeaderCore'] = exportHeader
    
    accuracy = float(doc.meta['mavinci.camAccuracy'])
    print(u"[[gpsAccuracyAssumption]]" % (accuracy) )
    
    if not mavinciOptimize(False):
        return False
   
    saveTimestamp("process2.10")
    
    doc.save()
    saveTimestamp("process2.11")
    
    
    
    if doc.meta['mavinci.dense.enable']=='1':
      qualDense = APIdense[qualitiesDenseEN.index(doc.meta['mavinci.dense.quality'])]
      print("qualDense")
      print(qualDense)
      testOp(chunk.buildDepthMaps(quality=qualDense,filter=PhotoScan.FilterMode.MildFiltering),u"[[buildDepthMaps]]")
      testOp(chunk.buildDenseCloud(point_colors = True),u"[[buildDenseCloud]]")
      saveTimestamp("process2.12")
      doc.save()
      PhotoScan.app.document.open(doc.path)#try to fix memory fragmentation by doing this
      doc = PhotoScan.app.document
      chunk = doc.chunk
      pointSource=PhotoScan.DataSource.DenseCloudData
    else:
      pointSource=PhotoScan.DataSource.PointCloudData
      
    
    #universal export parameters
    crs = PhotoScan.CoordinateSystem(doc.meta['mavinci.wkt'])
    folder = os.path.abspath(os.path.join(doc.path, os.pardir))
    
    saveTimestamp("process2.15")
    if doc.meta['mavinci.points.enable']!='0':
        testOp(doc.chunk.exportPoints(os.path.join(folder,'points.las'), binary=True, precision=6, colors=True, source = pointSource, format=PhotoScan.PointsFormat.PointsFormatLAS, projection=crs),u"[[exportPoints]]")

    
    maxPix = 30000
    maxPixDem = maxPix
    blockOffset = 0.2
    
    #we take coordinates of first photo (you can add a check that it's aligned),
    #then take small horizontal vectors along OX and OY
    #and calculate their distance.. by doing so, we get the transform scale
    orgCrs = doc.chunk.crs
    doc.chunk.crs = crs
    doc.chunk.updateTransform()
    
    crd = doc.chunk.cameras[0].reference.location  #coordinates of first photo
    
    #longitude
    v1 = PhotoScan.Vector( (crd[0],crd[1],0) )
    v2 = PhotoScan.Vector( (crd[0]+0.01,crd[1],0) )
    if doc.chunk.crs == None:
      vm1 = v1
      vm2 = v2
    else:         
      vm1 = doc.chunk.crs.unproject(v1)
      vm2 = doc.chunk.crs.unproject(v2)
    
    res_x = (vm1 - vm2).norm() * 100
    
    #latitude
    v2 = PhotoScan.Vector( (crd[0],crd[1]+0.01,0) )
    if doc.chunk.crs == None:
      vm2 = v2
    else:         
      vm2 = doc.chunk.crs.unproject(v2)
    
    
    doc.chunk.crs = orgCrs
    doc.chunk.updateTransform()
    
    res_y = (vm1 - vm2).norm() * 100
    
    
    
    
    
    saveTimestamp("process2.23")
    if doc.meta['mavinci.dsm.enable']!='0':
      
      resolutionDSM = float(doc.meta['mavinci.dsm.gsd'])
      d_x_DSM = resolutionDSM / res_x  
      d_y_DSM = resolutionDSM/ res_y
      print(u"d_x_DSM=%e  d_y_DSM=%e"%(d_x_DSM,d_y_DSM))
        
      testOp(chunk.buildDem(source=pointSource, interpolation=PhotoScan.Interpolation.EnabledInterpolation, projection=crs),u"[[buildDem]]")
      
      pixXdem = doc.chunk.elevation.width
      pixYdem = doc.chunk.elevation.height
      print("x-pixDEM=%i  y-pixDEM=%i" % (pixXdem, pixYdem))
      blockCountXdem = math.ceil(pixXdem  / maxPixDem + blockOffset)
      blockCountYdem = math.ceil(pixYdem  / maxPixDem + blockOffset)
      print("blockCountDEM x=%i  y=%i" % (blockCountXdem, blockCountYdem))
      doc.meta['mavinci.blockCount.dem'] = "%i:%i" % (blockCountXdem, blockCountYdem)
      blockwDEM= math.ceil(pixXdem/blockCountXdem)
      blockhDEM= math.ceil(pixYdem/blockCountYdem)
      print("blockDEM w=%i  h=%i" % (blockwDEM, blockhDEM))
      doc.save()
            
      if blockCountXdem > 1 or blockCountYdem > 1: 
        testOp(chunk.exportDem(os.path.join(folder,'dem.tif'), format = PhotoScan.RasterFormat.RasterFormatTiles, image_format=PhotoScan.ImageFormat.ImageFormatTIFF, write_world=True, projection=crs, dx=d_x_DSM, dy=d_y_DSM, tiff_big=True, blockw=blockwDEM,blockh=blockhDEM),u"[[exportDem]]")
      else:
        testOp(chunk.exportDem(os.path.join(folder,'dem.tif'), format = PhotoScan.RasterFormat.RasterFormatTiles, image_format=PhotoScan.ImageFormat.ImageFormatTIFF, write_world=True, projection=crs, dx=d_x_DSM, dy=d_y_DSM, tiff_big=True),u"[[exportDem]]")

    saveTimestamp("process2.24")
    if doc.meta['mavinci.ortho.enable']!='0':
    
      resolutionOrtho = float(doc.meta['mavinci.ortho.gsd'])
      d_x = resolutionOrtho / res_x  
      d_y = resolutionOrtho / res_y
      print(u"d_x=%e  d_y=%e"%(d_x,d_y))

      color_correction=doc.meta['mavinci.ortho.enableColCorrection']!='0'
      if color_correction:
        testOp(chunk.calibrateColors(source_data=PhotoScan.DataSource.ElevationData, color_balance=False),u"[[calibrateColors]]")
      testOp(chunk.buildOrthomosaic(surface=PhotoScan.DataSource.ElevationData, blending=PhotoScan.BlendingMode.MosaicBlending, fill_holes=True, projection=crs, dx=d_x, dy=d_y),u"[[buildOrthophoto]]")
      
      pixX = doc.chunk.orthomosaic.width
      pixY = doc.chunk.orthomosaic.height
      print("x-pix=%i  y-pix=%i" % (pixX, pixY))
      blockCountX = math.ceil(pixX  / maxPix + blockOffset)
      blockCountY = math.ceil(pixY  / maxPix + blockOffset)
      print("blockCount x=%i  y=%i" % (blockCountX, blockCountY))
      doc.meta['mavinci.blockCount.ortho'] = "%i:%i" % (blockCountX, blockCountY)
      blockw= math.ceil(pixX/blockCountX)
      blockh= math.ceil(pixY/blockCountY)
      print("block w=%i  h=%i" % (blockw, blockh))
      doc.save()

      if blockCountX > 1 or blockCountY > 1:
        testOp(chunk.exportOrthomosaic(os.path.join(folder,'ortho.tif'), format = PhotoScan.RasterFormat.RasterFormatTiles, image_format=PhotoScan.ImageFormat.ImageFormatTIFF, raster_transform=PhotoScan.RasterTransformNone, write_kml=False, write_world=True, projection=crs, dx=d_x, dy=d_y, blockw=blockw,blockh=blockh),u"[[exportOrthophoto]]")
      else:
        testOp(chunk.exportOrthomosaic(os.path.join(folder,'ortho.tif'), format = PhotoScan.RasterFormat.RasterFormatTiles, image_format=PhotoScan.ImageFormat.ImageFormatTIFF, raster_transform=PhotoScan.RasterTransformNone, write_kml=False, write_world=True, projection=crs, dx=d_x, dy=d_y),u"[[exportOrthophoto]]")

    #generate report
    reportPath = os.path.join(folder,'report.pdf')
    saveTimestamp("process2.26")
    if doc.meta['mavinci.title']==None or doc.meta['mavinci.description']==None:
        testOp(chunk.exportReport(reportPath),u"[[exportReport]]")
    else:
        testOp(chunk.exportReport(reportPath, title=doc.meta['mavinci.title'].replace("\\n","\n"),description=doc.meta['mavinci.description'].replace("\\n","\n")),u"[[exportReport]]")
    
    #testOp(chunk.exportReport(reportPath, title="Hello\\nTitle",description="Hello\\nDescription"),u"export report")
    openFile(reportPath)
    saveTimestamp("process2.27")
    doc.save()            


    if doc.meta['mavinci.model.enable']!='0':
      testOp(chunk.buildTiledModel(pixel_size=float(doc.meta['mavinci.model.gsd']), tile_size=int(float(doc.meta['mavinci.model.tileSize']))),u"[[buildTiledModel]]")
      saveTimestamp("process2.25")
      #redo the report, to get the runtime statistics of the model on the last page... but du it also before, in case of crash during model generation
      testOp(chunk.exportReport(reportPath, title=doc.meta['mavinci.title'].replace("\\n","\n"),description=doc.meta['mavinci.description'].replace("\\n","\n")),u"[[exportReport]]")
      doc.save()
    


    
    
    #saveTimestamp("process2.26")
    openFile(folder)
    
    #saveTimestamp("process2.27")    
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"Intel post GCP processing ready. Completed in %s sec" % t)
    if showReady:
        PhotoScan.app.messageBox(u"Intel post GCP processing ready. Completed in %s sec" % t)
    return True
    
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
  finally:
    if showReady:
      storeLog()



class SetupDialog(QtWidgets.QDialog):
    def __init__(self, parent=None):
        super(SetupDialog, self).__init__(parent)
        self.process=0
    
    def doProcess(self):
        self.process=1
        self.accept()
        
        
    def gpsTypeChanged(self):
        self.spinAccuracy.setEnabled(self.cmbGPStype.currentText()!='DGPS_RTK')
        self.lblAccuracy.setEnabled(self.cmbGPStype.currentText()!='DGPS_RTK')
        self.lblOptimization.setText(u"[[optimizationType.2xPhotoscan]]" if self.cmbGPStype.currentText()!='DGPS_RTK' else u"[[optimizationType.mavinci]]")
        
    def checkBoxChanged(self,someValue):
        self.grpOrtho.setEnabled(self.grpDSM.isChecked())
        self.cmbDense.setEnabled(self.chkDense.isChecked())
        

def fixSettings():
    doc = PhotoScan.app.document
    #fix for backward compatibility
    if doc.meta['mavinci.ortho.gsd']==None:
      doc.meta['mavinci.ortho.gsd'] = doc.meta['mavinci.resolution']
      
    if doc.meta['mavinci.dsm.gsd']==None:
      doc.meta['mavinci.dsm.gsd'] = str(float(doc.meta['mavinci.resolution'])*2)
    
    qual = doc.meta['mavinci.quality']
    
    if doc.meta['mavinci.align.quality']==None:
        doc.meta['mavinci.align.quality']=qual
        
    if doc.meta['mavinci.dense.quality']==None:
        doc.meta['mavinci.dense.quality']=qual
        
    if doc.meta['mavinci.points.enable']==None:
        doc.meta['mavinci.points.enable']='0'
        
        
    if doc.meta['mavinci.useMasks']==None:
        doc.meta['mavinci.useMasks']='0'
        
    if doc.meta['mavinci.model.enable']==None:
        doc.meta['mavinci.model.enable']='0'  
    if doc.meta['mavinci.model.gsd']==None:
      doc.meta['mavinci.model.gsd'] = doc.meta['mavinci.resolution']
    if doc.meta['mavinci.model.tileSize']==None:
        doc.meta['mavinci.model.tileSize']='512'
    
    if doc.meta['mavinci.zScale']==None:
        if doc.meta['mavinci.gpsType']=='DGPS_RTK':
            doc.meta['mavinci.zScale']='0.1'
        else:
            doc.meta['mavinci.zScale']='1.0'
           

def setup():
  try:
    print(u"[[setup.start]]")
    saveTimestamp("setup.start")
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return
  
    
    app = QtWidgets.QApplication.instance()
    parent = app.activeWindow()
    dlg  = SetupDialog(parent)
    
    mainLayout = QtWidgets.QVBoxLayout()
    dlg.setLayout(mainLayout)
    
    
    dlg.setWindowTitle(u"[[setup.title]]")
    
    #doc.meta['mavinci.desktopPath'] #here a path to MAVinci desktop 
    fixSettings()
    

    #################################
    ############### HW Type #########
    #################################
    
    
    horizontalGroupBox = QtWidgets.QGroupBox(u"[[setup.hwSpecs]]");
    layout = QtWidgets.QFormLayout();
    horizontalGroupBox.setLayout(layout)
    mainLayout.addWidget(horizontalGroupBox)
    
    
    cmbGPStype = QtWidgets.QComboBox()
    cmbGPStype.addItems(gpsTypes)
    index = cmbGPStype.findText(doc.meta['mavinci.gpsType']);
    if index != -1 :
      cmbGPStype.setCurrentIndex(index)
    dlg.cmbGPStype = cmbGPStype 
    QtCore.QObject.connect( cmbGPStype, QtCore.SIGNAL('currentIndexChanged(int)'), dlg, QtCore.SLOT('gpsTypeChanged()') );
    
    layout.addRow(QtWidgets.QLabel(u"[[setup.GPStype]]"),cmbGPStype)
    
    
    lblAccuracy = QtWidgets.QLabel(u"[[setup.accuracy]]")
    dlg.lblAccuracy=lblAccuracy
    
    spinAccuracy = QtWidgets.QDoubleSpinBox();
    spinAccuracy.setRange(.1,2500)
    spinAccuracy.setValue(float(doc.meta['mavinci.camAccuracy'])*100)
    spinAccuracy.setSingleStep(10)
    spinAccuracy.setSuffix(u" cm")
    spinAccuracy.setDecimals(1)
    spinAccuracy.setAlignment(QtCore.Qt.AlignRight)
    dlg.spinAccuracy = spinAccuracy 
    
    layout.addRow(lblAccuracy,spinAccuracy)
    
    
    
    
    #################################
    ############### Processing Options  #########
    #################################
    
    
    horizontalGroupBox = QtWidgets.QGroupBox(u"[[mavinci.processing]]");
    layout = QtWidgets.QFormLayout();
    horizontalGroupBox.setLayout(layout)
    mainLayout.addWidget(horizontalGroupBox)
    
    cmbAlign = QtWidgets.QComboBox()
    cmbAlign.addItems(qualities)
    index = cmbAlign.findText(qualities[qualitiesEN.index(doc.meta['mavinci.align.quality'])]);
    if index != -1 :
      cmbAlign.setCurrentIndex(index)
    layout.addRow(QtWidgets.QLabel(u"[[mavinci.align]]"),cmbAlign)
    
    
    dlg.lblOptimization= QtWidgets.QLabel()
    
    layout.addRow(QtWidgets.QLabel(u"[[mavinci.optimization]]"),dlg.lblOptimization)
    
    chkDense = QtWidgets.QCheckBox(u"[[mavinci.dense]]")
    chkDense.setChecked(doc.meta['mavinci.dense.enable']!='0')
    dlg.chkDense=chkDense
    cmbDense = QtWidgets.QComboBox()
    cmbDense.addItems(qualitiesDense)
    index = cmbDense.findText(qualitiesDense[qualitiesDenseEN.index(doc.meta['mavinci.dense.quality'])]);
    if index != -1 :
      cmbDense.setCurrentIndex(index)
    layout.addRow(chkDense,cmbDense)
    dlg.cmbDense=cmbDense
    QtCore.QObject.connect(chkDense, QtCore.SIGNAL('stateChanged(int)'), dlg, QtCore.SLOT('checkBoxChanged(int)'))
    
    
    
    #################################
    ############### Projection #########
    #################################
    
    
    grpProjection = QtWidgets.QGroupBox(u"[[setup.projection]]")
    layout = QtWidgets.QGridLayout()
    grpProjection.setLayout(layout)
    mainLayout.addWidget(grpProjection)
    
    
    projMAVinci = QtWidgets.QLabel(doc.meta['mavinci.wktName'],dlg)
    layout.addWidget(projMAVinci,0,0,1,1)
    
    
    #################################
    ############### DEM     #########
    #################################
    
    
    grpDSM = QtWidgets.QGroupBox(u"[[setup.dsm]]");
    layout = QtWidgets.QFormLayout();
    grpDSM.setLayout(layout)
    mainLayout.addWidget(grpDSM)
    grpDSM.setCheckable(True);
    grpDSM.setChecked(doc.meta['mavinci.dsm.enable']!='0');
    QtCore.QObject.connect(grpDSM, QtCore.SIGNAL('toggled(bool)'), dlg, QtCore.SLOT('checkBoxChanged(bool)'))
    dlg.grpDSM = grpDSM
    
    
    spinDsmGSD = QtWidgets.QDoubleSpinBox();
    spinDsmGSD.setRange(.1,100)
    spinDsmGSD.setValue(float(doc.meta['mavinci.dsm.gsd'])*100)
    spinDsmGSD.setSingleStep(0.1)
    spinDsmGSD.setDecimals(1)
    spinDsmGSD.setSuffix(u" cm")
    spinDsmGSD.setAlignment(QtCore.Qt.AlignRight)
    layout.addRow(QtWidgets.QLabel(u"[[setup.dsm.gsd]]"),spinDsmGSD)
    
    
    #################################
    ############### Ortho  #########
    #################################
    
    
    grpOrtho = QtWidgets.QGroupBox(u"[[setup.ortho]]");
    layout = QtWidgets.QFormLayout();
    grpOrtho.setLayout(layout)
    mainLayout.addWidget(grpOrtho)
    grpOrtho.setCheckable(True);
    grpOrtho.setChecked(doc.meta['mavinci.ortho.enable']!='0');
    dlg.grpOrtho=grpOrtho
    
    
    spinOrthoGSD = QtWidgets.QDoubleSpinBox();
    spinOrthoGSD.setRange(.1,100)
    spinOrthoGSD.setValue(float(doc.meta['mavinci.ortho.gsd'])*100)
    spinOrthoGSD.setSingleStep(0.1)
    spinOrthoGSD.setDecimals(1)
    spinOrthoGSD.setSuffix(u" cm")
    spinOrthoGSD.setAlignment(QtCore.Qt.AlignRight)
    layout.addRow(QtWidgets.QLabel(u"[[setup.ortho.gsd]]"),spinOrthoGSD)
    
    
    chkColCorr = QtWidgets.QCheckBox()
    chkColCorr.setChecked(doc.meta['mavinci.ortho.enableColCorrection']=='1')
    layout.addRow(QtWidgets.QLabel(u"[[setup.ortho.colorCorrection]]"),chkColCorr)
    
    
    
    #################################
    ############### Points export     #########
    #################################
    
    
    grpPoints = QtWidgets.QGroupBox(u"[[setup.points]]");
    layout = QtWidgets.QFormLayout();
    grpPoints.setLayout(layout)
    mainLayout.addWidget(grpPoints)
    grpPoints.setCheckable(True);
    grpPoints.setChecked(doc.meta['mavinci.points.enable']!='0');
    dlg.grpPoints = grpPoints

    #################################
    ############### Model     #########
    #################################
    
    
    grpModel = QtWidgets.QGroupBox(u"[[setup.model]]");
    layout = QtWidgets.QFormLayout();
    grpModel.setLayout(layout)
    mainLayout.addWidget(grpModel)
    grpModel.setCheckable(True);
    grpModel.setChecked(doc.meta['mavinci.model.enable']!='0');
    dlg.grpModel = grpModel
    

    spinModelGSD = QtWidgets.QDoubleSpinBox();
    spinModelGSD.setRange(.1,100)
    spinModelGSD.setValue(float(doc.meta['mavinci.model.gsd'])*100)
    spinModelGSD.setSingleStep(0.1)
    spinModelGSD.setDecimals(1)
    spinModelGSD.setSuffix(u" cm")
    spinModelGSD.setAlignment(QtCore.Qt.AlignRight)
    layout.addRow(QtWidgets.QLabel(u"[[setup.model.gsd]]"),spinModelGSD)
    
    cmbModelTileSize = QtWidgets.QComboBox()
    cmbModelTileSize.addItems(modelTileSizes)
    index = modelTileSizes.index(doc.meta['mavinci.model.tileSize']);
    if index != -1 :
      cmbModelTileSize.setCurrentIndex(index)
    layout.addRow(QtWidgets.QLabel(u"[[setup.model.tileSize]]"),cmbModelTileSize)
    
    #################################
    ############### BUTTONS #########
    #################################
    
    
    horizontalGroupBox = QtWidgets.QGroupBox();
    horizontalGroupBox.setStyleSheet("QGroupBox{border:0;}");
    layout = QtWidgets.QHBoxLayout();
    horizontalGroupBox.setLayout(layout)
    mainLayout.addWidget(horizontalGroupBox)
    
    
    btnCancel = QtWidgets.QPushButton(u"[[setup.btnCancel]]")
    layout.addWidget(btnCancel)
    QtCore.QObject.connect(btnCancel, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('reject()'))
    
    btnSave = QtWidgets.QPushButton(u"[[setup.btnSave]]")
    layout.addWidget(btnSave)
    btnSave.setDefault(True)
    QtCore.QObject.connect(btnSave, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('accept()'))
    
    btnProcess = QtWidgets.QPushButton(u"[[setup.btnProcess]]")
    layout.addWidget(btnProcess)
    QtCore.QObject.connect(btnProcess, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('doProcess()'))
    
    ####################################################################
    ### change visibility settings and so on based on selected values###
    ####################################################################
    
    dlg.gpsTypeChanged() 
    dlg.checkBoxChanged(0) 
    
    
    ########
    ## align forms in different groups
    ## see: http://stackoverflow.com/questions/1331308/in-qt-how-do-i-align-form-elements-in-different-group-boxes
    
    dlg.show() #layout seems only be computed correct when visible
    
    width = 0
    #print(dlg.children())
    layouts = dlg.findChildren(QtWidgets.QFormLayout)
    #print ("layouts",layouts)
    labels =[]
    for layout in layouts:
      #Loop through each layout and get the label on column 0.
      for i in range(0,layout.rowCount()):
        foundLabel = layout.itemAt(i,QtWidgets.QFormLayout.LabelRole).widget()
        #print ("label",foundLabel)
        labels.append(foundLabel)

        #Get the width.
        width = max( foundLabel.width(), width )
        #print ("w",foundLabel,foundLabel.text(),foundLabel.width(),width)

    for label in labels:
      label.setMinimumWidth( width )
    
    #dlg.hide()
    
    dlg.exec_()
    
    saveTimestamp("setup.closed")
    if dlg.result()!= QtWidgets.QDialog.Accepted:
      print(u"[[setup.cancelt]]")  
      return
    
    
    ###########################
    ##### Save settings #######
    ###########################
    doc.meta['mavinci.gpsType'] = cmbGPStype.currentText()
    doc.meta['mavinci.camAccuracy'] = str(spinAccuracy.value()/100.)
    
    doc.meta['mavinci.align.quality'] = qualitiesEN[qualities.index(cmbAlign.currentText())]
    doc.meta['mavinci.dense.enable'] ='1' if chkDense.isChecked() else '0'
    doc.meta['mavinci.dense.quality'] = qualitiesDenseEN[qualitiesDense.index(cmbDense.currentText())]
    
    if grpOrtho.isChecked() and grpDSM.isChecked():
        doc.meta['mavinci.ortho.enable'] ='1'
    else:
        doc.meta['mavinci.ortho.enable'] ='0'
    doc.meta['mavinci.ortho.gsd'] = str(spinOrthoGSD.value()/100.)
    doc.meta['mavinci.ortho.enableColCorrection'] ='1' if chkColCorr.isChecked() else '0'
    
    doc.meta['mavinci.dsm.enable'] ='1' if grpDSM.isChecked() else '0'
    doc.meta['mavinci.dsm.gsd'] = str(spinDsmGSD.value()/100.)
    
    
    doc.meta['mavinci.points.enable'] ='1' if grpPoints.isChecked() else '0'
    
    doc.meta['mavinci.model.enable'] ='1' if grpModel.isChecked() else '0'
    doc.meta['mavinci.model.gsd'] = str(spinModelGSD.value()/100.)
    doc.meta['mavinci.model.tileSize'] = cmbModelTileSize.currentText()
    
    
    dlg.close()
    
    print(u"[[setup.ready]]")
    saveTimestamp("setup.saved")
    if dlg.process==1 :
        return process()
    return True
    
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return True
    
    
    
def estimateImageQuality():
  try:
    PhotoScan.app.console.clear()
    print("Intel Plugin: %s" % exportHeader)
    saveTimestamp("estimateImageQuality.start")
    print(u"[[estimateImageQuality.start]] ")
    ts = time.time()
    doc = PhotoScan.app.document
    chunk = doc.chunk
    if doc ==None or chunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False

    if chunk.cameras[-1].meta.__contains__("Image/Quality") or chunk.cameras[-1].photo.meta.__contains__("Image/Quality"):
        print(u"[[estimateImageQuality.available]] ")
    else:
        testOp(chunk.estimateImageQuality(),u"[[estimateImageQuality]]")

    errors = []
    for p in chunk.cameras:
        if p.photo.meta["Image/Quality"] == None:
            q = float(p.meta["Image/Quality"])
        else:
	        q = float(p.photo.meta["Image/Quality"])
        errors.append(q)
        print (u"%s -> %f%%" % (p.label,q*100))

    average = sum(errors) / len(errors)
    stdDeviation = math.sqrt( sum((average - value) ** 2 for value in errors) / len(errors) )

    print(u"average:      %f" % average)
    print(u"stdDeviation: %f" % stdDeviation)
    
    
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    txt = u"[[estimateImageQuality.ready]]" % (t,average*100,stdDeviation*100)
    print(txt)
    saveTimestamp("estimateImageQuality.end")
    PhotoScan.app.messageBox(txt)
    
    return True
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
  finally:
    storeLog()


def addJob1():
  addJob(1)
  
def addJob2():
  addJob(2)

def addJob(jobType = 0):
  global allJobs
  global allJobTypes
  doc = PhotoScan.app.document
  path = doc.path
  if path=='':
    PhotoScan.app.messageBox(u"[[jobs.saveFirst]]" )
    #print("jobs:",allJobs)
    return
    
  if path in allJobs:
    PhotoScan.app.messageBox(u"[[jobs.allreadyInList]]" % path)
    return
  
  allJobs.append(path)
  allJobTypes.append(jobType)
  PhotoScan.app.messageBox(u"[[jobs.added]]" % path)
  #print("jobs:",allJobs)



class JobsListDialog(QtWidgets.QDialog):
  def __init__(self, parent=None):
    super(JobsListDialog, self).__init__(parent)
      
  def fillList(self):
    jobType=[u"[[jobs.types.withoutGCP]]", u"[[jobs.types.withGCP1]]",u"[[jobs.types.withGCP2]]"]
    global allJobs
    global allJobTypes
    for i in range(len(allJobs)):
      str = jobType[allJobTypes[i]] + ": " +allJobs[i]
      #print(str)
      self.lst.addItem(str)
    
  
  def doClear(self):
    global allJobs
    global allJobTypes
    allJobs = []
    allJobTypes = []
    self.lst.clear()
    self.fillList()
    if len(allJobs)==0:
        self.btnStart.setEnabled(False)
        self.btnRemove.setEnabled(False)
        self.btnClear.setEnabled(False)
        
  def doRemove(self):
    global allJobs
    global allJobTypes
    
    listItems=self.lst.selectedItems()
    if not listItems: return        
    for item in listItems:
        allJobs.pop(self.lst.row(item))
        allJobTypes.pop(self.lst.row(item))

    self.lst.clear()
    self.fillList()
    if len(allJobs)==0:
        self.btnStart.setEnabled(False)
        self.btnRemove.setEnabled(False)
        self.btnClear.setEnabled(False)
        
  def doRunJobs(self):
    global allJobs
    global allJobTypes
    runJobs()
    self.lst.clear()
    self.fillList()
    if len(allJobs)==0:
        self.close()

def showJobs():
  try:
    dlg = JobsListDialog(QtWidgets.QApplication.instance().activeWindow())
    mainLayout = QtWidgets.QVBoxLayout()
    dlg.setLayout(mainLayout)
    dlg.setWindowTitle(u"[[jobs.dlg.title]]")
    lst = QtWidgets.QListWidget(dlg)
    dlg.lst = lst
    dlg.fillList()
    mainLayout.addWidget(lst)
    
    
    horizontalGroupBox = QtWidgets.QGroupBox();
    horizontalGroupBox.setStyleSheet("QGroupBox{border:0;}");
    layout = QtWidgets.QHBoxLayout();
    horizontalGroupBox.setLayout(layout)
    mainLayout.addWidget(horizontalGroupBox)
      
      
    btnOK = QtWidgets.QPushButton(u"[[jobs.dlg.ok]]")
    btnOK.setDefault(True)
    layout.addWidget(btnOK)
    QtCore.QObject.connect(btnOK, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('accept()'))
    
    btnClear = QtWidgets.QPushButton(u"[[jobs.dlg.clear]]")
    layout.addWidget(btnClear)
    dlg.btnClear=btnClear
    QtCore.QObject.connect(btnClear, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('doClear()'))

    btnRemove = QtWidgets.QPushButton(u"[[jobs.dlg.remove]]")
    layout.addWidget(btnRemove)
    dlg.btnRemove=btnRemove
    QtCore.QObject.connect(btnRemove, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('doRemove()'))
    
    btnStart = QtWidgets.QPushButton(u"[[jobs.dlg.start]]")
    layout.addWidget(btnStart)
    dlg.btnStart=btnStart
    if len(allJobs)==0:
        btnStart.setEnabled(False)
        btnRemove.setEnabled(False)
        btnClear.setEnabled(False)
    QtCore.QObject.connect(btnStart, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('doRunJobs()'))
    
    dlg.exec_()
    dlg.close()
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False
  
  
def runJobs():
    
  try:
    global allJobs
    global allJobTypes
    
    if len(allJobs)==0:
      return False
    
    msgBox = QtWidgets.QMessageBox(QtWidgets.QApplication.instance().activeWindow())
    msgBox.setText(u"[[jobs.startNow]]")
    msgBox.setStandardButtons(QtWidgets.QMessageBox.Ok | QtWidgets.QMessageBox.Cancel)
    msgBox.setDefaultButton(QtWidgets.QMessageBox.Ok)
    ret = msgBox.exec_()
    msgBox.close()
    if ret != QtWidgets.QMessageBox.Ok:
      return False
  
    if not checkSetup():
      return False
  
    ts = time.time()
    #saveTimestamp("batch.start")
  
    doc = PhotoScan.app.document
    path = doc.path
    print(path)
    if path!='':
      doc.save()
    allJobs2 = list(allJobs)
    allJobTypes2 = list(allJobTypes)
    for i in range(len(allJobs2)):
      path = allJobs2[i]
      jobType = allJobTypes2[i]
      try:
        PhotoScan.app.document.open(path)
        doc = PhotoScan.app.document
        print("-----------------")
        print(u"loaded: %s" % doc.path)
        ok=False
        if jobType==0:
          ok = process(False)
        elif jobType==1:
          ok = process1(False)
        elif jobType==2:
          ok = process2(False)
        if not ok:
          return False
        #remove job from list
        allJobs.pop(0)
        allJobTypes.pop(0)
        print(u"executed: %s" % doc.path)
        print("-----------------")
        
      except (KeyboardInterrupt, SystemExit):
        print('>>> INTERRUPTED BY USER <<<')
        return False
      except:
        e = sys.exc_info()[0]
        print(e)
        print('>>> traceback <<<')
        traceback.print_exc()
        print('>>> end of traceback <<<')
        #PhotoScan.app.messageBox(u"[[error]]" )
      
    #saveTimestamp("batch.end")
    #doc.save()
    #clear list of jobs
    allJobs = []
    allJobTypes = []
    
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"[[batch.ready]]" % t)
    PhotoScan.app.messageBox(u"[[batch.ready]]" % t)
    return True
  
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
  

def scanDirJobs(jobType = 0):
  try:
    folder = PhotoScan.app.getExistingDirectory("[[batch.folderScan]]");
    if not folder:
        return False
    for root, subdirs, files in os.walk(folder):
        for name in files:
            if name.endswith(".psz") or name.endswith(".psx"):
                file = os.path.join(root, name)
                print("found psz: %s" % file)
                allJobs.append(file)
                allJobTypes.append(jobType)
                
    showJobs()
    return True
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
    return False
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    return False

def scanDirJobs1():
    return scanDirJobs(1)

def scanDirJobs2():
    return scanDirJobs(2)

def saveTimestamp(name):
    ts = time.time()
    print("timestamp: %s -> %f" % (name,ts))
    doc = PhotoScan.app.document
    if doc ==None:
        return
    chunk = doc.chunk
    if chunk == None:
        return
    doc.meta['mavinci.ts.%s' % name] = "%f" % ts
    
isInit = False
isCompatible = False

def mavinciInit():
    global devices
    global allJobs
    global allJobTypes
    
    try:
        locale.setlocale(locale.LC_ALL, 'en_US.UTF-8')
        print('locale set to en_US.UTF-8')
    except:
        locale.setlocale(locale.LC_ALL, '')
        print('locale set to default')

    allJobs=[]
    allJobTypes=[]
    
    print("Intel Plugin: %s" % exportHeader)
    #----------Remaining Init code-------------------
    print("PhotoscanVersion: %s"  % (PhotoScan.app.version))
    if PhotoScan.app.version.startswith('0.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False
    elif PhotoScan.app.version.startswith('1.0.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False
    elif PhotoScan.app.version.startswith('1.1.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False
    elif PhotoScan.app.version.startswith('1.2.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False    
    elif PhotoScan.app.version.startswith('1.3.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False
    else:
        devices = PhotoScan.app.enumGPUDevices()
        isCompatible = True
        print(u"[[readyInit]]")
    isInit = True
    
    return isCompatible



def mavinciMenueInit():
    if mavinciInit():
        label = u"[[mavinci]]/[[setup]]"
        PhotoScan.app.addMenuItem(label, setup,'CTRL+M')
        
        label = u"[[mavinci]]/[[processing]]"
        PhotoScan.app.addMenuItem(label, process,'SHIFT+CTRL+M')
        
        label = u"[[mavinci]]/[[processing1]]"
        PhotoScan.app.addMenuItem(label, process1)
        
        label = u"[[mavinci]]/[[processing2]]"
        PhotoScan.app.addMenuItem(label, process2)
        
        PhotoScan.app.addMenuSeparator(u"[[mavinci]]")
        
        #label = u"[[mavinci]]/[[openMAVinciDesktop]]"
        #PhotoScan.app.addMenuItem(label, openMAVinciDesktop)
        
        label = u"[[mavinci]]/[[estimateImageQuality]]"
        PhotoScan.app.addMenuItem(label, estimateImageQuality)
        
        label = u"[[mavinci]]/[[optimize]]"
        PhotoScan.app.addMenuItem(label, mavinciOptimize)
        
        PhotoScan.app.addMenuSeparator(u"[[mavinci]]")
        
        label = u"[[mavinci]]/[[addJob]]/[[noGCPs]]"
        PhotoScan.app.addMenuItem(label, addJob)
        label = u"[[mavinci]]/[[addJob]]/[[withGCPsStep1]]"
        PhotoScan.app.addMenuItem(label, addJob1)
        label = u"[[mavinci]]/[[addJob]]/[[withGCPsStep2]]"
        PhotoScan.app.addMenuItem(label, addJob2)
        
        label = u"[[mavinci]]/[[addJobScan]]/[[noGCPs]]"
        PhotoScan.app.addMenuItem(label, scanDirJobs)
        label = u"[[mavinci]]/[[addJobScan]]/[[withGCPsStep1]]"
        PhotoScan.app.addMenuItem(label, scanDirJobs1)
        label = u"[[mavinci]]/[[addJobScan]]/[[withGCPsStep2]]"
        PhotoScan.app.addMenuItem(label, scanDirJobs2)
        
        
        label = u"[[mavinci]]/[[showJobs]]"
        PhotoScan.app.addMenuItem(label, showJobs)
        
        label = u"[[mavinci]]/[[runJobs]]"
        PhotoScan.app.addMenuItem(label, runJobs)
