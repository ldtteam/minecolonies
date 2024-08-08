package com.minecolonies.core.colony.jobs.views;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Extended Crafter job information on the client side, valid for all job types.
 */
public class CrafterJobView extends DefaultJobView
{
    /**
     * The Token of the data store which belongs to this job.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * Instantiate the Crafter job view.
     * @param iColonyView the colony it belongs to.
     * @param iCitizenDataView the citizen it belongs to.
     */
    public CrafterJobView(final IColonyView iColonyView, final ICitizenDataView iCitizenDataView)
    {
        super(iColonyView, iCitizenDataView);
    }

    @Override
    public void deserialize(final RegistryFriendlyByteBuf buffer)
    {
        super.deserialize(buffer);
        this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(buffer);
    }

    /**
     * Getter for the data store which belongs to this job.
     *
     * @return the crafter data store.
     */
    public IRequestSystemCrafterJobDataStore getDataStore()
    {
        return getColonyView()
                 .getRequestManager()
                 .getDataStoreManager()
                 .get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE);
    }

    /**
     * Get the data store token assigned to it.
     * @return the token.
     */
    public IToken<?> getRsDataStoreToken()
    {
        return rsDataStoreToken;
    }
}
