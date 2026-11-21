package dpphong.ntu.appqlcv.ck;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    public interface Listener {
        void onEdit(TaskList taskList);

        void onDelete(TaskList taskList);

        void onShare(TaskList taskList);
    }

    private final List<TaskList> lists;
    private final Listener listener;
    private final Map<String, Integer> counts = new HashMap<>();

    public ListAdapter(List<TaskList> lists, Listener listener) {
        this.lists = lists;
        this.listener = listener;
    }

    public void updateCounts(Map<String, Integer> newCounts) {
        counts.clear();
        if (newCounts != null) counts.putAll(newCounts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TaskList taskList = lists.get(position);
        holder.tvName.setText(!TextUtils.isEmpty(taskList.getName()) ? taskList.getName() : "Danh sách");

        Integer count = counts.get(taskList.getId());
        holder.tvCount.setText((count != null ? count : 0) + " công việc");

        String color = taskList.getColor();
        if (!TextUtils.isEmpty(color)) {
            try {
                Drawable drawable = holder.viewColor.getBackground().mutate();
                DrawableCompat.setTint(drawable, Color.parseColor(color));
                holder.viewColor.setBackground(drawable);
            } catch (Exception ignored) {
            }
        } else {
            holder.viewColor.setBackgroundResource(R.drawable.bg_color_dot);
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(taskList));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(taskList));
        holder.btnShare.setOnClickListener(v -> listener.onShare(taskList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(taskList));
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvName, tvCount;
        ImageButton btnEdit, btnDelete, btnShare;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.view_list_color);
            tvName = itemView.findViewById(R.id.tv_list_name);
            tvCount = itemView.findViewById(R.id.tv_list_count);
            btnEdit = itemView.findViewById(R.id.btn_edit_list);
            btnDelete = itemView.findViewById(R.id.btn_delete_list);
            btnShare = itemView.findViewById(R.id.btn_share_list);
        }
    }
}
