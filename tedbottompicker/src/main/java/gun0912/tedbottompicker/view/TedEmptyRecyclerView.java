package gun0912.tedbottompicker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class TedEmptyRecyclerView extends RecyclerView {
	@Nullable
	View emptyView;

	public TedEmptyRecyclerView(Context context) {
		super(context);
	}

	public TedEmptyRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TedEmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	void checkIfEmpty() {
		if (emptyView != null) {
			
			emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
		}
	}

	final @NonNull
	AdapterDataObserver observer = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			checkIfEmpty();
		}
	};
	
	
	
	@Override
	public void setAdapter(@Nullable Adapter adapter) {
		final Adapter oldAdapter = getAdapter();
		if (oldAdapter != null) {
			oldAdapter.unregisterAdapterDataObserver(observer);
		}
		super.setAdapter(adapter);
		if (adapter != null) {
			adapter.registerAdapterDataObserver(observer);
		}
	}

	public void setEmptyView(@Nullable View emptyView) {
		this.emptyView = emptyView;
		checkIfEmpty();
	}
}