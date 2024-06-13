package com.enioka.scanner.sdk.zebraoss;

import android.content.Context;
import androidx.annotation.Nullable;

import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.bt.api.BluetoothScanner;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.zebraoss.commands.ActivateAllSymbologies;
import com.enioka.scanner.sdk.zebraoss.commands.Beep;
import com.enioka.scanner.sdk.zebraoss.commands.InitCommand;
import com.enioka.scanner.sdk.zebraoss.commands.LedOff;
import com.enioka.scanner.sdk.zebraoss.commands.LedOn;
import com.enioka.scanner.sdk.zebraoss.commands.ManagementCommandGetAttribute;
import com.enioka.scanner.sdk.zebraoss.commands.ManagementCommandGetBufferSize;
import com.enioka.scanner.sdk.zebraoss.commands.RequestParam;
import com.enioka.scanner.sdk.zebraoss.commands.RequestRevision;
import com.enioka.scanner.sdk.zebraoss.commands.ScanDisable;
import com.enioka.scanner.sdk.zebraoss.commands.ScanEnable;
import com.enioka.scanner.sdk.zebraoss.commands.SetPickListMode;
import com.enioka.scanner.sdk.zebraoss.commands.StartSession;
import com.enioka.scanner.sdk.zebraoss.commands.StopSession;
import com.enioka.scanner.sdk.zebraoss.data.ParamSend;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttribute;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttributeReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

class ZebraOssScanner extends ZebraOssPairing implements Scanner, Scanner.WithTriggerSupport, Scanner.WithBeepSupport, Scanner.WithLedSupport, Scanner.WithInventorySupport {
    private static final String LOG_TAG = "SsiParser";

    private ScannerDataCallbackProxy dataCallback = null;
    private final BluetoothScanner btScanner;
    private final Map<String, String> statusCache = new HashMap<>();
    private final String providerKey;

