package com.google.firebase.quickstart.database.adapter;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.quickstart.database.PostDetailActivity;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.viewholder.PostViewHolder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class PostRecyclerAdapter extends FirebaseRecyclerAdapter<Post, PostViewHolder> {
    private static final String TAG = "PostRecyclerAdapter";

    private DatabaseReference mRef;
    private ChildEventListener mChildEventListener;
    private Context mContext;
    private ArrayList<Post> mRestaurants = new ArrayList<>();

    public PostRecyclerAdapter(Class<Post> modelClass, int modelLayout,
                                         Class<PostViewHolder> viewHolderClass,
                                         Query ref, Context context) {

        super(modelClass, modelLayout, viewHolderClass, ref);
        mRef = ref.getRef();
        mContext = context;
        Log.d(TAG, "Read from " + mRef.toString());
        mChildEventListener = mRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //TODO: only trigger on add on registered path, but initialize return all children in subtree
                mRestaurants.add(dataSnapshot.getValue(Post.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void populateViewHolder(final PostViewHolder postViewHolder, Post post, int position) {
        final DatabaseReference postRef = getRef(position);
        //TODO: skip initialize return all children in subtree, parsed post.body=null
        // Set click listener for the whole post view
        final String postKey = postRef.getKey();
        postViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PostDetailActivity
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                mContext.startActivity(intent);
            }
        });

        // Bind Post to ViewHolder, setting OnClickListener for the star button
        postViewHolder.bindToPost(post, new View.OnClickListener() {
            @Override
            public void onClick(View starView) {
                // Need to write to both places the post is stored
                //DatabaseReference globalPostRef = mRef.child("posts").child(postRef.getKey());
                //DatabaseReference userPostRef = mRef.child("user-posts").child(post.uid).child(postRef.getKey());
            }
        });
    }

    @Override
    public void cleanup() {
        super.cleanup();
        mRef.removeEventListener(mChildEventListener);
        //IMP: avoid client does not have permission err if keep connect after logout
        mChildEventListener = null;
    }
}
