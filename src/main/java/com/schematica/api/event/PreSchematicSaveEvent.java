package com.schematica.api.event;

import com.schematica.api.ISchematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/**
 * This event is fired after the schematic has been Captured, but before it is serialized to the schematic format.
 * This is your opportunity to add Metadata.
 * Register to this event using MinecraftForge.EVENT_BUS
 */
public class PreSchematicSaveEvent extends Event {
    private final Map<String, Short> mappings;

    /**
     * The schematic that will be saved.
     */
    public final ISchematic schematic;

    /**
     * The Extended Metadata tag compound provides a facility to add custom metadata to the schematic.
     */
    public final NBTTagCompound extendedMetadata;

    public PreSchematicSaveEvent(ISchematic schematic, Map<String, Short> mappings) {
        this.schematic = schematic;
        this.mappings = mappings;
        this.extendedMetadata = new NBTTagCompound();
    }
}
