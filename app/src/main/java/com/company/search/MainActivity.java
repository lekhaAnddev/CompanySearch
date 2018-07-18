package com.company.search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import andriod.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String url = "http://api.myjson.com/bins/jz6bp";
    Context ctx;
    CompanyAdapter adapter;
    EditText searchEt;
    ImageView search, clearEt;
    RelativeLayout parLay2;
    ListView list;

    ArrayList<HashMap<String, String>> participantsList = new ArrayList<>(), backup_participantsList = new ArrayList<>(), search_participants = new ArrayList<>();
    private TextWatcher textWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            search_participants = new ArrayList<>();
            String searchString = searchEt.getText().toString().toLowerCase();
            int textLength = searchString.trim().length();
            for (int i = 0; i < backup_participantsList.size(); i++) {
                String query1 = "", query2 = "", query3 = "", query4 = "", query5 = "", query6 = "";
                try {
                    query1 = backup_participantsList.get(i).get("companyName").toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    query2 = backup_participantsList.get(i).get("parent");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    query3 = backup_participantsList.get(i).get("managers").toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    query4 = backup_participantsList.get(i).get("phones").toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    query5 = backup_participantsList.get(i).get("name").toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    query6 = backup_participantsList.get(i).get("addresses").toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (textLength <= query1.length() || textLength <= query2.length() || textLength <= query3.length()
                        || textLength <= query4.length() || textLength <= query5.length() || textLength <= query6.length())
                    if (query1.contains(searchString) || query2.contains(searchString) || query3.contains(searchString)
                            || query4.contains(searchString) || query5.contains(searchString) || query6.contains(searchString))
                        search_participants.add(backup_participantsList.get(i));
            }
            if (textLength != 0)
                participantsList = search_participants;
            else
                participantsList = backup_participantsList;
            adapter.notifyDataSetChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0)
                getSupportActionBar().setTitle("All Companies: " + participantsList.size());
            else
                getSupportActionBar().setTitle("Filtered List: " + search_participants.size());
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_main);
        searchEt = (EditText) findViewById(R.id.searchEt);
        clearEt = (ImageView) findViewById(R.id.clearEt);
        search = (ImageView) findViewById(R.id.search);
        parLay2 = (RelativeLayout) findViewById(R.id.parLay2);
        list = (ListView) findViewById(R.id.list);
        new GetDataFromServer().execute(url);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setVisibility(View.INVISIBLE);
                parLay2.setVisibility(View.VISIBLE);
                searchEt.requestFocus();
                showKeyboard(ctx, searchEt);
            }
        });
        clearEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEt.setText("");
                hideKeyboard(ctx, searchEt);
                parLay2.setVisibility(View.INVISIBLE);
                search.setVisibility(View.VISIBLE);
                participantsList = backup_participantsList;
                adapter.notifyDataSetChanged();
            }
        });
        searchEt.addTextChangedListener(textWatcher);
    }

    ProgressDialog createProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progressdialog);
        return dialog;
    }

    String getSourceCode(String requestURL) {
        String response = "";
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return response;
        }
        return response;
    }

    class GetDataFromServer extends AsyncTask<String, String, String> {
        ProgressDialog pd;

        @Override
        public void onPreExecute() {
            pd = createProgressDialog(ctx);
            pd.show();
        }

        @Override
        public String doInBackground(String... params) {
            String res = getSourceCode(params[0]);
            return res;
        }

        @Override
        public void onPostExecute(String result) {
            if (pd.isShowing())
                pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jArr = jsonObject.getJSONArray("contacts");
                if (jArr.length() == 0)
                    Toast.makeText(ctx, "No data", Toast.LENGTH_LONG).show();
                else {
                    participantsList.clear();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject eDoll = jArr.getJSONObject(i);
                        HashMap<String, String> values = new HashMap<>();
                        values.put("companyName", eDoll.optString("companyName"));
                        values.put("name", eDoll.optString("name"));
                        values.put("parent", eDoll.optString("parent"));
                        String str1 = eDoll.optString("managers");
                        str1 = str1.replace("[\"", "");
                        str1 = str1.replace("\"]", "");
                        str1 = str1.replace("\",\"", "\n");
                        values.put("managers", str1);
                        String str2 = eDoll.optString("phones");
                        str2 = str2.replace("[\"", "");
                        str2 = str2.replace("\"]", "");
                        str2 = str2.replace("\",\"", "\n");
                        values.put("phones", str2);
                        String str3 = eDoll.optString("addresses");
                        str3 = str3.replace("[\"", "");
                        str3 = str3.replace("\"]", "");
                        str3 = str3.replace("\",\"", "\n");
                        values.put("addresses", str3);
                        participantsList.add(values);
                    }
                    backup_participantsList = participantsList;
                    adapter = new CompanyAdapter(ctx);
                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException jse) {
                jse.printStackTrace();
            }
        }
    }

    class CompanyAdapter extends BaseAdapter {
        TextView tv1, tv2;
        Button check;
        LayoutInflater li;
        Context c;

        CompanyAdapter(Context ct) {
            li = LayoutInflater.from(ct);
            c = ct;
        }

        @Override
        public int getCount() {
            return participantsList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = li.inflate(R.layout.list_all_companies, null);
            tv1 = (TextView) convertView.findViewById(R.id.tv1);
            tv2 = (TextView) convertView.findViewById(R.id.tv2);
            check = (Button) convertView.findViewById(R.id.check);
            if (!(position == participantsList.size())) {
                String str1 = participantsList.get(position).get("companyName");
                if (str1.length() == 0 || str1.equals("null"))
                    str1 = participantsList.get(position).get("name");
                tv1.setText((position + 1) + ") " + str1);
                String str2 = participantsList.get(position).get("parent");
                if (str2.length() == 0 || str2.equals("null"))
                    str2 = participantsList.get(position).get("phones");
                tv2.setText(str2);
                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ctx, Views.class);
                        i.putExtra("companyName", participantsList.get(position).get("companyName"));
                        i.putExtra("name", participantsList.get(position).get("name"));
                        i.putExtra("parent", participantsList.get(position).get("parent"));
                        i.putExtra("managers", participantsList.get(position).get("managers"));
                        i.putExtra("phones", participantsList.get(position).get("phones"));
                        i.putExtra("addresses", participantsList.get(position).get("addresses"));
                        startActivity(i);
                    }
                });
            }
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    void hideKeyboard(Context ctx, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Context ctx, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                    0);
        }
    }
}
