package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class EnumValueTest {

    private static final Gson gson = new Gson();
    
    
    @Test
    public void testDeserialize() {
        TestContainer vals = gson.fromJson("{\"standard\": \"B\", \"named\": \"BB\", \"deserializer\": \"VAL_B\"" +
                ", \"notFound\": \"D\", \"list\": [\"A\",\"B\",\"D\"]}", TestContainer.class);
        
        assertSame(Enum.B, vals.standard.get());
        assertEquals("B", vals.standard.getString());
        
        assertSame(EnumWithNames.B, vals.named.get());
        assertEquals("BB", vals.named.getString());
        
        assertSame(EnumWithDeserializer.B, vals.deserializer.get());
        assertEquals("VAL_B", vals.deserializer.getString());
        
        assertSame(null, vals.notFound.get());
        assertEquals("D", vals.notFound.getString());
        
        assertSame(3, vals.list.size());
        assertSame(Enum.A, vals.list.get(0).get());
        assertSame(Enum.B, vals.list.get(1).get());
        assertNull(vals.list.get(2).get());
    }
    
    @Test
    public void testOf() {
        // Valid
        EnumValue<EnumWithNames> val1 = EnumValue.of("BB", EnumWithNames.class, gson);
        assertNotNull(val1);
        assertEquals(EnumWithNames.B, val1.get());
        assertEquals("BB", val1.getString());
    
        // Invalid
        EnumValue<EnumWithNames> val2 = EnumValue.of("DD", EnumWithNames.class, gson);
        assertNotNull(val2);
        assertNull(val2.get());
        assertEquals("DD", val2.getString());
    }
    
    
    static class TestContainer {
        EnumValue<Enum> standard;
        EnumValue<EnumWithNames> named;
        EnumValue<EnumWithDeserializer> deserializer;
        EnumValue<Enum> notFound;
        List<EnumValue<Enum>> list;
    }
    
    
    enum Enum {
        A, B, C
    }
    
    enum EnumWithNames {
        @SerializedName("AA") A,
        @SerializedName("BB") B,
        @SerializedName("CC") C
    }
    
    @JsonAdapter(Deserializer.class)
    enum EnumWithDeserializer {
        A,
        B,
        C
    }
    
    static class Deserializer implements JsonDeserializer<EnumWithDeserializer> {
        @Override
        public EnumWithDeserializer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.getAsString().equals("VAL_B") ? EnumWithDeserializer.B : null;
        }
    }

}