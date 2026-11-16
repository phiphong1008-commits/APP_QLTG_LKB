package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<Diary> diaryList;

    public DiaryAdapter(List<Diary> diaryList) {
        this.diaryList = diaryList;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        Diary diary = diaryList.get(position);

        holder.tvTitle.setText(diary.getTitle() != null && !diary.getTitle().trim().isEmpty()
                ? diary.getTitle()
                : "Nhật ký không tiêu đề");
        holder.tvDate.setText(formatDate(diary.getDate()));
        holder.tvContent.setText(diary.getContent() != null ? diary.getContent() : "");

        holder.itemView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Xóa nhật ký")
                    .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                    .setIcon(R.drawable.ic_delete)
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (diary.getId() != null) {
                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                    .getReference("Diaries")
                                    .child(diary.getId())
                                    .removeValue();
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

        // XỬ LÝ CHỈNH SỬA (NHẤN CHẠM)
        holder.itemView.setOnClickListener(v -> {
            AddDiaryFragment editFragment = new AddDiaryFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("DIARY_EDIT", diary);
            editFragment.setArguments(bundle);

            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "--/--";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = input.parse(dateStr);
            if (date != null) {
                return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
            }
        } catch (Exception ignored) {
        }
        return dateStr;
    }

    @Override
    public int getItemCount() {
        return diaryList != null ? diaryList.size() : 0;
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_diary_title);
            tvDate = itemView.findViewById(R.id.tv_diary_date);
            tvContent = itemView.findViewById(R.id.tv_diary_content);
        }
    }
}
