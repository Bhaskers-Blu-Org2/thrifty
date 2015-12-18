package com.bendb.thrifty.compiler.spi;

import com.squareup.javapoet.TypeSpec;

/**
 * When specified as part of code generation, processes all types after they
 * are computed, but before they are written to disk.  This allows you to make
 * arbitrary modifications to types such as implementing your own interfaces,
 * renaming fields, or anything you might wish to do.
 *
 * <p>For example, a processor that implements java.lang.Serializable on all
 * generated types:
 *
 * <pre><code>
 * public class SerializableTypeProcessor implements TypeProcessor {
 *    {@literal @}Override
 *     public TypeSpec process(TypeSpec type) {
 *         TypeSpec builder = type.toBuilder();
 *
 *         builder.addSuperinterface(Serializable.class);
 *         builder.addField(FieldSpec.builder(long.class, "serialVersionUID")
 *                 .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
 *                 .initializer("$L", -1)
 *                 .build());
 *
 *         return builder.build();
 *     }
 * }
 * </code></pre>
 */
public interface TypeProcessor {
    /**
     * Processes and returns a given type.
     *
     * <p>The given {@code type} will have been generated from compiled Thrift
     * files, and will not have been written to disk.  It can be returned
     * unaltered, or a modified copy can be returned.
     *
     * @param type a {@link TypeSpec} generated based on Thrift IDL.
     * @return a (possibly modified) {@link TypeSpec} to be written to disk.
     */
    TypeSpec process(TypeSpec type);
}
