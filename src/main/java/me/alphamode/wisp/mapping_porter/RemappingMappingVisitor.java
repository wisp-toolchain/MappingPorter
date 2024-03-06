package me.alphamode.wisp.mapping_porter;

import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemappingMappingVisitor extends ForwardingMappingVisitor {
    private final MemoryMappingTree oldIntermediary, intermediary;
    private final int sourceNamespace, targetNamespace;
    protected RemappingMappingVisitor(MappingWriter mappings, MemoryMappingTree oldIntermediary, MemoryMappingTree intermediary, int sourceNamespace, int targetNamespace) {
        super(mappings);
        this.oldIntermediary = oldIntermediary;
        this.intermediary = intermediary;
        this.sourceNamespace = sourceNamespace;
        this.targetNamespace = targetNamespace;
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        super.visitNamespaces(srcNamespace, dstNamespaces);
    }

    Map<String, MappingTree.FieldMapping> fieldLookup = new HashMap<>();
    Map<String, MappingTree.MethodMapping> methodLookup = new HashMap<>();

    @Override
    public boolean visitClass(String srcName) throws IOException {
        MappingTree.ClassMapping old = oldIntermediary.getClass(srcName, sourceNamespace);
        if (old == null) {
            System.out.println("Skipping null class: " + srcName);
            return false;
        }
        MappingTree.ClassMapping entry = intermediary.getClass(old.getSrcName(), targetNamespace);
        old.getMethods().forEach(methodMapping -> {
            var mapping = entry.getMethod(methodMapping.getSrcName(), methodMapping.getSrcDesc(), targetNamespace);
            if (mapping != null)
                methodLookup.put(methodMapping.getDstName(sourceNamespace), mapping);
        });
        old.getFields().forEach(fieldMapping -> {
            fieldLookup.put(fieldMapping.getDstName(sourceNamespace), entry.getField(fieldMapping.getSrcName(), fieldMapping.getSrcDesc(), targetNamespace));
        });
        return super.visitClass(entry.getSrcName());
    }

    @Override
    public boolean visitField(String srcName, String srcDesc) throws IOException {
        if (!srcName.contains("field_"))
            return super.visitMethod(srcName, srcDesc);
        if (!fieldLookup.containsKey(srcName)) {
            System.out.println("Skipping null field: " + srcName);
            return false;
        }
        MappingTree.FieldMapping mapping = fieldLookup.get(srcName);
        return super.visitField(mapping.getSrcName(), mapping.getSrcDesc());
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        if (!srcName.contains("method_"))
            return super.visitMethod(srcName, srcDesc);
        if (!methodLookup.containsKey(srcName)) {
            System.out.println("Skipping null method: " + srcName);
            return false;
        }
        MappingTree.MethodMapping mapping = methodLookup.get(srcName);
        return super.visitMethod(mapping.getSrcName(), mapping.getSrcDesc());
    }
}
