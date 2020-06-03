package ak.tools.logging;

public interface Logger
{
    void info(String format, Object... args);
    void warn(String format, Object... args);
    void error(String format, Object... args);
}
