# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.ssh.forward_x11 = true

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
      # Don't boot with headless mode
      #   vb.gui = true
      #
      #   # Use VBoxManage to customize the VM. For example to change memory:
      vb.customize ["modifyvm", :id, "--memory", "3192"]
      vb.customize ["modifyvm", :id, "--ioapic", "on"]
      vb.customize ["modifyvm", :id, "--cpus", "2"]
      # Make some effort to avoid clock skew
      vb.customize ["guestproperty", "set", :id, "/VirtualBox/GuestAdd/VBoxService/--timesync-set-threshold", "5000"]
      vb.customize ["guestproperty", "set", :id, "/VirtualBox/GuestAdd/VBoxService/--timesync-set-start"]
      vb.customize ["guestproperty", "set", :id, "/VirtualBox/GuestAdd/VBoxService/--timesync-set-on-restore", "1"]
  end

  # If you are on windows then you must use a version of git >= 1.8.x
  # to update the submodules in order to build. Older versions of git
  # use absolute paths for submodules which confuses things.

  # removing this line causes "A box must be specified." error
  # and this is the default box that will be booted if no name is specified
  config.vm.box = "ubuntu/bionic64"

  # 18.04 LTS
  config.vm.define "bionic64", primary: true do |bionic64|
    bionic64.vm.box = "ubuntu/bionic64"
    bionic64.vm.provision :shell, path: "toolchain/vagrant/initvagrant.sh"
    bionic64.vm.provider "virtualbox" do |vb|
      vb.name = "ArduPilot OMC (bionic64)"
    end
  end

  # 18.04 LTS
  config.vm.define "bionic64-desktop", primary: true do |bionic64|
    bionic64.vm.box = "ubuntu/bionic64"
    bionic64.vm.provision :shell, path: "toolchain/vagrant/initvagrant-desktop.sh"
    bionic64.vm.provider "virtualbox" do |vb|
      vb.name = "ArduPilot OMC (bionic64-desktop)"
      vb.gui = true
    end
  end

  # 20.04 LTS
  config.vm.define "focal", autostart: false do |focal|
    focal.vm.box = "ubuntu/focal64"
    focal.vm.provision :shell, path: "toolchain/vagrant/initvagrant.sh"
    focal.vm.provider "virtualbox" do |vb|
      vb.name = "ArduPilot OMC (focal)"
    end
    focal.vm.boot_timeout = 1200
  end

  # 20.04 LTS
  config.vm.define "focal-desktop", autostart: false do |focal|
    focal.vm.box = "ubuntu/focal64"
    focal.vm.provision :shell, path: "toolchain/vagrant/initvagrant-desktop.sh"
    focal.vm.provider "virtualbox" do |vb|
      vb.name = "ArduPilot OMC (focal-desktop)"
      vb.gui = true
    end
    focal.vm.boot_timeout = 1500
  end

  # 20.10
  config.vm.define "groovy", autostart: false do |groovy|
    groovy.vm.box = "ubuntu/groovy64"
    groovy.vm.provision :shell, path: "toolchain/vagrant/initvagrant.sh"
    groovy.vm.provider "virtualbox" do |vb|
      vb.name = "ArduPilot OMC (groovy)"
    end
    groovy.vm.boot_timeout = 1200
  end

end
