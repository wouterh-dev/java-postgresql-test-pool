package nl.wouterh.pgpool;

interface StoppableRunnable extends Runnable {
  void stop();
}
