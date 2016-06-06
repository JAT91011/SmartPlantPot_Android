package es.deusto.mysmartplant.utils;

import es.deusto.mysmartplant.entities.SmartPlant;

public interface SmartPlantListener {

    public void smartPlantFound(SmartPlant smartPlant);
    public void searchState(int state);
    public void operationError(int status);
}