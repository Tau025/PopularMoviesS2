package com.devtau.popularmoviess2.utility;

import android.content.Context;
import android.graphics.Bitmap;
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
            //создадим новый файл для картинки
            File file = new File(appDir, getImageFileName(posterPath));
            Logger.d(LOG_TAG, "Creating new file at: " + String.valueOf(file.getAbsolutePath()));
            outputStream = new FileOutputStream(file);

            //сожмем картинку в JPEG и сохраним ее в созданный выше файл
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            killFileWithDelay(file);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while handling FileOutputStream", e);
        } finally {
            try {
                if(outputStream != null) {
                    Logger.v(LOG_TAG, "Closing outputStream");
                    outputStream.close();
                }
            } catch (IOException e) {
                Logger.e(LOG_TAG, "Error while closing outputStream", e);
            }
        }
    }

    @NonNull
    private static String getFullAppDirectoryPath(Context context) {
        if(!"".equals(fullAppDirectoryPath)) return fullAppDirectoryPath;

        //найдем на устройстве папку нашего приложения по умолчанию
        //locate default app directory on device
        String baseDir = context.getApplicationInfo().dataDir;

        //и создадим внутри нее папку кэша
        //then create a cache directory inside of it
        File appDir = new File(baseDir, Constants.IMAGES_CACHE_DIR_NAME);
        if (!checkDirectoryExists(appDir)) {
            Logger.d(LOG_TAG, "Dir not found. Creating at: " + String.valueOf(appDir.getAbsolutePath()));
            if(!appDir.mkdirs()) {
                Logger.e(LOG_TAG, "Failed to create image cache directory");
            }
        }

        fullAppDirectoryPath = appDir.getPath();
        return fullAppDirectoryPath;
    }

    //метод нужен из-за того, что мы конвертируем все в jpg независимо от формата на сервере
    //method is needed because we convert all images to jpeg regardless of their format on server
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

    //определим срок жизни фотографии в кеше на устройстве
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
