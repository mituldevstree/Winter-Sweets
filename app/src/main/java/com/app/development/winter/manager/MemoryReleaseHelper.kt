package com.app.development.winter.manager

import android.content.Context
import android.util.Log
import java.io.File


object MemoryReleaseHelper {

    fun clearAppCache(context: Context) {
        try {
            val cacheDirectory = context.cacheDir
            deleteDir(cacheDirectory)
            Log.e("check_cycle_ads", "complete clear cache")
        } catch (e: java.lang.Exception) {
            Log.e("check_cycle_ads", "Exception clear cache ${e.message}")
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val childrenDir = children[i]
                if (childrenDir.equals("pollfish", true)) continue
                val success = deleteDir(File(dir, childrenDir))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }

    fun processAndDeleteFromAlFolder(directory: File): Collection<FileData> {
        val fileDataList: MutableList<FileData> = mutableListOf()

        // Get all the files from the directory.
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                val fileSizeInKB = calculateSize(file)

                // Check if the current directory is "al" and the file size exceeds 256
                if (directory.name == "al" && fileSizeInKB > 16) {
                    val deleted = file.delete()
                    if (deleted) {
                        android.util.Log.wtf(
                            "check_cycle_ads FILES",
                            "Deleted file: " + file.absolutePath + " due to size exceeding 256KB"
                        )
                        continue  // Continue to the next iteration since the file was deleted
                    } else {
                        android.util.Log.wtf("check_cycle_ads FILES", "Failed to delete file: " + file.absolutePath)
                    }
                }
                if (file.isDirectory) {
                    // If it's a directory, process its files but don't delete.
                    fileDataList.addAll(processAndDeleteFromAlFolder(file))
                } else {
                    fileDataList.add(FileData(file, fileSizeInKB))
                }
            }
        }

        return fileDataList
    }

    private fun calculateSize(file: File): Long {
        if (file.isFile) {
            return file.length() / 1024 // Convert bytes to kilobytes (KB)
        }
        var size: Long = 0
        val subFiles = file.listFiles()
        if (subFiles != null) {
            for (subFile in subFiles) {
                size += calculateSize(subFile) // Recursive call for directories
            }
        }
        return size
    }

}