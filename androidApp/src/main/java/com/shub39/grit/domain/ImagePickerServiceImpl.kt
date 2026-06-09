package com.shub39.grit.domain

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.shub39.grit.app.GritApplication
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "ImagePickerService"

class ImagePickerServiceImpl : ImagePickerService {

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var currentCallback: ((String?) -> Unit)? = null
    private var currentPhotoPath: String? = null

    fun registerActivityResultLaunchers(activity: Activity) {
        Log.d(TAG, "registerActivityResultLaunchers: 开始注册ActivityResultLaunchers")
        
        takePhotoLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "takePhotoLauncher: 返回结果, resultCode=${result.resultCode}")
            
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "takePhotoLauncher: 拍照成功, photoPath=$currentPhotoPath")
                currentCallback?.invoke(currentPhotoPath)
            } else {
                Log.w(TAG, "takePhotoLauncher: 拍照取消或失败, resultCode=${result.resultCode}")
                currentCallback?.invoke(null)
            }
            currentCallback = null
        }

        pickImageLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "pickImageLauncher: 返回结果, resultCode=${result.resultCode}, data=${result.data}")
            
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data
                Log.d(TAG, "pickImageLauncher: 选择图片成功, uri=$uri")
                
                uri?.let {
                    val path = getRealPathFromURI(it)
                    Log.d(TAG, "pickImageLauncher: URI转换结果, path=$path")
                    currentCallback?.invoke(path)
                } ?: run {
                    Log.w(TAG, "pickImageLauncher: URI为空")
                    currentCallback?.invoke(null)
                }
            } else {
                Log.w(TAG, "pickImageLauncher: 选择图片取消或失败")
                currentCallback?.invoke(null)
            }
            currentCallback = null
        }
        
        Log.d(TAG, "registerActivityResultLaunchers: ActivityResultLaunchers注册完成")
    }

    override fun takePhoto(callback: (String?) -> Unit) {
        Log.d(TAG, "takePhoto: 开始拍照流程")
        currentCallback = callback

        val context = GritApplication.instance
        
        // 检查相机权限
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "takePhoto: 相机权限检查结果: $hasPermission")
        
        if (!hasPermission) {
            Log.e(TAG, "takePhoto: 相机权限未授予")
            Toast.makeText(context, "需要相机权限", Toast.LENGTH_SHORT).show()
            callback(null)
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val hasCameraApp = intent.resolveActivity(context.packageManager) != null
        
        Log.d(TAG, "takePhoto: 是否有可用相机应用: $hasCameraApp")
        
        if (hasCameraApp) {
            val photoFile = createImageFile()
            
            if (photoFile != null) {
                currentPhotoPath = photoFile.absolutePath
                Log.d(TAG, "takePhoto: 创建照片文件成功, path=$currentPhotoPath")
                
                try {
                    val photoURI = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    Log.d(TAG, "takePhoto: FileProvider URI生成成功, uri=$photoURI")
                    
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePhotoLauncher.launch(intent)
                    Log.d(TAG, "takePhoto: 启动相机Intent成功")
                } catch (e: Exception) {
                    Log.e(TAG, "takePhoto: FileProvider URI生成失败", e)
                    Toast.makeText(context, "无法创建照片文件", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            } else {
                Log.e(TAG, "takePhoto: 创建照片文件失败")
                Toast.makeText(context, "无法创建照片文件", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } else {
            Log.e(TAG, "takePhoto: 没有可用的相机应用")
            Toast.makeText(context, "没有可用的相机应用", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    override fun pickImage(callback: (String?) -> Unit) {
        Log.d(TAG, "pickImage: 开始选择图片流程")
        currentCallback = callback

        val context = GritApplication.instance
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        
        val hasGalleryApp = intent.resolveActivity(context.packageManager) != null
        
        Log.d(TAG, "pickImage: 是否有可用图库应用: $hasGalleryApp")
        
        if (hasGalleryApp) {
            pickImageLauncher.launch(intent)
            Log.d(TAG, "pickImage: 启动图库Intent成功")
        } else {
            Log.e(TAG, "pickImage: 没有可用的图片应用")
            Toast.makeText(context, "没有可用的图片应用", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun createImageFile(): File? {
        Log.d(TAG, "createImageFile: 开始创建临时照片文件")
        
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = GritApplication.instance.getExternalFilesDir(null)
            
            Log.d(TAG, "createImageFile: storageDir=$storageDir")
            
            if (storageDir == null) {
                Log.e(TAG, "createImageFile: 存储目录为空")
                return null
            }
            
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
            
            Log.d(TAG, "createImageFile: 文件创建成功, path=${file.absolutePath}")
            file
        } catch (ex: Exception) {
            Log.e(TAG, "createImageFile: 文件创建失败", ex)
            null
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        Log.d(TAG, "getRealPathFromURI: 开始转换URI, uri=$uri")
        
        return try {
            val context = GritApplication.instance
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            
            if (cursor == null) {
                Log.w(TAG, "getRealPathFromURI: Cursor为空")
                return null
            }
            
            val moved = cursor.moveToFirst()
            Log.d(TAG, "getRealPathFromURI: Cursor移动到第一条记录: $moved")
            
            if (!moved) {
                cursor.close()
                Log.w(TAG, "getRealPathFromURI: Cursor无数据")
                return null
            }
            
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            Log.d(TAG, "getRealPathFromURI: 列索引: $columnIndex")
            
            val path = if (columnIndex >= 0) cursor.getString(columnIndex) else null
            cursor.close()
            
            Log.d(TAG, "getRealPathFromURI: 转换结果, path=$path")
            path
        } catch (ex: Exception) {
            Log.e(TAG, "getRealPathFromURI: URI转换失败", ex)
            null
        }
    }
}
