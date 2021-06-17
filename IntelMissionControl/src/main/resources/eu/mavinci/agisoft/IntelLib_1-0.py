#
# * Copyright (c) 2020 Intel Corporation
# *
# * SPDX-License-Identifier: GPL-3.0-or-later

# MAVinci -> Photoscan Processing Script 
import os
import os.path
import sys
import math
import datetime
import PhotoScan
import traceback
from PySide import QtGui, QtCore
import subprocess
import time
import multiprocessing


mavinciRelease = u"{{mavinci.release}}"
exportHeader = u"{{mavinci.exportHeader}}"
    
global qualities
qualities = ['high','medium','low']

global gpsTypes
gpsTypes = ['GPS','DGPS','DGPS_RTK']

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
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    

physCPU = 0

def getLines(args):
	out=[]
	for l in subprocess.Popen(args,stdout=subprocess.PIPE).communicate()[0].splitlines():
		s = l.decode('ascii').strip()
		if len(l) == 0:
			continue
		out.append(s)
	return out

def physicalCPUs():
    global physCPU
    if physCPU>0:
        return physCPU
    
    try:
        cpu_info = dict()
        for o in getLines('lscpu'):
#            print(o)
            l = o.split(":")
            if len(l) != 2:
                continue
            cpu_info[l[0].strip()] = l[1].strip(' .kB')
        sockets = int(cpu_info['Socket(s)'])
        cores_per_socket = int(cpu_info['Core(s) per socket'])
        physCPU = sockets * cores_per_socket 
        if physCPU > 0:
            return physCPU
    except:
#        traceback.print_exc()
        pass
    print(u"no lscpu avaliable")
    
    #for new enought windows PCs... XP should work,but not on my sample...
    try:    
        cpu_info = dict()
        physCPU = 0;
        for o in getLines('WMIC CPU Get NumberOfCores,NumberOfLogicalProcessors /Format:List'):
#            print(o)
            l = o.split("=")
            if len(l) != 2:
                continue
            cpu_info[l[0].strip()] = l[1].strip()
            if l[0]=='NumberOfCores':
                physCPU += int(l[1].strip())
        #return int(cpu_info['NumberOfCores'])
        if physCPU > 0:
            return physCPU
    except:
#        traceback.print_exc()
        pass
    print(u"no WMIC avaliable")
        
    if hasattr(os, 'sysconf'):
        if 'SC_NPROCESSORS_ONLN' in os.sysconf_names:
            #Linux and Unix
            ncpus = os.sysconf('SC_NPROCESSORS_ONLN')
            if isinstance(ncpus, int) and ncpus > 0:
                physCPU = ncpus
                return physCPU
    
    print(u"SC_NPROCESSORS_ONLN not found in sysconfig")
    
    #MacOS X
    try:
        physCPU =int(subprocess.Popen('sysctl -n hw.ncpu',stdout=subprocess.PIPE).stdout.read().decode('ascii').strip())
        if physCPU > 0: 
            return physCPU
    except:
#        traceback.print_exc()
        pass
    print(u"sysctl -n hw.ncpu not working")
     
    #for Windows
    if 'NUMBER_OF_PROCESSORS' in os.environ:
        physCPU = int(os.environ['NUMBER_OF_PROCESSORS']) #I think this is giving logical CPUs only....
        if physCPU > 0:
            return physCPU
    print(u"NUMBER_OF_PROCESSORS not in environment")
    
    #return the default value
    physCPU = multiprocessing.cpu_count() 
    return physCPU


