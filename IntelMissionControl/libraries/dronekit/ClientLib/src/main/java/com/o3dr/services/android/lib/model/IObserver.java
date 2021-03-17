// IObserver.aidl
package com.o3dr.services.android.lib.model;

import android.os.Bundle;

/**
* Asynchronous notification on change of vehicle state is available by registering observers for
* attribute changes.
*/
/* oneway */ public interface IObserver {

    /**
    * Notify observer that the named attribute has changed.
    * @param attributeEvent event describing the update. The supported events are listed in {@link com.o3dr.services.android.lib.drone.attribute.AttributeEvent}
    * @param attributeBundle bundle object from which additional event data can be retrieved.
    */
    void onAttributeUpdated(String attributeEvent, /* in */ Bundle eventExtras);


    
    public abstract class Stub implements IObserver {}

}
