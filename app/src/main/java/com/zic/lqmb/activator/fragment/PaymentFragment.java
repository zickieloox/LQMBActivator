package com.zic.lqmb.activator.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.utils.NetUtils;
import com.zic.lqmb.activator.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.zic.lqmb.activator.data.MyApplication.PAYMENT_URL;

public class PaymentFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PaymentFragment";

    private String telco = "VTT";
    private String message = "";
    private TextInputLayout tilCard, tilSerial;
    private Button btnPay;
    private MaterialDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        MaterialSpinner spinner = (MaterialSpinner) view.findViewById(R.id.spinner_telco);
        spinner.setItems("Viettel", "Mobifone", "Vinaphone");
        // "VN Mobile", "FPT GATE", "ZING", "OnCash", "Megacard"
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                switch (position) {
                    case 0:
                        telco = "VTT";
                        break;
                    case 1:
                        telco = "VMS";
                        break;
                    case 2:
                        telco = "VNP";
                        break;
//                    case 3:
//                        telco = "VNM";
//                        break;
//                    case 4:
//                        telco = "GATE";
//                        break;
//                    case 5:
//                        telco = "ZING";
//                        break;
//                    case 6:
//                        telco = "ONC";
//                        break;
//                    case 7:
//                        telco = "MGC";
//                        break;
                    default:
                        telco = "VTT";
                }
            }
        });

        tilCard = (TextInputLayout) view.findViewById(R.id.til_card);
        tilSerial = (TextInputLayout) view.findViewById(R.id.til_serial);

        btnPay = (Button) view.findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_pay:
                btnPay.setEnabled(false);
                checkPayment();
                break;
        }
    }

    private void checkPayment() {

        assert tilCard.getEditText() != null;
        assert tilSerial.getEditText() != null;
        String cardNumber = tilCard.getEditText().getText().toString().trim();
        String serialNumber = tilSerial.getEditText().getText().toString().trim();

        new PaymentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, telco, cardNumber, serialNumber);
        progressDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.dialog_content_processing)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private class PaymentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String key = Utils.generateKey(getActivity());
            String paymentUrl = PAYMENT_URL;
            String telco = strings[0];
            String cardNumber = strings[1];
            String serialNumber = strings[2];
            String resultJson = null;

            paymentUrl += "?telco=" + telco + "&card=" + cardNumber + "&serial=" + serialNumber + "&key=" + key;

            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                resultJson = NetUtils.getHtml(paymentUrl);
            }

            return resultJson;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            btnPay.setEnabled(true);

            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.toast_check_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject root;
            try {
                root = new JSONObject(result);
                message = root.getString("message");
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return;
            }

            new MaterialDialog.Builder(getActivity())
                    .content(message)
                    .positiveText(android.R.string.ok)
                    .show();
        }
    }
}
