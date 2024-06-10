package com.enioka.scanner.sdk.camera;

import android.text.TextUtils;
import android.util.Log;

import com.enioka.scanner.data.BarcodeType;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Analyse frames with ZBar and set result. Listens on a queue. Stops when queue contains null.
 */
class ZBarFrameAnalyser extends FrameAnalyser {
    private static final Map<Integer, BarcodeType> barcodeTypeZBar2Lib;
    private static final Map<BarcodeType, Integer> barcodeTypeLib1ZBar;

    static {
        barcodeTypeZBar2Lib = new HashMap<>();
        barcodeTypeZBar2Lib.put(Symbol.CODE39, BarcodeType.CODE39);
        barcodeTypeZBar2Lib.put(Symbol.CODE93, BarcodeType.CODE93);
        barcodeTypeZBar2Lib.put(Symbol.CODE128, BarcodeType.CODE128);
        barcodeTypeZBar2Lib.put(Symbol.DATABAR_EXP, BarcodeType.GS1_DATABAR_EXPANDED);
        barcodeTypeZBar2Lib.put(Symbol.DATABAR, BarcodeType.GS1_DATABAR);
        barcodeTypeZBar2Lib.put(Symbol.I25, BarcodeType.INT25);
        barcodeTypeZBar2Lib.put(Symbol.EAN8, BarcodeType.EAN8);
        barcodeTypeZBar2Lib.put(Symbol.EAN13, BarcodeType.EAN13);
        barcodeTypeZBar2Lib.put(Symbol.UPCA, BarcodeType.UPCA);
        barcodeTypeZBar2Lib.put(Symbol.UPCE, BarcodeType.UPCE);
        barcodeTypeZBar2Lib.put(Symbol.QRCODE, BarcodeType.QRCODE);
        barcodeTypeZBar2Lib.put(Symbol.PDF417, BarcodeType.PDF417);
        // To be confirmed
        barcodeTypeZBar2Lib.put(Symbol.ISBN13, BarcodeType.EAN13);
        barcodeTypeZBar2Lib.put(Symbol.ISBN10, BarcodeType.ISBN10);
        barcodeTypeZBar2Lib.put(Symbol.CODABAR, BarcodeType.CODABAR);

        barcodeTypeLib1ZBar = new HashMap<>();
        for (Map.Entry<Integer, BarcodeType> entry : barcodeTypeZBar2Lib.entrySet()) {
            barcodeTypeLib1ZBar.put(entry.getValue(), entry.getKey());
        }
    }

    private ImageScanner scanner;
    private final Set<Integer> symbologies = new HashSet<>(10);

    ZBarFrameAnalyser(BlockingQueue<FrameAnalysisContext> queue, FrameAnalyserManager parent) {
        super(parent);
        this.queue = queue;

        Log.i(TAG, "Analyser (ZBar) is ready inside pool");
    }

    @Override
    protected synchronized void initScanner() {
        // Barcode analyzer (properties: 0 = all symbologies, 256 = config, 3 = value)
        this.scanner = new ImageScanner();
        //this.scanner.setConfig(0, 256, 0); // 256 =   ZBAR_CFG_X_DENSITY (disable vertical scanning)
        //this.scanner.setConfig(0, 257, 3); // 257 =  ZBAR_CFG_Y_DENSITY (skip 2 out of 3 lines)
        this.scanner.setConfig(0, 0, 0); //  0 = ZBAR_CFG_ENABLE (disable all symbologies)

        // Enable select symbologies
        symbologies.add(Symbol.CODE128);
        for (int symbology : symbologies) {
            this.scanner.setConfig(symbology, 0, 1);
        }
    }

    @Override
    synchronized void addSymbology(BarcodeType barcodeType) {
        Integer zBarSymbology = barcodeTypeLib1ZBar.get(barcodeType);
        if (zBarSymbology == null) {
            Log.w(TAG, "Requested unsupported symbology: " + barcodeType.code);
            return;
        }
        symbologies.add(zBarSymbology);
        if (scanner != null) {
            this.scanner.setConfig(zBarSymbology, 0, 1);
        }
    }

    @Override
    protected void onPreviewFrame(FrameAnalysisContext ctx) {
        long start = System.nanoTime();
        //Log.i(TAG, "New frame analysis");
        String symData;
        int symType;
        CroppedPicture barcodeData = ctx.croppedPicture;

        // Analysis
        //TODO: reuse image if present.
        Image pic = new Image(barcodeData.croppedDataWidth, barcodeData.croppedDataHeight, "GREY");
        pic.setData(barcodeData.barcode);

        if (this.scanner.scanImage(pic) > 0) {
            // There is a result! Extract it.
            SymbolSet var15 = this.scanner.getResults();
            Iterator i$ = var15.iterator();

            Set<String> foundStrings = new HashSet<>();
            while (i$.hasNext()) {
                Symbol sym = (Symbol) i$.next();
                symData = sym.getData();
                symType = sym.getType();

                if (!TextUtils.isEmpty(symData) && !foundStrings.contains(symData)) {
                    foundStrings.add(symData);
                    parent.handleResult(symData, barcodeTypeZBar2Lib.get(symType), ctx);
                }
            }
        }
        int luma = (int) ((double) barcodeData.lumaSum / (barcodeData.croppedDataWidth * barcodeData.croppedDataHeight));
        parent.fpsCounter(luma);
        //Log.v(TAG, "Took ms: " + (System.nanoTime() - start) / 1000000);
    }
}
