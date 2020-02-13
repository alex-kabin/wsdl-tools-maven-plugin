package ak.tools.wsdl;

import ak.tools.logging.MavenLogger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URI;

@Mojo(name = "download")
public class DownloadMojo extends AbstractMojo
{
    @Parameter(required = true)
    private URI wsdlUrl;

    @Parameter(required = true)
    private File outputDir;

    @Parameter(required = false)
    private String prefix;

    @Parameter(required = false, defaultValue = "false")
    private Boolean skip;

    @Parameter(required = false, defaultValue = "true")
    private Boolean cleanOutputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(skip) {
            return;
        }

        if(cleanOutputDir) {
            if (outputDir.exists()) {
                File[] files = outputDir.listFiles(pathname -> {
                    String name = pathname.getName().toLowerCase();
                    return (name.endsWith(".xsd") || name.endsWith(".wsdl")) && pathname.isFile();
                });
                if(files != null) {
                    getLog().info("Cleaning output directory "+outputDir.getAbsolutePath());
                    for (File file : files) {
                        getLog().debug("Deleting "+file.getName());
                        file.delete();
                    }
                }
            }
        }

        Downloader downloader;
        try {
            downloader = Downloader.getInstance();
        }
        catch (Exception ex) {
            throw new MojoExecutionException("Failed initializing downloader", ex);
        }

        try {
            downloader.run(wsdlUrl, outputDir, prefix, new MavenLogger(getLog()));
        }
        catch (Exception ex) {
            throw new MojoExecutionException("Download error", ex);
        }
    }
}
