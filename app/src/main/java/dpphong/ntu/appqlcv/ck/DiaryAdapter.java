package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public interface Listener {
        void onTogglePin(Diary diary);
    }

    private final List<Diary> diaryList;
    private final Listener listener;

    public DiaryAdapter(List<Diary> diaryList, Listener listener) {
        this.diaryList = diaryList;
        this.listener = listener;
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
        holder.tvPin.setVisibility(diary.isPinned() ? View.VISIBLE : View.GONE);

        String meta = buildMeta(diary);
        if (meta.isEmpty()) {
            holder.tvMeta.setVisibility(View.GONE);
        } else {
            holder.tvMeta.setText(meta);
            holder.tvMeta.setVisibility(View.VISIBLE);
        }

        loadThumbnail(holder.ivThumb, diary);

        holder.itemView.setOnLongClickListener(v -> {
            boolean pinned = diary.isPinned();
            String[] actions = {pinned ? "Bỏ ghim" : "Ghim lên đầu", "Xóa nhật ký"};
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle(diary.getTitle() != null ? diary.getTitle() : "Nhật ký")
                    .setItems(actions, (dialog, which) -> {
                        if (which == 0) {
                            if (listener != null) listener.onTogglePin(diary);
                        } else {
                            confirmDelete(v, diary);
                        }
                    })
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

    private void confirmDelete(View anchor, Diary diary) {
        if (diary.getId() == null) return;
        new MaterialAlertDialogBuilder(anchor.getContext())
                .setTitle("Xóa nhật ký")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Xóa", (dialog, which) ->
                        com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("Diaries")
                                .child(diary.getId())
                                .removeValue())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String buildMeta(Diary diary) {
        StringBuilder builder = new StringBuilder();
        String mood = moodLabel(diary.getMood());
        if (!mood.isEmpty()) builder.append(mood);
        String weather = weatherLabel(diary.getWeather());
        if (!weather.isEmpty()) {
            if (builder.length() > 0) builder.append(" • ");
            builder.append(weather);
        }
        if (builder.length() == 0 && !TextUtils.isEmpty(diary.getPrompt())) {
            builder.append("💭 ").append(diary.getPrompt());
        }
        return builder.toString();
    }

    private String moodLabel(String mood) {
        if (mood == null) return "";
        switch (mood) {
            case "happy": return "😄 Vui";
            case "sad": return "😔 Buồn";
            case "angry": return "😡 Tức giận";
            case "tired": return "😴 Mệt";
            case "ok": return "🙂 Bình thường";
            default: return "";
        }
    }

    private String weatherLabel(String weather) {
        if (weather == null) return "";
        switch (weather) {
            case "sun": return "☀️ Nắng";
            case "cloud": return "⛅ Nhiều mây";
            case "rain": return "🌧 Mưa";
            case "cold": return "❄️ Lạnh";
            default: return "";
        }
    }

    private void loadThumbnail(ImageView view, Diary diary) {
        String base64 = diary.getPhotoBase64();
        if (TextUtils.isEmpty(base64)) {
            view.setVisibility(View.GONE);
            view.setImageDrawable(null);
            return;
        }
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            int sample = 1;
            int maxSide = Math.max(options.outWidth, options.outHeight);
            while (maxSide / sample > 300) sample *= 2;

            options.inJustDecodeBounds = false;
            options.inSampleSize = sample;
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                    .decodeByteArray(bytes, 0, bytes.length, options);

            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            view.setVisibility(View.GONE);
        }
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
        TextView tvTitle, tvDate, tvContent, tvMeta, tvPin;
        ImageView ivThumb;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_diary_title);
            tvDate = itemView.findViewById(R.id.tv_diary_date);
            tvContent = itemView.findViewById(R.id.tv_diary_content);
            tvMeta = itemView.findViewById(R.id.tv_diary_meta);
            tvPin = itemView.findViewById(R.id.tv_diary_pin);
            ivThumb = itemView.findViewById(R.id.iv_diary_thumb);
        }
    }
}
