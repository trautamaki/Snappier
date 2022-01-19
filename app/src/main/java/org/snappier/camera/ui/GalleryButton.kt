package org.snappier.camera.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

class GalleryButton(context: Context, attrs: AttributeSet) :
    com.google.android.material.floatingactionbutton.FloatingActionButton(context, attrs),
    View.OnClickListener {

    private val galleryIntent: Intent

    init {
        setOnClickListener(this)
        galleryIntent = Intent(Intent.ACTION_DEFAULT)
            .setType("image/*")

        val packageManager = context.packageManager
        val galleryPackage = getPackageForGallery(packageManager)
        if (galleryPackage.isNotEmpty()) {
            val icon: Drawable = packageManager.getApplicationIcon(galleryPackage)
            setImageDrawable(icon)
        }
    }

    private fun getPackageForGallery(packageManager: PackageManager): String {
        val packages = packageManager.queryIntentActivities(galleryIntent, 0)
        for (res in packages) {
            return res.activityInfo.packageName
        }
        return ""
    }

    override fun onClick(v: View?) {
        context.startActivity(galleryIntent)
    }
}