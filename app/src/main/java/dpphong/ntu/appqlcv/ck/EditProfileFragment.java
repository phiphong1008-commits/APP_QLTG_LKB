package dpphong.ntu.appqlcv.ck;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private TextView tvAvatar, tvEmail;
    private TextInputEditText edtName;
    private ChipGroup chipColors;
    private MaterialButton btnSave;
    private FirebaseAuth mAuth;

    private String currentColor = "#4C6FFF";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        tvAvatar = view.findViewById(R.id.tv_edit_avatar);
        tvEmail = view.findViewById(R.id.tv_edit_profile_email);
        edtName = view.findViewById(R.id.edt_profile_name);
        chipColors = view.findViewById(R.id.chip_group_avatar_colors);
        btnSave = view.findViewById(R.id.btn_save_profile);
        ImageButton btnBack = view.findViewById(R.id.btn_back_edit_profile);
        MaterialButton btnChangePassword = view.findViewById(R.id.btn_change_password);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        chipColors.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentColor = getSelectedColor(group);
            applyAvatarColor();
        });

        btnSave.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> sendPasswordReset());

        edtName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAvatarLetter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        loadProfile();
        return view;
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        tvEmail.setText(user.getEmail());

        String name = user.getDisplayName();
        if (TextUtils.isEmpty(name) && user.getEmail() != null) {
            name = user.getEmail().split("@")[0];
        }
        edtName.setText(name);
        updateAvatarLetter(name);

        String uid = user.getUid();
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String savedName = snapshot.child("displayName").getValue(String.class);
                        String savedColor = snapshot.child("avatarColor").getValue(String.class);

                        if (!TextUtils.isEmpty(savedName)) {
                            edtName.setText(savedName);
                            updateAvatarLetter(savedName);
                        }
                        if (!TextUtils.isEmpty(savedColor)) {
                            currentColor = savedColor;
                            checkColorChip(savedColor);
                            applyAvatarColor();
                        } else {
                            applyAvatarColor();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        applyAvatarColor();
                    }
                });
    }

    private void updateAvatarLetter(String name) {
        if (TextUtils.isEmpty(name)) {
            tvAvatar.setText("?");
            return;
        }
        String trimmed = name.trim();
        tvAvatar.setText(trimmed.isEmpty() ? "?" : trimmed.substring(0, 1).toUpperCase());
    }

    private void applyAvatarColor() {
        try {
            ViewCompat.setBackgroundTintList(tvAvatar,
                    ColorStateList.valueOf(Color.parseColor(currentColor)));
        } catch (Exception ignored) {
            ViewCompat.setBackgroundTintList(tvAvatar,
                    ColorStateList.valueOf(Color.parseColor("#4C6FFF")));
        }
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Vui lòng nhập tên hiển thị");
            return;
        }

        btnSave.setEnabled(false);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(request).addOnCompleteListener(task -> {
            Map<String, Object> data = new HashMap<>();
            data.put("displayName", name);
            data.put("avatarColor", currentColor);
            data.put("email", user.getEmail());

            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid())
                    .updateChildren(data)
                    .addOnCompleteListener(dbTask -> {
                        btnSave.setEnabled(true);
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Không lưu được hồ sơ", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void sendPasswordReset() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Toast.makeText(getContext(), "Không tìm thấy email tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Đã gửi email đổi mật khẩu tới " + user.getEmail(), Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Không gửi được email: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void checkColorChip(String color) {
        int id = R.id.chip_avatar_blue;
        if (!TextUtils.isEmpty(color)) {
            switch (color.toUpperCase()) {
                case "#FF6B6B":
                    id = R.id.chip_avatar_red;
                    break;
                case "#22C55E":
                    id = R.id.chip_avatar_green;
                    break;
                case "#F59E0B":
                    id = R.id.chip_avatar_amber;
                    break;
                case "#A855F7":
                    id = R.id.chip_avatar_purple;
                    break;
                case "#14B8A6":
                    id = R.id.chip_avatar_teal;
                    break;
                default:
                    id = R.id.chip_avatar_blue;
                    break;
            }
        }
        chipColors.check(id);
    }

    private String getSelectedColor(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chip_avatar_red) return "#FF6B6B";
        if (id == R.id.chip_avatar_green) return "#22C55E";
        if (id == R.id.chip_avatar_amber) return "#F59E0B";
        if (id == R.id.chip_avatar_purple) return "#A855F7";
        if (id == R.id.chip_avatar_teal) return "#14B8A6";
        return "#4C6FFF";
    }
}
