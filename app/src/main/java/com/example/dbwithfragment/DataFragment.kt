package com.example.dbwithfragment

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_data.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

// 방 전체 조회
class DataFragment : Fragment() {
    val IP_ADDRESS = "3.35.105.27"
    val TAG = "phptest"

    lateinit var mJsonString : String
    lateinit var mArrayList : ArrayList<RoomData>
    lateinit var mAdapter: RoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public fun newInstance() : DataFragment {
        return DataFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mArrayList = arrayListOf<RoomData>()
        mAdapter = RoomAdapter(mArrayList)

        listView_main_list.layoutManager = LinearLayoutManager(activity)
        listView_main_list.adapter = mAdapter

        val dividerItemDecoration = DividerItemDecoration(activity, LinearLayoutManager(activity).orientation)
        listView_main_list.addItemDecoration(dividerItemDecoration)

        setButtons()
    }

    fun setButtons() {
        button_main_search.setOnClickListener {
            mArrayList.clear()
            mAdapter.notifyDataSetChanged()

            val keyWord = editText_main_searchKeyword.text.toString()
            editText_main_searchKeyword.setText("")

            val task = GetData()
            task.execute("http://" + IP_ADDRESS + "/query.php", keyWord)
        }
        button_main_all.setOnClickListener {
            mArrayList.clear()
            mAdapter.notifyDataSetChanged()

            val task = GetData()
            task.execute("http://" + IP_ADDRESS + "/getjson.php", "")
        }
    }

    inner class GetData : AsyncTask<String, Void, String>() {
        var progressDialog : ProgressDialog? = null
        lateinit var errorString : String
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(activity,"Please Wait",null,true,true)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            progressDialog?.dismiss()

            Log.d(TAG, "response - $result")

            if(result == null)
                Log.e(TAG,errorString)
            else {
                mJsonString = result
                showResult()
                for(i in 0 until mArrayList.size)
                    Log.d(TAG, "response - ${mArrayList.get(i).room_no}")
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val serverURL = params[0]
            val postParameters = "room_no=" + params[1]

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection

                httpURLConnection.readTimeout = 5000
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.doInput = true
                httpURLConnection.connect()

                val outputStream = httpURLConnection.outputStream
                outputStream.write(postParameters?.toByteArray(Charset.defaultCharset()))
                outputStream.flush()
                outputStream.close()

                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "response code - $responseStatusCode");

                var inputStream : InputStream? = null
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                    inputStream = httpURLConnection.inputStream
                else
                    inputStream = httpURLConnection.errorStream

                val inputStreamReader = InputStreamReader(inputStream, Charset.defaultCharset())
                val bufferedReader = BufferedReader(inputStreamReader)

                val sb = StringBuilder()
                var line : String? = null

                while(true) {
                    line = bufferedReader.readLine()
                    if(line != null)
                        sb.append(line)
                    else
                        break
                }
                bufferedReader.close()
                return sb.toString().trim()
            } catch (e : Exception) {
                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();
                return null
            }
        }
    }

    fun showResult() {
        val TAG_JSON = "joljak_dev"
        val TAG_ID = "room_no"
        val TAG_TEMPERATURE ="temperature"
        val TAG_HUMIDITY = "humidity"
        val TAG_GAS = "gas"
        val TAG_DUST = "dust"
        val TAG_LIGHT = "light"

        try {
            val jsonObject = JSONObject(mJsonString)
            val jsonArray = jsonObject.getJSONArray(TAG_JSON)

            for(i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                val id = item.getString(TAG_ID)
                val temperature = item.getString(TAG_TEMPERATURE)
                val humidity = item.getString(TAG_HUMIDITY)
                val gas = item.getString(TAG_GAS)
                val dust = item.getString(TAG_DUST)
                val light = item.getString(TAG_LIGHT)

                val roomData = RoomData()
                roomData.room_no = id
                roomData.room_temperature = temperature
                roomData.room_humidity = humidity
                roomData.room_gas = gas
                roomData.room_dust = dust
                roomData.room_light = light
                mArrayList.add(roomData)
                mAdapter.notifyDataSetChanged()
            }
        } catch (e: JSONException) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}