def checkSetup():
  try:
    saveTimestamp("checkSetup.start")
    print(u"[[processing.checkOpenCL]]")
    settings = QtCore.QSettings()
    
    
    logicalCPU = multiprocessing.cpu_count()
    physicalCPU = physicalCPUs()
    
    print(u"CPUs detected logical:%i  physical:%i" %(logicalCPU,physicalCPU))
    
    htFactor = int(logicalCPU / physicalCPU)
    
    mask = 0
    inactive = 0
    i = 1
    for device in devices:
      vendor = device['vendor'].lower() 
      vendor = vendor.replace('corporation','')#eg. intel corperation was matched as ATI
      if 'nvidia' in vendor or 'ati' in vendor or 'amd' in vendor or 'advanced micro devices' in vendor:
        mask += i
        inactive +=1
      i *= 2
      
    inactive *= htFactor
    curMask = int(settings.value('main/opencl_mask',0))
    curInactive = int(settings.value('main/opencl_cores_inactive',0))
    if curMask != mask or inactive != curInactive:
      app = QtGui.QApplication.instance()
      reply = QtGui.QMessageBox.question(app.activeWindow(), 'QMessageBox.question()',u"[[processing.askFixOpenCL]]",QtGui.QMessageBox.Yes | QtGui.QMessageBox.No | QtGui.QMessageBox.Cancel)
      if reply == QtGui.QMessageBox.Yes:
        settings.setValue('main/opencl_mask',str(mask))
        settings.setValue('main/opencl_cores_inactive',str(inactive))
        saveTimestamp("checkSetup.fixed")
        print(u"[[processing.fixOpenCL]]")
      elif reply == QtGui.QMessageBox.No:
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

def process():
  ts = time.time()
  saveTimestamp("process.start")
  if not process1(showReady=False):
      return False
  
  if not process2(showReady=False):
      return False

  saveTimestamp("process.end")
  t = str(datetime.timedelta(seconds=round(time.time() - ts)))
  print(u"[[processing.ready]]" % t)
  PhotoScan.app.messageBox(u"[[processing.ready]]" % t)
  
  return True

def testOp(succeed,name):
  if not succeed:
      str = u"[[OperationFailed]]" % name
      PhotoScan.app.messageBox(str)
      raise Exception(str)  


def process1(showReady=True):
  try:
    saveTimestamp("process1.1start")
    if not checkSetup():
      return False
      
    saveTimestamp("process1.2")
    
    print(u"[[processing1.start]]")
    ts = time.time()
    doc = PhotoScan.app.document
    activeChunk = doc.activeChunk
    if doc ==None or activeChunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    doc.meta['mavinci.plugin.release'] = mavinciRelease
    doc.meta['mavinci.plugin.exportHeaderCore'] = exportHeader
    
    qual = doc.meta['mavinci.quality']
    resolution = float(doc.meta['mavinci.resolution'])
    accuracy = float(doc.meta['mavinci.camAccuracy'])
    print(u"[[gpsAccuracyAssumption]]" % (accuracy) )
    saveTimestamp("process1.3")
    activeChunk.ground_control.accuracy_cameras = accuracy
    if qual != 'low':
        saveTimestamp("process1.4")
        testOp(activeChunk.matchPhotos(accuracy=qual, preselection='ground control'),u"[[matchPhotos]]")
        saveTimestamp("process1.5")
        doc.save()
    else:
        saveTimestamp("process1.6")
        testOp(activeChunk.matchPhotos(accuracy=qual, preselection='ground control',point_limit=20000),u"[[matchPhotos]]")
    saveTimestamp("process1.7")
    testOp(activeChunk.alignPhotos(),u"[[alignPhotos]]")
    saveTimestamp("process1.8")
    doc.save()
 
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

