package com.expedia.adaptivealerting.core.anomaly;

/**
 * Anomaly Type enum.
 *
 * @author kashah
 */
public enum AnomalyType {

    /**
     * Left tail. Generate alerts below the threshold.
     */
    LEFT_TAILED,

    /**
     * Right tail. Generate alerts above the threshold.
     */
    RIGHT_TAILED,

    /**
     * Both tails. Includes both left and right tails.
     */
    TWO_TAILED

}
