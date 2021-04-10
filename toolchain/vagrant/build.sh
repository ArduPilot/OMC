#!/bin/bash

set -e
set -x

cd /vagrant

mvn clean package
