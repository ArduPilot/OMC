package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectorTree implements Comparable {
    private List<SectorTree> children = new ArrayList<>();
    private Sector sector;
    private SectorGeometry sectorGeometry;

    public SectorTree(Sector sector, SectorGeometry sectorGeometry) {
        this.sector = sector;
        this.sectorGeometry = sectorGeometry;
    }

    public SectorTree(Sector sector) {
        this.sector = sector;
    }

    public void addChild(SectorTree child) {
        if (sector == null) {
            sector = child.sector;
        } else {
            sector = Sector.union(sector, child.sector);
        }

        this.children.add(child);
    }

    public SectorGeometry getSectorGeometry() {
        return sectorGeometry;
    }

    public Sector getSector() {
        return sector;
    }

    public List<SectorTree> getChildren() {
        return children;
    }

    public SectorTree contains(Angle latitude, Angle longitude, boolean notAskNode) {
        if (sector != null && (notAskNode || sector.contains(latitude, longitude))) {
            if (children.isEmpty()) {
                return this;
            }

            Iterator<SectorTree> it = children.iterator();
            while (it.hasNext()) {
                SectorTree n = it.next();
                SectorTree s = n.contains(latitude, longitude, !it.hasNext());
                if (s != null) {
                    return s;
                }
            }
        }

        return null;
    }

    @Override
    public int compareTo(Object o) {
        if (o != null && sector != null && o instanceof SectorTree) {
            if (((SectorTree)o).sector.getDeltaLatRadians() != this.sector.getDeltaLatRadians())
                return sector.compareTo(((SectorTree)o).sector);
        }

        return 0;
    }

    public void makeTree() {
        Sector sector = this.getSector();
        Sector[] subSectors = sector.subdivide();
        for (Sector subSector : subSectors) {
            SectorTree newNode = null;

            ArrayList<SectorTree> subSubChildren = new ArrayList<>();
            for (SectorTree n : this.getChildren()) {
                Sector childSector = n.getSector();
                if (subSector.contains(childSector)) {
                    if (newNode == null) {
                        newNode = new SectorTree(n.getSector());
                    }

                    subSubChildren.add(n);
                    newNode.addChild(n);
                }
            }

            this.getChildren().removeAll(subSubChildren);

            if (newNode != null) {
                this.getChildren().add(newNode);
                if (newNode.getChildren().size() > 1) {
                    newNode.makeTree();
                }
            }
        }
    }
}
