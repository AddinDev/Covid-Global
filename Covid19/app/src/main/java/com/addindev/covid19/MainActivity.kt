package com.addindev.covid19

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.addindev.covid19.adapter.AdapterCountry
import com.addindev.covid19.model.CountriesItem
import com.addindev.covid19.model.ResponseCountry
import com.addindev.covid19.network.ApiService
import com.addindev.covid19.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private var ascending = true

    companion object {
        lateinit var adapters: AdapterCountry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapters.filter.filter(newText)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getNegara()
            swipe_refresh.isRefreshing = false
        }

        initializedView()
        getNegara()

    }

    private fun initializedView() {
        sequence.setOnClickListener {
            sequenzeWithoutInternet(ascending)
            ascending = !ascending
        }
    }

    private fun sequenzeWithoutInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending) {
                    (layoutManager as LinearLayoutManager).reverseLayout = true
                    (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity, "Z - A", Toast.LENGTH_SHORT).show()
            } else {
                (layoutManager as LinearLayoutManager).reverseLayout = false
                (layoutManager as LinearLayoutManager).stackFromEnd = false
                Toast.makeText(this@MainActivity, "A - Z", Toast.LENGTH_SHORT).show()
            }
            adapter = adapter

        }
    }

    private fun getNegara() {
        val api = retrofit.create(ApiService::class.java)
        api.getAllNegara().enqueue(object : Callback<ResponseCountry> {
            override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                progress_bar.visibility = View.GONE

            }

            override fun onResponse(call: Call<ResponseCountry>, response: Response<ResponseCountry>) {
                if (response.isSuccessful) {
                    val getlistDataCorona = response.body()!!.global
                    val formatter : NumberFormat = DecimalFormat("#,##")
                    txt_confirmed_globe.text = formatter.format(getlistDataCorona?.totalConfirmed?.toDouble())
                    txt_recovered_globe.text = formatter.format(getlistDataCorona?.totalRecovered?.toDouble())
                    txt_death_globe.text = formatter.format(getlistDataCorona?.totalDeaths?.toDouble())
                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progress_bar.visibility = View.GONE
                        adapters = AdapterCountry(
                            response.body()!!.countries as ArrayList<CountriesItem>
                        ) {

                        }
                        adapter = adapters
                    }
                } else {
                    progress_bar?.visibility = View.GONE
                }
            }

        })
    }
}