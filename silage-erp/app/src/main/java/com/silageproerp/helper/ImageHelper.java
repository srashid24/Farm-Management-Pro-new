package com.silageproerp.helper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper to pick images from gallery or take a photo,
 * save to internal app storage, and load into an ImageView.
 */
public class ImageHelper {

    public static final int REQUEST_GALLERY = 1001;
    public static final int REQUEST_CAMERA  = 1002;

    /** Launch gallery picker */
    public static void pickFromGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_GALLERY);
    }

    /** Launch camera */
    public static void takePhoto(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    /**
     * Save URI image to internal storage and return path.
     * Call this from onActivityResult when REQUEST_GALLERY is returned.
     */
    public static String saveImageFromUri(Context context, Uri uri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File dir = new File(context.getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) fos.write(buffer, 0, len);
            fos.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Save Bitmap (from camera) to internal storage and return path.
     */
    public static String saveBitmap(Context context, Bitmap bitmap, String fileName) {
        try {
            File dir = new File(context.getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Load image from path into an ImageView. Falls back to placeholder if path is null/invalid.
     */
    public static void loadImage(ImageView imageView, String imagePath, int placeholder) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                if (bmp != null) {
                    imageView.setImageBitmap(bmp);
                    return;
                }
            }
        }
        imageView.setImageResource(placeholder);
    }

    /**
     * Delete an image file from internal storage.
     */
    public static void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) file.delete();
        }
    }
}
