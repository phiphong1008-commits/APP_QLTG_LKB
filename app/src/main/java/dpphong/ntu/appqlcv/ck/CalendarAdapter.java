package dpphong.ntu.appqlcv.ck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> daysOfMonth;
    private HashMap<String, String> taskDataMap;
    private String currentMonthYear; // Định dạng "2026-05" để ghép với ngày

    public CalendarAdapter(Context context, ArrayList<String> daysOfMonth, String currentMonthYear, HashMap<String, String> taskDataMap) {
        this.context = context;
        this.daysOfMonth = daysOfMonth;
        this.currentMonthYear = currentMonthYear;
        this.taskDataMap = taskDataMap;
    }

    @Override
    public int getCount() {
        return daysOfMonth.size();
    }

    @Override
    public Object getItem(int position) {
        return daysOfMonth.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        if (cellView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cellView = inflater.inflate(R.layout.cell_day, parent, false);
        }

        TextView tvDayNumber = cellView.findViewById(R.id.tv_day_number);
//        ImageView imgStudy = cellView.findViewById(R.id.img_ic_study);
//        ImageView imgMeeting = cellView.findViewById(R.id.img_ic_meeting);

        String day = daysOfMonth.get(position);
        tvDayNumber.setText(day);

        // Mặc định ẩn các icon đi để tránh lỗi lặp icon khi cuộn lưới
//        if (imgStudy != null) imgStudy.setVisibility(View.GONE);
//        if (imgMeeting != null) imgMeeting.setVisibility(View.GONE);

        // Nếu ô đó không phải ô trống (ngày hợp lệ)
        if (!day.equals("")) {
            int dayInt = Integer.parseInt(day);
            // Định dạng ngày thành key chuẩn: "yyyy-MM-dd" (Ví dụ: "2026-05-18")
            String dateKey = String.format("%s-%02d", currentMonthYear, dayInt);

            // Kiểm tra map dữ liệu từ Firebase xem ngày này có việc gì không
//            if (taskDataMap != null && taskDataMap.containsKey(dateKey)) {
//                String taskType = taskDataMap.get(dateKey);
//                if ("STUDY".equals(taskType) && imgStudy != null) {
//                    imgStudy.setVisibility(View.VISIBLE);
//                } else if ("MEETING".equals(taskType) && imgMeeting != null) {
//                    imgMeeting.setVisibility(View.VISIBLE);
//                }
//            }
        } else {
            // Ô trống ở đầu/cuối tháng thì ẩn viền đi cho đẹp
            cellView.setBackgroundResource(android.R.color.transparent);
        }

        return cellView;
    }
}