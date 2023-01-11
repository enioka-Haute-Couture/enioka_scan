package com.enioka.scanner.data;

/**
 * The result of a barcode scan.
 */
public class Barcode {
    private String barcode;
    /**
     * rawBarcode without trim, useful for 2D barcode who can have a structure based on some white space.
     */
    private String rawBarcode;
    private BarcodeType barcodeType = BarcodeType.UNKNOWN;

    public Barcode(String barcode, BarcodeType barcodeType) {
        this.rawBarcode = barcode;

        this.barcode = barcode.trim();
        this.barcodeType = barcodeType;
    }

    public String getBarcode() {
        return barcode;
    }

    public BarcodeType getBarcodeType() {
        return barcodeType;
    }

    public String getRawBarcode() {return rawBarcode;}

    @Override
    public String toString() {
        return this.barcode + " (" + this.barcodeType.code + ")";
    }
}
