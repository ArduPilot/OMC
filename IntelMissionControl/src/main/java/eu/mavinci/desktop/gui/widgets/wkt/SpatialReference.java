package eu.mavinci.desktop.gui.widgets.wkt;

import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SpatialReference {

    public static final String GEO_SRS = "geoSrs";
    public static final String PROJ_SRS = "projSrs";
    public static final String PRIV_SRS = "privSrs";

    private MSpatialReference reference;
    private StringProperty id = new SimpleStringProperty();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty category = new SimpleStringProperty();
    private StringProperty wkt = new SimpleStringProperty();
    private boolean isGroup;

    public SpatialReference() {
        isGroup = true;
    }

    public SpatialReference(MSpatialReference reference) {
        this.reference = reference;
        id.setValue(reference.id);
        name.setValue(reference.name);
        wkt.setValue(reference.wkt);
        category.setValue(
            reference.isPrivate() ? PRIV_SRS : reference.isGeographic() ? GEO_SRS : reference.getCathegory());
    }

    @Override
    public String toString() {
        if (!isGroup) {
            return id.get() + " " + name.get();
        } else {
            return category.get();
        }
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getId() {
        return id.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getCategory() {
        return category.get();
    }

    public String getWkt() {
        return wkt.get();
    }

    public Boolean isGeoidOK() {
        if (reference == null) return true;
        try {
            reference.getGeoid();
            return true;
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }

    public File getGeoidFileName() {
        if (reference == null) return null;
        try {
            return reference.getGeoidFileName();
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    public boolean isGroup() {
        return isGroup;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SpatialReference) {
            if (getId() != null && getId().equals(((SpatialReference)obj).getId())) {
                return true;
            }
        }

        return false;
    }
}
