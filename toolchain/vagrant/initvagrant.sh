#!/bin/bash

set -e
set -x

echo "Start"

apt-get update
apt-get install -y maven

pushd /vagrant
  sudo -H -u vagrant ./toolchain/vagrant/build.sh
popd
