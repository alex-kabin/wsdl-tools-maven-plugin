# wsdl-tools-maven-plugin
_WSDL related utility and Maven plugin_

This utility can help download root WSDL file and referenced definitions and schemas (XSD) from specified URL into specified directory.
All schema locations in downloaded files will be fixed to point to local files. 

---
To use as standalone utility execute from command line with args:

    java -jar wsdl-tools-maven-plugin.jar download <WsdlUrl> <OutputDir> [<Prefix>]  

where 
* `WsdlUrl` - URL to root WSDL document, 
* `OutputDir` - downloaded files output directory path, 
* `Prefix` - optional output file name prefix.
    
for example:

    java -jar wsdl-tools-maven-plugin.jar download http://my.web.com/ws/SuperService/ProxyService?wsdl ./SuperService SuperService
    
---
To use as maven plugin, include in your .pom (as example):

    <plugin>
        <groupId>ak.tools</groupId>
        <artifactId>wsdl-tools-maven-plugin</artifactId>
        <version>0.1</version>
        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>download</goal>
                </goals>
                <configuration>
                    <!-- skip execution -->
                    <skip>false</skip>
                    <!-- remove .wsdl and .xsd from output directory beforehand -->
                    <cleanOutputDir>true</cleanOutputDir>
                    <!-- wsdl url -->
                    <wsdlUrl>http://my.web.com/ws/SuperService/ProxyService?wsdl</wsdlUrl>
                    <!-- output dir path -->
                    <outputDir>./src/wsdl</outputDir>
                    <!-- file name prefix -->
                    <prefix>SuperService</prefix>
                </configuration>
            </execution>
        </executions>
    </plugin>
