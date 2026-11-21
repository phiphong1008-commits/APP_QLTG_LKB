package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Danh sách mẫu công việc (P2). Mẫu được tạo từ menu nhấn giữ ở TaskAdapter.
 */
public class TemplatesFragment extends Fragment {

    private RecyclerView rvTemplates;
    private View layoutEmpty;
    private TemplateAdapter adapter;
    private final List<TaskTemplate> templates = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_templates, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) userId = user.getUid();

        rvTemplates = view.findViewById(R.id.rv_templates);
        layoutEmpty = view.findViewById(R.id.layout_empty_templates);
        ImageButton btnBack = view.findViewById(R.id.btn_back_templates);

        adapter = new TemplateAdapter(new TemplateAdapter.Listener() {
            @Override
            public void onUse(TaskTemplate template) {
                useTemplate(template);
            }

            @Override
            public void onDelete(TaskTemplate template) {
                confirmDelete(template);
            }
        });

        rvTemplates.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTemplates.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        loadTemplates();
        return view;
    }

    private void loadTemplates() {
        if (userId == null) {
            render();
            return;
        }

        Query query = FirebaseDatabase.getInstance().getReference("Templates")
                .orderByChild("userId").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                templates.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskTemplate template = child.getValue(TaskTemplate.class);
                    if (template != null) {
                        if (template.getId() == null) template.setId(child.getKey());
                        templates.add(template);
                    }
                }
                adapter.update(templates);
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void render() {
        if (!isAdded()) return;
        boolean empty = templates.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTemplates.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void useTemplate(TaskTemplate template) {
        if (userId == null) return;
        String taskId = FirebaseDatabase.getInstance().getReference("Tasks").push().getKey();
        if (taskId == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        HashMap<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", taskId);
        taskMap.put("userId", userId);
        taskMap.put("title", template.getTitle() != null ? template.getTitle() : template.getName());
        taskMap.put("description", template.getDescription());
        taskMap.put("date", today);
        taskMap.put("time", "");
        taskMap.put("priority", template.getPriority() != null ? template.getPriority() : "Vừa");
        taskMap.put("icon", template.getIcon() != null ? template.getIcon() : "work");
        taskMap.put("isCompleted", false);
        taskMap.put("timestamp", System.currentTimeMillis());
        taskMap.put("listId", template.getListId());
        taskMap.put("listName", template.getListName());
        taskMap.put("listColor", template.getListColor());
        taskMap.put("recurrence", template.getRecurrence() != null ? template.getRecurrence() : "none");
        taskMap.put("reminderOffsets", Collections.emptyList());
        taskMap.put("subtasks", Collections.emptyList());
        taskMap.put("nextSpawned", false);

        FirebaseDatabase.getInstance().getReference("Tasks").child(taskId).setValue(taskMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Đã tạo việc cho hôm nay từ mẫu", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Không tạo được: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void confirmDelete(TaskTemplate template) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa mẫu")
                .setMessage("Xóa mẫu '" + template.getName() + "'?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (template.getId() != null) {
                        FirebaseDatabase.getInstance().getReference("Templates")
                                .child(template.getId()).removeValue();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
