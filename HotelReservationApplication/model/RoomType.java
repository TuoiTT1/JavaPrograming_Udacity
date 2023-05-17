package model;

public enum RoomType {
    SINGLE(1),
    DOUBLE(2);
    private int valueInt;

    private RoomType(int valueInt) {
        this.valueInt = valueInt;
    }

    public static RoomType findByValueInt(int valueInt) {
        for(RoomType roomType : values()) {
            if(roomType.valueInt == valueInt) {
                return roomType;
            }
        }
        throw new IllegalArgumentException();
    }
}
