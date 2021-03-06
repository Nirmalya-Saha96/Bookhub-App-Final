package com.nirmalya.bookhubb

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nirmalya.bookhubb.database.BookDatabase
import com.nirmalya.bookhubb.database.BookEntity
import com.nirmalya.bookhubb.model.book
import com.nirmalya.bookhubb.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

    lateinit var toolbar: Toolbar

    var bookId: String?="100"
    //var bookIdd: String?="100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor =findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
       progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"


        if(intent!=null){
            bookId = intent.getStringExtra("book_id")


            //println(bookId)
            //Toast.makeText(this@DescriptionActivity,"$bookId is showing!!!",Toast.LENGTH_SHORT).show()

        }else{
            finish()
            Toast.makeText(this@DescriptionActivity,"Some Unexpected error occured!!!",Toast.LENGTH_SHORT).show()
        }
        if(bookId == "100"){
            finish()
            Toast.makeText(this@DescriptionActivity,"Some Unexpected Error Occured!!!",Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)
        Toast.makeText(this@DescriptionActivity,"Showing $jsonParams!!!",Toast.LENGTH_SHORT).show()

        if(ConnectionManager().checkConnectivity(this@DescriptionActivity)){
            val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try{
                    val success = it.getBoolean("success")
                    if(success){
                        val bookJsonObject = it.getJSONObject("book_data")
                        progressLayout.visibility = View.GONE
                        val bookImageUrl = bookJsonObject.getString("image")
                        Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgBookImage)
                        txtBookName.text = bookJsonObject.getString("name")
                        txtBookAuthor.text = bookJsonObject.getString("author")
                        txtBookPrice.text = bookJsonObject.getString("price")
                        txtBookRating.text = bookJsonObject.getString("rating")
                        txtBookDesc.text = bookJsonObject.getString("description")

                        val bookEntity = BookEntity(
                            bookId?.toInt() as Int,
                            txtBookName.text.toString(),
                            txtBookAuthor.text.toString(),
                             txtBookPrice.text.toString(),
                             txtBookRating.text.toString(),
                            txtBookDesc.text.toString(),
                            bookImageUrl
                        )
                        val checkFav = DBAsyncTask(applicationContext, bookEntity , 1).execute()
                        val isFav = checkFav.get()

                        if(isFav){
                            btnAddToFav.text = "Remove from favourites"
                            val favColor = ContextCompat.getColor(applicationContext , R.color.fav)
                            btnAddToFav.setBackgroundColor(favColor)
                        }else{
                            btnAddToFav.text = "Add to favourites"
                            val noFavColor = ContextCompat.getColor(applicationContext , R.color.teal_200)
                            btnAddToFav.setBackgroundColor(noFavColor)
                        }
                        btnAddToFav.setOnClickListener{
                            if(!DBAsyncTask(applicationContext, bookEntity, 1 ).execute().get()){
                                val async = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                val result = async.get()
                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Book added to favourites",Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Remove from favourites"
                                    val favColor = ContextCompat.getColor(applicationContext , R.color.fav)
                                    btnAddToFav.setBackgroundColor(favColor)
                                }else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occured!!!",Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                val async = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                val result = async.get()
                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Removed from favourites",Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Add to favourites"
                                    val noFavColor = ContextCompat.getColor(applicationContext , R.color.teal_200)
                                    btnAddToFav.setBackgroundColor(noFavColor)
                                }else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occured!!!",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }else{
                        Toast.makeText(this@DescriptionActivity,"Some Error Occured!!!",Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception){
                    Toast.makeText(this@DescriptionActivity,"Some Error Occured!!!",Toast.LENGTH_SHORT).show()
                }

            },Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity,"Volley Error Occured!!!",Toast.LENGTH_SHORT).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }
            }
            queue.add(jsonRequest)
        }else{
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings"){text, listener ->
                //Do nothing
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text, listener ->
                //Do nothing
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }


    }
    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int): AsyncTask<Void,Void,Boolean>(){
        //mode-1: check for fav
        //2: add to fav
        //3: remove form fav

        val db = Room.databaseBuilder(context, BookDatabase::class.java,"books-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when(mode){
                1->{
                    //check DB if the book is fav or not
                    val book: BookEntity?= db.bookDao().getBookId(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2->{
                    //add to favourites
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3->{
                    //remove from fav
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }


            }

            return false
        }

    }
}