package ak.tools;

import ak.tools.logging.ConsoleLogger;
import ak.tools.wsdl.Downloader;

import java.io.File;
import java.net.URI;

public final class Program
{
    public static void main(String[] args) throws Exception
    {
        if(args.length < 3) {
            System.err.println("Command line syntax: download <WsdlUrl> <OutputDir> [<Prefix>]");
            return;
        }
        String command = args[0];
        switch (command) {
            case "download": {
                URI wsdlUrl = URI.create(args[1]);
                File outputDir = new File(args[2]);
                String prefix = args.length > 3 ? args[3] : null;

                Downloader.getInstance().run(wsdlUrl, outputDir, prefix, ConsoleLogger.INSTANCE);
                break;
            }
            default: {
                System.out.println("Unknown command '"+args[0]+"'");
            }
        }
    }
}
