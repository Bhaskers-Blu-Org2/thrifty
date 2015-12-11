package com.bendb.thrifty.schema;

import com.google.common.collect.ImmutableList;
import okio.BufferedSink;
import okio.Okio;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LoaderTest {
    @Rule public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void basicTest() throws Exception {
        String thrift = "\n" +
                "namespace java com.bendb.thrifty.test\n" +
                "\n" +
                "enum TestEnum {\n" +
                "  ONE = 1,\n" +
                "  TWO = 2\n" +
                "}\n" +
                "\n" +
                "typedef i32 Int\n" +
                "\n" +
                "struct S {\n" +
                "  1: required Int n\n" +
                "}\n" +
                "\n" +
                "service Svc {\n" +
                "  oneway void sayHello(1: S arg1)\n" +
                "}";

        File f = tempDir.newFile();
        writeTo(f, thrift);

        Loader loader = new Loader();
        loader.addThriftFile(f.getAbsolutePath());

        Schema schema = loader.load();

        assertThat(schema.enums().size(), is(1));
        assertThat(schema.structs().size(), is(1));
        assertThat(schema.services().size(), is(1));

        EnumType et = schema.enums().get(0);
        assertThat(et.name(), is("TestEnum"));
        assertThat(et.members().get(0).name(), is("ONE"));
        assertThat(et.members().get(1).name(), is("TWO"));

        StructType st = schema.structs().get(0);
        assertThat(st.name(), is("S"));
        assertThat(st.fields().size(), is(1));

        Field field = st.fields().get(0);
        assertThat(field.id(), is(1));
        assertThat(field.required(), is(true));
        assertThat(field.name(), is("n"));

        ThriftType fieldType = field.type();
        assertThat(fieldType.isTypedef(), is(true));
        assertThat(fieldType.name(), is("Int"));
        assertThat(fieldType.getTrueType(), is(ThriftType.I32));

        Service svc = schema.services().get(0);
        assertThat(svc.name(), is("Svc"));
        assertThat(svc.methods().size(), is(1));

        ServiceMethod method = svc.methods().get(0);
        assertThat(method.name(), is("sayHello"));
        assertThat(method.oneWay(), is(true));
        assertThat(method.paramTypes().size(), is(1));
        assertThat(method.exceptionTypes().size(), is(0));

        Field param = method.paramTypes().get(0);
        assertThat(param.name(), is("arg1"));
        assertThat(param.type().name(), is("S"));
        assertThat(param.type() == st.type(), is(true));
    }

    @Test
    public void oneInclude() throws Exception {
        String included = "\n" +
                "namespace java com.bendb.thrifty.test.include\n" +
                "\n" +
                "enum TestEnum {\n" +
                "  ONE,\n" +
                "  TWO\n" +
                "}";

        File f = tempDir.newFile();
        writeTo(f, included);

        String thrift = "\n" +
                "namespace java com.bendb.thrifty.test.include\n" +
                "include '" + f.getName() + "'\n" +
                "\n" +
                "typedef TestEnum Ordinal";

        File f1 = tempDir.newFile();
        writeTo(f1, thrift);

        Loader loader = new Loader();
        loader.addThriftFile(f.getAbsolutePath());
        loader.addThriftFile(f1.getAbsolutePath());

        Schema schema = loader.load();

        EnumType et = schema.enums().get(0);
        assertThat(et.type().name(), is("TestEnum"));

        Typedef td = schema.typedefs().get(0);
        assertThat(td.oldType(), sameInstance(et.type()));
    }

    @Test
    public void circularInclude() throws Exception {
        File f1 = tempDir.newFile();
        File f2 = tempDir.newFile();
        File f3 = tempDir.newFile();

        writeTo(f1, "include '" + f2.getName() + "'");
        writeTo(f2, "include '" + f3.getName() + "'");
        writeTo(f3, "include '" + f1.getName() + "'");

        try {
            new Loader()
                    .addThriftFile(f1.getAbsolutePath())
                    .addThriftFile(f2.getAbsolutePath())
                    .addThriftFile(f3.getAbsolutePath())
                    .load();
            fail("Circular includes should fail to load");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Circular include"));
        }
    }

    @Test
    public void circularTypedefs() throws Exception {
        String thrift = "" +
                "typedef A B\n" +
                "typedef B A";

        File f = tempDir.newFile();
        writeTo(f, thrift);

        Loader loader = new Loader();
        loader.addThriftFile(f.getAbsolutePath());

        try {
            loader.load();
            fail("Circular typedefs should fail to link");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void containerTypedefs() throws Exception {
        String thrift = "" +
                "typedef i32 StatusCode\n" +
                "typedef string Message\n" +
                "typedef map<StatusCode, Message> Messages";

        File f = tempDir.newFile();
        writeTo(f, thrift);

        Loader loader = new Loader();
        loader.addThriftFile(f.getAbsolutePath());

        Schema schema = loader.load();

        Typedef code = schema.typedefs().get(0);
        Typedef msg = schema.typedefs().get(1);
        Typedef map = schema.typedefs().get(2);

        assertThat(code.name(), is("StatusCode"));
        assertThat(code.oldType().isBuiltin(), is(true));
        assertThat(code.oldType().name(), is("i32"));

        assertThat(msg.name(), is("Message"));
        assertThat(msg.oldType().isBuiltin(), is(true));
        assertThat(msg.oldType().name(), is("string"));

        assertThat(map.name(), is("Messages"));
        assertThat(map.oldType().isMap(), is(true));

        ThriftType.MapType mt = (ThriftType.MapType) map.oldType();
        assertThat(mt.keyType(), sameInstance(code.type()));
        assertThat(mt.valueType(), sameInstance(msg.type()));
    }

    @Test
    public void crazyNesting() throws Exception {
        String thrift = "namespace java com.bendb.thrifty.compiler.testcases\n" +
                "\n" +
                "typedef string EmailAddress\n" +
                "\n" +
                "struct Wtf {\n" +
                "  1: required map<EmailAddress, ReceiptStatus> data = {\"foo@bar.com\": 0, \"baz@quux.com\": READ}\n" +
                "  2: required list<map<EmailAddress, set<ReceiptStatus>>> crazy = [{\"ben@thrifty.org\": [UNSENT, SENT]}]\n" +
                "}\n" +
                "\n" +
                "enum ReceiptStatus {\n" +
                "  UNSENT,\n" +
                "  SENT,\n" +
                "  READ\n" +
                "}";

        File f = tempDir.newFile();
        writeTo(f, thrift);

        Loader loader = new Loader();
        loader.addThriftFile(f.getAbsolutePath());

        Schema schema = loader.load();

        assertThat(schema.structs().get(0).fields().size(), is(2));
    }

    private static void writeTo(File file, String content) throws IOException {
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeUtf8(content);
        sink.flush();
        sink.close();
    }
}