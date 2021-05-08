package com.nirmalya.bookhubb

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.VoiceInteractor
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nirmalya.bookhubb.model.book
import com.nirmalya.bookhubb.util.ConnectionManager
import org.json.JSONException
import java.util.*


class DashboardFragment : Fragment() {
    lateinit var recyclerDashboard : RecyclerView
    lateinit var layoutManager :RecyclerView.LayoutManager
    lateinit var btnCheckInternet: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

   /* val bookList = arrayListOf(
            "PS I LOVE YOU",
            "THE GREAT GATSBY",
            "FIVE MISTAKES OF MY LIFE",
            "TIN TIN",
            "NIRMALYO SAHA",
            "LORD OF RINGS",
            "HARRY POTTER",
            "AVENGERS ASSEMBLY",
            "PRINCE OF PERSIA",
            "MOMY"
        )*/
    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    val bookInfoList = arrayListOf<book>()
    /*= arrayListOf<book>(
            book("P.S. I love You", "Cecelia Ahern", "Rs. 299", "4.5", R.drawable.ps_ily),
            book("The Great Gatsby", "F. Scott Fitzgerald", "Rs. 399", "4.1", R.drawable.great_gatsby),
            book("Anna Karenina", "Leo Tolstoy", "Rs. 199", "4.3", R.drawable.anna_kare),
            book("NIRMALYO SAHA", "nirmalya saha", "Rs. 500", "5.0", R.drawable.me),
            book("War and Peace", "Leo Tolstoy", "Rs. 249", "4.8", R.drawable.war_and_peace),
            book("Lolita", "Vladimir Nabokov", "Rs. 349", "3.9", R.drawable.lolita),
            book("Middlemarch", "George Eliot", "Rs. 599", "4.2", R.drawable.middlemarch),
            book("The Adventures of Huckleberry Finn", "Mark Twain", "Rs. 699", "4.5", R.drawable.adventures_finn),
            book("Moby-Dick", "Herman Melville", "Rs. 499", "4.5", R.drawable.moby_dick),
            book("The Lord of the Rings", "J.R.R Tolkien", "Rs. 749", "5.0", R.drawable.lord_of_rings)
    )*/


    var ratingComparator =  Comparator<book>{ book1 , book2->

        if(book1.bookRating.compareTo(book2.bookRating,true)==0){
            book1.bookName.compareTo(book2.bookName,true)
        }else{
            book1.bookRating.compareTo(book2.bookRating,true)
        }

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        
        setHasOptionsMenu(true)
        

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        btnCheckInternet = view.findViewById(R.id.btnCheckInternet)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)

        progressLayout.visibility = View.VISIBLE

        btnCheckInternet.setOnClickListener {
            if(ConnectionManager().checkConnectivity(activity as Context)){
                //Internet is available
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Success")
                dialog.setMessage("Internet Connection Found")
                dialog.setPositiveButton("OK"){text, listener ->
                    //Do nothing
                }
                dialog.setNegativeButton("Cancel"){text, listener ->
                    //Do nothing
                }
                dialog.create()
                dialog.show()
            }else{
                //Internet is not available
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Not Found")
                dialog.setPositiveButton("OK"){text, listener ->
                    //Do nothing
                }
                dialog.setNegativeButton("Cancel"){text, listener ->
                    //Do nothing
                }
                dialog.create()
                dialog.show()
            }
        }
        layoutManager = LinearLayoutManager(activity)

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if(ConnectionManager().checkConnectivity(activity as Context)){
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                //Here we handel the reponse
                try{
                    progressLayout.visibility = View.GONE
                    val success = it.getBoolean("success")
                    if(success){
                        val data = it.getJSONArray("data")
                        for(i in 0 until data.length()){
                            val bookJsonObject = data.getJSONObject(i)
                            val bookObject = book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                            )
                            bookInfoList.add(bookObject)
                            recyclerAdapter = DashboardRecyclerAdapter(activity as Context,bookInfoList)

                            recyclerDashboard.adapter = recyclerAdapter
                            recyclerDashboard.layoutManager = layoutManager

                            recyclerDashboard.addItemDecoration(
                                    DividerItemDecoration(
                                            recyclerDashboard.context,
                                            (layoutManager as LinearLayoutManager).orientation
                                    )
                            )
                        }
                    }else{
                        Toast.makeText(activity as Context,"Some Error Occured",Toast.LENGTH_SHORT).show()
                    }
                }catch (e: JSONException){
                    Toast.makeText(activity as Context,"Some Unexpected Error Occured!!!",Toast.LENGTH_SHORT).show()
                }


            }, Response.ErrorListener {

                //Here we handel the errors
                if(activity != null){
                    Toast.makeText(activity as Context, "Volley Error Occured!!!",Toast.LENGTH_SHORT).show()
                }


            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        }else{
            //Internet is not available
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings"){text, listener ->
                //Do nothing
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit"){text, listener ->
                //Do nothing
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_dashboard,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if(id==R.id.action_sort){
            Collections.sort(bookInfoList , ratingComparator)
            bookInfoList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)

    }




}