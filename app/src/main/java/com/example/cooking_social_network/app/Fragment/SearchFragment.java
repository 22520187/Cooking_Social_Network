package com.example.cooking_social_network.app.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cooking_social_network.app.Model.User;
import com.example.cooking_social_network.app.UserAdapter.TagAdapter;
import com.example.cooking_social_network.app.UserAdapter.UserAdapter;
import com.example.cooking_social_network.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.local.QueryContext;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private List <User> mUsers;
    private UserAdapter userAdapter;
    private SocialAutoCompleteTextView search_bar;

    private List<String> mHashTags;
    private List<String> mHashTagsCount;
    private TagAdapter tagAdapter;
    private RecyclerView recyclerViewTags;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setHasFixedSize(true);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        mHashTags = new ArrayList<>();
        mHashTagsCount = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext(), mHashTags , mHashTagsCount);
        recyclerViewTags.setAdapter(tagAdapter);

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(userAdapter);

        search_bar = view.findViewById(R.id.search_bar);

        readUser();
        readTags();

        search_bar.addTextChangedListener( new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });



        return view;
    }

    private void filter (String text) {
        List<String> mSearchTags = new ArrayList<>();
        List<String> mSearchTagsCount = new ArrayList<>();

        for (String s : mHashTags) {
            if (s.toLowerCase().contains(text.toLowerCase())){
                mSearchTags.add(s);
                mSearchTagsCount.add(mHashTagsCount.get(mHashTags.indexOf(s)));
            }
        }

        tagAdapter.filter(mSearchTags , mSearchTagsCount);
    }

    private void readTags() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("HashTags");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mHashTags.clear();
                mHashTagsCount.clear();


                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    mHashTags.add(snapshot.getKey());
                    mHashTagsCount.add(snapshot.getChildrenCount() + "");
                }

                tagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getDetails());
            }
        });
    }


    //Hiển thị danh sách User trong mục tìm kiếm
    private void readUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(TextUtils.isEmpty(search_bar.getText().toString())){
                    mUsers.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        User user = snapshot1.getValue(User.class);
                        mUsers.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void searchUser(String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(s).endAt(s+ "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}