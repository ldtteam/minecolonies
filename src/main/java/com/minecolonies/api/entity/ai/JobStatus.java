package com.minecolonies.api.entity.ai;

public enum JobStatus
{
    /**
     * I have nothing to do right this moment because I'm a very efficient worker, or I'm unemployed.
     */
    IDLE,
    /**
     * The citizen is currently working.
     */
    WORKING,
    /**
     * I have work to do, but I'm unable to work because I'm missing something.
     */
    STUCK
}
