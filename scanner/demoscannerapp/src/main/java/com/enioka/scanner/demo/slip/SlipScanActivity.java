package com.enioka.scanner.demo.slip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.demo.R;
import com.enioka.scanner.demo.WelcomeActivity;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Iterator;

/**
 * Provide an Packing slip scan capability on current consignment
 */
public class SlipScanActivity extends AppCompatActivity implements ZbarScanView.ResultHandler, SlipScanView.PictureHandler {
    private static String LOG_TAG = "PackingSlipScan";

    /**
     * Store the instance of scanner view
     */
    private SlipScanView scanner;

    /**
     * Store the instance of flashligth button
     */
    private ImageButton flashlight;

    /**
     * Store the instance of the instructions display text
     */
    private TextView infoText;

    /**
     * Store the instance of scan code text
     */
    private TextView scanCodeText;

    private Bitmap result = null;

    /**
     * A button to take the actual photo (not preview)
     */
    private ImageButton takePhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slip_scan);

        scanner = (SlipScanView) findViewById(R.id.packingslip_scanner);
        scanner.setResultHandler(this);
        scanner.setPictureHandler(this);
        scanner.setTorch(false);

        infoText = (TextView) findViewById(R.id.instructions);

        // Display initial instruction and expected packing slip
        infoText.setText(getText(R.string.packing_instruction_initial));

        // Display expected packing slip
        scanCodeText = (TextView) findViewById(R.id.scanned_code);
        scanCodeText.setText("scan any barcode");

        flashlight = (ImageButton) findViewById(R.id.flashlight);
        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanner.setTorch(!scanner.getTorchOn());
                displayTorch();
            }
        });

        takePhotoButton = (ImageButton) findViewById(R.id.takePhoto);
        takePhotoButton.setVisibility(View.VISIBLE);
        displayTorch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Force flashlight on start
        scanner.setTorch(true);
        displayTorch();

        Log.d(LOG_TAG, "Waiting for parcel slip scan - any barcode will do");
    }

    @Override
    public void onBackPressed() {
        scanner.cleanUp();
        super.onBackPressed();
    }

    /**
     * handle "real-time" barcode scanning validation and trigger a capture if the customer order ref was found
     *
     * @param code scanned code
     */
    @Override
    public void handleScanResult(final String code, BarcodeType type) {
        boolean codeOk = true;
        int colorText = R.color.colorAccent;

        // Indicate weather the scan was successful, and the following instructions
        CharSequence text = codeOk ? getText(R.string.packing_instruction_analysis) : getText(R.string.packing_scan_KO);

        infoText.setText(text);
        infoText.setTextColor(ContextCompat.getColor(this.getApplicationContext(), colorText));

        if (codeOk) {
            scanner.pauseScanner();
            scanner.pauseCamera();
            scanner.takePicture();
        } else {
            scanner.pauseScanner();
            takePhotoButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Perform an scan of the capture and search for the customer order ref barcode
     *
     * @param jpeg   Array of jpeg bytes picture
     * @param width  Value of the width of the capture
     * @param height Value of the height of the capture
     * @return True is the customer order ref was found, false otherwise
     */
    private boolean imageContainPackingCode(byte[] jpeg, int width, int height) {
        // Decode JPEG to Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

        // Get Raw bitmap content
        int size = width * height;
        int[] argbPixels = new int[size]; // ARGB 8888 pixels source
        byte[] y800Pixels = new byte[size]; // Y800 pixels destination
        bitmap.getPixels(argbPixels, 0, width, 0, 0, width, height);

        // Convert RGB To Y component , Y800 is an Y 8 bits per pixel plane format
        for (int i = 0; i < size; i++) {
            int R = Color.red(argbPixels[i]);
            int G = Color.green(argbPixels[i]);
            int B = Color.blue(argbPixels[i]);
            // the RGB to Y Cr Cb conversion formula is here
            // http://www.fourcc.org/fccyvrgb.php#mikes_answer
            byte Y = (byte) ((0.299 * R) + (0.587 * G) + (0.114 * B));
            y800Pixels[i] = Y;
        }

        // Scan the picture
        Image pic = new Image(width, height, "Y800");
        Rect scanArea = scanner.getAdjustedScannerBounds(width, height);
        pic.setData(y800Pixels);
        pic.setCrop(scanArea.left, scanArea.top, scanArea.right, scanArea.bottom);
        ImageScanner scanner = new ImageScanner();
        scanner.setConfig(0, 256, 0); // 256 =   ZBAR_CFG_X_DENSITY (disable vertical scanning)
        scanner.setConfig(0, 0, 0); //  0 = ZBAR_CFG_ENABLE (disable all symbologies)
        scanner.setConfig(128, 0, 1); //  0 = ZBAR_CFG_ENABLE (enable symbology 128)
        boolean found = false;
        // Scan barcode and customer order ref lookup
        if (scanner.scanImage(pic) != 0) {
            SymbolSet var15 = scanner.getResults();
            Iterator i$ = var15.iterator();
            while (!found && i$.hasNext()) {
                Symbol sym = (Symbol) i$.next();
                String symData = sym.getData();
                found = (symData != null);
            }
        }
        pic.destroy();
        scanner.destroy();
        return found;
    }

    @Override
    public boolean onPictureTaken(final byte[] data, final int width, final int height) {
        if (imageContainPackingCode(data, width, height)) {
            String message = getText(R.string.packing_instruction_confirm).toString();
            infoText.setText(message);

            // Set preview and let user decide if it should be kept.
            final SurfaceView des = (SurfaceView) findViewById(R.id.preview);
            des.setVisibility(View.VISIBLE);
            scanner.setVisibility(View.GONE);

            //TODO: put all this stuff inside a task to avoid blocking the main thread. (low priority)
            des.post(new Runnable() {
                @Override
                public void run() {
                    // Crop the picture to bounds
                    Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Rect boundsToCrop = scanner.getAdjustedFrameBounds(width, height);
                    result = Bitmap.createBitmap(picture, boundsToCrop.left, boundsToCrop.top, boundsToCrop.width(), boundsToCrop.height());

                    // Display the preview
                    Bitmap b2 = Bitmap.createScaledBitmap(result, des.getWidth(), des.getHeight(), true);
                    Canvas cv = des.getHolder().lockCanvas();
                    cv.drawBitmap(b2, des.getMeasuredWidth() - b2.getWidth() > 0 ? (des.getMeasuredWidth() - b2.getWidth()) / 2 : 0, 0, null);
                    des.getHolder().unlockCanvasAndPost(cv);
                }
            });

            findViewById(R.id.bt_ok).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_redo).setVisibility(View.VISIBLE);
        } else {
            String message = getText(R.string.packing_code_not_read).toString();
            infoText.setText(message);
            infoText.setTextColor(ContextCompat.getColor(this.getApplicationContext(), R.color.doneItemColor));
            scanner.pauseScanner();
            scanner.resumeCamera();
            takePhotoButton.setVisibility(View.VISIBLE);
        }
        return false;
    }

    /**
     * Validate scan button handler
     */
    public void onClickOk(View v) {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }

    /**
     * Redo scan button handler
     */
    public void onClickRedo(View v) {
        findViewById(R.id.bt_ok).setVisibility(View.GONE);
        findViewById(R.id.bt_redo).setVisibility(View.GONE);
        scanner.setVisibility(View.VISIBLE);
        final SurfaceView des = (SurfaceView) findViewById(R.id.preview);
        des.setVisibility(View.GONE);

        // Display again the initial instructions
        infoText.setText(getText(R.string.packing_instruction_initial));
        infoText.setTextColor(ContextCompat.getColor(this.getApplicationContext(), R.color.doneItemColor));

        takePhotoButton.setVisibility(View.VISIBLE);
        scanner.pauseScanner();
        scanner.resumeCamera();
    }

    /**
     * Display the torch button "on" or "off" is the device have capability.
     **/
    private void displayTorch() {
        if (!scanner.getSupportTorch()) {
            flashlight.setVisibility(View.GONE);
        }

        boolean isOn = scanner.getTorchOn();
        int iconId = isOn ? R.drawable.icn_flash_off_on : R.drawable.icn_flash_off;

        final int newColor = getResources().getColor(com.enioka.scanner.R.color.flashButtonColor);
        flashlight.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
        flashlight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), iconId));
    }

    /**
     * Scan current picture and hide the "take picture" button
     */
    public void onTakePhotoPressed(View v) {
        // Indicate scanning is on
        infoText.setText(getText(R.string.packing_instruction_scanning));
        infoText.setTextColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorPrimary));

        takePhotoButton.setVisibility(View.GONE);
        scanner.startScanner();
    }
}
