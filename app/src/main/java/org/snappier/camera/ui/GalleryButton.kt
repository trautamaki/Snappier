package org.snappier.camera.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.util.Size
import android.widget.ImageView

class GalleryButton(context: Context, attrs: AttributeSet) :
    ImageView(context, attrs),
    View.OnClickListener {

    var previousFile: Uri? = null

    init {
        setOnClickListener(this)

        val packageManager = context.packageManager
        val galleryPackage = getGalleryPackage(packageManager)
        if (galleryPackage.isNotEmpty()) {
            val icon: Drawable = packageManager.getApplicationIcon(galleryPackage)
            setImageDrawable(icon)
        }
    }

    fun setNewFile(uri: Uri?) {
        previousFile = uri

        if (uri != null) {
            val thumbnail = context.contentResolver.loadThumbnail(
                uri, Size(100,100), null)

            setImageBitmap(thumbnail)
        }
    }

    private fun getGalleryPackage(packageManager: PackageManager): String {
        val galleryIntent = Intent(Intent.ACTION_DEFAULT)
            .setType("image/*")
        val packages = packageManager.queryIntentActivities(galleryIntent, 0)
        for (res in packages) {
            return res.activityInfo.packageName
        }

        return ""
    }

    override fun onClick(v: View?) {
        val intent = Intent()

        if (previousFile != null) {
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(previousFile, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent)

            return
        }

        intent.action = Intent.ACTION_DEFAULT
        intent.type = "image/*"
        context.startActivity(intent)
    }
}