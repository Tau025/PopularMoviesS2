package com.devtau.popularmoviess2.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/**
 * Хелпер для операций работы с файловым хранилищем
 * Helper for work with local file storage
 */
public class FileManager {
    private static final String LOG_TAG = FileManager.class.getSimpleName();
    private static String fullAppDirectoryPath = "";

    @Nullable
    public static File getImageFromCache(Context context, String posterPath) {
        String appDir = getFullAppDirectoryPath(context);
        String fileName = getImageFileName(posterPath);
        File photoFile = new File(appDir, fileName);
        return checkFileExists(photoFile) ? photoFile : null;
    }

    public static void saveImageToCache(Context context, String posterPath, Bitmap bitmap) {
        String appDir = getFullAppDirectoryPath(context);
        OutputStream outputStream = null;

        try {
            //Создадим новый файл для картинки
            //Create new file for our image
            File file = new File(appDir, getImageFileName(posterPath));
            Logger.d(LOG_TAG, "Creating new file at: " + String.valueOf(file.getAbsolutePath()));
            outputStream = new FileOutputStream(file);

            //Сожмем картинку и сохраним ее в созданный выше файл
            //Compress our image and save it to earlier created file
            bitmap = resizeBitmapToExactWidthAndHeight(bitmap, context);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            killFileWithDelay(file);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while handling FileOutputStream", e);
        } finally {
            try {
                if(outputStream != null) {
//                    Logger.v(LOG_TAG, "Closing outputStream");
                    outputStream.close();
                }
            } catch (IOException e) {
                Logger.e(LOG_TAG, "Error while closing outputStream", e);
            }
        }
    }

    private static Bitmap resizeBitmapToExactWidthAndHeight(Bitmap bitmap, Context context) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
//        Logger.v(LOG_TAG, "old w*h: " + String.valueOf(width)+ "*" + String.valueOf(height));
        int newWidthSP = Utility.getPosterWidthAndHeight(context)[0];
        int newHeightSP = Utility.getPosterWidthAndHeight(context)[1];

        float scaleWidth = ((float) newWidthSP) / width;
        float scaleHeight = ((float) newHeightSP) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
//        Logger.v(LOG_TAG, "new w*h: " + String.valueOf(resizedBitmap.getWidth())+ "*" +
//                String.valueOf(resizedBitmap.getHeight()));
        return resizedBitmap;
    }

    private static Bitmap resizeBitmapKeepingAspectRatio(Bitmap bitmap, int maxWidth, int maxHeight) {
        float scale = Math.min(((float)maxHeight / bitmap.getWidth()),
                ((float)maxWidth / bitmap.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }

    @NonNull
    private static String getFullAppDirectoryPath(Context context) {
        if(!"".equals(fullAppDirectoryPath)) return fullAppDirectoryPath;

        //Найдем на устройстве папку нашего приложения по умолчанию
        //Locate default app directory on device
        String baseDir = context.getApplicationInfo().dataDir;

        //И создадим внутри нее папку кэша
        //Then create a cache directory inside of it
        File appDir = new File(baseDir, Constants.IMAGES_CACHE_DIR_NAME);
        if (!checkDirectoryExists(appDir)) {
//            Logger.d(LOG_TAG, "Dir not found. Creating at: " + String.valueOf(appDir.getAbsolutePath()));
            if(!appDir.mkdirs()) {
                Logger.e(LOG_TAG, "Failed to create image cache directory");
            }
        }

        fullAppDirectoryPath = appDir.getPath();
        return fullAppDirectoryPath;
    }

    //Метод нужен из-за того, что мы конвертируем все в jpeg независимо от формата на сервере
    //Method is needed because we convert all images to jpeg regardless of their format on server
    private static String getImageFileName(String posterPath) {
        String fileName = posterPath.substring(posterPath.lastIndexOf('/') + 1, posterPath.indexOf('.'));
        return fileName + Constants.CACHED_IMAGE_EXTENSION;
    }

    private static boolean checkDirectoryExists(File folder) {
        return folder.exists() && folder.isDirectory();
    }

    private static boolean checkFileExists(File file) {
        return file.exists() && file.isFile();
    }

    //Определим срок жизни фотографии в кеше на устройстве
    //Define lifetime of image in the device cache
    private static void killFileWithDelay(final File file) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String fileName = file.getAbsolutePath();
                boolean isDeleted = file.delete();
                Logger.d(LOG_TAG, "file: " + fileName + (isDeleted ? " had been deleted" : " is alive"));
            }
        }, Constants.CASHED_IMAGE_LIFETIME);
    }
}
