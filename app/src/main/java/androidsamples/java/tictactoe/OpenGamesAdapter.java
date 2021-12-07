package androidsamples.java.tictactoe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  private static final String TAG = "OpenGamesAdapter";

  private List<String> mGameIds;
  private OpenGamesAdapter.ClickListener mClickListener;

  public OpenGamesAdapter(OpenGamesAdapter.ClickListener clickListener) {
    mGameIds = new ArrayList<>();
    mClickListener = clickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    holder.mGameId = mGameIds.get(position);
    holder.mView.setOnClickListener(itemView -> {
      mClickListener.onClick(holder.mGameId);
    });
    holder.mIdView.setText(Integer.toString(position + 1));
    holder.mContentView.setText(holder.mGameId);
  }

  @Override
  public int getItemCount() {
    return mGameIds.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private final View mView;
    private final TextView mIdView;
    private final TextView mContentView;
    private String mGameId;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }

  public static interface ClickListener {
    public void onClick(String gameId);
  }

  public void setValues(List<String> values) {
    mGameIds = values;
    notifyDataSetChanged();
  }
}