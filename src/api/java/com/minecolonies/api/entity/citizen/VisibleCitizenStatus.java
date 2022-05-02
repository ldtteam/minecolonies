package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

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
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/hungry.png"), MESSAGE_INFO_CITIZEN_STATUS_HUNGRY);
    public final static VisibleCitizenStatus HOUSE       =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/house_big.png"), MESSAGE_INFO_CITIZEN_STATUS_IDLE);
    public final static VisibleCitizenStatus RAIDED      =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/raid_icon.png"), MESSAGE_INFO_CITIZEN_STATUS_RAID);
    public final static VisibleCitizenStatus MOURNING    =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/mourning.png"), MESSAGE_INFO_CITIZEN_STATUS_MOURNING);
    public final static VisibleCitizenStatus BAD_WEATHER =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/bad_weather.png"), MESSAGE_INFO_CITIZEN_STATUS_RAINING);
    public final static VisibleCitizenStatus SLEEP       =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/bed_icon.png"), MESSAGE_INFO_CITIZEN_STATUS_SLEEPING);
    public final static VisibleCitizenStatus SICK        =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/sick_icon.png"), MESSAGE_INFO_CITIZEN_STATUS_SICK);
    public final static VisibleCitizenStatus WORKING     =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/working.png"), MESSAGE_INFO_CITIZEN_STATUS_WORKING);

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
