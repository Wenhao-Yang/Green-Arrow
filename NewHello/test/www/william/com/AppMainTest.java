package www.william.com;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppMainTest {

	@Test
	public void testTestTriangle() {
		assertEquals(AppMain.testTriangle(1, 1, 1),"Equilateral Triangle.");
		assertEquals(AppMain.testTriangle(1, 1, 2),"It is not a triangle.");
		assertEquals(AppMain.testTriangle(5, 7, 1),"It is not a triangle.");
		assertEquals(AppMain.testTriangle(7, 5, 1),"It is not a triangle.");
		assertEquals(AppMain.testTriangle(2, 3, 3),"Isosceles Triangle.");
		assertEquals(AppMain.testTriangle(3, 2, 3),"Isosceles Triangle.");
		assertEquals(AppMain.testTriangle(2, 2, 1),"Isosceles Triangle.");
		assertEquals(AppMain.testTriangle(3, 4, 5),"Scalene Triangle.");
		assertEquals(AppMain.testTriangle(2, 2, 2),"Equilateral Triangle.");
		assertEquals(AppMain.testTriangle(-2, 1, 1),"Some sides are less than 0.");
		assertEquals(AppMain.testTriangle(2, -1, 1),"Some sides are less than 0.");
		assertEquals(AppMain.testTriangle(2, 1, -1),"Some sides are less than 0.");
		assertNotSame(AppMain.testTriangle(2, 1, 1),"ERROR");
	}

}
