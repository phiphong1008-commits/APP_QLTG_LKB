package dpphong.ntu.appqlcv.ck;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Nhóm / gia đình (P2): tạo nhóm bằng mã 6 ký tự, tham gia bằng mã,
 * xem thành viên và chia sẻ mã. Dữ liệu lưu ở Groups và Users/{uid}/groups.
 */
public class GroupsFragment extends Fragment {

    private LinearLayout containerGroups;
    private String userId;
    private String displayName = "Thành viên";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                displayName = user.getDisplayName();
            } else if (user.getEmail() != null) {
                displayName = user.getEmail().split("@")[0];
            }
        }

        containerGroups = view.findViewById(R.id.container_groups);
        ImageButton btnBack = view.findViewById(R.id.btn_back_groups);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.btn_create_group).setOnClickListener(v -> showCreateDialog());
        view.findViewById(R.id.btn_join_group).setOnClickListener(v -> showJoinDialog());

        loadMyGroups();
        return view;
    }

    private void loadMyGroups() {
        if (userId == null) return;
        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<GroupEntry> groups = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String code = child.getKey();
                            String name = child.getValue(String.class);
                            if (code != null) groups.add(new GroupEntry(code, name != null ? name : "Nhóm"));
                        }
                        renderGroups(groups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void renderGroups(List<GroupEntry> groups) {
        if (!isAdded() || containerGroups == null) return;
        containerGroups.removeAllViews();

        if (groups.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Bạn chưa tham gia nhóm nào.\nTạo nhóm mới hoặc nhập mã được chia sẻ.");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));
            empty.setTextSize(13f);
            empty.setPadding(0, dp(8), 0, dp(8));
            containerGroups.addView(empty);
            return;
        }

        for (GroupEntry group : groups) {
            containerGroups.addView(buildGroupCard(group));
        }
    }

    private View buildGroupCard(GroupEntry group) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_pill_neutral);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        card.setLayoutParams(params);

        TextView name = new TextView(requireContext());
        name.setText("👨‍👩‍👧 " + group.name);
        name.setTextSize(15f);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary));

        TextView code = new TextView(requireContext());
        code.setText("Mã nhóm: " + group.code);
        code.setTextSize(12f);
        code.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

        TextView hint = new TextView(requireContext());
        hint.setText("Chạm để xem thành viên • nhấn giữ để chia sẻ / rời nhóm");
        hint.setTextSize(11f);
        hint.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextHint));

        card.addView(name);
        card.addView(code);
        card.addView(hint);

        card.setOnClickListener(v -> showMembers(group));
        card.setOnLongClickListener(v -> {
            String[] actions = {"Chia sẻ mã nhóm", "Rời nhóm"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(group.name)
                    .setItems(actions, (dialog, which) -> {
                        if (which == 0) shareGroup(group);
                        else confirmLeave(group);
                    })
                    .show();
            return true;
        });
        return card;
    }

    private void showCreateDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Tên nhóm (vd: Gia đình)");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(dp(20), dp(12), dp(20), dp(12));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tạo nhóm mới")
                .setView(input)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createGroup(name);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createGroup(String name) {
        if (userId == null) return;
        String code = generateCode();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> group = new HashMap<>();
        group.put("name", name);
        group.put("ownerId", userId);
        group.put("createdAt", System.currentTimeMillis());
        group.put("members/" + userId, displayName);

        database.child("Groups").child(code).setValue(group)
                .addOnSuccessListener(aVoid -> {
                    database.child("Users").child(userId).child("groups")
                            .child(code).setValue(name);
                    Toast.makeText(getContext(), "Đã tạo nhóm. Mã: " + code, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Không tạo được nhóm: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showJoinDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Nhập mã 6 ký tự");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setPadding(dp(20), dp(12), dp(20), dp(12));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tham gia nhóm")
                .setView(input)
                .setPositiveButton("Tham gia", (dialog, which) -> {
                    String code = input.getText().toString().trim().toUpperCase(Locale.getDefault());
                    if (code.length() < 4) {
                        Toast.makeText(getContext(), "Mã nhóm không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    joinGroup(code);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void joinGroup(String code) {
        if (userId == null) return;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("Groups").child(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "Không tìm thấy nhóm với mã này",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        String name = snapshot.child("name").getValue(String.class);
                        database.child("Groups").child(code).child("members")
                                .child(userId).setValue(displayName);
                        database.child("Users").child(userId).child("groups")
                                .child(code).setValue(name != null ? name : "Nhóm");
                        Toast.makeText(getContext(), "Đã tham gia nhóm " + name,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showMembers(GroupEntry group) {
        FirebaseDatabase.getInstance().getReference("Groups").child(group.code).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder builder = new StringBuilder();
                        for (DataSnapshot member : snapshot.getChildren()) {
                            String name = member.getValue(String.class);
                            builder.append("• ").append(name != null ? name : "Thành viên").append("\n");
                        }
                        if (builder.length() == 0) builder.append("Chưa có thành viên");

                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(group.name + " • " + group.code)
                                .setMessage(builder.toString())
                                .setPositiveButton("Chia sẻ mã", (dialog, which) -> shareGroup(group))
                                .setNegativeButton("Đóng", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void shareGroup(GroupEntry group) {
        String text = "Mời bạn tham gia nhóm \"" + group.name + "\" trên APP_QLTG.\n"
                + "Mã nhóm: " + group.code + "\n"
                + "Mở app → Khám phá → Nhóm & gia đình → Tham gia, nhập mã này nhé!";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Chia sẻ mã nhóm"));
    }

    private void confirmLeave(GroupEntry group) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rời nhóm")
                .setMessage("Rời khỏi nhóm \"" + group.name + "\"?")
                .setPositiveButton("Rời", (dialog, which) -> {
                    if (userId == null) return;
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    database.child("Groups").child(group.code).child("members").child(userId).removeValue();
                    database.child("Users").child(userId).child("groups").child(group.code).removeValue();
                })
                .setNegativeButton("Hủy", null)
                .show();
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class GroupEntry {
        final String code;
        final String name;

        GroupEntry(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
