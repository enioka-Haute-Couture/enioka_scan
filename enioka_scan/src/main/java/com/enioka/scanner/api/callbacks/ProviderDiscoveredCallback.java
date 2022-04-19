package com.enioka.scanner.api.callbacks;

/**
 * Callback handling the discovery of scanner providers by {@link com.enioka.scanner.LaserScanner}
 */
public interface ProviderDiscoveredCallback {
    /**
     * Called once all providers are discovered.
     */
    void onDiscoveryDone();
}
