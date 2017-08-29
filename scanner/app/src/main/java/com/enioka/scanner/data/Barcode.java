package com.enioka.scanner.data;

/**
 * The result of a barcode scan.
 */
public class Barcode {
    private String barcode;
    private BarcodeType barcodeType;

    public Barcode(String barcode, BarcodeType barcodeType) {
        this.barcode = barcode.trim();
        this.barcodeType = barcodeType;
    }

    public String getBarcode() {
        return barcode;
    }

    public BarcodeType getBarcodeType() {
        return barcodeType;
    }
}
