package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiaryFragment extends Fragment {
    private RecyclerView rvDiary;
    private FloatingActionButton fabAddDiary;
    private DiaryAdapter diaryAdapter;
    private List<Diary> diaryList;
    private TextView tvDiarySubtitle;
    private View layoutEmptyDiary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);

        rvDiary = view.findViewById(R.id.rv_diary_list);
        fabAddDiary = view.findViewById(R.id.fab_add_diary);
        tvDiarySubtitle = view.findViewById(R.id.tv_diary_subtitle);
        layoutEmptyDiary = view.findViewById(R.id.layout_empty_diary);
        View cardCompose = view.findViewById(R.id.card_compose_diary);

        diaryList = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(diaryList);
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiary.setAdapter(diaryAdapter);

        loadDiaries();

        View.OnClickListener openEditor = v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, new AddDiaryFragment())
                .addToBackStack(null)
                .commit();
        fabAddDiary.setOnClickListener(openEditor);
        cardCompose.setOnClickListener(openEditor);

        return view;
    }

    private void loadDiaries() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateEmptyState();
            return;
        }

        String currentUserId = currentUser.getUid();

        Query query = FirebaseDatabase.getInstance().getReference("Diaries")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diaryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Diary diary = ds.getValue(Diary.class);
                    if (diary != null) {
                        // Bài mới nhất hiện lên trên cùng
                        diaryList.add(0, diary);
                    }
                }
                diaryAdapter.notifyDataSetChanged();
                if (tvDiarySubtitle != null) {
                    tvDiarySubtitle.setText(diaryList.size() + " bài viết • Lưu lại khoảnh khắc của bạn");
                }
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải nhật ký: " + error.getMessage());
            }
        });
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        boolean isEmpty = diaryList.isEmpty();
        layoutEmptyDiary.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvDiary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
