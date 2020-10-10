package uk.oczadly.karl.csgsi.config;

import uk.oczadly.karl.csgsi.internal.Util;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Helper class for writing valve configuration files.
 */
// TODO: don't write empty objects?
public class ValveConfigWriter implements Closeable, Flushable {
    
    private final Writer out;
    private final String indent, newLine;
    
    private Stack<ObjectState> stack = new Stack<>();
    
    public ValveConfigWriter(Writer out, int indentSize, String newLine) {
        this.out = out;
        this.indent = Util.repeatChar(' ', indentSize);
        this.newLine = newLine;
        this.stack.add(new ObjectState());
    }
    
    
    public ValveConfigWriter beginObject() throws IOException {
        ObjectState os = peekStack();
        flushObject(os);
        if (os.deferredKey == null)
            throw new IllegalStateException("A key must be set before writing an object.");
        // Write opening key
        writeIndent();
        writeKey(os.deferredKey, 0);
        write("{").write(newLine);
        os.deferredKey = null;
//        os.hasContent = true;
        // Create new object on stack
        stack.add(new ObjectState());
        return this;
    }
    
    public ValveConfigWriter endObject() throws IOException {
        ObjectState os = peekStack();
        if (os.deferredKey != null)
            throw new IllegalStateException("Object has an unfinished key.");
        flushObject(os); // Flush ending object
        stack.pop(); // Remove object from stack
        // Write closing brace
        writeIndent();
        write("}").write(newLine);
        return this;
    }
    
    public ValveConfigWriter key(String key) throws IOException {
        ObjectState os = peekStack();
        if (key == null) key = "";
        if (os.deferredKey != null)
            throw new IllegalStateException("A key has already been assigned and is waiting for a value.");
        if (os.deferredKV.containsKey(key))
            throw new IllegalStateException("Key \"" + key + "\" already exists.");
        os.deferredKey = key;
        return this;
    }
    
    public ValveConfigWriter value(Object val) throws IOException {
        ObjectState os = peekStack();
        if (os.deferredKey == null)
            throw new IllegalStateException("A key must be set before writing a value.");
        if (val != null) {
            os.deferredKV.put(os.deferredKey, val.toString());
//            os.hasContent = true;
        }
        os.deferredKey = null;
        return this;
    }
    
    @Override
    public void close() throws IOException {
        ObjectState os = peekStack();
        flushObject(os);
        if (stack.size() > 1 || os.deferredKey != null)
            throw new IllegalStateException("Incomplete value/object.");
        stack.empty();
        out.close();
    }
    
    @Override
    public void flush() throws IOException {
        flushObject(peekStack());
        out.flush();
    }
    
    
    private void writeKeyValues(Map<String, String> kvs) throws IOException {
        // Determine max key length
        int maxLen = 0;
        for (String key : kvs.keySet())
            maxLen = Math.max(key.length(), maxLen);
        // Write values
        for (Map.Entry<String, String> kv : kvs.entrySet()) {
            writeIndent();
            writeKey(kv.getKey(), maxLen);
            writeValue(kv.getValue());
        }
    }
    
    private void writeIndent() throws IOException {
        for (int i=0; i<stack.size()-1; i++)
            write(this.indent);
    }
    
    private void writeKey(String key, int maxKeyLen) throws IOException {
        write("\"").write(sanitizeValue(key)).write("\" ");
        
        // Spacers
        int spacePadding = Math.max(0, maxKeyLen - key.length());
        for (int i=0; i<spacePadding; i++) write(" ");
    }
    
    private void writeValue(String value) throws IOException {
        write("\"").write(sanitizeValue(value)).write("\"").write(newLine);
    }
    
    private void flushObject(ObjectState os) throws IOException {
        if (!os.deferredKV.isEmpty()) {
            writeKeyValues(os.deferredKV);
            os.deferredKV.clear();
        }
    }
    
    private ObjectState peekStack() {
        if (stack.isEmpty())
            throw new IllegalStateException("ConfigWriter is closed.");
        return stack.peek();
    }
    
    private ValveConfigWriter write(String str) throws IOException {
        out.write(str);
        return this;
    }
    
    private static String sanitizeValue(String val) {
        return val.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    
    private static class ObjectState {
        private final Map<String, String> deferredKV = new LinkedHashMap<>();
        private String deferredKey;
//        private boolean hasContent;
    }
    
}