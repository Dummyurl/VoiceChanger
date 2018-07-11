package com.bstech.voicechanger.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bstech.voicechanger.application.MyApplication;


/**
 * Utility class for helping parsing file systems.
 */
public final class FileUtil {

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";

        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {

                if (Build.VERSION.SDK_INT > 20) {
                    //getExternalMediaDirs() added in API 21
                    File extenal[] = context.getExternalMediaDirs();
                    if (extenal.length > 1) {
                        filePath = extenal[1].getAbsolutePath();
                        filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                    }
                } else {
                    filePath = "/storage/" + type + "/" + split[1];
                }
                return filePath;
            }

        } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            //final Uri contentUri = ContentUris.withAppendedId(
            // Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    String result = cursor.getString(index);
                    cursor.close();
                    return result;
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            // MediaProvider
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String[] ids = wholeID.split(":");
            String id;
            String type;
            if (ids.length > 1) {
                id = ids[1];
                type = ids[0];
            } else {
                id = ids[0];
                type = ids[0];
            }

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{id};
            final String column = "_data";
            final String[] projection = {column};
            Cursor cursor = context.getContentResolver().query(contentUri,
                    projection, selection, selectionArgs, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex(column);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return filePath;
        } else {
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                if (cursor.moveToFirst())
                    filePath = cursor.getString(column_index);
                cursor.close();
            }


            return filePath;
        }
        return null;
    }

    public static String getFileName(Uri uri,Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * The name of the primary volume (LOLLIPOP).
     */
    private static final String PRIMARY_VOLUME_NAME = "primary";


    public static String getFolderSizeLabel(File file) {
        long size = getFolderSize(file) / 1024; // Get size and convert bytes into Kb.
        if (size >= 1024) {
            return (size / 1024) + " Mb";
        } else {
            return size + " Kb";
        }
    }

    public static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                size += getFolderSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }


    /**
     * Hide default constructor.
     */
    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Determine the camera folder. There seems to be no Android API to work for real devices, so this is a best guess.
     *
     * @return the default camera folder.
     */
    public static String getDefaultCameraFolder() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (path.exists()) {
            File test1 = new File(path, "Camera/");
            if (test1.exists()) {
                path = test1;
            }
            else {
                File test2 = new File(path, "100ANDRO/");
                if (test2.exists()) {
                    path = test2;
                }
                else {
                    path = new File(path, "100MEDIA/");
                }
            }
        }
        else {
            path = new File(path, "Camera/");
        }
        return path.getAbsolutePath();
    }

    /**
     * Copy a file. The target file may even be on external SD card for Kitkat.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    @SuppressWarnings("null")
    public static boolean copyFile(@NonNull final File source, @NonNull final File target) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            else {
                if (SystemUtil.isAndroid5()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, true);
                    if (targetDocument != null) {
                        outStream = MyApplication.getAppContext().getContentResolver().openOutputStream(targetDocument.getUri());
                    }
                }
                else if (SystemUtil.isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreUtil.getUriFromFile(target.getAbsolutePath());
                    if (uri != null) {
                        outStream = MyApplication.getAppContext().getContentResolver().openOutputStream(uri);
                    }
                }
                else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        }
        catch (Exception e) {

            return false;
        }
        finally {
            try {
                inStream.close();
            }
            catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            }
            catch (Exception e) {
                // ignore exception
            }
            try {
                inChannel.close();
            }
            catch (Exception e) {
                // ignore exception
            }
            try {
                outChannel.close();
            }
            catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean deleteFile(@NonNull final File file) {
        // First try the normal deletion.
        if (file.delete()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, false, true);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            ContentResolver resolver = MyApplication.getAppContext().getContentResolver();

            try {
                Uri uri = MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                return !file.exists();
            }
            catch (Exception e) {
                return false;
            }
        }

        return !file.exists();
    }

    /**
     * Move a file. The target file may even be on external SD card.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    public static boolean moveFile(@NonNull final File source, @NonNull final File target) {
        // First try the normal rename.
        boolean success = source.renameTo(target);

        if (!success) {
            success = copyFile(source, target);
            if (success) {
                success = deleteFile(source);
            }
        }

//        if (success) {
//            PupilAndIrisDetector.notifyFileRename(source.getAbsolutePath(), target.getAbsolutePath());
//        }

        return success;
    }

    /**
     * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
     *
     * @param source The source folder.
     * @param target The target folder.
     * @return true if the renaming was successful.
     */
    public static boolean renameFolder(@NonNull final File source, @NonNull final File target) {
        // First try the normal rename.
        if (source.renameTo(target)) {
            return true;
        }
        if (target.exists()) {
            return false;
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (SystemUtil.isAndroid5() && source.getParent().equals(target.getParent())) {
            DocumentFile document = getDocumentFile(source, true, true);
            if (document != null && document.renameTo(target.getName())) {
                return true;
            }
        }

        // Try the manual way, moving files individually.
        if (!mkdir(target)) {
            return false;
        }

        File[] sourceFiles = source.listFiles();

        if (sourceFiles == null) {
            return true;
        }

        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            File targetFile = new File(target, fileName);
            if (!copyFile(sourceFile, targetFile)) {
                // stop on first error
                return false;
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (File sourceFile : sourceFiles) {
            if (!deleteFile(sourceFile)) {
                // stop on first error
                return false;
            }
        }
        return true;
    }

    /**
     * Get a temp file.
     *
     * @param file The base file for which to create a temp file.
     * @return The temp file.
     */
    @NonNull
    public static File getTempFile(@NonNull final File file) {
        File extDir = new File(MyApplication.getAppContext().getExternalCacheDir(), "temp");
        if (!extDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            extDir.mkdirs();
        }
        return new File(extDir, file.getName());
    }

    /**
     * Get all temp files.
     *
     * @return The list of existing temp files.
     */
    public static File[] getTempCameraFiles() {
        File tempDir = getTempCameraFolder();

        File[] files = tempDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(@NonNull final File file) {
                return file.isFile();
            }
        });
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(files);

        return files;
    }

    /**
     * Get the folder where temporary files from the camera are stored.
     *
     * @return The temp folder.
     */
    @NonNull
    public static File getTempCameraFolder() {
        File result = new File(MyApplication.getAppContext().getExternalCacheDir(), "Camera");
        if (!result.exists()) {
            //noinspection ResultOfMethodCallIgnored
            result.mkdirs();
        }
        return result;
    }

    /**
     * Create a folder. The folder may even be on external SD card for Kitkat.
     *
     * @param file The folder to be created.
     * @return True if creation was successful.
     */
    public static boolean mkdir(@NonNull final File file) {
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory();
        }

        // Try the normal way
        if (file.mkdir()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, true, true);
            // getDocumentFile implicitly creates the directory.
            return document != null && document.exists();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            File tempFile = new File(file, "dummyImage.jpg");

            File dummySong = copyDummyFiles();
            if (dummySong == null) {
                return false;
            }
            int albumId = MediaStoreUtil.getAlbumIdFromAudioFile(dummySong);
            Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DATA, tempFile.getAbsolutePath());
            contentValues.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);

            ContentResolver resolver = MyApplication.getAppContext().getContentResolver();
            if (resolver.update(albumArtUri, contentValues, null, null) == 0) {
                resolver.insert(Uri.parse("content://media/external/audio/albumart"), contentValues);
            }
            try {
                ParcelFileDescriptor fd = resolver.openFileDescriptor(albumArtUri, "r");
                if (fd != null) {
                    fd.close();
                }
            }
            catch (Exception e) {
                return false;
            }
            finally {
                FileUtil.deleteFile(tempFile);
            }

            return true;
        }

        return false;
    }

    /**
     * Delete a folder.
     *
     * @param file The folder name.
     * @return true if successful.
     */
    public static boolean rmdir(@NonNull final File file) {
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] fileList = file.list();
        if (fileList != null && fileList.length > 0) {
            // Delete only empty folder.
            return false;
        }

        // Try the normal way
        if (file.delete()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, true, true);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            ContentResolver resolver = MyApplication.getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the created entry, such that content provider will delete the file.
            resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
                    new String[] {file.getAbsolutePath()});
        }

        return !file.exists();
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */
    public static boolean deleteFilesInFolder(@NonNull final File folder) {
        boolean totalSuccess = true;

        String[] children = folder.list();
        if (children != null) {
            for (String child : children) {
                File file = new File(folder, child);
                if (!file.isDirectory()) {
                    boolean success = FileUtil.deleteFile(file);
                    if (!success) {

                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }

    /**
     * Delete a directory asynchronously.
     *
     * @param activity    The activity calling this method.
     * @param file        The folder name.
     * @param postActions Commands to be executed after success.
     */
    public static void rmdirAsynchronously(@NonNull final Activity activity, @NonNull final File file, final Runnable postActions) {
        new Thread() {
            @Override
            public void run() {
                int retryCounter = 5; // MAGIC_NUMBER
                while (!FileUtil.rmdir(file) && retryCounter > 0) {
                    try {
                        Thread.sleep(100); // MAGIC_NUMBER
                    }
                    catch (InterruptedException e) {
                        // do nothing
                    }
                    retryCounter--;
                }
                if (file.exists()) {
//                    DialogUtil.displayError(activity, R.string.message_dialog_failed_to_delete_folder, false,
//                            file.getAbsolutePath());
                }
                else {
                    activity.runOnUiThread(postActions);
                }

            }
        }.start();
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }

    // Utility methods for Android 5

    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    public static boolean isWritableNormalOrSaf(@Nullable final File folder) {
        // Verify that this is a directory.
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return true;
        }

        // Next check SAF writability.
        DocumentFile document = null;
        try {
            document = getDocumentFile(file, false, false);
        }
        catch (Exception e) {
            return false;
        }

        if (document == null) {
            return false;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        document.delete();

        return result;
    }

    /**
     * Get the SD card directory.
     *
     * @return The SD card directory.
     */
    @NonNull
    public static String getSdCardPath() {
        String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
        }
        catch (IOException ioe) {

        }
        return sdCardDirectory;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : MyApplication.getAppContext().getExternalFilesDirs("external")) {
            if (file != null && !file.equals(MyApplication.getAppContext().getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {

                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(@NonNull final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(@NonNull final File file) {
        return getExtSdCardFolder(file) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    public static DocumentFile getDocumentFile(@NonNull final File file, final boolean isDirectory,
                                               final boolean createDirectories) {
        String val = MyApplication.getUriTree();
        if (val == null)    return null;

        Uri[] treeUris = new Uri[] {Uri.parse(val)};
        Uri treeUri = null;

        if (treeUris.length == 0) {
            return null;
        }

        String fullPath;
        try {
            fullPath = file.getCanonicalPath();
        }
        catch (IOException e) {
            return null;
        }

        String baseFolder = null;

        // First try to get the base folder via unofficial StorageVolume API from the URIs.

        for (int i = 0; baseFolder == null && i < treeUris.length; i++) {
            String treeBase = getFullPathFromTreeUri(treeUris[i]);
            if (treeBase != null && fullPath.startsWith(treeBase)) {
                treeUri = treeUris[i];
                baseFolder = treeBase;
            }
        }

        if (baseFolder == null) {
            // Alternatively, take root folder from device and assume that base URI works.
            treeUri = treeUris[0];
            baseFolder = getExtSdCardFolder(file);
        }

        if (baseFolder == null) {
            return null;
        }

        String relativePath = fullPath.substring(baseFolder.length() + 1);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(MyApplication.getAppContext(), treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1) {
                    if (createDirectories) {
                        nextDocument = document.createDirectory(parts[i]);
                    }
                    else {
                        return null;
                    }
                }
                else if (isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                }
                else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    @Nullable
    public static String getFullPathFromTreeUri(@Nullable final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = FileUtil.getVolumePath(FileUtil.getVolumeIdFromTreeUri(treeUri));
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = FileUtil.getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            }
            else {
                return volumePath + File.separator + documentPath;
            }
        }
        else {
            return volumePath;
        }
    }

    /**
     * Get the path of a certain volume.
     *
     * @param volumeId The volume id.
     * @return The path.
     */
    private static String getVolumePath(final String volumeId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) MyApplication.getAppContext().getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        }
        else {
            return null;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        }
        else {
            return File.separator;
        }
    }

    // Utility methods for Kitkat

    /**
     * Copy a resource file into a private target directory, if the target does not yet exist. Required for the Kitkat
     * workaround.
     *
     * @param resource   The resource file.
     * @param folderName The folder below app folder where the file is copied to.
     * @param targetName The name of the target file.
     * @return the dummy file.
     * @throws IOException thrown if there are issues while copying.
     */
    private static File copyDummyFile(final int resource, final String folderName, @NonNull final String targetName)
            throws IOException {
        File externalFilesDir = MyApplication.getAppContext().getExternalFilesDir(folderName);
        if (externalFilesDir == null) {
            return null;
        }
        File targetFile = new File(externalFilesDir, targetName);

        if (!targetFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = MyApplication.getAppContext().getResources().openRawResource(resource);
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ex) {
                        // do nothing
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException ex) {
                        // do nothing
                    }
                }
            }
        }
        return targetFile;
    }

    /**
     * Copy the dummy image and dummy mp3 into the private folder, if not yet there. Required for the Kitkat workaround.
     *
     * @return the dummy mp3.
     */
    private static File copyDummyFiles() {
//        try {
//            copyDummyFile(R.raw.albumart, "mkdirFiles", "albumart.jpg");
//            return copyDummyFile(R.raw.silence, "mkdirFiles", "silence.mp3");
//
//        }
//        catch (IOException e) {
//            Log.e(MyApplication.TAG, "Could not copy dummy files.", e);
//            return null;
//        }
        return null;
    }

    /**
     * Check whether or not file can be writable
     * @param path file path
     * @return true if can write, false otherwise
     */
    public static boolean isWritable(String path) {
        File file = new File(path);
        return file.canWrite();
    }

}