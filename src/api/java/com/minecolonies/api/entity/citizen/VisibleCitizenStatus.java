package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for citizen status icons, resource location and translation
 */
public class VisibleCitizenStatus
{
    /**
     * All status with id,status
     */
    private static Map<Integer, VisibleCitizenStatus> visibleStatusMap;
    private static int                                idCounter = 1;

    /**
     * General public Icons
     */
    public final static VisibleCitizenStatus EAT         =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/hungry.png"), "com.minecolonies.gui.visiblestatus.eat");
    public final static VisibleCitizenStatus HOUSE       =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/house_big.png"), "com.minecolonies.gui.visiblestatus.idle");
    public final static VisibleCitizenStatus RAIDED      =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/raid_icon.png"), "com.minecolonies.gui.visiblestatus.raid");
    public final static VisibleCitizenStatus MOURNING    =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/mourning.png"), "com.minecolonies.gui.visiblestatus.mourn");
    public final static VisibleCitizenStatus BAD_WEATHER =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/bad_weather.png"), "com.minecolonies.gui.visiblestatus.rain");
    public final static VisibleCitizenStatus SLEEP       =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/bed_icon.png"), "com.minecolonies.gui.visiblestatus.sleep");
    public final static VisibleCitizenStatus SICK        =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/sick_icon.png"), "com.minecolonies.gui.visiblestatus.sick");
    public final static VisibleCitizenStatus WORKING     =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/working.png"), "com.minecolonies.gui.visiblestatus.working");

    /**
     * The status ID
     */
    private final int id;

    private final ResourceLocation icon;
    private final String           translationKey;

    public VisibleCitizenStatus(final ResourceLocation icon, final String translationKey)
    {
        this.icon = icon;
        this.translationKey = translationKey;
        if (visibleStatusMap == null)
        {
            visibleStatusMap = new HashMap<>();
            idCounter = 1;
        }
        this.id = idCounter++;
        visibleStatusMap.put(id, this);
    }

    /**
     * Returns the Icon to use
     *
     * @return icon texture location
     */
    public ResourceLocation getIcon()
    {
        return icon;
    }

    /**
     * Gets the translated display text
     *
     * @return String to display
     */
    public String getTranslationKey()
    {
        return translationKey;
    }

    /**
     * Returns the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * Gets the fitting status for the ID
     *
     * @param id id to get
     * @return Visible status
     */
    public static VisibleCitizenStatus getForId(final int id)
    {
        return visibleStatusMap.get(id);
    }

    /**
     * Get all visible status
     *
     * @return map of id, status
     */
    public static Map<Integer, VisibleCitizenStatus> getVisibleStatus()
    {
        return Collections.unmodifiableMap(visibleStatusMap);
    }
}
