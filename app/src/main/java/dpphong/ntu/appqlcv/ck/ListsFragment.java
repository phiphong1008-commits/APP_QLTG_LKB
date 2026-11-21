package dpphong.ntu.appqlcv.ck;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ListsFragment extends Fragment {

    private RecyclerView rvLists;
    private View layoutEmpty;
    private ListAdapter adapter;
    private final List<TaskList> lists = new ArrayList<>();
    private final Map<String, Integer> counts = new HashMap<>();

    private DatabaseReference database;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) userId = currentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();

        rvLists = view.findViewById(R.id.rv_lists);
        layoutEmpty = view.findViewById(R.id.layout_empty_lists);
        ImageButton btnBack = view.findViewById(R.id.btn_back_lists);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_list);
        ImageButton btnImport = view.findViewById(R.id.btn_import_list);

        adapter = new ListAdapter(lists, new ListAdapter.Listener() {
            @Override
            public void onEdit(TaskList taskList) {
                showListDialog(taskList);
            }

            @Override
            public void onDelete(TaskList taskList) {
                confirmDelete(taskList);
            }

            @Override
            public void onShare(TaskList taskList) {
                shareList(taskList);
            }
        });

        rvLists.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLists.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
        btnAdd.setOnClickListener(v -> showListDialog(null));
        btnImport.setOnClickListener(v -> showImportDialog());

        loadLists();
        loadTaskCounts();

        return view;
    }

    private void loadLists() {
        if (userId == null) return;

        Query query = database.child("Lists").orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lists.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskList taskList = child.getValue(TaskList.class);
                    if (taskList != null) {
                        if (taskList.getId() == null) taskList.setId(child.getKey());
                        lists.add(taskList);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTaskCounts() {
        if (userId == null) return;

        database.child("Tasks").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        counts.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Task task = child.getValue(Task.class);
                            if (task != null && task.getListId() != null) {
                                Integer current = counts.get(task.getListId());
                                counts.put(task.getListId(), (current != null ? current : 0) + 1);
                            }
                        }
                        adapter.updateCounts(counts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void updateEmptyState() {
        boolean empty = lists.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvLists.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showListDialog(@Nullable TaskList existing) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_list, null, false);

        TextInputEditText edtName = dialogView.findViewById(R.id.edt_list_name);
        ChipGroup chipColors = dialogView.findViewById(R.id.chip_group_list_colors);

        if (existing != null) {
            edtName.setText(existing.getName());
            checkColorChip(chipColors, existing.getColor());
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Tạo danh sách" : "Sửa danh sách")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(name)) {
                        edtName.setError("Vui lòng nhập tên danh sách");
                        return;
                    }

                    String color = getSelectedColor(chipColors);
                    saveList(existing, name, color);
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private void checkColorChip(ChipGroup group, String color) {
        int id = R.id.chip_color_blue;
        if (!TextUtils.isEmpty(color)) {
            switch (color.toUpperCase()) {
                case "#FF6B6B":
                    id = R.id.chip_color_red;
                    break;
                case "#22C55E":
                    id = R.id.chip_color_green;
                    break;
                case "#F59E0B":
                    id = R.id.chip_color_amber;
                    break;
                case "#A855F7":
                    id = R.id.chip_color_purple;
                    break;
                case "#14B8A6":
                    id = R.id.chip_color_teal;
                    break;
                default:
                    id = R.id.chip_color_blue;
                    break;
            }
        }
        group.check(id);
    }

    private String getSelectedColor(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chip_color_red) return "#FF6B6B";
        if (id == R.id.chip_color_green) return "#22C55E";
        if (id == R.id.chip_color_amber) return "#F59E0B";
        if (id == R.id.chip_color_purple) return "#A855F7";
        if (id == R.id.chip_color_teal) return "#14B8A6";
        return "#4C6FFF";
    }

    private void saveList(@Nullable TaskList existing, String name, String color) {
        if (userId == null) return;

        if (existing == null) {
            String id = database.child("Lists").push().getKey();
            if (id == null) return;
            TaskList taskList = new TaskList(id, name, color, userId, System.currentTimeMillis());
            database.child("Lists").child(id).setValue(taskList);
        } else {
            Map<String, Object> update = new HashMap<>();
            update.put("name", name);
            update.put("color", color);
            database.child("Lists").child(existing.getId()).updateChildren(update);

            // Cập nhật tên/màu mới cho các công việc đang thuộc danh sách này
            updateTasksDenormalized(existing.getId(), name, color);
        }
    }

    private void updateTasksDenormalized(String listId, String name, String color) {
        if (userId == null) return;

        database.child("Tasks").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Task task = child.getValue(Task.class);
                            if (task != null && listId.equals(task.getListId())) {
                                child.getRef().child("listName").setValue(name);
                                child.getRef().child("listColor").setValue(color);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void confirmDelete(TaskList taskList) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa danh sách")
                .setMessage("Xóa danh sách '" + taskList.getName() + "'? Các công việc bên trong sẽ không bị xóa.")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (taskList.getId() != null) {
                        database.child("Lists").child(taskList.getId()).removeValue();
                        clearTasksFromList(taskList.getId());
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearTasksFromList(String listId) {
        if (userId == null) return;

        database.child("Tasks").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Task task = child.getValue(Task.class);
                            if (task != null && listId.equals(task.getListId())) {
                                Map<String, Object> update = new HashMap<>();
                                update.put("listId", null);
                                update.put("listName", null);
                                update.put("listColor", null);
                                child.getRef().updateChildren(update);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // ==========================================
    // CHIA SẺ / NHẬP DANH SÁCH
    // ==========================================
    private void shareList(TaskList taskList) {
        if (userId == null || taskList == null || taskList.getId() == null) return;

        final String shareCode = !TextUtils.isEmpty(taskList.getShareCode())
                ? taskList.getShareCode()
                : generateCode();

        if (TextUtils.isEmpty(taskList.getShareCode())) {
            taskList.setShareCode(shareCode);
            database.child("Lists").child(taskList.getId()).child("shareCode").setValue(shareCode);
        }

        database.child("Tasks").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> taskMaps = new HashMap<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Task task = child.getValue(Task.class);
                            if (task == null) continue;
                            if (!taskList.getId().equals(task.getListId())) continue;

                            Map<String, Object> item = new HashMap<>();
                            item.put("title", safe(task.getTitle()));
                            item.put("description", safe(task.getDescription()));
                            item.put("date", safe(task.getDate()));
                            item.put("time", safe(task.getTime()));
                            item.put("priority", task.getPriority() != null ? task.getPriority() : "Vừa");
                            item.put("icon", task.getIcon() != null ? task.getIcon() : "work");
                            item.put("recurrence", task.getRecurrence() != null ? task.getRecurrence() : "none");
                            taskMaps.put(child.getKey(), item);
                        }

                        Map<String, Object> shared = new HashMap<>();
                        shared.put("code", shareCode);
                        shared.put("name", safe(taskList.getName()));
                        shared.put("color", safe(taskList.getColor()));
                        shared.put("ownerId", userId);
                        shared.put("createdAt", System.currentTimeMillis());
                        shared.put("taskCount", taskMaps.size());
                        shared.put("tasks", taskMaps);

                        database.child("SharedLists").child(shareCode)
                                .setValue(shared)
                                .addOnSuccessListener(aVoid -> showShareSheet(taskList.getName(),
                                        shareCode, taskMaps.size()))
                                .addOnFailureListener(e -> Toast.makeText(getContext(),
                                        "Không chia sẻ được: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Không đọc được công việc để chia sẻ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showShareSheet(String name, String code, int taskCount) {
        if (!isAdded()) return;

        String text = "Mời bạn dùng danh sách \"" + (name != null ? name : "Danh sách") + "\" trên APP_QLTG.\n"
                + "Mã chia sẻ: " + code + " • " + taskCount + " công việc\n"
                + "Mở app → Công việc → Danh sách → nhấn biểu tượng tải xuống → nhập mã này.";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Chia sẻ danh sách"));
    }

    private void showImportDialog() {
        if (!isAdded()) return;

        final EditText input = new EditText(requireContext());
        input.setHint("Nhập mã chia sẻ (6 ký tự)");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nhập danh sách được chia sẻ")
                .setIcon(R.drawable.ic_download)
                .setMessage("Nhập mã chia sẻ để sao chép danh sách và công việc vào tài khoản của bạn.")
                .setView(input)
                .setPositiveButton("Nhập", (dialog, which) -> {
                    String code = input.getText().toString().trim().toUpperCase(Locale.getDefault());
                    if (code.length() < 4) {
                        Toast.makeText(getContext(), "Mã chia sẻ không hợp lệ",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    importList(code);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void importList(String code) {
        if (userId == null) return;

        database.child("SharedLists").child(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "Không tìm thấy danh sách với mã này",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        String name = snapshot.child("name").getValue(String.class);
                        String color = snapshot.child("color").getValue(String.class);
                        if (TextUtils.isEmpty(name)) name = "Danh sách chia sẻ";

                        String newListId = database.child("Lists").push().getKey();
                        if (newListId == null) return;

                        TaskList newList = new TaskList(newListId, name, color, userId,
                                System.currentTimeMillis());
                        newList.setShareCode(code);
                        database.child("Lists").child(newListId).setValue(newList);

                        int count = 0;
                        for (DataSnapshot child : snapshot.child("tasks").getChildren()) {
                            Object value = child.getValue();
                            if (!(value instanceof Map)) continue;

                            String taskId = database.child("Tasks").push().getKey();
                            if (taskId == null) continue;

                            Map<String, Object> item = new HashMap<>((Map<String, Object>) value);
                            item.put("id", taskId);
                            item.put("userId", userId);
                            item.put("listId", newListId);
                            item.put("listName", name);
                            item.put("listColor", color != null ? color : "#4C6FFF");
                            item.put("isCompleted", false);
                            item.put("nextSpawned", false);
                            item.put("timestamp", System.currentTimeMillis());

                            database.child("Tasks").child(taskId).setValue(item);
                            count++;
                        }

                        Toast.makeText(getContext(),
                                "Đã nhập \"" + name + "\" với " + count + " công việc",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Không đọc được danh sách chia sẻ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
