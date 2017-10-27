package de.phytec.iot.phynode;

/*
    Copyright 2017  PHYTEC Messtechnik GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static de.phytec.iot.phynode.PeripheralActivity.EXTRA_DEVICE_ADDRESS;

public class FragmentEpaper extends Fragment implements BleCallbacks/*, PopupMenu.OnMenuItemClickListener*/ {

    private static final String TAG = "FragmentEpaper";
    private static final int ACTIVITY_FILE_CHOOSER = 42;

    private BleManager mBleManager = null;

    private ImageView mImagePreview;
    //private ImageButton mButtonEpaperMore;
    private Bitmap mBitmapPreview;
    private Bitmap mBitmapLogo = null;
    private Uri mBitmapLogoUri;
    private Paint mPaint;
    private Canvas mCanvas;
    private EditText mEditTitle;
    private EditText mEditName;
    private EditText mEditDepartment;
    private ImageButton mButtonLogo;
    private ProgressBar mProgressSend;
    private TextView mTextStatus;
    private Button mButtonSend;

    private int mProgressCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_epaper, container, false);

        mBleManager = new BleManager(getActivity(), this);
        mBleManager.setBleDeviceAddress(getActivity().getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS));

        mImagePreview = (ImageView) view.findViewById(R.id.image_preview);
        /*mButtonEpaperMore = (ImageButton) view.findViewById(R.id.button_epaper_more);
        mButtonEpaperMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(FragmentEpaper.this);
                popupMenu.inflate(R.menu.menu_epaper);
                popupMenu.show();
            }
        });*/
        mBitmapPreview = Bitmap.createBitmap(250, 128, Bitmap.Config.ALPHA_8);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mCanvas = new Canvas(mBitmapPreview);
        ColorMatrix matrix = new ColorMatrix();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        matrix.setSaturation(0);
        mPaint.setColorFilter(filter);
        mTextStatus = (TextView) view.findViewById(R.id.text_status);
        mProgressSend = (ProgressBar) view.findViewById(R.id.progress_send);
        mProgressSend.setVisibility(View.GONE);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateImagePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        mEditTitle = (EditText) view.findViewById(R.id.edit_title);
        mEditTitle.addTextChangedListener(textWatcher);
        mEditName = (EditText) view.findViewById(R.id.edit_name);
        mEditName.addTextChangedListener(textWatcher);
        mEditDepartment = (EditText) view.findViewById(R.id.edit_department);
        mEditDepartment.addTextChangedListener(textWatcher);
        mButtonLogo = (ImageButton) view.findViewById(R.id.button_logo);
        mProgressSend = (ProgressBar) view.findViewById(R.id.progress_send);
        mButtonSend = (Button) view.findViewById(R.id.button_send);

        mButtonLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
                fileChooser.setType("image/*");
                Intent intent = Intent.createChooser(fileChooser, "Choose a Logo");
                startActivityForResult(intent, ACTIVITY_FILE_CHOOSER);
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonSend();
            }
        });

        loadImage(null);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBleManager.disconnect();
        saveImage(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_FILE_CHOOSER:
                if (resultCode != RESULT_OK)
                    return;

                mBitmapLogoUri = data.getData();

                try {
                    mBitmapLogo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mBitmapLogoUri);
                } catch (Exception e) {
                    PhytecLog.e(TAG, e.getMessage());
                }

                updateImagePreview();
                break;
            default:
                break;
        }
    }

    /*@Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menuitem_load_image:
                PhytecLog.i(TAG, "load");
                loadImage(null);
                return true;
            case R.id.menuitem_save_image:
                PhytecLog.i(TAG, "save");
                saveImage(null);
                return true;
            default:
                return false;
        }
    }*/

    // private

    private void onButtonSend() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap image_rot = Bitmap.createBitmap(mBitmapPreview, 0, 0, 250, 128, matrix, false);
        ByteBuffer bb = ByteBuffer.allocate(image_rot.getByteCount());
        PhytecLog.i(TAG, "ByteBuffer bb = " + bb.toString() + "\n" + image_rot.getByteCount());
        image_rot.copyPixelsToBuffer(bb);
        ArrayList<byte[]> data = toByteArrayList(bb.array());
        PhytecLog.i(TAG, "data[" + data.size() + "] = " + data.toString());

        mBleManager.sendEpaperImage(data);
    }

    private void updateImagePreview() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        mPaint.setTextSize(22);
        mCanvas.drawText(mEditTitle.getText().toString(), 4, 28, mPaint);

        mPaint.setTextSize(32);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        TextPaint textPaint = new TextPaint(mPaint);
        StaticLayout layout = new StaticLayout(mEditName.getText().toString(), textPaint,
                mCanvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, -4, true);
        mCanvas.save();

        if (layout.getLineCount() == 1)
            mCanvas.translate(4, 40);
        else
            mCanvas.translate(4, 28);

        layout.draw(mCanvas);
        mCanvas.restore();

        mPaint.setTextSize(19);
        mPaint.setTypeface(Typeface.DEFAULT);

        if (layout.getLineCount() == 1)
            mCanvas.drawText(mEditDepartment.getText().toString(), 4, 120, mPaint);
        else
            mCanvas.drawText(mEditDepartment.getText().toString(), 4, 124, mPaint);

        if (mBitmapLogo != null) {
            final int x = 250 - mBitmapLogo.getWidth();
            final int y = 6;
            mCanvas.drawBitmap(mBitmapLogo, x, y, mPaint);
            mButtonLogo.setImageBitmap(mBitmapLogo);
        }

        mImagePreview.setImageBitmap(mBitmapPreview);
    }

    // Convert a single byte[] to an ArrayList<byte[]> with 250 byte[], where one byte contains
    // 8 pixels (one pixel equals one bit). The ArrayList contains byte[], which in turn consist of
    // 16 byte (1 column = 16 byte = 16 * 8 bit) each.
    private ArrayList<byte[]> toByteArrayList(byte[] array) {
        if (array.length != 32000) // 32000 = 250 * 128
            return null;

        ArrayList<byte[]> dataEightBpp = new ArrayList<>();
        ArrayList<byte[]> dataOneBpp = new ArrayList<>();

        // split the byte[] into a new ArrayList<byte[]>, data is still 8 bpp
        for (int i = 0; i < 32000; i += 128)
            dataEightBpp.add(Arrays.copyOfRange(array, i, i + 128));

        // create new ArrayList<byte[]> but now with 1 bpp
        for (byte[] bytes : dataEightBpp) {
            // java does not know about unsigned bytes and uses signed bytes instead, so -1 is
            // equivalent to 255
            byte[] column = {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            };

            int k = 0;
            int offset = 7;

            for (int i = 0; i < 128; i++) {
                if (bytes[i] != 0)
                    column[k] &= ~(1 << offset);

                if (offset == 0) {
                    offset = 7;
                    k++;
                } else {
                    offset--;
                }
            }

            dataOneBpp.add(column);
        }

        return dataOneBpp;
    }

    public void loadImage(String imageName) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // load the previous entries
        if (imageName == null) {
            String title = sharedPreferences.getString(getString(R.string.saved_title), "Default Title");
            String name = sharedPreferences.getString(getString(R.string.saved_name), "Default Name");
            String department = sharedPreferences.getString(getString(R.string.saved_department), "Default Department");

            mEditTitle.setText(title);
            mEditName.setText(name);
            mEditDepartment.setText(department);
        }
    }

    public void saveImage(String imageName) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // save the current entries
        if (imageName == null) {
            editor.putString(getString(R.string.saved_title), mEditTitle.getText().toString());
            editor.putString(getString(R.string.saved_name), mEditName.getText().toString());
            editor.putString(getString(R.string.saved_department), mEditDepartment.getText().toString());
        }

        editor.apply();
    }

    // BleCallbacks

    @Override
    public void deviceFound(String address, String name, int rssi) {}

    @Override
    public void scanStarted() {}

    @Override
    public void scanStopped() {}

    @Override
    public void servicesFound(final List<BluetoothGattService> services) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (BluetoothGattService service : services) {
                    PhytecLog.i(TAG, "\t" + service.getUuid().toString());

                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        PhytecLog.i(TAG, "\t\t" + characteristic.getUuid().toString());
                    }
                }
            }
        });
    }

    @Override
    public void sendingStarted() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonLogo.setEnabled(false);
                mButtonSend.setText(R.string.sending);
                mButtonSend.setEnabled(false);
                mProgressSend.setProgress(0);
                mProgressSend.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void sendingStatusChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mBleManager.getStatus()) {
                    case SERVICES_DISCOVERED:
                        mTextStatus.setText(R.string.services_discovered);
                        mProgressSend.setProgress(10);
                        break;
                    case WROTE_INIT:
                        mProgressCounter = 0;
                        mTextStatus.setText(R.string.epaper_init);
                        mProgressSend.setProgress(20);
                        break;
                    case WROTE_BUFFER_COLUMN:
                        mTextStatus.setText(R.string.epaper_buffer);
                        if (mProgressCounter++ % 4 == 0) {
                            mProgressSend.setProgress(mProgressSend.getProgress() + 1);
                        }
                        break;
                    case WROTE_BUFFER:
                        mTextStatus.setText(R.string.epaper_buffer);
                        mProgressSend.setProgress(90);
                        break;
                    case WROTE_UPDATE:
                        mTextStatus.setText(R.string.epaper_update);
                        mProgressSend.setProgress(100);
                        break;
                    case NONE:
                    default:
                        mProgressSend.setProgress(0);
                        mTextStatus.setText(R.string.disconnected);
                        break;
                }
            }
        });
    }

    @Override
    public void sendingStopped() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonLogo.setEnabled(true);
                mButtonSend.setText(R.string.send);
                mButtonSend.setEnabled(true);
            }
        });
    }

    @Override
    public void connected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextStatus.setText(R.string.connected);
            }
        });
    }

    @Override
    public void disconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextStatus.setText(R.string.disconnected);
                mProgressSend.setVisibility(View.GONE);
            }
        });
    }
}