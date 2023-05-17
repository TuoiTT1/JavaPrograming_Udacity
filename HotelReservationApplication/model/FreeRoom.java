package model;

import utils.Constants;

public class FreeRoom extends Room {

    public FreeRoom(String roomNumber, RoomType enumeration) {
        super(roomNumber, Constants.PRICE_FREE_ROOM, enumeration);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
