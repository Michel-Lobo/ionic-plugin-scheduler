package br.com.irsolu.jobscheduler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;

public class CDVJobSchedulerPlugin extends CordovaPlugin {
  private boolean isForceReload = false;

  @Override
  protected void pluginInitialize() {
    Activity activity   = cordova.getActivity();
    Intent launchIntent = activity.getIntent();
    String action       = launchIntent.getAction();

    if ((action != null) && (JobSchedulerPlugin.ACTION_FORCE_RELOAD.equalsIgnoreCase(action))) {
      isForceReload = true;
      activity.moveTaskToBack(true);
    }
  }

  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    boolean result = false;

    if (JobSchedulerPlugin.ACTION_CONFIGURE.equalsIgnoreCase(action)) {
      result = true;
      configure(data.getJSONObject(0), callbackContext);
    } else if (JobSchedulerPlugin.ACTION_START.equalsIgnoreCase(action)) {
      result = true;
      start(callbackContext);
    } else if (JobSchedulerPlugin.ACTION_STOP.equalsIgnoreCase(action)) {
      result = true;
      stop(callbackContext);
    } else if (JobSchedulerPlugin.ACTION_STATUS.equalsIgnoreCase(action)) {
      result = true;
      callbackContext.success(getAdapter().status());
    } else if (JobSchedulerPlugin.ACTION_FINISH.equalsIgnoreCase(action)) {
      finish(callbackContext);
      result = true;
    }

    return result;
  }

  private void configure(JSONObject options, final CallbackContext callbackContext) throws JSONException {
    JobSchedulerPlugin adapter = getAdapter();
    JobSchedulerPluginConfig.Builder config = new JobSchedulerPluginConfig.Builder().parse(options);

    JobSchedulerPlugin.Callback callback = new JobSchedulerPlugin.Callback() {
      @Override
      public void onFetch() {
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
      }
    };

    adapter.configure(config.build(), callback);

    if (isForceReload) {
      callback.onFetch();
    }

    isForceReload = false;
  }

  @TargetApi(21)
  private void start(CallbackContext callbackContext) {
    JobSchedulerPlugin adapter = getAdapter();
    adapter.start();
    callbackContext.success(adapter.status());
  }

  private void stop(CallbackContext callbackContext) {
    JobSchedulerPlugin adapter = getAdapter();
    adapter.stop();
    callbackContext.success();
  }

  private void finish(CallbackContext callbackContext) {
    JobSchedulerPlugin adapter = getAdapter();
    adapter.finish();
    callbackContext.success();
  }

  private JobSchedulerPlugin getAdapter() {
    return JobSchedulerPlugin.getInstance(cordova.getActivity().getApplicationContext());
  }
}
