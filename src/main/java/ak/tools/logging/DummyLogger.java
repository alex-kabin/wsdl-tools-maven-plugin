package ak.tools.logging;

public final class DummyLogger implements Logger
{
    public static final DummyLogger INSTANCE = new DummyLogger();

    private DummyLogger() {
    }

    @Override
    public void info(String format, Object... args) {
    }

    @Override
    public void error(String format, Object... args) {
    }
}
