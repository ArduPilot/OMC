#
# * Copyright (c) 2020 Intel Corporation
# *
# * SPDX-License-Identifier: GPL-3.0-or-later

import os
import os.path
import sys
from PySide2 import QtGui, QtCore, QtWidgets

try:
    sys.path.append(os.path.join(QtCore.QStandardPaths.writableLocation(QtCore.QStandardPaths.DataLocation),"scripts"))
except:
    print('path scripts not set')

import IntelLib

IntelLib.mavinciMenueInit()