def mavinciOptimize(showReady=True):
  try:
    ts = time.time()
    saveTimestamp("mavinciOptimize.01")
    settings = QtCore.QSettings()
    print(u"[[mavinciOptimize.start]]")
    saveTimestamp("mavinciOptimize.02")
    doc = PhotoScan.app.document
    activeChunk = doc.activeChunk
    if doc ==None or activeChunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    saveTimestamp("mavinciOptimize.03")
    qual = doc.meta['mavinci.quality']
    if doc.meta['mavinci.gpsType']=='DGPS_RTK' :
      print(u"[[processing.RTK.optimisation.start]]")
      saveTimestamp("mavinciOptimize.04") 
      gc = doc.activeChunk.ground_control
      gci = gc.locations.items()

      for i in range(len(gci)):
        [cam,gc] = gci[i]
        if type(cam) is PhotoScan.Camera:
                gc.enabled = True

      saveTimestamp("mavinciOptimize.05")
      acc=0.1
      doc.activeChunk.ground_control.accuracy_cameras = acc
      doc.activeChunk.optimizePhotos(fit_aspect=True, fit_skew=True, fit_p1p2=True,fit_k4=True)
      lastpercentage=0
      
      saveTimestamp("mavinciOptimize.06")
      while acc>=0.004:
           posenabled=0
           mean=0
           meancnt=0
           totalcnt=0
           # just count how many we would throw out
           for i in range(len(gci)):
                [cam,gc] = gci[i]
                if type(cam) is PhotoScan.Camera and type(gc) is PhotoScan.GroundControlLocation and type(cam.center) is PhotoScan.Vector:
                        worldsource = doc.activeChunk.projection.unproject(PhotoScan.Vector(gc.coord))
                        center = cam.center
                        center.size=4
                        center.w=1
                        cc = doc.activeChunk.transform * center
                        cc.size=3
                        worldc = cc
                        err = (worldc - worldsource).norm() # distance in m
                        totalcnt = totalcnt +1
                        if err <= 20*acc :
                                mean = mean + err
                                posenabled = posenabled + 1
           percentage = posenabled/totalcnt
           s = 'mean error was ' +repr(mean/posenabled)+', '+repr(posenabled)+' of '+repr(totalcnt)+' are within '+repr(20*acc)+'m ('+repr(percentage)+')'
           print(s)
           if percentage<0.6:
                print('Stop optimization at '+repr(doc.activeChunk.ground_control.accuracy_cameras)+'m camera accuracy, since less then 60% would be used.')
                break
           if percentage<0.90*lastpercentage:
                print('Stop optimization at '+repr(doc.activeChunk.ground_control.accuracy_cameras)+'m camera accuracy, since removed amount dropped more then 10%.')
                break
           lastpercentage = percentage
           # now really do it
           for i in range(len(gci)):
                [cam,gc] = gci[i]
                if type(cam) is PhotoScan.Camera and type(gc) is PhotoScan.GroundControlLocation and type(cam.center) is PhotoScan.Vector:
                        worldsource = doc.activeChunk.projection.unproject(PhotoScan.Vector(gc.coord))
                        center = cam.center
                        center.size=4
                        center.w=1
                        cc = doc.activeChunk.transform * center
                        cc.size=3
                        worldc = cc
                        err = (worldc - worldsource).norm() # distance in m
                        totalcnt = totalcnt +1
                        if err > 20*acc :
                                gc.enabled = False
                        else:
                                gc.enabled = True
           doc.activeChunk.ground_control.accuracy_cameras = acc
           testOp(doc.activeChunk.optimizePhotos(fit_aspect=True, fit_skew=True, fit_p1p2=True,fit_k4=True),u"[[optimizePhotos]]")
           acc = acc - 0.005
           
      saveTimestamp("mavinciOptimize.07")

    elif qual != 'low':   #non RTK GPS used
        saveTimestamp("mavinciOptimize.08")
        testOp(activeChunk.optimizePhotos(fit_aspect=True, fit_skew=True, fit_p1p2=True,fit_k4=True),u"[[optimizePhotos]]")
        saveTimestamp("mavinciOptimize.09")
        testOp(activeChunk.optimizePhotos(fit_aspect=True, fit_skew=True, fit_p1p2=True,fit_k4=True),u"[[optimizePhotos]]")
    
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

