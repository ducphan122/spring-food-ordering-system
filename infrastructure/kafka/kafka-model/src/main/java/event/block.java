/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package event;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;

@org.apache.avro.specific.AvroGenerated
public class block extends org.apache.avro.specific.SpecificRecordBase
        implements org.apache.avro.specific.SpecificRecord {
    private static final long serialVersionUID = -1509139701355429768L;

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser()
            .parse(
                    "{\"type\":\"record\",\"name\":\"block\",\"namespace\":\"event\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"total_order\",\"type\":\"long\"},{\"name\":\"data_collection_order\",\"type\":\"long\"}],\"connect.version\":1,\"connect.name\":\"event.block\"}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    private static final SpecificData MODEL$ = new SpecificData();

    private static final BinaryMessageEncoder<block> ENCODER = new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

    private static final BinaryMessageDecoder<block> DECODER = new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

    /**
     * Return the BinaryMessageEncoder instance used by this class.
     * @return the message encoder used by this class
     */
    public static BinaryMessageEncoder<block> getEncoder() {
        return ENCODER;
    }

    /**
     * Return the BinaryMessageDecoder instance used by this class.
     * @return the message decoder used by this class
     */
    public static BinaryMessageDecoder<block> getDecoder() {
        return DECODER;
    }

    /**
     * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
     * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
     * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
     */
    public static BinaryMessageDecoder<block> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
    }

    /**
     * Serializes this block to a ByteBuffer.
     * @return a buffer holding the serialized data for this instance
     * @throws java.io.IOException if this instance could not be serialized
     */
    public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
        return ENCODER.encode(this);
    }

    /**
     * Deserializes a block from a ByteBuffer.
     * @param b a byte buffer holding serialized data for an instance of this class
     * @return a block instance decoded from the given buffer
     * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
     */
    public static block fromByteBuffer(java.nio.ByteBuffer b) throws java.io.IOException {
        return DECODER.decode(b);
    }

    private java.lang.String id;
    private long total_order;
    private long data_collection_order;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public block() {}

    /**
     * All-args constructor.
     * @param id The new value for id
     * @param total_order The new value for total_order
     * @param data_collection_order The new value for data_collection_order
     */
    public block(java.lang.String id, java.lang.Long total_order, java.lang.Long data_collection_order) {
        this.id = id;
        this.total_order = total_order;
        this.data_collection_order = data_collection_order;
    }

    @Override
    public org.apache.avro.specific.SpecificData getSpecificData() {
        return MODEL$;
    }

    @Override
    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    @Override
    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0:
                return id;
            case 1:
                return total_order;
            case 2:
                return data_collection_order;
            default:
                throw new IndexOutOfBoundsException("Invalid index: " + field$);
        }
    }

    // Used by DatumReader.  Applications should not call.
    @Override
    @SuppressWarnings(value = "unchecked")
    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0:
                id = value$ != null ? value$.toString() : null;
                break;
            case 1:
                total_order = (java.lang.Long) value$;
                break;
            case 2:
                data_collection_order = (java.lang.Long) value$;
                break;
            default:
                throw new IndexOutOfBoundsException("Invalid index: " + field$);
        }
    }

    /**
     * Gets the value of the 'id' field.
     * @return The value of the 'id' field.
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the 'id' field.
     * @param value the value to set.
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the 'total_order' field.
     * @return The value of the 'total_order' field.
     */
    public long getTotalOrder() {
        return total_order;
    }

    /**
     * Sets the value of the 'total_order' field.
     * @param value the value to set.
     */
    public void setTotalOrder(long value) {
        this.total_order = value;
    }

    /**
     * Gets the value of the 'data_collection_order' field.
     * @return The value of the 'data_collection_order' field.
     */
    public long getDataCollectionOrder() {
        return data_collection_order;
    }

    /**
     * Sets the value of the 'data_collection_order' field.
     * @param value the value to set.
     */
    public void setDataCollectionOrder(long value) {
        this.data_collection_order = value;
    }

    /**
     * Creates a new block RecordBuilder.
     * @return A new block RecordBuilder
     */
    public static event.block.Builder newBuilder() {
        return new event.block.Builder();
    }

    /**
     * Creates a new block RecordBuilder by copying an existing Builder.
     * @param other The existing builder to copy.
     * @return A new block RecordBuilder
     */
    public static event.block.Builder newBuilder(event.block.Builder other) {
        if (other == null) {
            return new event.block.Builder();
        } else {
            return new event.block.Builder(other);
        }
    }

    /**
     * Creates a new block RecordBuilder by copying an existing block instance.
     * @param other The existing instance to copy.
     * @return A new block RecordBuilder
     */
    public static event.block.Builder newBuilder(event.block other) {
        if (other == null) {
            return new event.block.Builder();
        } else {
            return new event.block.Builder(other);
        }
    }

    /**
     * RecordBuilder for block instances.
     */
    @org.apache.avro.specific.AvroGenerated
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<block>
            implements org.apache.avro.data.RecordBuilder<block> {

        private java.lang.String id;
        private long total_order;
        private long data_collection_order;

        /** Creates a new Builder */
        private Builder() {
            super(SCHEMA$, MODEL$);
        }

        /**
         * Creates a Builder by copying an existing Builder.
         * @param other The existing Builder to copy.
         */
        private Builder(event.block.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }
            if (isValidValue(fields()[1], other.total_order)) {
                this.total_order = data().deepCopy(fields()[1].schema(), other.total_order);
                fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }
            if (isValidValue(fields()[2], other.data_collection_order)) {
                this.data_collection_order = data().deepCopy(fields()[2].schema(), other.data_collection_order);
                fieldSetFlags()[2] = other.fieldSetFlags()[2];
            }
        }

        /**
         * Creates a Builder by copying an existing block instance
         * @param other The existing instance to copy.
         */
        private Builder(event.block other) {
            super(SCHEMA$, MODEL$);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.total_order)) {
                this.total_order = data().deepCopy(fields()[1].schema(), other.total_order);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.data_collection_order)) {
                this.data_collection_order = data().deepCopy(fields()[2].schema(), other.data_collection_order);
                fieldSetFlags()[2] = true;
            }
        }

        /**
         * Gets the value of the 'id' field.
         * @return The value.
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the 'id' field.
         * @param value The value of 'id'.
         * @return This builder.
         */
        public event.block.Builder setId(java.lang.String value) {
            validate(fields()[0], value);
            this.id = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'id' field has been set.
         * @return True if the 'id' field has been set, false otherwise.
         */
        public boolean hasId() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'id' field.
         * @return This builder.
         */
        public event.block.Builder clearId() {
            id = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'total_order' field.
         * @return The value.
         */
        public long getTotalOrder() {
            return total_order;
        }

        /**
         * Sets the value of the 'total_order' field.
         * @param value The value of 'total_order'.
         * @return This builder.
         */
        public event.block.Builder setTotalOrder(long value) {
            validate(fields()[1], value);
            this.total_order = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'total_order' field has been set.
         * @return True if the 'total_order' field has been set, false otherwise.
         */
        public boolean hasTotalOrder() {
            return fieldSetFlags()[1];
        }

        /**
         * Clears the value of the 'total_order' field.
         * @return This builder.
         */
        public event.block.Builder clearTotalOrder() {
            fieldSetFlags()[1] = false;
            return this;
        }

        /**
         * Gets the value of the 'data_collection_order' field.
         * @return The value.
         */
        public long getDataCollectionOrder() {
            return data_collection_order;
        }

        /**
         * Sets the value of the 'data_collection_order' field.
         * @param value The value of 'data_collection_order'.
         * @return This builder.
         */
        public event.block.Builder setDataCollectionOrder(long value) {
            validate(fields()[2], value);
            this.data_collection_order = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /**
         * Checks whether the 'data_collection_order' field has been set.
         * @return True if the 'data_collection_order' field has been set, false otherwise.
         */
        public boolean hasDataCollectionOrder() {
            return fieldSetFlags()[2];
        }

        /**
         * Clears the value of the 'data_collection_order' field.
         * @return This builder.
         */
        public event.block.Builder clearDataCollectionOrder() {
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public block build() {
            try {
                block record = new block();
                record.id = fieldSetFlags()[0] ? this.id : (java.lang.String) defaultValue(fields()[0]);
                record.total_order = fieldSetFlags()[1] ? this.total_order : (java.lang.Long) defaultValue(fields()[1]);
                record.data_collection_order =
                        fieldSetFlags()[2] ? this.data_collection_order : (java.lang.Long) defaultValue(fields()[2]);
                return record;
            } catch (org.apache.avro.AvroMissingFieldException e) {
                throw e;
            } catch (java.lang.Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumWriter<block> WRITER$ =
            (org.apache.avro.io.DatumWriter<block>) MODEL$.createDatumWriter(SCHEMA$);

    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumReader<block> READER$ =
            (org.apache.avro.io.DatumReader<block>) MODEL$.createDatumReader(SCHEMA$);

    @Override
    public void readExternal(java.io.ObjectInput in) throws java.io.IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    @Override
    protected boolean hasCustomCoders() {
        return true;
    }

    @Override
    public void customEncode(org.apache.avro.io.Encoder out) throws java.io.IOException {
        out.writeString(this.id);

        out.writeLong(this.total_order);

        out.writeLong(this.data_collection_order);
    }

    @Override
    public void customDecode(org.apache.avro.io.ResolvingDecoder in) throws java.io.IOException {
        org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.id = in.readString();

            this.total_order = in.readLong();

            this.data_collection_order = in.readLong();

        } else {
            for (int i = 0; i < 3; i++) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.id = in.readString();
                        break;

                    case 1:
                        this.total_order = in.readLong();
                        break;

                    case 2:
                        this.data_collection_order = in.readLong();
                        break;

                    default:
                        throw new java.io.IOException("Corrupt ResolvingDecoder.");
                }
            }
        }
    }
}
