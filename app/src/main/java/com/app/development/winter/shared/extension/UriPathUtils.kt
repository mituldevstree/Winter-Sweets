package com.app.development.winter.shared.extension

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

@Suppress("DEPRECATION")
object UriPathUtils {

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        when {
            // DocumentProvider
            DocumentsContract.isDocumentUri(context, uri) -> {
                when {
                    // ExternalStorageProvider
                    isExternalStorageDocument(uri) -> {
                        // Toast.makeText(context, "From Internal & External Storage dir", Toast.LENGTH_SHORT).show()
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        // This is for checking Main Memory
                        return if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + split[1]
                            } else {
                                Environment.getExternalStorageDirectory().toString() + "/"
                            }
                            // This is for checking SD Card
                        } else {
                            "storage" + "/" + docId.replace(":", "/")
                        }
                    }

                    isDownloadsDocument(uri) -> {
                        // Toast.makeText(context, "From Downloads dir", Toast.LENGTH_SHORT).show()
                        val fileName = getFilePath(context, uri)
                        if (fileName != null) {
                            return Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                        }
                        var id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            id = id.replaceFirst("raw:".toRegex(), "")
                            val file = File(id)
                            if (file.exists()) return id
                        }
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        return getDataColumn(context, contentUri, null, null)
                    }

                    isMediaDocument(uri) -> {
                        // Toast.makeText(context, "From Documents dir", Toast.LENGTH_SHORT).show()
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        val contentUri: Uri?
                        when (type) {
                            "image" -> {
                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }

                            "video" -> {
                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }

                            "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }

                            else -> {
                                // non-media files i.e documents and other files
                                contentUri = MediaStore.Files.getContentUri("external")
                                val selection =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        MediaStore.MediaColumns.RELATIVE_PATH + "=?"
                                    } else "_id=?"
                                val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS)
                                return getMediaDocumentPath(
                                    context,
                                    contentUri,
                                    selection,
                                    selectionArgs
                                )
                            }
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }

                    isGoogleDriveUri(uri) -> {
                        Toast.makeText(context, "From Google Drive", Toast.LENGTH_SHORT).show()
                        return getDriveFilePath(uri, context)
                    }
                    /*else -> {
                        Toast.makeText(context, "Unknown Directory", Toast.LENGTH_SHORT).show()
                    }*/
                }
            }

            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Toast.makeText(context, "From Content Uri", Toast.LENGTH_SHORT).show()
                // Return the remote address
                return when {
                    isGooglePhotosUri(uri) -> uri.lastPathSegment
                    isGoogleDriveUri(uri) || isOneDriveUri(uri) -> getDriveFilePath(uri, context)
                    else -> getDataColumn(context, uri, null, null)
                }
            }

            "file".equals(uri.scheme, ignoreCase = true) -> {
                // Toast.makeText(context, "From File Uri", Toast.LENGTH_SHORT).show()
                return uri.path
            }
        }
        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: IllegalArgumentException) {
            Log.d("Exception",e.message.toString())
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getMediaDocumentPath(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getDriveFilePath(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            // int bufferSize = 1024;
            val bufferSize = min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("Size %s", file.length().toString())
            inputStream.close()
            outputStream.close()
            returnCursor.close()
            Log.e("Path %s", file.path)
            Log.e("Size %s", file.length().toString())
        } catch (e: Exception) {
            Log.e("Exception",e.message.toString())
        }
        return file.path
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority ||
                "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is One Drive.
     */
    private fun isOneDriveUri(uri: Uri): Boolean {
        return "com.microsoft.skydrive.content.external" == uri.authority
    }
}
