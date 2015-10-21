package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by pedram on 16/10/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {

    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri scoresUri = DatabaseContract.scores_table.buildScoreWithDate();
                String[] date = new String[1];
                Date currentDate = new Date(System.currentTimeMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                date[0] = format.format(currentDate);
                data = getContentResolver().query(scoresUri, null, null, date, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                views.setTextViewText(R.id.widget_home_name, data.getString(COL_HOME));
                views.setTextViewText(R.id.widget_away_name, data.getString(COL_AWAY));
                views.setTextViewText(R.id.widget_date_textview, data.getString(COL_MATCHTIME));
                views.setTextViewText(R.id.widget_score_textview, Utilities.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS)));

                int homeCrest = Utilities.getTeamCrestByTeamName(data.getString(COL_HOME));
                int awayCrest = Utilities.getTeamCrestByTeamName(data.getString(COL_AWAY));

                views.setImageViewResource(R.id.widget_home_crest, homeCrest);
                views.setImageViewResource(R.id.widget_away_crest, awayCrest);

                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}