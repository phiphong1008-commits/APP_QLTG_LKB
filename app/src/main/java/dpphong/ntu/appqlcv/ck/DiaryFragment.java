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
    private TextView tvHeaderTitle;
    private RecyclerView rvDiary;
    private FloatingActionButton fabAddDiary;
    private DiaryAdapter diaryAdapter;
    private List<Diary> diaryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng chung layout cấu trúc với Todo hoặc tự tạo fragment_diary.xml có id tương ứng
        View view = inflater.inflate(R.layout.fragment_todo, container, false); // Tạm dùng layout có sẵn rv_todo_today và fab_add_todo

        rvDiary = view.findViewById(R.id.rv_todo_today);
        fabAddDiary = view.findViewById(R.id.fab_add_todo);
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        tvHeaderTitle.setText("Nhật Ký");
        diaryList = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(diaryList);
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiary.setAdapter(diaryAdapter);

        loadDiaries();

        fabAddDiary.setOnClickListener(v -> {
            // Chuyển sang trang thêm Nhật ký (Tên Fragment nhập liệu của bạn)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, new AddDiaryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadDiaries() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        // Truy vấn bảng Diaries, lấy các bài của user đang đăng nhập
        Query query = FirebaseDatabase.getInstance().getReference("Diaries")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diaryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Diary diary = ds.getValue(Diary.class);
                    if (diary != null) {
                        // Thêm vào đầu danh sách để bài mới nhất hiện lên trên cùng
                        diaryList.add(0, diary);
                    }
                }
                diaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải nhật ký: " + error.getMessage());
            }
        });
    }
}