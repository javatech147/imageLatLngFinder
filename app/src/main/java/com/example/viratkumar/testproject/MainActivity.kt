package com.example.viratkumar.testproject

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val CAMERA_INTENT_CONSTANT = 23
    private val WRITE_EXTERNAL_STORAGE_PERMISSION: Int = 24
    private val OPEN_GALLERY_REQUEST_CODE = 25
    val TAG = "MainActivity"

    private lateinit var imageFilePath: File
    private lateinit var imageFileUri: Uri

    private lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        imageView = findViewById<ImageView>(R.id.imageView)
        val buttonSelectImage = findViewById<Button>(R.id.btnSelectImage)
        buttonSelectImage.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun showChooserDialog() {
        AlertDialog.Builder(this)
                .setPositiveButton("camera", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        openCamera()
                    }

                })
                .setNegativeButton("Gallery", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        openGallery()
                    }
                })
                .setNeutralButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog!!.dismiss()
                    }
                }).show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, OPEN_GALLERY_REQUEST_CODE)
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_PERMISSION)
        } else {
            showChooserDialog()
        }
    }


    private fun openCamera() {
        val pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageName = getImageName()

        /* imageFilePath will return path in this format
        *  /storage/emulated/0/Pictures/MAN_201835049_214948.jpg
        * */
        imageFilePath = File(pictureDirectory, imageName)


        val authorities = "$packageName.provider"
        //val authorities = "${BuildConfig.APPLICATION_ID}.provider"    // is also valid


        /* imageFile will return path in this format
        *  content://com.example.viratkumar.testproject.provider/external_files/Pictures/MAN_201835049_214948.jpg
        * */
        imageFileUri = FileProvider.getUriForFile(this, "$authorities", imageFilePath)


        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


        //cameraIntent.putExtra(Intent.EXTRA_STREAM, imageFileUri) // Alert, don't use
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri)

        /*
        * You must require to add Intent.FLAG_GRANT_WRITE_URI_PERMISSION if going to access using file uri
        * Need not require to add Intent.FLAG_GRANT_WRITE_URI_PERMISSION if going to use using file path
        */
        cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        startActivityForResult(cameraIntent, CAMERA_INTENT_CONSTANT)
    }

    private fun getImageName(): String {
        val simpleDateFormat = SimpleDateFormat("yyyyDDmm_HHmmss")
        val imageName = simpleDateFormat.format(Date())
        return "MAN_$imageName.jpg"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log(TAG, "onActivityResult")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_INTENT_CONSTANT) {
                /*
                * val imageBitmap: Bitmap = data!!.extras.get("data") as Bitmap
                *
                * Camera intent is going to return captured image either in intent of onActivityResult or
                * at the given location specified by using  cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri)
                * i.e. if you have specified EXTRA_OUTPUT then data!!.extras.get("data") will return null
                * */


                log(TAG, "Image File Uri $imageFileUri")
                log(TAG, "Image File Uri tostring ${imageFileUri.toString()}")


                log(TAG, "Image File Path $imageFilePath")
                //  /storage/emulated/0/Pictures/MAN_201835031_233132.jpg

                log(TAG, "Image File Path absolutePath ${imageFilePath.absolutePath}")
                //  /storage/emulated/0/Pictures/MAN_201835031_233132.jpg

                log(TAG, "Image File Path path ${imageFilePath.path}")
                //  /storage/emulated/0/Pictures/MAN_201835031_233132.jpg

                /*
                * Set image to ImageView using image file uri
                * Even you are getting image from Uri, you also require WRITE_EXTERNAL_STORAGE
                */
                Picasso.get().load(imageFileUri).into(imageView)


                /*
                * Set image to ImageViw using image file path
                * Required WRITE_EXTERNAL_STORAGE permission
                */
                //Picasso.get().load(imageFilePath).into(imageView)

                getMetaDataOfImage(imageFilePath.absolutePath)
            }


            if (requestCode == OPEN_GALLERY_REQUEST_CODE) {
                val imageFileUri = data!!.data

                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(imageFileUri, filePathColumn, null, null, null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val filePath = cursor.getString(columnIndex)
                imageFilePath = File(filePath)
                cursor.close()

                Picasso.get().load(imageFilePath).into(imageView)
                getMetaDataOfImage(imageFilePath.absolutePath)

                log(TAG, "Image File Uri from Gallery :  ${imageFileUri}")
                log(TAG, "Image File Path from Gallery :  ${filePath}")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                showChooserDialog()
            }
        }
    }


    private fun getMetaDataOfImage(imageFilePath: String) {
        /*
        * Using imageFileUri with ExifInterface is Invalid
        * content://com.example.viratkumar.testproject.provider/external_files/Pictures/MAN_201835034_233412.jpg
        * Note -
        * imageFileUri.toString() will return the same path i.e. ontent://com.example.viratkumar.testproject.provider/external_files/Pictures/MAN_201835034_233412.jpg
        * Hence using imageFileUri.toString() is invalid for ExifInterface
        *
        * Image File Path must be in the following form
        * /storage/emulated/0/Pictures/MAN_201835034_233412.jpg
        *
        * */


        val myExifUtils = MyExifUtils(imageFilePath)
        val lat = myExifUtils.latitude
        val lng = myExifUtils.longitude

        log(TAG, "Latitude $lat -- Longitude $lng")

        if (lat != null && lng != null) {

            // Convert lat lng to address
            val address = myExifUtils.latLngToAddress(this, lat, lng)
            val addressString = "Image Captured Details : \nLatitude $lat \nLongitude $lng \nAddress :\n$address"
            val textView = findViewById<TextView>(R.id.textView)
            textView.text = addressString
        } else {
            toast(this, "Unable to get location")
        }
    }

    override fun onBackPressed() {

        AlertDialog.Builder(this)
                .setTitle("Are you sure you want to exit ?")
                .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        super@MainActivity.onBackPressed()
                    }

                })
                .setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog!!.dismiss()
                    }
                }).show()

    }
}