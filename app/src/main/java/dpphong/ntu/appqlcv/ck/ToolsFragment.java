package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Trung tâm tính năng nâng cao (P1 + P2): lịch tuần, thêm nhanh, tập trung,
 * thói quen, thống kê, ma trận, mẫu, thành tích, trợ lý và nhóm.
 */
public class ToolsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        ImageButton btnBack = view.findViewById(R.id.btn_back_tools);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        bind(view, R.id.btn_tool_week, new WeekFragment());
        bind(view, R.id.btn_tool_quick_add, new QuickAddDialogFragment());
        bind(view, R.id.btn_tool_focus, new FocusFragment());
        bind(view, R.id.btn_tool_habits, new HabitsFragment());
        bind(view, R.id.btn_tool_stats, new StatsFragment());
        bind(view, R.id.btn_tool_matrix, new MatrixFragment());
        bind(view, R.id.btn_tool_templates, new TemplatesFragment());
        bind(view, R.id.btn_tool_achievements, new AchievementsFragment());
        bind(view, R.id.btn_tool_assistant, new AssistantFragment());
        bind(view, R.id.btn_tool_groups, new GroupsFragment());

        return view;
    }

    private void bind(View root, int id, Fragment target) {
        View button = root.findViewById(id);
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (target instanceof androidx.fragment.app.DialogFragment) {
                ((androidx.fragment.app.DialogFragment) target)
                        .show(getParentFragmentManager(), "quick_add_tool");
                return;
            }
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, target)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
