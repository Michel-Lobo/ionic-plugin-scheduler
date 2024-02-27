declare var window: { JobSchedulerPlugin: any };

export class JobSchedulerPlugin {
  static setup(
    jobFn: () => Promise<void>,
    errorFn: Function,
    options: { intervalInMinutes: number }
  ) {
    window.JobSchedulerPlugin.configure(
      async () => {
        await jobFn();
        window.JobSchedulerPlugin.finish();
      },
      errorFn,
      { minimumFetchInterval: options.intervalInMinutes }
    );
  }
}
