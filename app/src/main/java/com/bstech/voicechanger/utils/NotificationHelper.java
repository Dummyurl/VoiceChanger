package com.bstech.voicechanger.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;

import java.io.IOException;


/**
 * Created by tanpt on 03/04/2017.
 */

public class NotificationHelper {

    private static int requestCode = 10;

    public static Notification buildNotification(Context context, MediaSessionCompat mSession, boolean isPlaying, Song song) {
        requestCode = SharedPrefs.getInstance().get(Keys.PREF_REQ_CODE, Integer.class, 10);

        int playState = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;


        String title = song.getNameSong();
        if (TextUtils.isEmpty(title)) {
            title = "Song Unknown";
        }

        final Bitmap[] albumArt = new Bitmap[1];


        try {
            albumArt[0] = Utils.getThumbnail(Uri.parse(song.getPath()), context);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (albumArt[0] == null) {
            albumArt[0] = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_play_arrow_black_24dp);
        }

        String artist = song.getNameArtist();
        if (TextUtils.isEmpty(artist)) {
            artist = "Unknown";
        }
        if (albumArt[0] != null) {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt[0])
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .build()
            );
        } else {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .build()
            );
        }

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(playState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build());

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT); // khanh change 0 => PendingIntent.FLAG_UPDATE_CURRENT

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(song.getNameSong())
                .setContentText(song.getNameArtist())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "", retrievePlaybackAction(context, MusicService.NOTIFY_PREVIOUS));

        builder.setLargeIcon(albumArt[0]);

        if (isPlaying) {
            builder.addAction(R.drawable.ic_pause_black_24dp, "", retrievePlaybackAction(context, MusicService.NOTIFY_PLAY));
        } else {
            builder.addAction(R.drawable.ic_play_arrow_black_24dp, "", retrievePlaybackAction(context, MusicService.NOTIFY_PLAY));
        }

        builder.addAction(R.drawable.ic_skip_next_black_24dp, "", retrievePlaybackAction(context, MusicService.NOTIFY_NEXT))
                .setShowWhen(false);

        android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(retrievePlaybackAction(context, MusicService.NOTIFY_STOP));

        builder.setStyle(style);

        builder.setSmallIcon(R.mipmap.ic_launcher);

        SharedPrefs.getInstance().put(Keys.PREF_REQ_CODE, requestCode);

        return builder.build();
    }

    private static PendingIntent retrievePlaybackAction(Context context, final String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
