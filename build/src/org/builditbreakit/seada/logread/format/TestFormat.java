package org.builditbreakit.seada.logread.format;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.VisitorType;

public final class TestFormat {

	public static void main(String[] args) {
		GalleryState state = new GalleryState();
		long time = 1;
		state.arriveAtBuilding(time++, "Jake", VisitorType.GUEST);
		state.arriveAtRoom(time++, "Jake", VisitorType.GUEST, 102);
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "Jill", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "Alice", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Jill", VisitorType.EMPLOYEE, 101);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.departBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
		// state.departBuilding(time++, "John", VisitorType.GUEST);

		Formatter formatter = new StateFormatter(state);
		System.out.print(formatter.format());
		System.out.println();
		System.out.println("-----------------");

		formatter = new VisitorRoomFormatter(state.getVisitor("Bob",
				VisitorType.EMPLOYEE));
		System.out.print(formatter.format());
		System.out.println();
		System.out.println("-----------------");

		formatter = new VisitorTimeFormatter(state.getVisitor("Bob",
				VisitorType.EMPLOYEE));
		System.out.print(formatter.format());
		System.out.println();
		System.out.println("-----------------");
	}
}
