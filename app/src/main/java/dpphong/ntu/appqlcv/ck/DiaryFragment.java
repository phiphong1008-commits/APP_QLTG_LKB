package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Danh sách nhật ký: tìm kiếm, lọc theo tháng, ghim bài viết,
 * hiển thị tâm trạng / thời tiết và khóa bằng mã PIN.
 */
public class DiaryFragment extends Fragment {

    private RecyclerView rvDiary;
    private FloatingActionButton fabAddDiary;
    private DiaryAdapter diaryAdapter;
    private TextView tvDiarySubtitle, tvEmptyTitle, tvEmptyHint;
    private View layoutEmptyDiary, layoutLocked;
    private EditText edtSearch;
    private ChipGroup chipMonth;
    private ImageButton btnLock;

    private final List<Diary> allDiaries = new ArrayList<>();
    private final List<Diary> visibleDiaries = new ArrayList<>();

    private boolean unlocked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);

        rvDiary = view.findViewById(R.id.rv_diary_list);
        fabAddDiary = view.findViewById(R.id.fab_add_diary);
        tvDiarySubtitle = view.findViewById(R.id.tv_diary_subtitle);
        layoutEmptyDiary = view.findViewById(R.id.layout_empty_diary);
        layoutLocked = view.findViewById(R.id.layout_diary_locked);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);
        edtSearch = view.findViewById(R.id.edt_diary_search);
        chipMonth = view.findViewById(R.id.chip_group_diary_month);
        btnLock = view.findViewById(R.id.btn_lock_diary);
        View cardCompose = view.findViewById(R.id.card_compose_diary);

        diaryAdapter = new DiaryAdapter(visibleDiaries, this::togglePin);
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiary.setAdapter(diaryAdapter);

        boolean hasPin = Prefs.hasDiaryPin(requireContext());
        unlocked = !hasPin;
        btnLock.setVisibility(hasPin ? View.VISIBLE : View.GONE);

        btnLock.setOnClickListener(v -> {
            unlocked = false;
            updateLockState();
        });
        view.findViewById(R.id.btn_unlock_diary).setOnClickListener(v -> showPinDialog());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipMonth.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());

        View.OnClickListener openEditor = v -> {
            if (!unlocked) {
                showPinDialog();
                return;
            }
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, new AddDiaryFragment())
                    .addToBackStack(null)
                    .commit();
        };
        fabAddDiary.setOnClickListener(openEditor);
        cardCompose.setOnClickListener(openEditor);

        updateLockState();
        loadDiaries();
        return view;
    }

    private void updateLockState() {
        if (!isAdded() || layoutLocked == null) return;
        boolean hasPin = Prefs.hasDiaryPin(requireContext());
        boolean locked = hasPin && !unlocked;
        layoutLocked.setVisibility(locked ? View.VISIBLE : View.GONE);
        btnLock.setVisibility(hasPin ? View.VISIBLE : View.GONE);
        updateEmptyState();
    }

    private void showPinDialog() {
        if (!isAdded()) return;

        final EditText input = new EditText(requireContext());
        input.setHint("Nhập mã PIN");
        input.setGravity(Gravity.CENTER);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        int padding = dp(18);
        input.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mở khóa nhật ký")
                .setIcon(R.drawable.ic_lock)
                .setView(input)
                .setPositiveButton("Mở khóa", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String pin = input.getText().toString().trim();
                    if (Prefs.checkDiaryPin(requireContext(), pin)) {
                        unlocked = true;
                        updateLockState();
                        dialog.dismiss();
                    } else {
                        input.setError("Mã PIN không đúng");
                    }
                }));
        dialog.show();
    }

    private void loadDiaries() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            applyFilter();
            return;
        }

        Query query = FirebaseDatabase.getInstance().getReference("Diaries")
                .orderByChild("userId").equalTo(currentUser.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allDiaries.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Diary diary = ds.getValue(Diary.class);
                    if (diary != null) {
                        if (diary.getId() == null) diary.setId(ds.getKey());
                        allDiaries.add(diary);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải nhật ký: " + error.getMessage());
            }
        });
    }

    private void applyFilter() {
        if (!isAdded() || diaryAdapter == null) return;

        String query = edtSearch != null && edtSearch.getText() != null
                ? edtSearch.getText().toString().trim().toLowerCase(Locale.getDefault())
                : "";

        String monthPrefix = null;
        int checkedChip = chipMonth != null ? chipMonth.getCheckedChipId() : View.NO_ID;
        Calendar calendar = Calendar.getInstance();
        if (checkedChip == R.id.chip_month_current) {
            monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    .format(calendar.getTime());
        } else if (checkedChip == R.id.chip_month_prev) {
            calendar.add(Calendar.MONTH, -1);
            monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    .format(calendar.getTime());
        }

        visibleDiaries.clear();
        for (Diary diary : allDiaries) {
            if (!query.isEmpty() && !matchesQuery(diary, query)) continue;
            if (monthPrefix != null) {
                String date = diary.getDate();
                if (date == null || !date.startsWith(monthPrefix)) continue;
            }
            visibleDiaries.add(diary);
        }

        Collections.sort(visibleDiaries, (a, b) -> {
            if (a.isPinned() != b.isPinned()) return a.isPinned() ? -1 : 1;
            if (a.getTimestamp() != b.getTimestamp()) {
                return Long.compare(b.getTimestamp(), a.getTimestamp());
            }
            String da = a.getDate() != null ? a.getDate() : "";
            String db = b.getDate() != null ? b.getDate() : "";
            return db.compareTo(da);
        });

        diaryAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateSubtitle();
    }

    private boolean matchesQuery(Diary diary, String query) {
        String title = diary.getTitle() != null ? diary.getTitle().toLowerCase(Locale.getDefault()) : "";
        String content = diary.getContent() != null ? diary.getContent().toLowerCase(Locale.getDefault()) : "";
        return title.contains(query) || content.contains(query);
    }

    private void updateSubtitle() {
        if (!isAdded() || tvDiarySubtitle == null) return;

        if (Prefs.hasDiaryPin(requireContext()) && !unlocked) {
            tvDiarySubtitle.setText("Đã khóa • Nhập PIN để xem nhật ký");
            return;
        }

        int pinned = 0;
        for (Diary diary : visibleDiaries) {
            if (diary.isPinned()) pinned++;
        }
        String text = visibleDiaries.size() + " bài viết";
        if (pinned > 0) text += " • " + pinned + " đã ghim";
        tvDiarySubtitle.setText(text);
    }

    private void updateEmptyState() {
        if (!isAdded() || layoutEmptyDiary == null) return;

        boolean locked = Prefs.hasDiaryPin(requireContext()) && !unlocked;
        if (locked) {
            layoutEmptyDiary.setVisibility(View.GONE);
            return;
        }

        boolean isEmpty = visibleDiaries.isEmpty();
        layoutEmptyDiary.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvDiary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (allDiaries.isEmpty()) {
            tvEmptyTitle.setText("Chưa có bài viết nào");
            tvEmptyHint.setText("Bắt đầu ghi lại ngày hôm nay của bạn");
        } else {
            tvEmptyTitle.setText("Không tìm thấy bài viết");
            tvEmptyHint.setText("Thử từ khóa khác hoặc chọn tháng khác");
        }
    }

    private void togglePin(Diary diary) {
        if (diary.getId() == null) return;

        HashMap<String, Object> update = new HashMap<>();
        update.put("pinned", !diary.isPinned());

        FirebaseDatabase.getInstance().getReference("Diaries")
                .child(diary.getId())
                .updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                diary.isPinned() ? "Đã bỏ ghim" : "Đã ghim lên đầu",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không cập nhật được: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
