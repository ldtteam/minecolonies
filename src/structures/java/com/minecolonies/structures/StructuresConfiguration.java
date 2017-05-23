package com.minecolonies.structures;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The main interaction point of mods that use this Structures module.
 * Allows for IO between the module and the mod using it.
 */
public class StructuresConfiguration
{

    private static File schematicsFolder;

    private static boolean isClient;

    private static UUID serverID;

    private static Consumer<Boolean> schematicsDownloadedCallback;

    private static Supplier<Integer> inuseDecorationsCountCallback;

    /**
     * Getter for the Folder were schematics are stored.
     *
     * @return The folder were schematics are stored.
     */
    public static File getSchematicsFolder()
    {
        return schematicsFolder;
    }

    /**
     * Setter for the Folder were schematics are stored.
     * <p>
     * TODO: Set this correctly from the Minecolonies Proxy. Before {@link Structures#init()} is called.
     *
     * @param schematicsFolder The new schematics folder.
     */
    public static void setSchematicsFolder(final File schematicsFolder)
    {
        StructuresConfiguration.schematicsFolder = schematicsFolder;
    }

    /**
     * Method that indicates if this Structures instances runs on the client or on the server.
     *
     * @return True when this instance runs on the client, false when not.
     */
    public static boolean isClient()
    {
        return isClient;
    }

    /**
     * Setter for the indication of the side that this instance is running on.
     *
     * @param isClient True when this is the client, false when not.
     */
    public static void setClient(final boolean isClient)
    {
        StructuresConfiguration.isClient = isClient;
    }

    /**
     * Getter for the {@link Consumer} that sets the SchematicsDownloaded flag when the download has been completed.
     *
     * @return The callback for the completion of the download of a schematic.
     */
    public static Consumer<Boolean> getSchematicsDownloadedCallback()
    {
        return schematicsDownloadedCallback;
    }

    /**
     * Setter for the {@link Consumer} that sets the SchematicsDownloaded flag when the download has been completed.
     *
     * @param schematicsDownloadedCallback The new callback for the completion of the download of a schematic.
     */
    public static void setSchematicsDownloadedCallback(final Consumer<Boolean> schematicsDownloadedCallback)
    {
        StructuresConfiguration.schematicsDownloadedCallback = schematicsDownloadedCallback;
    }

    /**
     * Getter for the {@link Supplier} that checks the amount of decorations that are in use right now.
     *
     * @return A {@link Supplier} that can indicate how many decorations are in use at the moment.
     */
    public static Supplier<Integer> getInuseDecorationsCountCallback()
    {
        return inuseDecorationsCountCallback;
    }

    /*
    TODO: Set the following callback setter with a callback that holds this code:
    for (final IColony c : ColonyManager.getColonies())
        {
            for (final IWorkOrder workOrder : c.getWorkManager().getWorkOrders().values())
            {
                if (workOrder instanceof IWorkOrderBuild && ((IWorkOrderBuild) workOrder).isDecoration())
                {
                    final String schematicName = ((IWorkOrderBuild) workOrder).getStructureName();
                    if (md5Set.contains(schematicName))
                    {
                        md5Set.remove(schematicName);
                        countInUseStructures++;
                    }
                }
            }
        }
     */

    /**
     * Setter for the {@link Supplier} that checks the amount of decorations that are in use right now.
     *
     * @param inuseDecorationsCountCallback A new {@link Supplier} that can indicate how many decorations are in use at the moment.
     */
    public static void setInuseDecorationsCountCallback(final Supplier<Integer> inuseDecorationsCountCallback)
    {
        StructuresConfiguration.inuseDecorationsCountCallback = inuseDecorationsCountCallback;
    }

    /**
     * Method to get the Unique ID of the server to identify it on the client side,
     * when the client side checks its cache.
     *
     * @return The ID of the server.
     */
    public static UUID getServerID()
    {
        return serverID;
    }

    /**
     * Method to set the Unique ID of the server to identify it on the client side,
     * when the client side checks its cache.
     *
     * @param serverID The new id of the server.
     */
    public static void setServerID(final UUID serverID)
    {
        StructuresConfiguration.serverID = serverID;
    }
}
