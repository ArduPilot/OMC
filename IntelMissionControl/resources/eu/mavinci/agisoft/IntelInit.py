import os
import os.path
import sys
from PySide import QtGui, QtCore
sys.path.append(os.path.join(QtGui.QDesktopServices.storageLocation(QtGui.QDesktopServices.DataLocation),"scripts"))

import IntelLib

IntelLib.mavinciMenueInit()