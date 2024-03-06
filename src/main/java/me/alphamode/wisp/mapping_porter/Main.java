package me.alphamode.wisp.mapping_porter;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 3)
            throw new RuntimeException("Not enough arguments <source> <dest> <old-intermediaries> <new-intermediaries> [old-namespace] [target-namespace]");

        int sourceNamespace = 0;
        int targetNamespace = 2;

        if (args.length > 4)
            sourceNamespace = Integer.parseInt(args[4]);
        if (args.length > 5)
            targetNamespace = Integer.parseInt(args[5]);

        try (MappingWriter writer = MappingWriter.create(Path.of(args[1]), MappingFormat.ENIGMA_DIR)) {
            MemoryMappingTree oldIntermediary = new MemoryMappingTree();
            MappingReader.read(Path.of(args[2]), oldIntermediary);
            MemoryMappingTree intermediary = new MemoryMappingTree();
            MappingReader.read(Path.of(args[3]), intermediary);
            RemappingMappingVisitor remappingMappingVisitor = new RemappingMappingVisitor(writer, oldIntermediary, intermediary, sourceNamespace, targetNamespace);
            MappingReader.read(Path.of(args[0]), remappingMappingVisitor);
        }
    }
}