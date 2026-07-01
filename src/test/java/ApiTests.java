import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTests {

    private static final String UNEXISTING_PET_ID = "54532323";
    private static final String AVAILABLE_STATUS = "available";

    @BeforeAll
    public static void clearTestData() {
        given().delete("https://petstore.swagger.io/v2/pet/" + UNEXISTING_PET_ID);
    }

    @BeforeEach
    public void setup() {
        baseURI = "https://petstore.swagger.io/v2/";
    }

    @Test //Not BDD
    public void notFoundTestWithAssert() {

        //Вызываем get метод, ответ кладем в response
        Response response = given().get("pet/" + UNEXISTING_PET_ID);

        //Выводим response на консоль
        System.out.println("Response: " + response.asPrettyString());

        assertEquals(404, response.statusCode(), "Не тот status code");
        assertEquals("HTTP/1.1 404 Not Found", response.statusLine(), "Некорректная status line");
        assertEquals("Pet not found", response.jsonPath().get("message"), "Не то сообщение об ошибке");

    }

    @Test //Not BDD
    public void petNotFoundTest() {

        //Вызываем get метод, ответ кладем в response
        Response response = given().get("pet/" + UNEXISTING_PET_ID);

        //Выводим response на консоль
        System.out.println("Response: " + response.asPrettyString());

        //Объект типа ValidatableResponse нужен для валидации ответа
        ValidatableResponse validatableResponse = response.then();

        //Проверяем status code
        validatableResponse.statusCode(404);

        //Проверяем status line
        validatableResponse.statusLine("HTTP/1.1 404 Not Found");

        //Проверяем response body
        validatableResponse.body("message", equalTo("Pet not found"));

    }

    @Test
    public void petNotFoundTestBdd() {
        given().when()
                .get(baseURI + "pet/" + UNEXISTING_PET_ID)
                .then()
                .log().all()
                .statusCode(404)
                .statusLine("HTTP/1.1 404 Not Found")
                .body("message", equalTo("Pet not found"));
    }

    @Test
    public void checkTypeForPetTest() {
        given().when()
                .get(baseURI + "pet/" + UNEXISTING_PET_ID)
                .then()
                .log().all()
                .body("type", equalTo("error"));
    }

    @Test
    @DisplayName("Добавление нового питомца")
    public void newPetTest() {

        String name = "cat";
        String status = AVAILABLE_STATUS;

        Map<String, String> request = new HashMap<>();
        request.put("id", UNEXISTING_PET_ID);
        request.put("name", name);
        request.put("status", status);

        given().contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .time(lessThan(5000L))
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(UNEXISTING_PET_ID)),
                        "name", equalTo(name),
                        "status", equalTo(status));

        clearTestData();

    }

    @Test
    @DisplayName("Поиск питомца по статусу available")
    public void searchPetWithStatusAvailable() {
        given().when()
                .log().all()
                .params("status", AVAILABLE_STATUS)
                .get("pet/findByStatus")
                .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(3000L))
                .body("status", everyItem(equalTo(AVAILABLE_STATUS)));

    }

}
