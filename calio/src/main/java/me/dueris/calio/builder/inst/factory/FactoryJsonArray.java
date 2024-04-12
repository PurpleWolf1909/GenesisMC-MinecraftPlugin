package me.dueris.calio.builder.inst.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;

public class FactoryJsonArray {
    protected JsonArray handle;
    
    public FactoryJsonArray(JsonArray array) {
        this.handle = array;
    }

    public FactoryElement[] asArray() {
        return this.handle.asList().stream()
                .map(FactoryElement::fromJson)
            .toArray(FactoryElement[]::new);
    }
    
    public List<FactoryElement> asList() {
        return this.handle.asList().stream()
                .map(FactoryElement::fromJson)
            .collect(Collectors.toList());
    }

    public List<FactoryJsonObject> asJsonObjectList() {
        return this.handle.asList().stream()
                .map(FactoryElement::fromJson).map(FactoryElement::toJsonObject)
            .collect(Collectors.toList());
    }

    public Iterator<FactoryElement> iterator(){
        return this.asList().iterator();
    }

    public void forEach(Consumer<FactoryElement> consumer){
        this.asList().forEach(consumer);
    }
}
