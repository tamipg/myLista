package com.tapago.mylista;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ItemListActivity extends AppCompatActivity {

    private boolean mTwoPane;
    public static List<LibroItem> ITEMS = new ArrayList<LibroItem>();
    public static Map<String, LibroItem> ITEM_MAP = new HashMap<String, LibroItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setBackgroundResource(R.drawable.banlibros);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            getDatosVolley();
        } else {
            Toast.makeText(ItemListActivity.this,"Error: No hay Internet ", Toast.LENGTH_LONG).show();
        }

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<LibroItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LibroItem item = (LibroItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<LibroItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position){
            holder.mAuthorView.setText(mValues.get(position).author);
            holder.mTitleView.setText(mValues.get(position).title);
            Picasso.get().load(mValues.get(position).url_image).into(holder.mPortadaView);
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mAuthorView;
            final TextView mTitleView;
            final ImageView mPortadaView;
            ViewHolder(View view) {
                super(view);
                mAuthorView = (TextView) view.findViewById(R.id.author);
                mTitleView = (TextView) view.findViewById(R.id.title);
                mPortadaView = (ImageView) view.findViewById(R.id.portada);
            }
        }
    }
    private void getDatosVolley(){
        String url="https://mybooks-6a979.web.app/json/mybooks_id.json";
        final Gson gson = new Gson();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                try {
                    JSONArray mJSONArray=response.getJSONArray("mybooks");//Nombre del json
                    ITEMS.clear();
                    LibroItem item;
                    for (int i=0; i<mJSONArray.length(); i++){
                        JSONObject mJSONObject = mJSONArray.getJSONObject(i);
                        item = gson.fromJson(mJSONObject.toString(), LibroItem.class);
                        ITEMS.add(item);//Añade cada ítem del JSON al arreglo de LibroItem
                        ITEM_MAP.put(Integer.toString(i), item );//Mapea con la posición i
                        View recyclerView = findViewById(R.id.item_list);//Pinta la lista
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }
                } catch (JSONException err) {
                    err.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err){}
        });
        queue.add(request);
    }
}