package com.example.mediaplayer.frags

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mediaplayer.PhotoAdapter.PhotoItem
import com.example.mediaplayer.R

class EditDialogFragment constructor() : DialogFragment() {
    open interface OnCompleteListener {
        fun onInputedData(item: PhotoItem?)
    }

    private var mCallback: OnCompleteListener? = null
    public override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mCallback = context as OnCompleteListener?
        } catch (e: ClassCastException) {
            Log.d("DialogFragment", "Activity doesn't implement the OnCompleteListener interface")
        }
    }

    public override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder((getContext())!!)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog, null)
        builder.setView(view)
        val bundle: Bundle? = arguments
        val item: PhotoItem? = bundle!!.getSerializable("photoItem") as PhotoItem?
        val submit: Button = view.findViewById(R.id.buttonSubmit)
        //        final EditText email = view.findViewById(R.id.edittextEmailAddress);
//        final EditText password = view.findViewById(R.id.edittextPassword);
        val displayname: EditText = view.findViewById(R.id.edit_displayname)
        val contenttype: EditText = view.findViewById(R.id.edit_contenttype)
        val modified: EditText = view.findViewById(R.id.edit_modified)
        val relative: EditText = view.findViewById(R.id.edit_relative)
        val pathdata: EditText = view.findViewById(R.id.edit_pathdata)
        val datetaken: EditText = view.findViewById(R.id.edit_datetaken)
        displayname.setText(item!!.mDISPLAY_NAME)
        contenttype.setText(item.mCONTENT_TYPE)
        modified.setText(item.mDATE_MODIFIED)
        relative.setText(item.mRELATIVE_PATH)
        pathdata.setText(item.mDATA)
        datetaken.setText(item.mDate)
        submit.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
//                String strEmail = email.getText().toString();
//                String strPassword = password.getText().toString();
                dismiss()
                //                mCallback.onInputedData(strEmail, strPassword);
                mCallback!!.onInputedData(item)
            }
        })
        return builder.create()
    }
}