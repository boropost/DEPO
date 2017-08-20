package com.google.firebase.quickstart.database.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.ChangeEventListener.EventType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.List;

//ref https://github.com/rohanoid5/Yaadafy/blob/master/app/src/main/app/src/main/java/com/example/user/yaadafy/FirebaseRecyclerAdapter.java
public abstract class PostRecyclerAdapter<T, VH extends ViewHolder> extends Adapter<VH>  {
    private static final String TAG = "PostRecyclerAdapter";

    private PostArray mSnapshots;
    private Class<T> mModelClass;
    protected Class<VH> mViewHolderClass;
    protected int mModelLayout;
    //private Context mContext;

    PostRecyclerAdapter(Class<T> modelClass, @LayoutRes int modelLayout, Class<VH> viewHolderClass, PostArray snapshots) {
        mModelClass = modelClass;
        mModelLayout = modelLayout;
        mViewHolderClass = viewHolderClass;
        mSnapshots = snapshots;
        mSnapshots.setOnChangedListener(new ChangeEventListener() {
            public void onChildChanged(EventType type, int index, int oldIndex) {
                PostRecyclerAdapter.this.onChildChanged(type, index, oldIndex);
            }

            public void onDataChanged() {
                PostRecyclerAdapter.this.onDataChanged();
            }

            public void onCancelled(DatabaseError error) {
                PostRecyclerAdapter.this.onCancelled(error);
            }
        });
    }

    public PostRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, List<Query> refs) {
        this(modelClass, modelLayout, viewHolderClass, new PostArray(refs));
    }

    public void cleanup() {
        mSnapshots.cleanup();
    }

    public int getItemCount() {
        return mSnapshots.getCount();
    }

    public T getItem(int position) {
        return parseSnapshot(mSnapshots.getItem(position));
    }

    protected T parseSnapshot(DataSnapshot snapshot) {
        return snapshot.getValue(mModelClass);
    }

    public DatabaseReference getRef(int position) {
        return mSnapshots.getItem(position).getRef();
    }

    public long getItemId(int position) {
        return (long)mSnapshots.getItem(position).getKey().hashCode();
    }

    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        try {
            Constructor<VH> constructor = mViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void onBindViewHolder(VH viewHolder, int position) {
        T model = getItem(position);
        populateViewHolder(viewHolder, model, position);
    }

    public int getItemViewType(int position) {
        return mModelLayout;
    }

    protected void onChildChanged(EventType type, int index, int oldIndex) {
        switch(type) {
            case ADDED:
                notifyItemInserted(index);
                break;
            case CHANGED:
                notifyItemChanged(index);
                break;
            case REMOVED:
                notifyItemRemoved(index);
                break;
            case MOVED:
                notifyItemMoved(oldIndex, index);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }

    }

    protected void onDataChanged() {
    }

    protected void onCancelled(DatabaseError error) {
        Log.w("FirebaseRecyclerAdapter", error.toException());
    }

    protected abstract void populateViewHolder(VH viewHolder, T model, int position);
}
