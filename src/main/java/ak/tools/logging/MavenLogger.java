package ak.tools.logging;

import org.apache.maven.plugin.logging.Log;

public class MavenLogger implements Logger
{
    private final Log log;

    public MavenLogger(Log log) {
        this.log = log;
    }

    @Override
    public void info(String format, Object... args) {
        log.info(String.format(format, args));
    }

    @Override
    public void warn(String format, Object... args) {
        log.warn(String.format(format, args));
    }

    @Override
    public void error(String format, Object... args) {
        log.error(String.format(format, args));
    }
}
