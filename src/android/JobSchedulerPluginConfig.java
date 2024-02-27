package br.com.irsolu.jobscheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JobSchedulerPluginConfig {
  private Builder config;

  private static final String FETCH_INTERVAL_OPTION = "fetchInterval";
  private static final String MINIMUM_FETCH_INTERVAL_OPTION = "minimumFetchInterval";
  private static final String JOB_SERVICE_OPTION = "jobService";

  private static final int MINIMUM_FETCH_INTERVAL = 1;

  public static class Builder {
    private int minimumFetchInterval = MINIMUM_FETCH_INTERVAL;
    private String jobService = null;

    public Builder setMinimumFetchInterval(int fetchInterval) {
      if (fetchInterval >= MINIMUM_FETCH_INTERVAL) {
        this.minimumFetchInterval = fetchInterval;
      }

      return this;
    }

    public Builder setJobService(String className) {
      this.jobService = className;
      return this;
    }

    public JobSchedulerPluginConfig build() {
      return new JobSchedulerPluginConfig(this);
    }

    public Builder parse(JSONObject options) throws JSONException {
      if (options.has("minimumFetchInterval")) {
        setMinimumFetchInterval(options.getInt("minimumFetchInterval"));
      }

      return this;
    }

    public JobSchedulerPluginConfig load(Context context) {
      SharedPreferences preferences = context.getSharedPreferences(JobSchedulerPlugin.TAG, 0);

      if (preferences.contains(FETCH_INTERVAL_OPTION)) {
        setMinimumFetchInterval(preferences.getInt(FETCH_INTERVAL_OPTION, minimumFetchInterval));
      }

      if (preferences.contains(JOB_SERVICE_OPTION)) {
        setJobService(preferences.getString(JOB_SERVICE_OPTION, null));
      }

      return new JobSchedulerPluginConfig(this);
    }
  }

  private JobSchedulerPluginConfig(Builder builder) {
    config = builder;
  }

  public void save(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(JobSchedulerPlugin.TAG, 0);
    SharedPreferences.Editor editor = preferences.edit();

    editor.putInt(MINIMUM_FETCH_INTERVAL_OPTION, config.minimumFetchInterval);
    editor.putString(JOB_SERVICE_OPTION, config.jobService);
    editor.apply();
  }

  public int getMinimumFetchInterval() {
    return config.minimumFetchInterval;
  }

  public String getJobService() {
    return config.jobService;
  }

  public String toString() {
    JSONObject output = new JSONObject();

    try {
      output.put(MINIMUM_FETCH_INTERVAL_OPTION, config.minimumFetchInterval);
      output.put(JOB_SERVICE_OPTION, config.jobService);

      return output.toString(2);
    } catch (JSONException e) {
      return serializeOutputWithError(output,  e);
    }
  }

  private String serializeOutputWithError(JSONObject output, JSONException error) {
    return output.toString() + " with Error: " + error.toString();
  }
}