    ZebraOssScanner(final String providerKey, BluetoothScanner btScanner) {
        super(providerKey);
        this.providerKey = providerKey;
        this.btScanner = btScanner;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new StartSession(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new StopSession(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCallback = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.disconnect();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new ScanDisable(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new ScanEnable(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Beeps
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep((byte) 0x01), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep((byte) 0x12), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new Beep((byte) 0x14), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LEDs
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new LedOn(color), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void ledColorOff(ScannerLedColor color,@Nullable ScannerCommandCallbackProxy cb) {
        this.btScanner.runCommand(new LedOff(), null);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getStatus(String key) {
        return statusCache.get(key);
    }

    @Override
    public String getStatus(String key, boolean allowCache) {
        if (!allowCache) {
            final Semaphore s = new Semaphore(0);
            startStatusCacheRefresh(new DataSubscriptionCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    s.release();
                }

                @Override
                public void onFailure() {
                    // Ignored
                }

                @Override
                public void onTimeout() {
                    // Ignored
                }
            });
            try {
                s.acquire();
            } catch (InterruptedException e) {
                return null;
            }
        }
        return getStatus(key);
    }

    @Override
    public Map<String, String> getStatus() {
        return new HashMap<>(statusCache);
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init and data cb
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return providerKey;
    }

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
        this.dataCallback = dataCallback;

        // Hook connection / disconnection events
        this.btScanner.registerStatusCallback(statusCallback);

        // Subscribe to read barcodes.
        this.btScanner.registerSubscription(new DataSubscriptionCallback<Barcode>() {
            @Override
            public void onSuccess(final Barcode data) {
                final List<Barcode> res = new ArrayList<>(1);
                res.add(data);
                ZebraOssScanner.this.dataCallback.onData(ZebraOssScanner.this, res);
            }

            @Override
            public void onFailure() {
                // TODO
            }

            @Override
            public void onTimeout() {
                // Ignore - no timeouts on persistent subscriptions.
            }
        }, Barcode.class);

        // Request scanner configuration
        startStatusCacheRefresh(null);

        this.btScanner.runCommand(new InitCommand(), null);
        this.btScanner.runCommand(new SetPickListMode((byte) 2), null);
        this.btScanner.runCommand(new ActivateAllSymbologies(), null);
        this.btScanner.runCommand(new ScanEnable(), null);

        // We are already connected if the scanner could be created...
        initCallback.onConnectionSuccessful(this);
    }

    private void startStatusCacheRefresh(final DataSubscriptionCallback<String> finalCallback) {
        this.btScanner.runCommand(new RequestParam(), new DataSubscriptionCallback<ParamSend>() {
            @Override
            public void onFailure() {
                Log.e(LOG_TAG, "failed to get scanner parameters");
            }

            @Override
            public void onTimeout() {
                Log.e(LOG_TAG, "timeout while fetching scanner parameters");
            }

            @Override
            public void onSuccess(ParamSend data) {
                for (ParamSend.Param prm : data.parameters) {
                    ZebraOssScanner.this.statusCache.put("ZEBRA_SSI_PRM_" + prm.number, prm.getStringValue());
                }

                ZebraOssScanner.this.btScanner.runCommand(new ManagementCommandGetBufferSize(), new DataSubscriptionCallback<RsmAttributeReply>() {
                    @Override
                    public void onFailure() {
                        Log.e(LOG_TAG, "could not retrieve RSM buffer data - timeout");
                    }

                    @Override
                    public void onTimeout() {
                        Log.e(LOG_TAG, "could not retrieve RSM buffer data - timeout");
                    }

                    @Override
                    public void onSuccess(RsmAttributeReply data) {
                        Log.i(LOG_TAG, "Zebra scanner has an RSM buffer of " + data);

                        // Model number, S/N, MAC address, battery%, battery health, battery model
                        ZebraOssScanner.this.btScanner.runCommand(new ManagementCommandGetAttribute(533, 534, 541, 30012, 30013, 30017), new DataSubscriptionCallback<RsmAttributeReply>() {
                            @Override
                            public void onFailure() {
                                Log.e(LOG_TAG, "could not retrieve RSM data");
                            }

                            @Override
                            public void onTimeout() {
                                Log.e(LOG_TAG, "could not retrieve RSM data - timeout");
                            }

                            @Override
                            public void onSuccess(RsmAttributeReply data) {
                                Log.i(LOG_TAG, "Scanner S/N & other RSM data found. " + data.toString());

                                for (RsmAttribute attribute : data.attributes) {
                                    statusCache.put("ZEBRA_RSM_ATTR_" + attribute.id, attribute.data);
                                }

                                // Finally some revision data.
                                ZebraOssScanner.this.btScanner.runCommand(new RequestRevision(), new DataSubscriptionCallback<ReplyRevision>() {
                                    @Override
                                    public void onSuccess(ReplyRevision data) {
                                        ZebraOssScanner.this.statusCache.put(SCANNER_STATUS_FIRMWARE, data.softwareRevision);

                                        normalizeAttributes();

                                        if (finalCallback != null) {
                                            finalCallback.onSuccess(null);
                                        }
                                    }

                                    @Override
                                    public void onFailure() {
                                        Log.e(LOG_TAG, "could not retrieve revision data");
                                    }

                                    @Override
                                    public void onTimeout() {
                                        Log.e(LOG_TAG, "timeout when retrieving revision data");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void normalizeAttributes() {
        String var = this.statusCache.get("ZEBRA_RSM_ATTR_533");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_SCANNER_SN, var);
        }
        var = this.statusCache.get("ZEBRA_RSM_ATTR_533");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_SCANNER_MODEL, var);
        }

        var = this.statusCache.get("ZEBRA_RSM_ATTR_30017");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_BATTERY_MODEL, var);
        }
        var = this.statusCache.get("ZEBRA_RSM_ATTR_30013");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_BATTERY_WEAR, var);
        }
        var = this.statusCache.get("ZEBRA_RSM_ATTR_30012");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_BATTERY_CHARGE, var);
        }

        var = this.statusCache.get("ZEBRA_RSM_ATTR_541");
        if (var != null) {
            this.statusCache.put(SCANNER_STATUS_BT_MAC, var);
        }

        Log.i(LOG_TAG, "Scanner status cache contains: ");
        for (Map.Entry<String, String> e : this.statusCache.entrySet()) {
            Log.i(LOG_TAG, String.format("\t%-30s - %s", e.getKey(), e.getValue()));
        }
    }
}
