package de.febanhd.anticrash.checks;

public class CheckResult {

    private final String reason;
    private final boolean b;

    public CheckResult(boolean b, String reason) {
        this.reason = reason;
        this.b = b;
    }

    public boolean check() {
        return b;
    }

    public String getReason() {
        return reason;
    }

    public static final class Positive extends CheckResult {

        public Positive(String reason) {
            super(true, reason);
        }

        public Positive() {
            super(true, "");
        }
    }

    public static final class Negative extends CheckResult {

        public Negative(String reason) {
            super(false, reason);
        }
        public Negative() {
            super(false, "");
        }
    }
}