def process2(showReady=True):
  try:
    saveTimestamp("process2.01")
    ts = time.time()
    settings = QtCore.QSettings()
    print(u"[[processing2.start]]")
    saveTimestamp("process2.02")
    doc = PhotoScan.app.document
    activeChunk = doc.activeChunk
    if doc ==None or activeChunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
    
    
    doc.meta['mavinci.plugin.release'] = mavinciRelease
    doc.meta['mavinci.plugin.exportHeaderCore'] = exportHeader
    
    qual = doc.meta['mavinci.quality']
    resolution = float(doc.meta['mavinci.resolution'])
    accuracy = float(doc.meta['mavinci.camAccuracy'])
    print(u"[[gpsAccuracyAssumption]]" % (accuracy) )
    
    mavinciOptimize(False)
   
    saveTimestamp("process2.10")
    if qual !='low':
        doc.save()
        saveTimestamp("process2.11")
        testOp(activeChunk.buildDenseCloud(quality=qual,gpu_mask=int(settings.value('main/opencl_mask',0)), cpu_cores_inactive=int(settings.value('main/opencl_cores_inactive',0))),u"[[buildDenseCloud]]")
        saveTimestamp("process2.12")
        #activeChunk.buildDepth(quality=qual,gpu_mask=1, cpu_cores_inactive=1)
        #activeChunk.buildModel(object='height field', geometry='sharp', faces=0)
        testOp(doc.activeChunk.buildModel(surface='height field', source='dense',interpolation='enabled', faces=qual),u"[[buildModel]]")
        saveTimestamp("process2.13")
        doc.save()
        saveTimestamp("process2.14")
        testOp(doc.activeChunk.buildTexture(mapping='orthophoto', blending='mosaic',size=4096),u"[[buildTexture]]")
        saveTimestamp("process2.15")
        doc.save()
        saveTimestamp("process2.16")
    else:
        testOp(doc.activeChunk.buildModel(surface='height field', source='sparse',interpolation='enabled', faces=qual),u"[[buildModel]]")
        saveTimestamp("process2.18")
        testOp(doc.activeChunk.buildTexture(mapping='orthophoto', blending='mosaic',size=2048),u"[[buildTexture]]")
        saveTimestamp("process2.19")
        

    saveTimestamp("process2.20")
    crs = PhotoScan.CoordinateSystem()
    projection = PhotoScan.GeoProjection()  #chunk projection in WGS84  
    crs.init(doc.meta['mavinci.wkt'])
    projection.init(doc.meta['mavinci.wkt'])
    
    folder = os.path.abspath(os.path.join(doc.path, os.pardir))


    #we take coordinates of first photo (you can add a check that it's aligned),
    #then take small horizontal vectors along OX and OY
    #and calculate their 
    
    orgProjection = doc.activeChunk.ground_control.projection 
    doc.activeChunk.ground_control.projection = projection
    doc.activeChunk.ground_control.apply()
    
    crd = doc.activeChunk.ground_control.locations.items()[0][1].coord  #coordinates of first photo
    
    #longitude
    v1 = PhotoScan.Vector( (crd[0],crd[1],0) )
    v2 = PhotoScan.Vector( (crd[0]+0.01,crd[1],0) )
    
    vm1 = doc.activeChunk.projection.unproject(v1)
    vm2 = doc.activeChunk.projection.unproject(v2)
    
    res_x = (vm1 - vm2).norm() * 100
    
    #latitude
    v2 = PhotoScan.Vector( (crd[0],crd[1]+0.01,0) )
    vm2 = doc.activeChunk.projection.unproject(v2)
    
    
    doc.activeChunk.ground_control.projection = orgProjection
    doc.activeChunk.ground_control.apply()
    
    res_y = (vm1 - vm2).norm() * 100
        
    pixel_x = pixel_y = resolution  #export resolution 50cm/pix
    d_x = pixel_x / res_x  
    d_y = pixel_y / res_y
    
    print(u"d_x=%e  d_y=%e"%(d_x,d_y))
    
    
    vertices = activeChunk.model.vertices
    maxSamples = 100000
    step = 1
    if len(vertices)>maxSamples*step:
        step = int(len(vertices)/maxSamples)
    xmin = float('inf')
    xmax = -1*float('inf')
    ymin = float('inf')
    ymax = -1*float('inf')

    for i in range (0, len(vertices), step):
        v = vertices[i].coord
        v.size = 4
        v.w = 1
        v_t = activeChunk.transform * v
        v_t.size = 3
    
        v_new = activeChunk.crs.project(v_t)
        if v_new.x < xmin:
            xmin = v_new.x
        
        if v_new.x > xmax:
            xmax = v_new.x
        
        if v_new.y < ymin:
            ymin = v_new.y
        
        if v_new.y > ymax:
            ymax = v_new.y    
    
    print("x=%f < %f  y=%f < %f" % (xmin, xmax, ymin,ymax))
    
    v0 = PhotoScan.Vector( (xmin,ymin,0) )
    vX = PhotoScan.Vector( (xmax,ymin,0) )
    vY = PhotoScan.Vector( (xmin,ymax,0) )
    
    vm0 = doc.activeChunk.projection.unproject(v0)
    vmX = doc.activeChunk.projection.unproject(vX)
    vmY = doc.activeChunk.projection.unproject(vY)
    
    size_x = (vm0 - vmX).norm()
    size_y = (vm0 - vmY).norm()
    
    print("x-size=%f m  y-size=%f m" % (size_x, size_y))

    maxPix = 30000
    maxPixDem = maxPix
    blockOffset = 0.2
    
    
    pixX = math.ceil(size_x / resolution)
    pixY = math.ceil(size_y / resolution)
    pixXdem = math.ceil(size_x / (2*resolution))
    pixYdem = math.ceil(size_y / (2*resolution))



    print("x-pix=%i  y-pix=%i" % (pixX, pixY))
    print("x-pixDEM=%i  y-pixDEM=%i" % (pixXdem, pixYdem))
    
    blockCountX = math.ceil(pixX  / maxPix + blockOffset)
    blockCountY = math.ceil(pixY  / maxPix + blockOffset)
    
    blockCountXdem = math.ceil(pixXdem  / maxPixDem + blockOffset)
    blockCountYdem = math.ceil(pixYdem  / maxPixDem + blockOffset)
    
    print("blockCount x=%i  y=%i" % (blockCountX, blockCountY))
    print("blockCountDEM x=%i  y=%i" % (blockCountXdem, blockCountYdem))
    
    doc.meta['mavinci.blockCount.ortho'] = "%i:%i" % (blockCountX, blockCountY)
    doc.meta['mavinci.blockCount.dem'] = "%i:%i" % (blockCountXdem, blockCountYdem)
    
    blockw= math.ceil(pixX/blockCountX)
    blockh= math.ceil(pixY/blockCountY)
    
    blockwDEM= math.ceil(pixXdem/blockCountXdem)
    blockhDEM= math.ceil(pixYdem/blockCountYdem)
    
    print("block w=%i  h=%i" % (blockw, blockh))
    print("blockDEM w=%i  h=%i" % (blockwDEM, blockhDEM))
    
    reportPath = os.path.join(folder,'report.pdf')
    saveTimestamp("process2.21")
    testOp(activeChunk.exportReport(reportPath),u"[[exportReport]]")
    saveTimestamp("process2.22")
    openFile(reportPath)

    saveTimestamp("process2.23")
    
    if blockCountX > 1 or blockCountY > 1: 
        testOp(activeChunk.exportOrthophoto(os.path.join(folder,'ortho.tif'), format='tif', blending='mosaic',color_correction=doc.meta['mavinci.enableColCorrection']!='0', projection=crs, dx=d_x, dy=d_y, write_kml=False, write_world=True, blockw=blockw,blockh=blockh),u"[[exportOrthophoto]]")
    else:
        testOp(activeChunk.exportOrthophoto(os.path.join(folder,'ortho.tif'), format='tif', blending='mosaic',color_correction=doc.meta['mavinci.enableColCorrection']!='0', projection=crs, dx=d_x, dy=d_y, write_kml=False, write_world=True),u"[[exportOrthophoto]]")
        
    saveTimestamp("process2.24")
    if blockCountXdem > 1 or blockCountYdem > 1: 
        testOp(activeChunk.exportDem(os.path.join(folder,'dem.tif'), format='tif', projection=crs, dx=2*d_x, dy=2*d_y, write_world=True, blockw=blockwDEM,blockh=blockhDEM),u"[[exportDem]]")
    else:
        testOp(activeChunk.exportDem(os.path.join(folder,'dem.tif'), format='tif', projection=crs, dx=2*d_x, dy=2*d_y, write_world=True),u"[[exportDem]]")
        
    saveTimestamp("process2.25")
    doc.save()
    saveTimestamp("process2.26")
    openFile(folder)
    
    saveTimestamp("process2.27")    
    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"[[processing2.ready]]" % t)
    if showReady:
        PhotoScan.app.messageBox(u"[[processing2.ready]]" % t)
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



