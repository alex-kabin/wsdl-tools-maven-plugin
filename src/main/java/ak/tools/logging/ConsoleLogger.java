package ak.tools.logging;

public final class ConsoleLogger implements Logger
{
    public static final ConsoleLogger INSTANCE = new ConsoleLogger();

    @Override
    public void info(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    @Override
    public void error(String format, Object... args) {
        System.err.println(String.format(format, args));
    }
}
