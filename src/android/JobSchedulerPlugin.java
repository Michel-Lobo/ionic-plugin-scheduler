package br.com.irsolu.jobscheduler;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JobSchedulerPlugin {
  public static final String TAG = "JobSchedulerPluginTag";

  public static final String ACTION_CONFIGURE = "configure";
  public static final String ACTION_START     = "start";
  public static final String ACTION_STOP      = "stop";
  public static final String ACTION_FINISH    = "finish";
  public static final String ACTION_STATUS    = "status";
  public static final String ACTION_FORCE_RELOAD = TAG + "-forceReload";

  public static final String EVENT_FETCH      = ".event.BACKGROUND_FETCH";

  public static final String CONFIGURE_LOG_MSG = "configuring job with options: ";
  public static final String START_JOB_SCHEDULE_LOG_MSG = "scheduling job";
  public static final String STOP_JOB_SCHEDULE_LOG_MSG = "stoping job schedule";
  public static final String FINISH_JOB_EXECUTION_LOG_MSG = "- finishing job execution";
  public static final String ON_FETCH_LOG_MSG = "starting job execution";
  public static final String MAIN_ACTIVITY_ACTIVE_LOG_MSG = "- MainActivity is active";
  public static final String MAIN_ACTIVITY_INACTIVE_LOG_MSG = "- MainActivity is inactive";
  public static final String FORCE_MAIN_ACTIVITY_RELOAD_LOG_MSG = "-forcing MainActivity reload";
  public static final String FORCE_MAIN_ACTIVITY_RELOAD_FAILURE_LOG_MSG = "-error while forcing MainActivity reload: launch intent not found";
  public static final String GET_TASKS_PERMISSION_ERROR_LOG_MSG = "!!! JobSchedulerPlugin attempted to determine if MainActivity is active but was stopped due to a missing permission.  Please add the permission 'android.permission.GET_TASKS' to your AndroidManifest.  See Installation steps for more information";
  public static final String ON_BOOT_LOG_MSG = "booting device";

  public static final int STATUS_AVAILABLE = 2;

  private static JobSchedulerPlugin mInstance = null;
  private static int FETCH_JOB_ID = 999;

  public static JobSchedulerPlugin getInstance(Context context) {
    if (mInstance == null) {
      mInstance = getInstanceSynchronized(context.getApplicationContext());
    }

    return mInstance;
  }

  private static synchronized JobSchedulerPlugin getInstanceSynchronized(Context context) {
    if (mInstance == null) mInstance = new JobSchedulerPlugin(context.getApplicationContext());
    return mInstance;
  }

  private Context mContext;
  private JobSchedulerPlugin.Callback mCallback;
  private JobSchedulerPluginConfig mConfig;
  private FetchJobService.CompletionHandler mCompletionHandler;

  private JobSchedulerPlugin(Context context) {
    mContext = context;
  }

  public void configure(JobSchedulerPluginConfig config, JobSchedulerPlugin.Callback callback) {
    Log.d(TAG, CONFIGURE_LOG_MSG + config);

    mCallback = callback;
    config.save(mContext);
    mConfig = config;

    start();
  }

  public void onBoot() {
    Log.d(TAG, ON_BOOT_LOG_MSG);
    mConfig = new JobSchedulerPluginConfig.Builder().load(mContext);
    start();
  }

  @TargetApi(21)
  public void start() {
    Log.d(TAG, START_JOB_SCHEDULE_LOG_MSG);
    long fetchInterval = mConfig.getMinimumFetchInterval() * 60L * 1000L;
    JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    JobInfo.Builder builder = new JobInfo.Builder(FETCH_JOB_ID, new ComponentName(mContext, FetchJobService.class))
      .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
      .setRequiresDeviceIdle(false)
      .setRequiresCharging(false)
      .setPersisted(true);

    if (android.os.Build.VERSION.SDK_INT >= 24) {
      builder.setPeriodic(fetchInterval, fetchInterval);
    } else {
      builder.setPeriodic(fetchInterval);
    }

    if (jobScheduler != null) {
      jobScheduler.schedule(builder.build());
    }
  }

  public void stop() {
    Log.d(TAG, STOP_JOB_SCHEDULE_LOG_MSG);

    if (mCompletionHandler != null) {
      mCompletionHandler.finish();
    }

    JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);

    if (jobScheduler != null) {
      jobScheduler.cancel(FETCH_JOB_ID);
    }
  }

  public void finish() {
    Log.d(TAG, FINISH_JOB_EXECUTION_LOG_MSG);

    if (mCompletionHandler != null) {
      mCompletionHandler.finish();
      mCompletionHandler = null;
    }
  }

  public int status() {
    return STATUS_AVAILABLE;
  }

  public void onFetch(FetchJobService.CompletionHandler completionHandler) {
    mCompletionHandler = completionHandler;
    onFetch();
  }

  public void onFetch() {
    Log.d(TAG, ON_FETCH_LOG_MSG);

    if (mConfig == null) {
      mConfig = new JobSchedulerPluginConfig.Builder().load(mContext);
    }

    if (isMainActivityActive()) {
      Log.d(TAG, MAIN_ACTIVITY_ACTIVE_LOG_MSG);

      if (mCallback != null) {
        mCallback.onFetch();
      }
    } else {
      Log.d(TAG, MAIN_ACTIVITY_INACTIVE_LOG_MSG);
      forceMainActivityReload();
    }
  }

  public void forceMainActivityReload() {
    Log.i(TAG, FORCE_MAIN_ACTIVITY_RELOAD_LOG_MSG);

    PackageManager pm = mContext.getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(mContext.getPackageName());

    if (launchIntent == null) {
      Log.w(TAG, FORCE_MAIN_ACTIVITY_RELOAD_FAILURE_LOG_MSG);

      return;
    }

    launchIntent.setAction(ACTION_FORCE_RELOAD);
    launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

    mContext.startActivity(launchIntent);
  }

  public Boolean isMainActivityActive() {
    Boolean isActive = false;

    if (mContext == null) {
      return false;
    }

    ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

    try {
      List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

      for (ActivityManager.RunningTaskInfo task : tasks) {
        if (mContext.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
          isActive = true;
          break;
        }
      }
    } catch (java.lang.SecurityException e) {
      Log.w(TAG, GET_TASKS_PERMISSION_ERROR_LOG_MSG);

      throw e;
    }

    return isActive;
  }

  /**
   * @interface JobSchedulerPlugin.Callback
   */
  public interface Callback {
    void onFetch();
  }
}
