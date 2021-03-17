Libraries
=========

This directory is intended hold source code for libraries that IMC uses. At the moment the only library in here is DroneKit (We could probably move our WorldWindJava in here also)


DroneKit desktop
---------------

This is a port of [DroneKit-Android](https://github.com/dronekit/dronekit-android) to desktop Java. The source in this directory is Git subtree mirror of our [internal desktop port](https://git.drones.intel.com/projects/IMC/repos/dronekit-java/browse), note that the desktop branch is name `desktop-port` so as not to conflict with upstream.

Please do not commit any changes to DroneKit desktop to this directory. Instead, commit to the upstream Bitbucket repository and follow the instructions below to update the subtree. If you are feeling bold, you can commit here and and use `git subtree push`, but please be careful to make the commits end up in upstream Bitbucket repo. You might find it helpful to read this [some more about Git subtree](https://www.atlassian.com/blog/git/alternatives-to-git-submodule-git-subtree).

### To install the DroneKit libraries into IMC

1. Run `build_and_install.bat` which will use Maven to build DroneKit and will install it into IMC's local maven repository (`<repo_root>/maven-local-repository`)

   *NOTE* this batch script requires maven and git to on your Path

### To update the Dronekit subtree:

1. Change into the root repository directory (that is `../..` from here) 

2. Run the following command to create a squash commit with all of the changes to DroneKit desktop:

    ```
    git subtree pull --prefix IntelMissionControl/libraries/dronekit ssh://git@git.drones.intel.com:7999/imc/dronekit-java.git desktop-port --squash
    ```

    Note that we are pulling from the `desktop-port` branch. You must run this command from the root of the IMC repo or else Git will complain *'You need to run this command from the toplevel of the working tree.'*

3. You should see two commit in `git log`: a merge commit and one squash commit summarizing all the changes from the last subtree pull

    > ```
    > commit d647257846ebf7a9426cdde2b9ffa1b1a1297264 (HEAD -> feature/dronekit)
    > Merge: d68972fae8 3e974a6b30
    > Author: Max Stein <maxx.t.stein@intel.com>
    > Date:   Thu Jul 19 16:04:44 2018 -0700
    > 
    >     Merge commit '3e974a6b30fe1aad9436b47cb1d998e78d82efa8' into feature/dronekit
    > 
    > commit 3e974a6b30fe1aad9436b47cb1d998e78d82efa8
    > Author: Max Stein <maxx.t.stein@intel.com>
    > Date:   Thu Jul 19 16:02:52 2018 -0700
    > 
    >     Squashed 'IntelMissionControl/libraries/dronekit/' changes from 772f08db82..5177b0ef2b
    > 
    >     b719850148 Add status display, cleanup
    >     8e99fda128 Clean up
    >     28875f5169 {WIP} fix UI
    >     ...
    > ```
