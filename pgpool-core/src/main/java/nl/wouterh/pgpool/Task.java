package nl.wouterh.pgpool;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Task {

  private final Thread thread;
  private final StoppableRunnable runnable;

  public void stop() {
    runnable.stop();
  }

  public void interrupt() {
    thread.interrupt();
  }

  public void join() throws InterruptedException {
    thread.join();
  }
}
