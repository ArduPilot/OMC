/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.MTLLoader;
import com.jme3.scene.plugins.OBJLoader;

public class Test {

    public static void main(String[] args) {
        Vector3f cam = new Vector3f(1, 1, 1);
        Vector3f camDir = cam.multLocal(0.6f);

        var foo = new OBJLoader();
        var assetManager = new DesktopAssetManager();
        assetManager.registerLocator("c:\\Users\\jtroseme\\Downloads\\ExampleModels\\", FileLocator.class);
        assetManager.registerLoader(OBJLoader.class, "obj");
assetManager.registerLoader(MTLLoader.class, "mtl");
  //      Material mat_default = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        teapot.setMaterial(mat_default);
        var teapot = assetManager.loadModel("BoingDefect.obj");
        //
        //
        //            var is = new FileInputStream("c:\\Users\\jtroseme\\Downloads\\ExampleModels\\Liaoning.obj");
        //            var a = new StreamAssetInfo(am, new
        // AssetKey<>("c:\\Users\\jtroseme\\Downloads\\ExampleModels\\Liaoning.obj"), is);
        //
        //            teapot = am.loadModel("c:\\Users\\jtroseme\\Downloads\\ExampleModels\\Liaoning.obj");
        teapot.getTriangleCount();

        System.out.println("Hello World!" + cam + camDir); // Display the string.
    }

}