class SetupDialog(QtGui.QDialog):
    def __init__(self, parent=None):
        super(SetupDialog, self).__init__(parent)
        self.process=0
    
    def doProcess(self):
        self.process=1
        self.accept()
        
    def gpsTypeChanged(self):
        self.spinAccuracy.setEnabled(self.cmbGPStype.currentText()!='DGPS_RTK')
        self.lblAccuracy.setEnabled(self.cmbGPStype.currentText()!='DGPS_RTK')
    

def setup():
  try:
    print(u"[[setup.start]]")
    saveTimestamp("setup.start")
    doc = PhotoScan.app.document
    activeChunk = doc.activeChunk
    if doc ==None or activeChunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return
  
    
    app = QtGui.QApplication.instance()
    parent = app.activeWindow()
    dlg  = SetupDialog(parent)
    dlg.setWindowTitle(u"[[setup.title]]")
    dlg.resize(280, 360)
    
    
    lblProcess = QtGui.QLabel(u"[[setup.gsd]]", dlg)
    lblProcess.move(10,10)
    
    spinResolution = QtGui.QDoubleSpinBox(dlg);
    spinResolution.setRange(.1,100)
    spinResolution.setValue(float(doc.meta['mavinci.resolution'])*100)
    spinResolution.setSingleStep(0.1)
    spinResolution.setDecimals(1)
    spinResolution.move(140,10)
    spinResolution.setSuffix(u" cm")
    spinResolution.setAlignment(QtCore.Qt.AlignRight)
    spinResolution.setFixedWidth(130)
    
    lblProjection = QtGui.QLabel(u"[[setup.projection]]", dlg)
    lblProjection.move(10,50)
    
    lblProjection2 = QtGui.QLabel(doc.meta['mavinci.wktName'], dlg)
    lblProjection2.move(10,80)
        
    lblQuality = QtGui.QLabel(u"[[setup.quality]]", dlg)
    lblQuality.move(10,120)
    
    cmbQuality = QtGui.QComboBox(dlg)
    cmbQuality.addItems(qualities)
    index = cmbQuality.findText(doc.meta['mavinci.quality']);
    if index != -1 :
      cmbQuality.setCurrentIndex(index)
    cmbQuality.move(140,120)
    cmbQuality.setFixedWidth(130)
    
    
    lblAccuracy = QtGui.QLabel(u"[[setup.accuracy]]", dlg)
    lblAccuracy.move(10,160)
    dlg.lblAccuracy=lblAccuracy
    
    spinAccuracy = QtGui.QDoubleSpinBox(dlg);
    spinAccuracy.setEnabled(doc.meta['mavinci.gpsType']!='DGPS_RTK')
    lblAccuracy.setEnabled(doc.meta['mavinci.gpsType']!='DGPS_RTK')
    spinAccuracy.setRange(.1,2500)
    spinAccuracy.setValue(float(doc.meta['mavinci.camAccuracy'])*100)
    spinAccuracy.setSingleStep(10)
    spinAccuracy.setSuffix(u" cm")
    spinAccuracy.setDecimals(1)
    spinAccuracy.move(140,160)
    spinAccuracy.setAlignment(QtCore.Qt.AlignRight)
    spinAccuracy.setFixedWidth(130)
    dlg.spinAccuracy = spinAccuracy 
    
    
    
    
    lblGPStype = QtGui.QLabel(u"[[setup.GPStype]]", dlg)
    lblGPStype.move(10,200)
    
    cmbGPStype = QtGui.QComboBox(dlg)
    cmbGPStype.addItems(gpsTypes)
    index = cmbGPStype.findText(doc.meta['mavinci.gpsType']);
    if index != -1 :
      cmbGPStype.setCurrentIndex(index)
    cmbGPStype.move(140,200)
    cmbGPStype.setFixedWidth(130)
    dlg.cmbGPStype = cmbGPStype 
    QtCore.QObject.connect( cmbGPStype, QtCore.SIGNAL('currentIndexChanged(int)'), dlg, QtCore.SLOT('gpsTypeChanged()') );
    
    
    chkColCorr = QtGui.QCheckBox(u"[[setup.colorCorrection]]", dlg)
    chkColCorr.setChecked(doc.meta['mavinci.enableColCorrection']!='0')
    chkColCorr.move(130,240)
    chkColCorr.setFixedWidth(140)
    
    
    
    btnCancel = QtGui.QPushButton(u"[[setup.btnCancel]]",dlg)
    btnCancel.move(10,320)
    btnCancel.setFixedWidth(120)
    QtCore.QObject.connect(btnCancel, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('reject()'))
    
    btnSave = QtGui.QPushButton(u"[[setup.btnSave]]",dlg)
    btnSave.move(150,320)
    btnSave.setFixedWidth(120)
    QtCore.QObject.connect(btnSave, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('accept()'))
    
    btnProcess = QtGui.QPushButton(u"[[setup.btnProcess]]",dlg)
    btnProcess.move(10,280)
    btnProcess.setFixedWidth(220)
    QtCore.QObject.connect(btnProcess, QtCore.SIGNAL('clicked()'), dlg, QtCore.SLOT('doProcess()'))
    
    dlg.exec_()
    
    saveTimestamp("setup.closed")
    if dlg.result()!= QtGui.QDialog.Accepted:
      print(u"[[setup.cancelt]]")  
      return
    
    
    
    doc.meta['mavinci.resolution'] = str(spinResolution.value()/100.)
    doc.meta['mavinci.quality'] = cmbQuality.currentText()
    doc.meta['mavinci.camAccuracy'] = str(spinAccuracy.value()/100.)
    doc.meta['mavinci.gpsType'] = cmbGPStype.currentText()
    if chkColCorr.isChecked():
      doc.meta['mavinci.enableColCorrection'] ='1'
    else:
      doc.meta['mavinci.enableColCorrection'] ='0'
    
    dlg.close()
    
    print(u"[[setup.ready]]")
    saveTimestamp("setup.saved")
    if dlg.process==1 :
        process()
    
  except (KeyboardInterrupt, SystemExit):
    print('>>> INTERRUPTED BY USER <<<')
  except:
    e = sys.exc_info()[0]
    print(e)
    print('>>> traceback <<<')
    traceback.print_exc()
    print('>>> end of traceback <<<')
    PhotoScan.app.messageBox(u"[[error]]" )
    
    
    
