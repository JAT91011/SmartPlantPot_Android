package es.deusto.mysmartplant.utils;

import es.deusto.mysmartplant.entities.SmartPlant;

public interface SmartPlantGattSuscriber {
    public void characteristicsRead(SmartPlant sp);
    public void characteristicWrite(SmartPlant sp);
    public void operationError(SmartPlant sp, int status);
    public void gattConnected(SmartPlant sp);
    public void gattDisconnected(SmartPlant sp);
}
