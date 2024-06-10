package com.enioka.scanner.sdk.camera;

import android.util.Log;

import com.enioka.scanner.data.BarcodeType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Analyse frames with ZXing and set result. Listens on a queue. Stops when queue contains null.
 */
class ZXingFrameAnalyser extends FrameAnalyser {
    private static final Map<BarcodeFormat, BarcodeType> barcodeTypeZXing2Lib;
    private static final Map<BarcodeType, BarcodeFormat> barcodeTypeLib1ZXing;

    static {
        barcodeTypeZXing2Lib = new HashMap<>();
        barcodeTypeZXing2Lib.put(BarcodeFormat.CODE_39, BarcodeType.CODE39);
        barcodeTypeZXing2Lib.put(BarcodeFormat.CODE_93, BarcodeType.CODE93);
        barcodeTypeZXing2Lib.put(BarcodeFormat.CODE_128, BarcodeType.CODE128);
        barcodeTypeZXing2Lib.put(BarcodeFormat.CODABAR, BarcodeType.CODABAR);
        barcodeTypeZXing2Lib.put(BarcodeFormat.ITF, BarcodeType.INT25);
        barcodeTypeZXing2Lib.put(BarcodeFormat.UPC_A, BarcodeType.UPCA);
        barcodeTypeZXing2Lib.put(BarcodeFormat.UPC_E, BarcodeType.UPCE);
        barcodeTypeZXing2Lib.put(BarcodeFormat.EAN_8, BarcodeType.EAN8);
        barcodeTypeZXing2Lib.put(BarcodeFormat.EAN_13, BarcodeType.EAN13);
        // To be tested
        barcodeTypeZXing2Lib.put(BarcodeFormat.UPC_EAN_EXTENSION, BarcodeType.EAN13);
        barcodeTypeZXing2Lib.put(BarcodeFormat.QR_CODE, BarcodeType.QRCODE);
        barcodeTypeZXing2Lib.put(BarcodeFormat.DATA_MATRIX, BarcodeType.DATAMATRIX);
        barcodeTypeZXing2Lib.put(BarcodeFormat.AZTEC, BarcodeType.AZTEC);
        barcodeTypeZXing2Lib.put(BarcodeFormat.PDF_417, BarcodeType.PDF417);
        barcodeTypeZXing2Lib.put(BarcodeFormat.MAXICODE, BarcodeType.MAXICODE);
        barcodeTypeZXing2Lib.put(BarcodeFormat.RSS_14, BarcodeType.GS1_DATABAR);
        barcodeTypeZXing2Lib.put(BarcodeFormat.RSS_EXPANDED, BarcodeType.GS1_DATABAR_EXPANDED);

        barcodeTypeLib1ZXing = new HashMap<>();
        for (Map.Entry<BarcodeFormat, BarcodeType> entry : barcodeTypeZXing2Lib.entrySet()) {
            barcodeTypeLib1ZXing.put(entry.getValue(), entry.getKey());
        }
    }

    private MultiFormatReader scanner;
    private Set<BarcodeFormat> symbologies = new HashSet<>(10);
    private Map<DecodeHintType, Object> hints;

    ZXingFrameAnalyser(BlockingQueue<FrameAnalysisContext> queue, FrameAnalyserManager parent) {
        super(parent);
        this.queue = queue;

        Log.i(TAG, "Analyser (ZXing) is ready inside pool");
    }

    @Override
    protected synchronized void initScanner() {
        this.scanner = new MultiFormatReader();

        symbologies.add(BarcodeFormat.CODE_128);
        hints = new HashMap<>(3);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, symbologies);

        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        this.scanner.setHints(hints);
    }

    @Override
    synchronized void addSymbology(BarcodeType barcodeType) {
        BarcodeFormat zXingSymbology = barcodeTypeLib1ZXing.get(barcodeType);
        if (zXingSymbology == null) {
            Log.w(TAG, "Requested unsupported symbology: " + barcodeType.code);
            return;
        }
        symbologies.add(zXingSymbology);

        if (this.scanner == null) {
            initScanner();
        }

        this.scanner.setHints(hints);
    }

    @Override
    protected void onPreviewFrame(FrameAnalysisContext ctx) {
        long start = System.nanoTime();

        CroppedPicture barcodeData = ctx.croppedPicture;

        Binarizer binarizer = new HybridBinarizer(new PlanarYUVLuminanceSource(barcodeData.barcode, barcodeData.croppedDataWidth, barcodeData.croppedDataHeight, 0, 0, barcodeData.croppedDataWidth, barcodeData.croppedDataHeight, false));
        BinaryBitmap bitmap = new BinaryBitmap(binarizer);

        // Analysis
        Result res = null;
        try {
            res = this.scanner.decodeWithState(bitmap);
        } catch (NotFoundException e) {
            // Nothing to do. Barcode not found, this happens most of the time.
        }

        if (res != null) {
            String readBarcode = res.getText();
            if (readBarcode != null && !readBarcode.isEmpty()) {
                parent.handleResult(readBarcode, barcodeTypeZXing2Lib.get(res.getBarcodeFormat()), ctx);
            }
        }

        // End
        int luma = (int) ((double) barcodeData.lumaSum / (barcodeData.croppedDataWidth * barcodeData.croppedDataHeight));
        parent.fpsCounter(luma);
        //Log.v(TAG, "Took ms: " + (System.nanoTime() - start) / 1000000);
    }
}