def estimateImageQuality():
  try:
    saveTimestamp("estimateImageQuality.start")
    print(u"[[estimateImageQuality.start]]")
    ts = time.time()
    doc = PhotoScan.app.document
    activeChunk = doc.activeChunk
    if doc ==None or activeChunk == None:
      PhotoScan.app.messageBox(u"[[error.noDocChunk]]" )
      return False
  
    if not activeChunk.cameras[-1].frames[0].meta.__contains__("Image/Quality"):
      testOp(activeChunk.estimateImageQuality(),u"[[estimateImageQuality]]")
  
    errors = []
    for p in activeChunk.cameras:
        q = float(p.frames[0].meta["Image/Quality"])
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


def saveTimestamp(name):
    ts = time.time()
    print("timestamp: %s -> %f" % (name,ts))
    doc = PhotoScan.app.document
    if doc ==None:
        return
    activeChunk = doc.activeChunk
    if activeChunk == None:
        return
    doc.meta['mavinci.ts.%s' % name] = "%f" % ts
    
isInit = False
isCompatible = False

def mavinciInit():
    global devices
    
    print("Intel Plugin: %s" % exportHeader)
    #----------Remaining Init code-------------------
    print("PhotoscanVersion: %s"  % (PhotoScan.app.version))
    if PhotoScan.app.version.startswith('0.'):
        PhotoScan.app.messageBox(u"[[notCompatible]]" % PhotoScan.app.version )
        isCompatible = False
    else:
        devices = PhotoScan.app.enumOpenCLDevices()
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
        
        #label = u"[[mavinci]]/[[openMAVinciDesktop]]"
        #PhotoScan.app.addMenuItem(label, openMAVinciDesktop)
        
        label = u"[[mavinci]]/[[estimateImageQuality]]"
        PhotoScan.app.addMenuItem(label, estimateImageQuality)
        
        label = u"[[mavinci]]/[[optimize]]"
        PhotoScan.app.addMenuItem(label, mavinciOptimize)
        
        
