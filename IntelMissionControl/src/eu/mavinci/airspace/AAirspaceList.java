/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AAirspaceList implements IAirspaceList {
	
	List<IAirspace> list = new ArrayList<>();
	
	public String getName() {
		return "AAirspaceList";
	}

	public AAirspaceList(Collection<IAirspace> airspaces) {
		list.addAll(airspaces);
	}
	
	private boolean isActive = true;
	
	public boolean isActive(){
		return isActive;
	}

	public void setActive(boolean active){
		if (isActive != active){
			isActive = active;
		}
	}

	/* (non-Javadoc)
	 * @see eu.mavinci.airspace.IAirspaceList#getAirspaces()
	 */
	public List<IAirspace> getAirspaces() {
		return list;
	}
	
	/* (non-Javadoc)
	 * @see eu.mavinci.airspace.IAirspaceList#getMaxMAVAltitude(eu.mavinci.airspace.BoundingBox, int)
	 */
	public LowestAirspace getMaxMAVAltitude(Sector bb, double groundLevelElevationEGM) {
		List<IAirspace> airspaces = getAirspaces(bb);
		LowestAirspace la = new LowestAirspace(groundLevelElevationEGM);
		LatLon ll1 = new LatLon(bb.getMinLatitude(), bb.getMinLongitude());
		LatLon ll2 = new LatLon(bb.getMinLatitude(), bb.getMaxLongitude());
		LatLon ll3 = new LatLon(bb.getMaxLatitude(), bb.getMinLongitude());
		LatLon ll4 = new LatLon(bb.getMaxLatitude(), bb.getMaxLongitude());
		for (IAirspace s : airspaces) {
			if (!s.getType().isMAVAllowed()) {
				la.computeOther(s, ll1);
				la.computeOther(s, ll2);
				la.computeOther(s, ll3);
				la.computeOther(s, ll4);
			}
		}
		return la;
	}
	
	/* (non-Javadoc)
	 * @see eu.mavinci.airspace.IAirspaceList#getAirspaces(eu.mavinci.airspace.BoundingBox)
	 */
	public List<IAirspace> getAirspaces(Sector bb) {
		ArrayList<IAirspace> ilist = new ArrayList<IAirspace>(list.size());
		for (IAirspace a : list) {
			if (a.getBoundingBox().intersects(bb)) {
				ilist.add(a);
			}
		}
		LatLon ll1 = new LatLon(bb.getMinLatitude(), bb.getMinLongitude());
		LatLon ll2 = new LatLon(bb.getMinLatitude(), bb.getMaxLongitude());
		LatLon ll3 = new LatLon(bb.getMaxLatitude(), bb.getMinLongitude());
		LatLon ll4 = new LatLon(bb.getMaxLatitude(), bb.getMaxLongitude());
		
		LinkedList<IAirspace> deleteList = new LinkedList<IAirspace>();
		for (IAirspace s : ilist) {
			if (!s.insidePolygon(ll1) && !s.insidePolygon(ll2) && !s.insidePolygon(ll3) && !s.insidePolygon(ll4)) {
				deleteList.add(s);
			}
		}
		for (IAirspace s : deleteList) {
			ilist.remove(s);
		}
		return ilist;
	}
	
	/* (non-Javadoc)
	 * @see eu.mavinci.airspace.IAirspaceList#getAirspaceIndices(eu.mavinci.airspace.BoundingBox)
	 */
	public List<Integer> getAirspaceIndices(Sector bb) {
		ArrayList<Integer> ilist = new ArrayList<Integer>(list.size());
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getBoundingBox().intersects(bb)) {
				ilist.add(i);
			}
		}
		return ilist;
	}
	
	public int size() {
		return list.size();
	}
	

	IAirspaceListener listener = null;
	
	public void setListener(IAirspaceListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AAirspaceList that = (AAirspaceList) o;

		if (!getName().equals(that.getName())) return false;
		return list != null ? list.equals(that.list) : that.list == null;
	}

	@Override
	public int hashCode() {
		return list != null ? list.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "AAirspaceList{" +
				"list=" + list +
				", isActive=" + isActive +
				", name=" + getName() +
				'}';
	}
}
