package android.os;

public class IBinder {
    public boolean pingBinder() {
        return true;
    }

    public void unlinkToDeath(DeathRecipient binderDeathRecipient, int i) {

    }

    public void linkToDeath(DeathRecipient binderDeathRecipient, int i) {

    }

    public interface DeathRecipient {
        void binderDied();
    }
}
