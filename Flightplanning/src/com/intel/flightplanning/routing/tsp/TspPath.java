/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.routing.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TspPath<T> {

    public List<T> nodes;
    private List<T> previousTravel;

    public DistanceFunction<T> distanceFunction;
    //	public IsSwappableFunction<T>  isSwappableFunction;
    public TspPath() {
        // nodes = new ArrayList<T>();
    }

    public TspPath(List<T> nodes) {
        this.nodes = nodes;
    }

    public TspPath(TspPath<T> p) {
        nodes = new ArrayList<T>();
        nodes.addAll(p.nodes);
        distanceFunction = p.distanceFunction;
        //		isSwappableFunction = p.isSwappableFunction;
    }

    public interface DistanceFunction<T> {
        public double distance(List<T> path);

        public boolean isAcceptable(List<T> path);
    }

    //	public interface IsSwappableFunction<T> {
    //		public boolean canBeSwapped(T a, T b);
    //	}

    public double getDistance() {
        return distanceFunction.distance(nodes);
    }

    public boolean isAcceptable() {
        return distanceFunction.isAcceptable(nodes);
    }

    public void swapNodesRandomly() {
        //		boolean canBeSwapped = false;
        int a = 0;
        int b = 0;
        //		while(!canBeSwapped) {
        a = (int)(Math.random() * nodes.size());
        b = a + 1 + (int)(Math.random() * (nodes.size() - 1));
        b %= nodes.size();
        //	        canBeSwapped = isSwappableFunction.canBeSwapped(nodes.get(a),nodes.get(b));
        //	      }
        // T x = nodes.get(a);
        // T y = nodes.get(b);
        // nodes.set(a, y);
        // nodes.set(b, x);
        previousTravel = new ArrayList<T>(nodes);
        if (Math.random() < .4) {
            nodes.add(a, nodes.remove(b));
        } else {
            if (a > b) {
                int tmp = a;
                a = b;
                b = tmp;
            }

            while (a < b) {
                Collections.swap(nodes, a, b);
                a++;
                b--;
            }
        }
    }
    /*
    	public void swapNodesRandomly() {
    //		boolean canBeSwapped = false;
    		int a = 0;
    		int b = 0;
    		int c = 0;
    //		while(!canBeSwapped) {
    			 a = (int) (Math.random() * nodes.size());
    	         b = a+1+(int) (Math.random() * (nodes.size()-1));
    	         b %= nodes.size();
    	         int minAB = Math.min(a, b);
    	         int maxAB = Math.max(a, b);
    	         //cut out a segment [a,b)  (b could be smaller then a!!) and insert it before c
    	         // so c can be in [0,minAB) or (maxAB,n]
    	         c = (int) (Math.random() * (nodes.size() -1-maxAB+minAB));
    	         if (c >= minAB) c= maxAB+ c-minAB+1;
    //	        canBeSwapped = isSwappableFunction.canBeSwapped(nodes.get(a),nodes.get(b));
    //	      }
    		//T x = nodes.get(a);
            //T y = nodes.get(b);
            //nodes.set(a, y);
            //nodes.set(b, x);
    		previousTravel = new ArrayList<T>(nodes);

    		if (a>b) {
    			int tmp = a;
    			a=b;
    			b=tmp;
    		}
    		int sign = (int)Math.signum(b-a);
    		while (a!=b){
    			if (c>a){
    				nodes.add(c-1, nodes.remove(a));
    			} else{
    				nodes.add(c, nodes.remove(a));
    			}
    			a+=sign;
    		}
    	}
    */

    public void generateInitialTravel() {
        // Collections.shuffle(nodes);

    }

    public void revertSwap() {
        nodes = previousTravel;
    }

}
