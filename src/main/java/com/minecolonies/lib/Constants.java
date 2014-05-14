package com.minecolonies.lib;

public class Constants
{
    public static final String MODID               = "minecolonies";
    public static final String MODNAME             = "MineColonies";
    public static final String VERSION             = "0.0.4"; //TODO can gradle set this?
    public static final String CLIENTPROXYLOCATION = "com.minecolonies.proxy.ClientProxy";
    public static final String COMMONPROXYLOCATION = "com.minecolonies.proxy.CommonProxy";
    public static final String PlayerPropertyName  = "MineColoniesPlayerProperties";

    public static final int    SIZENEEDEDFORSHIP     = 32;
    public static final double MAXDISTANCETOTOWNHALL = 200;
    public static final int    DEFAULTMAXCITIZENS    = 4;

    public enum Gui
    {
        TownHall, RenameTown, HutBuilder, HutDeliveryman, HutDeliverymanSettings;
    }
}
