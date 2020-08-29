package com.example.capture.frags;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.capture.PhotoAdapter;
import com.example.capture.R;

public class EditDialogFragment extends DialogFragment {

    public interface OnCompleteListener{
        void onInputedData(PhotoAdapter.PhotoItem item);
    }

    private OnCompleteListener mCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnCompleteListener) context;
        }
        catch (ClassCastException e) {
            Log.d("DialogFragment", "Activity doesn't implement the OnCompleteListener interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog,null);
        builder.setView(view);

        Bundle bundle = getArguments();
        final PhotoAdapter.PhotoItem item = (PhotoAdapter.PhotoItem) bundle.getSerializable("photoItem");

        final Button submit = view.findViewById(R.id.buttonSubmit);
//        final EditText email = view.findViewById(R.id.edittextEmailAddress);
//        final EditText password = view.findViewById(R.id.edittextPassword);
        final EditText displayname = view.findViewById(R.id.edit_displayname);
        final EditText contenttype = view.findViewById(R.id.edit_contenttype);
        final EditText modified = view.findViewById(R.id.edit_modified);
        final EditText relative = view.findViewById(R.id.edit_relative);
        final EditText pathdata = view.findViewById(R.id.edit_pathdata);
        final EditText datetaken = view.findViewById(R.id.edit_datetaken);

        displayname.setText(item.mDISPLAY_NAME);
        contenttype.setText(item.mCONTENT_TYPE);
        modified.setText(item.mDATE_MODIFIED);
        relative.setText(item.mRELATIVE_PATH);
        pathdata.setText(item.mDATA);
        datetaken.setText(item.mDate);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String strEmail = email.getText().toString();
//                String strPassword = password.getText().toString();
                dismiss();
//                mCallback.onInputedData(strEmail, strPassword);
                mCallback.onInputedData(item);
            }
        });

        return builder.create();

    }
}
