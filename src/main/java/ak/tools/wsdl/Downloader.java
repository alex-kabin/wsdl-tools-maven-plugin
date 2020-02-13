package ak.tools.wsdl;

import ak.tools.logging.DummyLogger;
import ak.tools.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Downloader
{
    private enum DocType { WSDL, XSD }

    private static final NamespaceContext WsdlNamespaceResolver = new NamespaceContext() {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("No prefix provided!");
            } else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return "http://schemas.xmlsoap.org/wsdl/";
            } else if (prefix.equals("wsdl")) {
                return "http://schemas.xmlsoap.org/wsdl/";
            } else if (prefix.equals("xsd")) {
                return "http://www.w3.org/2001/XMLSchema";
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    };

    private static final NamespaceContext XsdNamespaceResolver = new NamespaceContext() {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("No prefix provided!");
            } else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return "http://schemas.xmlsoap.org/wsdl/";
            } else if (prefix.equals("xsd")) {
                return "http://www.w3.org/2001/XMLSchema";
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    };

    private static DocumentBuilderFactory documentBuilderFactory;
    private static XPathExpression wsdlWsdlImportExpr;
    private static XPathExpression wsdlXsdImportExpr;
    private static XPathExpression xsdXsdImportExpr;
    private static Transformer transformer;

    public static Downloader getInstance() throws XPathException, TransformerConfigurationException {
        if(documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            transformer = TransformerFactory.newInstance().newTransformer();

            // prepare XPath expressions
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath;

            xPath = xPathFactory.newXPath();
            xPath.setNamespaceContext(WsdlNamespaceResolver);
            wsdlWsdlImportExpr = xPath.compile("//wsdl:import/@location");
            wsdlXsdImportExpr = xPath.compile("//xsd:import/@schemaLocation | //xsd:include/@schemaLocation");

            xPath = xPathFactory.newXPath();
            xPath.setNamespaceContext(XsdNamespaceResolver);
            xsdXsdImportExpr = xPath.compile("//xsd:import/@schemaLocation | //xsd:include/@schemaLocation");
        }
        return new Downloader();
    }

    private Downloader() { }

    private Map<String, String> cache = new HashMap<>();
    private int counter;
    private Path outputDir;
    private String prefix;
    private Logger log;

    public void run(URI wsdlUrl, File outputDir, String prefix, Logger log) throws Exception {
        if(outputDir == null) {
            throw new IllegalArgumentException("outputDir parameter must be specified");
        } else {
            outputDir.mkdirs();
            this.outputDir = outputDir.toPath();
        }

        String url;
        if(wsdlUrl == null) {
            throw new IllegalArgumentException("wsdlUrl parameter must be specified");
        } else {
            url = wsdlUrl.toString();
        }

        this.prefix = "Service";
        if(prefix == null) {
            String path = wsdlUrl.getPath();
            if(path != null && !path.isEmpty()) {
                if(path.endsWith("/")) {
                    path = path.substring(0, path.length()-1);
                }
                int i = path.lastIndexOf("/");
                if(i >= 0) {
                    path = path.substring(i+1);
                    if(path.length() > 0) {
                        this.prefix = path;
                    }
                }
            }
        } else {
            this.prefix = prefix;
        }

        this.log = log == null ? DummyLogger.INSTANCE : log;
        this.counter = -1;
        this.cache.clear();
        resolveDocument(url, DocType.WSDL);
    }

    private void processImports(NodeList nodes, DocType docType) throws Exception {
        int count = nodes.getLength();
        if(count > 0) {
            for(int i=0; i<count; i++) {
                Node node = nodes.item(i);
                String url = node.getNodeValue();
                if(url != null) {
                    String path = resolveDocument(url, docType);
                    node.setNodeValue(path);
                }
            }
        }
    }

    private String buildNextFileName(DocType docType) {
        counter += 1;
        String suffix = counter > 0 ? "_" + counter : "";
        String ext = docType == DocType.WSDL ? "wsdl" : "xsd";
        return prefix + suffix + '.' + ext;
    }

    private String resolveDocument(String url, DocType docType) throws Exception {
        if (cache.containsKey(url)) {
            return cache.get(url);
        }

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        log.info("Downloading %s from %s", docType, url);

        Document doc = documentBuilder.parse(url);
        //doc.getDocumentElement().normalize();

        String fileName = buildNextFileName(docType);
        cache.put(url, fileName);

        if (docType == DocType.WSDL) {
            NodeList wsdlImports = (NodeList) wsdlWsdlImportExpr.evaluate(doc, XPathConstants.NODESET);
            processImports(wsdlImports, DocType.WSDL);

            NodeList xsdImports = (NodeList) wsdlXsdImportExpr.evaluate(doc, XPathConstants.NODESET);
            processImports(xsdImports, DocType.XSD);
        } else {
            NodeList xsdImports = (NodeList) xsdXsdImportExpr.evaluate(doc, XPathConstants.NODESET);
            processImports(xsdImports, DocType.XSD);
        }

        Source input = new DOMSource(doc);
        Result output = new StreamResult(outputDir.resolve(fileName).toFile());

        transformer.transform(input, output);

        return fileName;
    }
}
