package com.rudraditya.mydashboard;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.ComponentName;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import java.util.List;

public class DashboardNotificationListener extends NotificationListenerService {
    public static final String ACTION_MEDIA = "com.rudraditya.mydashboard.MEDIA";
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_MEDIA.equals(intent.getAction())) {
            control(intent.getStringExtra("command"));
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override public void onNotificationPosted(StatusBarNotification s) { DashboardWidget.refresh(this); }
    @Override public void onNotificationRemoved(StatusBarNotification s) { DashboardWidget.refresh(this); }

    void control(String command) {
        try {
            MediaSessionManager msm = (MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
            List<MediaController> sessions = msm.getActiveSessions(new ComponentName(this, DashboardNotificationListener.class));
            for (MediaController c : sessions) {
                PlaybackState state = c.getPlaybackState();
                if (state == null || state.getState() == PlaybackState.STATE_NONE) continue;
                MediaController.TransportControls t = c.getTransportControls();
                if ("toggle".equals(command)) {
                    if (state.getState() == PlaybackState.STATE_PLAYING) t.pause(); else t.play();
                } else if ("next".equals(command)) t.skipToNext();
                else if ("previous".equals(command)) t.skipToPrevious();
                break;
            }
        } catch (SecurityException ignored) {}
        DashboardWidget.refresh(this);
    }
}
