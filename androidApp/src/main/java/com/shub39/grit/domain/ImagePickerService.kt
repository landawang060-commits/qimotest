package com.shub39.grit.domain

interface ImagePickerService {
    fun takePhoto(callback: (String?) -> Unit)
    fun pickImage(callback: (String?) -> Unit)
}
