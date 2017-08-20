package com.google.firebase.quickstart.database.adapter;

import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.ChangeEventListener.EventType;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

//ref com.firebase.ui.database.FirebaseArray
class PostArray implements ChildEventListener{
    private List<Query> mRefs;
    private ChangeEventListener mListener;
    private List<DataSnapshot> mSnapshots = new ArrayList();

    public PostArray(List<Query> refs) {
        mRefs = refs;
        for (Query ref : mRefs) {
            ref.addChildEventListener(this);
        }
    }

    public void cleanup() {
        for (Query ref : mRefs) {
            ref.removeEventListener(this);
        }
        mListener = null;
    }

    public int getCount() {
        return mSnapshots.size();
    }

    public DataSnapshot getItem(int index) {
        return (DataSnapshot)mSnapshots.get(index);
    }

    private int getIndexForKey(String key) {
        int index = 0;

        for(Iterator it = mSnapshots.iterator(); it.hasNext(); ++index) {
            DataSnapshot snapshot = (DataSnapshot)it.next();
            if(snapshot.getKey().equals(key)) {
                return index;
            }
        }

        throw new IllegalArgumentException("Key not found");
    }

    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
        int index = 0;
        if(previousChildKey != null) {
            index = getIndexForKey(previousChildKey) + 1;
        }

        mSnapshots.add(index, snapshot);
        notifyChangedListeners(EventType.ADDED, index);
    }


    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
        int index = getIndexForKey(snapshot.getKey());
        mSnapshots.set(index, snapshot);
        notifyChangedListeners(EventType.CHANGED, index);
    }

    public void onChildRemoved(DataSnapshot snapshot) {
        int index = getIndexForKey(snapshot.getKey());
        mSnapshots.remove(index);
        notifyChangedListeners(EventType.REMOVED, index);
    }

    public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
        int oldIndex = getIndexForKey(snapshot.getKey());
        mSnapshots.remove(oldIndex);
        int newIndex = previousChildKey == null?0:getIndexForKey(previousChildKey) + 1;
        mSnapshots.add(newIndex, snapshot);
        notifyChangedListeners(EventType.MOVED, newIndex, oldIndex);
    }

    public void onDataChange(DataSnapshot dataSnapshot) {
        mListener.onDataChanged();
    }

    public void onCancelled(DatabaseError error) {
        notifyCancelledListeners(error);
    }

    public void setOnChangedListener(ChangeEventListener listener) {
        mListener = listener;
    }

    protected void notifyChangedListeners(EventType type, int index) {
        notifyChangedListeners(type, index, -1);
    }

    protected void notifyChangedListeners(EventType type, int index, int oldIndex) {
        if(mListener != null) {
            mListener.onChildChanged(type, index, oldIndex);
        }

    }

    protected void notifyCancelledListeners(DatabaseError error) {
        if(mListener != null) {
            mListener.onCancelled(error);
        }
    }
}
