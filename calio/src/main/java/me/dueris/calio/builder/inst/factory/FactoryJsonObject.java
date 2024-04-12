package me.dueris.calio.builder.inst.factory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.dueris.calio.util.MiscUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class FactoryJsonObject {
    public JsonObject handle;
    
    public FactoryJsonObject(JsonObject object) {
        this.handle = object;
    }

    public boolean isEmpty(){
        return this.handle.isEmpty();
    }

    public FactoryJsonObject getJsonObject(String key){
        return new FactoryJsonObject(this.handle.get(key).getAsJsonObject());
    }

    public FactoryJsonObject getJsonObjectOrNew(String key){
        return isPresent(key) ? getJsonObject(key) : new FactoryJsonObject(new JsonObject());
    }

    public FactoryJsonArray getJsonArray(String key){
        return new FactoryJsonArray(this.handle.get(key).getAsJsonArray());
    }

    public boolean isJsonObject(String key){
        return this.handle.get(key).isJsonObject();
    }

    public boolean isJsonArray(String key){
        return this.handle.get(key).isJsonArray();
    }

    public boolean isGsonPrimative(String key){
        return this.handle.get(key).isJsonPrimitive();
    }

    public FactoryNumber getNumber(String key){
        return new FactoryNumber(this.handle.get(key).getAsJsonPrimitive());
    }

    public FactoryNumber getNumberOrDefault(String key, Number number){
        return new FactoryNumber(this.isPresent(key) ? this.handle.get(key).getAsJsonPrimitive() : new JsonPrimitive(number));
    }

    public <T extends Enum<T>> T getEnumValue(String key, Class<T> enumClass){
        String value = this.handle.get(key).getAsString();
        T[] enumConstants = enumClass.getEnumConstants();
        for (T enumValue : enumConstants) {
            if (enumValue.toString().toUpperCase().equalsIgnoreCase(value.toUpperCase())) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("Provided JsonValue from key \"{key}\" was not an instanceof enum \"{enum}\"");
    }

    public boolean getBoolean(String key){
        return this.handle.get(key).getAsJsonPrimitive().getAsBoolean();
    }

    public String getString(String key){
        return this.handle.get(key).getAsString();
    }

    public boolean isPresent(String key){
        return this.handle.has(key);
    }

    public boolean getBooleanOrDefault(String key, boolean def){
        return isPresent(key) ? getBoolean(key) : def;
    }

    public String getStringOrDefault(String key, String def){
        return isPresent(key) ? getString(key) : def;
    }

    public FactoryElement getElement(String key){
        return new FactoryElement(this.handle.get(key));
    }

    public Set<String> keySet(){
        return this.handle.keySet();
    }

    public List<FactoryElement> values(){
        return this.handle.asMap().values().stream().map(FactoryElement::fromJson).collect(Collectors.toUnmodifiableList());
    }

    public NamespacedKey getNamespacedKey(String key){
        return NamespacedKey.fromString(this.getString(key));
    }

    public ResourceLocation getResourceLocation(String key){
        return CraftNamespacedKey.toMinecraft(this.getNamespacedKey(key));
    }

    public Material getMaterial(String key){
        return MiscUtils.getBukkitMaterial(this.getString(key));
    }

    public CompoundTag getCompoundTag(String key){
        return MiscUtils.ParserUtils.parseJson(new com.mojang.brigadier.StringReader(this.getString(key)), CompoundTag.CODEC);
    }

    public <T> TagKey<T> getTagKey(String key, ResourceKey<Registry<T>> registry){
        return TagKey.create(registry, this.getResourceLocation(key));
    }

    public ItemStack getItemStack(String key){
        FactoryElement inst = this.getElement(key);
        if (inst == null) return null;
        if (this.isJsonObject(key)){
            
        }
        if (inst.isJsonObject()) {
            FactoryJsonObject obj = inst.toJsonObject();
            String materialVal = "head";
            int amt = 1;
            if (obj.isPresent("item")) {
                materialVal = obj.getString("item");
            }
            if (obj.isPresent("amount")) {
                amt = obj.getNumber("amount").getInt();
            }
            return new ItemStack(getMaterial(materialVal), amt);
        } else { // Assuming provided string
            return new ItemStack(getMaterial(inst.getString()));
        }
    }
}
