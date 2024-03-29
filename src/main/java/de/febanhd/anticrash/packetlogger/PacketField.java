package de.febanhd.anticrash.packetlogger;

public class PacketField {

    private String type;
    private String value;

    public PacketField(String type, Object value) {
        this.type = type;
        this.value = value+"";
    }

    @Override
    public String toString() {
        return  value + "(" + type + ")" + "\n";
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
