package com.example.mediaplayer.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mediaplayer.BuildConfig
import com.example.mediaplayer.R
import java.io.Serializable

class ListViewFragment  // ** uses unchecked or unsafe operations
    : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView: ListView = view.findViewById<View>(R.id.list_view) as ListView
        val bundle: Bundle? = arguments
        if (BuildConfig.DEBUG && bundle == null) {
            error("Assertion failed")
        }
        @Suppress("UNCHECKED_CAST") val mData: List<String> = bundle!!.getSerializable("data") as List<String>
        val adapter = MyAdapter(mData)
        listView.adapter = adapter
    }

    private class MyAdapter(private val mmData: List<String>?) : BaseAdapter() {
        override fun getCount(): Int {
            return mmData!!.size
        }

        override fun getItem(position: Int): Any {
            return mmData!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @Suppress("NAME_SHADOWING")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var newConvertView: View? = convertView
            val viewHolder: ViewHolder

            if (newConvertView == null) {
                viewHolder = ViewHolder()
                newConvertView = LayoutInflater.from(parent.context)
                        .inflate(android.R.layout.simple_list_item_1, parent, false)
                viewHolder.textView = newConvertView!!.findViewById<View>(android.R.id.text1) as TextView?
                newConvertView!!.tag = viewHolder
            } else {
                viewHolder = newConvertView.tag as ViewHolder
            }


            val data: String = mmData!![position]
            viewHolder.textView!!.text = data
            return newConvertView
        }
    }

    private class ViewHolder constructor() {
        var textView: TextView? = null
    }

    companion object {
        // TODO: Customize parameter initialization
        fun newInstance(data: List<String>?): ListViewFragment {
            val fragment = ListViewFragment()
            val bundle = Bundle()
            bundle.putSerializable("data", data as Serializable?)
            fragment.arguments = bundle
            return fragment
        }
    }
}