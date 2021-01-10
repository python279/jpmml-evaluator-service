package org.python279.jpmmlevaluatorservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.testing.CsvUtil;
import org.jpmml.model.PMMLUtil;
import org.springframework.core.io.ClassPathResource;


public class JpmmlUtils {
    static
    public PMML readPMML(File file) throws Exception {
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                return PMMLUtil.unmarshal(is);
            }
        } else {
            try (InputStream is = new ClassPathResource(file.getPath()).getInputStream()) {
                return PMMLUtil.unmarshal(is);
            }
        }
    }

    static
    public void writePMML(PMML pmml, File file) throws Exception {

        try (OutputStream os = new FileOutputStream(file)) {
            PMMLUtil.marshal(pmml, os);
        }
    }

    static
    public CsvUtil.Table readTable(File file, String separator) throws IOException {

        try (InputStream is = new FileInputStream(file)) {
            return CsvUtil.readTable(is, separator);
        }
    }

    static
    public void writeTable(CsvUtil.Table table, File file) throws IOException {

        try (OutputStream os = new FileOutputStream(file)) {
            CsvUtil.writeTable(table, os);
        }
    }

    static
    public Function<String, String> createCellParser(Collection<String> missingValues) {
        Function<String, String> function = new Function<String, String>() {

            @Override
            public String apply(String string) {

                if (missingValues != null && missingValues.contains(string)) {
                    return null;
                }

                // Remove leading and trailing quotation marks
                string = stripQuotes(string, '\"');
                string = stripQuotes(string, '\"');

                // Standardize European-style decimal marks (',') to US-style decimal marks ('.')
                if (string.indexOf(',') > -1) {
                    String usString = string.replace(',', '.');

                    try {
                        Double.parseDouble(usString);

                        string = usString;
                    } catch (NumberFormatException nfe) {
                        // Ignored
                    }
                }

                return string;
            }

            private String stripQuotes(String string, char quoteChar) {

                if (string.length() > 1 && ((string.charAt(0) == quoteChar) && (string.charAt(string.length() - 1) == quoteChar))) {
                    return string.substring(1, string.length() - 1);
                }

                return string;
            }
        };

        return function;
    }

    static
    public Function<Object, String> createCellFormatter(String separator, String missingValue) {
        Function<Object, String> function = new Function<Object, String>() {

            @Override
            public String apply(Object object) {
                object = EvaluatorUtil.decode(object);

                if (object == null) {
                    return missingValue;
                }

                String string = object.toString();

                if (string.indexOf('\"') > -1) {
                    string = string.replaceAll("\"", "\"\"");
                } // End if

                if (string.contains(separator)) {
                    string = ("\"" + string + "\"");
                }

                return string;
            }
        };

        return function;
    }

    static
    public Object newInstance(String name) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(name);

        Method newInstanceMethod = clazz.getDeclaredMethod("newInstance");

        return newInstanceMethod.invoke(null);
    }
}
