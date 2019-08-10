package com.enioka.scanner.bt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BtSearchOptions {
    public Set<UUID> services = new HashSet<>(1);


    public BtSearchOptions mustSupportService(UUID serviceId) {
        services.add(serviceId);
        return this;
    }
    public BtSearchOptions mustSupportSspService() {
        return mustSupportService(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }
}
