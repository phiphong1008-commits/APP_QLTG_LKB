package dpphong.ntu.appqlcv.ck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    public interface Listener {
        void onUse(TaskTemplate template);

        void onDelete(TaskTemplate template);
    }

    private final List<TaskTemplate> templates = new ArrayList<>();
    private final Listener listener;

    public TemplateAdapter(Listener listener) {
        this.listener = listener;
    }

    public void update(List<TaskTemplate> newTemplates) {
        templates.clear();
        if (newTemplates != null) templates.addAll(newTemplates);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        TaskTemplate template = templates.get(position);
        holder.tvName.setText(template.getName() != null ? template.getName() : "Mẫu công việc");

        String priority = template.getPriority() != null ? template.getPriority() : "Vừa";
        String list = template.getListName() != null && !template.getListName().isEmpty()
                ? template.getListName() : "Không có danh sách";
        String title = template.getTitle() != null ? template.getTitle() : "";
        holder.tvMeta.setText(title + "\nƯu tiên " + priority + " • " + list);

        if (template.getIcon() != null) {
            switch (template.getIcon()) {
                case "study": holder.ivIcon.setImageResource(R.drawable.ic_book); break;
                case "home": holder.ivIcon.setImageResource(R.drawable.ic_home); break;
                case "event": holder.ivIcon.setImageResource(R.drawable.ic_event); break;
                default: holder.ivIcon.setImageResource(R.drawable.ic_work); break;
            }
        }

        holder.btnUse.setOnClickListener(v -> listener.onUse(template));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(template));
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvMeta;
        MaterialButton btnUse;
        ImageButton btnDelete;

        TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_template_icon);
            tvName = itemView.findViewById(R.id.tv_template_name);
            tvMeta = itemView.findViewById(R.id.tv_template_meta);
            btnUse = itemView.findViewById(R.id.btn_use_template);
            btnDelete = itemView.findViewById(R.id.btn_delete_template);
        }
    }
}
