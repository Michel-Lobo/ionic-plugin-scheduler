# ionic-plugin-scheduler

An Ionic/Cordova plugin that schedules periodic Android background tasks using the [`JobScheduler`](https://developer.android.com/reference/android/app/job/JobScheduler.html).

## Installation

```bash
  $ ionic cordova plugin add bitbucket:irsolu/ionic-plugin-scheduler
```

## Usage

To schedule a job:

```typescript
import { JobSchedulerPlugin } from "ionic-plugin-jobscheduler";

JobSchedulerPlugin.setup(
  periodicTask,
  errorHandler,
  { intervalInMinutes: i } // i in minutes
);
```

`periodicTask` is an async function that will trigger the release of the Android wakelock.

## Example

```javascript
const periodicTask = async function() {
  codePush.sync(null);
  window.JobSchedulerPlugin.finish();
};

const errorHandler = function(error) {
  console.log("JobSchedulerPlugin error: ", error);
};

JobSchedulerPlugin.setup(
  periodicTask,
  errorHandler,
  { intervalInMinutes: 60 } // run every hour
);
```

## Debugging

- Observe the plugin logs:

```bash
$ adb logcat -s JobSchedulerPluginTag
```

- Simulate a background-fetch event on a device (only works for Android >=7.0):

```bash
$ adb shell cmd jobscheduler run -f <your.application.id> 999
```

- See all scheduled jobs on your phone (only works for Android >=7.0)

```bash
$ adb shell dumpsys jobscheduler
```

## Notes

- The same [jobId](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html) (999) is used every time a job is scheduled, so former fetch handlers are overriden when you define a new fetch handler.
- The job is persisted on device reboots.
- `JobSchedulerPlugin.finish()` will call [`JobService.jobFinished`](https://developer.android.com/reference/android/app/job/JobService.html#jobFinished).

## Credits

Heavily inspired by the works of https://github.com/transistorsoft/cordova-plugin-background-fetch, https://github.com/transistorsoft/transistor-background-fetch and https://github.com/raphaelmerx/cordova-plugin-scheduler.
