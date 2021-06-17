#
# * Copyright (c) 2020 Intel Corporation
# *
# * SPDX-License-Identifier: GPL-3.0-or-later

import os
import os.path
import sys
import math
import datetime
import PhotoScan
from PySide2 import QtGui, QtCore, QtWidgets
import time
import locale



def scanDirJobs(jobType = 0):
  global allJobs
  global allJobTypes
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


def meansq(l):

    s = 0
    for e in l:
        s += e*e

    s /= len(l)

    return math.sqrt(s)

def median(l):


    l.sort()


    if len(l) % 2 == 0:

        return (l[int(len(l) / 2 - 1)] + l[int(len(l) / 2 )]) /  2
    else:
        return l[int(len(l) // 2)]


def offsetsTest(document):


    chunk = document.chunk

    a_set=[1, 1.25, 1.5, 2, 2.5, 3, 3.5, 4, 5, 6, 7, 9, 11, 13, 15]
    zaccscale=[0.5, 1, 1.5, 2]
    xyacc=0.03

    chunk.marker_projection_accuracy = 3

    x_list = []
    y_list = []
    z_list = []

    x_med = 0
    y_med = 0
    z_med = 0

    x_min = 100
    x_max = -100

    y_min = 100
    y_max = -100

    z_min = 100
    z_max = -100

    adj_r = 0
    adj_p = 0
    adj_y = 0

    f = 0

    meansq_x = 0
    meansq_y = 0
    meansq_z = 0

    pathCSV = document.path + "_offsets_test.csv"


    # accuracy
    # median x y z error min max
    # adj rpy
    #focal length

    with open(pathCSV, "w") as test:
        test.write("t.p.acc z.acc x.med y.med z.med x.min y.min z.min x.max y.max z.max adj.r adj.p adj.y f meansqx meansqy meansqz \n")

    #load markers
    path = os.path.dirname(PhotoScan.app.document.path)
    chunk.importMarkers(path + '/markers.xml')
    print ("Markers path: " + path + '/markers.xml')

    for scale in zaccscale :
        z_acc = xyacc * scale
        chunk.camera_location_accuracy = PhotoScan.Vector((float(xyacc),float(xyacc),float(z_acc)))
        for a in a_set :
            print(str(a) + ' ' + str(z_acc))
            #set parameters

            chunk.tiepoint_accuracy = a
            # uncheck markers
            #for m in chunk.markers:
                #m.reference.enabled = False
            chunk.remove(chunk.markers)

            #optimise
            chunk.optimizeCameras(fit_f=True, fit_cx=True, fit_cy=True, fit_b1=True, fit_b2=True, fit_k1=True, fit_k2=True,fit_k3=True,fit_k4=True,
            fit_p1=True, fit_p2=True,fit_p3=False, fit_p4=False)

            # check markers
            #for m in chunk.markers:
                #m.reference.enabled = True
            chunk.importMarkers(path + '/markers.xml')

            #export errors

            for m in chunk.markers:
                if len(m.projections.items()) > 2:
                    r = chunk.crs.unproject(m.reference.location)
                    e = chunk.transform.matrix.mulp(m.position)
                    l = chunk.crs.localframe(chunk.transform.matrix.mulp(m.position))
                    v = l.mulv(e - r)
                    print("Errors: " + str(v[0]) + " "+ str(v[1]) + " "+ str(v[2]) + " \n")
                    if(v[0] < x_min):
                        x_min = v[0]
                    if(v[0] > x_max):
                        x_max = v[0]
                    if(v[1] < y_min):
                        y_min = v[1]
                    if(v[1] > y_max):
                        y_max = v[1]
                    if(v[2] < z_min):
                        z_min = v[2]
                    if(v[2] > z_max):
                        z_max = v[2]


                    x_list.append(v[0])
                    y_list.append(v[1])
                    z_list.append(v[2])


            x_med = median(x_list)
            y_med = median(y_list)
            z_med = median(z_list)


            meansq_x = meansq(x_list)
            meansq_y = meansq(y_list)
            meansq_z = meansq(z_list)


            f =  chunk.sensors[0].calibration.f

            with open(pathCSV, "a") as test:
                test.write(str(a) + ' ' + str(z_acc) + ' ' +  str(x_med) + ' ' + str(y_med) + ' ' + str(z_med) + ' ' + str(x_min) + ' ' + str(y_min) + ' ' + str(z_min) + ' ' + str(x_max) + ' ' + str(y_max) + ' ' + str(z_max) + ' ' + str(chunk.sensors[0].antenna.rotation[2]) + ' ' + str(chunk.sensors[0].antenna.rotation[1]) + ' ' + str(chunk.sensors[0].antenna.rotation[0]) + ' ' + str(f) + ' ' + str(meansq_x) + ' ' + str(meansq_y) + ' ' + str(meansq_z) + '\n')


def exportErrors():

  try:
    global allJobs
    global allJobTypes

    if len(allJobs)==0:
      return False

    ts = time.time()
    #saveTimestamp("batch.start")

    doc = PhotoScan.app.document
    path = doc.path
    print(path)
    if path!='':
      doc.save()

    for i in range(len(allJobs)):
      path = allJobs[i]

      try:
        PhotoScan.app.document.open(path)
        doc = PhotoScan.app.document
        print("-----------------")
        print(u"loaded: %s" % doc.path)

        offsetsTest(doc)


      except (KeyboardInterrupt, SystemExit):
        print('>>> INTERRUPTED BY USER <<<')
        return False
      except:
        e = sys.exc_info()[0]
        print(e)
        print('>>> traceback <<<')
        traceback.print_exc()
        print('>>> end of traceback <<<')
        #PhotoScan.app.messageBox(u"Es gab einen Fehler im MAVinci-Plugin." )

    #saveTimestamp("batch.end")
    #doc.save()
    #clear list of jobs
    allJobs = []
    allJobTypes = []

    t = str(datetime.timedelta(seconds=round(time.time() - ts)))
    print(u"Stapelverarbeitung beendet. Durchgeführt in %s sek." % t)
    PhotoScan.app.messageBox(u"Stapelverarbeitung beendet. Durchgeführt in %s sek." % t)
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


def initDebugMenu():
    global allJobs
    global allJobTypes
    allJobs=[]
    allJobTypes=[]
    label = u"&Intel_Debug/&Scan directory for batch processing"
    PhotoScan.app.addMenuItem(label, scanDirJobs)
    label = u"&Intel_Debug/&Export errors"
    PhotoScan.app.addMenuItem(label, exportErrors)
    print("-----------------")
    print("Intel debug plugin loaded")
    print("-----------------")

initDebugMenu()
