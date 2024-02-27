package br.com.irsolu.jobscheduler;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@TargetApi(21)
public class FetchJobService extends JobService {
  public static final String FINISH_LOG_MSG = "=> job finished";
  public static final String ON_STOP_JOB_LOB_MSG = "on stop job called";
  public static final String NO_NETWORK_AVAILABLE_LOG_MSG = "no network available, noop job";

  @Override
  public boolean onStartJob(final JobParameters params) {
    Context context = getApplicationContext();

    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

    if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
      Log.d(JobSchedulerPlugin.TAG, NO_NETWORK_AVAILABLE_LOG_MSG);

      return false;
    }

    CompletionHandler completionHandler = new CompletionHandler() {
      @Override
      public void finish() {
        Log.d(JobSchedulerPlugin.TAG, FINISH_LOG_MSG);
        jobFinished(params, false);
      }
    };

    JobSchedulerPlugin.getInstance(context).onFetch(completionHandler);

    return true;
  }

  @Override
  public boolean onStopJob(final JobParameters params) {
    Log.d(JobSchedulerPlugin.TAG, FetchJobService.ON_STOP_JOB_LOB_MSG);

    jobFinished(params, false);

    return true;
  }

  public interface CompletionHandler {
    void finish();
  }
}
