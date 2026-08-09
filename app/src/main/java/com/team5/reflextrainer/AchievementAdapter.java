package com.team5.reflextrainer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.VH> {

    private final boolean[] earned;

    public AchievementAdapter(boolean[] earned) {
        this.earned = earned;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        boolean unlocked = earned[position];
        int avatarIndex = Achievements.BADGE_ICON_AVATAR_INDEX[position];

        h.icon.setImageResource(Avatars.resFor(avatarIndex));
        h.icon.setAlpha(unlocked ? 1f : 0.28f);
        h.name.setText(Achievements.NAMES[position]);
        h.description.setText(Achievements.DESCRIPTIONS[position]);

        Context ctx = h.status.getContext();
        h.status.setText(unlocked ? "Unlocked" : "Locked");
        h.status.setTextColor(ctx.getColor(unlocked ? R.color.accent : R.color.text_secondary));
    }

    @Override
    public int getItemCount() { return Achievements.COUNT; }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, description, status;
        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivBadgeIcon);
            name = itemView.findViewById(R.id.tvBadgeName);
            description = itemView.findViewById(R.id.tvBadgeDescription);
            status = itemView.findViewById(R.id.tvBadgeStatus);
        }
    }
}
