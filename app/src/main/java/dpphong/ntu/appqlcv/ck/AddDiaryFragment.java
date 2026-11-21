package dpphong.ntu.appqlcv.ck;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class AddDiaryFragment extends Fragment {

    private Diary diaryToEdit = null; // Biến lưu trữ Nhật ký nếu đang ở chế độ sửa

    private TextView tvHeaderTitle, tvDate, tvPrompt;
    private EditText edtTitle, edtContent;
    private MaterialButton btnSave;
    private ChipGroup chipMood, chipWeather;
    private ImageView ivPhoto;
    private MaterialButton btnPickPhoto, btnRemovePhoto;

    private String databaseDateString;
    private String photoBase64 = null;
    private String currentPrompt;

    private ActivityResultLauncher<String> imagePicker;

    private static final String[] PROMPTS = {
            "Hôm nay điều gì làm bạn mỉm cười?",
            "Một việc nhỏ bạn tự hào hôm nay là gì?",
            "Điều gì khiến bạn biết ơn hôm nay?",
            "Nếu viết một câu cho ngày mai, bạn sẽ viết gì?",
            "Hôm nay bạn học được điều gì?",
            "Cảm xúc nổi bật nhất hôm nay là gì?",
            "Bạn muốn nhắn gì cho chính mình của một năm sau?",
            "Điều gì bạn muốn làm khác đi vào ngày mai?"
    };

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String encoded = encodeImage(uri);
                if (encoded != null) {
                    photoBase64 = encoded;
                    showPhoto(encoded);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        tvDate = view.findViewById(R.id.tv_diary_date);
        edtTitle = view.findViewById(R.id.edt_diary_title);
        edtContent = view.findViewById(R.id.edt_diary_content);
        btnSave = view.findViewById(R.id.btn_save_diary);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        chipMood = view.findViewById(R.id.chip_group_diary_mood);
        chipWeather = view.findViewById(R.id.chip_group_diary_weather);
        tvPrompt = view.findViewById(R.id.tv_diary_prompt);
        TextView btnChangePrompt = view.findViewById(R.id.btn_change_prompt);
        ivPhoto = view.findViewById(R.id.iv_diary_photo);
        btnPickPhoto = view.findViewById(R.id.btn_pick_diary_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_diary_photo);

        currentPrompt = randomPrompt();
        tvPrompt.setText(currentPrompt);
        btnChangePrompt.setOnClickListener(v -> {
            currentPrompt = randomPrompt();
            tvPrompt.setText(currentPrompt);
        });

        btnPickPhoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnRemovePhoto.setOnClickListener(v -> {
            photoBase64 = null;
            ivPhoto.setVisibility(View.GONE);
            ivPhoto.setImageDrawable(null);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(v -> saveDiaryToFirebase());

        // ==========================================
        // KIỂM TRA CHẾ ĐỘ (THÊM MỚI HAY CHỈNH SỬA)
        // ==========================================
        if (getArguments() != null && getArguments().containsKey("DIARY_EDIT")) {
            diaryToEdit = (Diary) getArguments().getSerializable("DIARY_EDIT");

            if (diaryToEdit != null) {
                tvHeaderTitle.setText("Sửa Nhật Ký");
                btnSave.setText("Cập nhật");

                edtTitle.setText(diaryToEdit.getTitle());
                edtContent.setText(diaryToEdit.getContent());

                databaseDateString = diaryToEdit.getDate();
                tvDate.setText("Ngày: " + databaseDateString);

                checkMoodChip(diaryToEdit.getMood());
                checkWeatherChip(diaryToEdit.getWeather());

                if (!TextUtils.isEmpty(diaryToEdit.getPrompt())) {
                    currentPrompt = diaryToEdit.getPrompt();
                    tvPrompt.setText(currentPrompt);
                }

                photoBase64 = diaryToEdit.getPhotoBase64();
                if (!TextUtils.isEmpty(photoBase64)) {
                    showPhoto(photoBase64);
                }
            }
        } else {
            tvHeaderTitle.setText("Viết Nhật Ký");
            btnSave.setText("Lưu nhật ký");
            setupCurrentDate();
        }

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }

    private String randomPrompt() {
        return PROMPTS[new Random().nextInt(PROMPTS.length)];
    }

    // ==========================================
    // TÂM TRẠNG / THỜI TIẾT
    // ==========================================
    private void checkMoodChip(String mood) {
        if (mood == null) return;
        switch (mood) {
            case "happy": chipMood.check(R.id.chip_mood_happy); break;
            case "sad": chipMood.check(R.id.chip_mood_sad); break;
            case "angry": chipMood.check(R.id.chip_mood_angry); break;
            case "tired": chipMood.check(R.id.chip_mood_tired); break;
            default: chipMood.check(R.id.chip_mood_ok); break;
        }
    }

    private String getSelectedMood() {
        int id = chipMood.getCheckedChipId();
        if (id == R.id.chip_mood_happy) return "happy";
        if (id == R.id.chip_mood_sad) return "sad";
        if (id == R.id.chip_mood_angry) return "angry";
        if (id == R.id.chip_mood_tired) return "tired";
        if (id == R.id.chip_mood_ok) return "ok";
        return null;
    }

    private void checkWeatherChip(String weather) {
        if (weather == null) return;
        switch (weather) {
            case "sun": chipWeather.check(R.id.chip_weather_sun); break;
            case "cloud": chipWeather.check(R.id.chip_weather_cloud); break;
            case "rain": chipWeather.check(R.id.chip_weather_rain); break;
            case "cold": chipWeather.check(R.id.chip_weather_cold); break;
        }
    }

    private String getSelectedWeather() {
        int id = chipWeather.getCheckedChipId();
        if (id == R.id.chip_weather_sun) return "sun";
        if (id == R.id.chip_weather_cloud) return "cloud";
        if (id == R.id.chip_weather_rain) return "rain";
        if (id == R.id.chip_weather_cold) return "cold";
        return null;
    }

    // ==========================================
    // ẢNH
    // ==========================================
    private String encodeImage(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = requireContext().getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(input, null, bounds);
            }

            int sample = 1;
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSide / sample > 900) {
                sample *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap bitmap;
            try (InputStream input = requireContext().getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(input, null, options);
            }
            if (bitmap == null) return null;

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);
            byte[] bytes = output.toByteArray();
            bitmap.recycle();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không đọc được ảnh: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showPhoto(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                ivPhoto.setImageBitmap(bitmap);
                ivPhoto.setVisibility(View.VISIBLE);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    // ==========================================
    // NGÀY & LƯU
    // ==========================================
    private void showAlertDialog(String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Đồng ý", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat displayFormat = new SimpleDateFormat("'Hôm nay, 'dd/MM/yyyy", new Locale("vi", "VN"));
        tvDate.setText(displayFormat.format(currentDate));

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        databaseDateString = dbFormat.format(currentDate);
    }

    private void saveDiaryToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showAlertDialog("Lỗi", "Vui lòng đăng nhập trước!");
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            showAlertDialog("Cảnh báo", "Bạn chưa viết nội dung nhật ký!");
            return;
        }

        String mood = getSelectedMood();
        String weather = getSelectedWeather();

        if (diaryToEdit != null) {
            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("title", title);
            updateMap.put("content", content);
            updateMap.put("mood", mood);
            updateMap.put("weather", weather);
            updateMap.put("prompt", currentPrompt);
            updateMap.put("photoBase64", photoBase64);

            mDatabase.child("Diaries").child(diaryToEdit.getId()).updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> getParentFragmentManager().popBackStack())
                    .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
        } else {
            String diaryId = mDatabase.child("Diaries").push().getKey();

            HashMap<String, Object> diaryMap = new HashMap<>();
            diaryMap.put("id", diaryId);
            diaryMap.put("userId", currentUser.getUid());
            diaryMap.put("title", title);
            diaryMap.put("content", content);
            diaryMap.put("date", databaseDateString);
            diaryMap.put("timestamp", System.currentTimeMillis());
            diaryMap.put("mood", mood);
            diaryMap.put("weather", weather);
            diaryMap.put("prompt", currentPrompt);
            diaryMap.put("photoBase64", photoBase64);
            diaryMap.put("pinned", false);

            if (diaryId != null) {
                mDatabase.child("Diaries").child(diaryId).setValue(diaryMap)
                        .addOnSuccessListener(aVoid -> getParentFragmentManager().popBackStack())
                        .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
            }
        }
    }
}
